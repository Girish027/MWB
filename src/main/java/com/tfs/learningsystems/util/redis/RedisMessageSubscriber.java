package com.tfs.learningsystems.util.redis;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.tfs.learningsystems.db.ModelTestBatchBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.DataManagementManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.EvaluationResponse;
import com.tfs.learningsystems.ui.model.IntentScore;
import com.tfs.learningsystems.ui.model.UtteranceEvaluation;
import com.tfs.learningsystems.ui.rest.ApiException;
import com.tfs.learningsystems.ui.rest.impl.ModelTestApiServiceImpl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelection;
import com.tfs.learningsystems.util.CSVFileUtil;
import com.tfs.learningsystems.util.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;

@Service
@Slf4j
public class RedisMessageSubscriber implements MessageListener {

  private static final String MODEL_TEST_DIR = "model_test";

  private DataManagementManager dataManagementManager;
  private String web2nlUrl;
  private String apiKey;
  private String orionUrl;
  private String repositoryRoot;


  public RedisMessageSubscriber() {
    // Do nothing
  }


  public RedisMessageSubscriber setDataManagementManager(
      DataManagementManager dataManagementManager) {

    this.dataManagementManager = dataManagementManager;
    return (this);
  }

  public RedisMessageSubscriber setWeb2nlUrl(String web2nlUrl) {

    this.web2nlUrl = web2nlUrl;
    return (this);
  }

  public RedisMessageSubscriber setApiKey(String apiKey) {

    this.apiKey = apiKey;
    return (this);
  }

  public RedisMessageSubscriber setOrionUrl(String orionUrl) {

    this.orionUrl = orionUrl;
    return (this);
  }

  public RedisMessageSubscriber setRepositoryRoot(String repositoryRoot) {

    this.repositoryRoot = repositoryRoot;
    return (this);
  }

  public void onMessage(final Message message, final byte[] pattern) {

    String[] testInfo = new String(message.getBody()).split("_");
    String testId = testInfo[0];
    String testModelType = testInfo[1];

    log.info("Model test request received : " + new String(message.getBody()));

    ModelTestBatchBO mtb = new ModelTestBatchBO();
    // double check by reading it back?
    mtb = mtb.findOne(testId);
    EvaluationResponse.StatusEnum status = EvaluationResponse.StatusEnum
        .valueOf(mtb.getStatus().toUpperCase());
    if (!EvaluationResponse.StatusEnum.QUEUED.equals(status)) {
      // someone already handled it
      return;
    }
    mtb.setStatus(EvaluationResponse.StatusEnum.IN_PROGRESS.toString());
    mtb.update();

    String payload = mtb.getRequestPayload();
    if (StringUtils.isEmpty(payload)) {
      log.error("No payload found for - " + testId);
      mtb.setStatus(EvaluationResponse.StatusEnum.FAILED.toString());
      mtb.update();
      return;
    }

    String[] datasetIds = payload.split(",");
    String modelId = mtb.getModelId();
    String projectId = Long.toString(mtb.getProjectId());
    String clientId = Long.toString(mtb.getClientId());
    List<String> idList = Arrays.asList(datasetIds);

    long startTime = System.currentTimeMillis();

    File uniqueTrascriptsFile = dataManagementManager
        .exportUnique(clientId, projectId, idList, "AND", false, false);

    log.info("Export unique transcript - {} - {} - {} takes {} ", projectId, testId,
        StringUtils.join(idList, ", "), System.currentTimeMillis() - startTime);

    startTime = System.currentTimeMillis();
    long lineCnt = 0;

    log.info("Remove::::RedisMessageSubscriber::dumpResult::Path: {}", uniqueTrascriptsFile.getAbsolutePath());

    try(CSVReader reader = new CSVReader(
            new InputStreamReader(new FileInputStream(uniqueTrascriptsFile), StandardCharsets.UTF_8))) {
      List<String[]> results = new LinkedList<>();
      String[] fields = reader.readNext();
      fields = reader.readNext();
      while (fields != null) {
        String intent = fields[0];
        String transcription = fields[1];
        String count = fields[3];
        UtteranceEvaluation eval = null;
        int retryLimit = 3;
        int retry = 0;
        lineCnt++;
        while (retry < retryLimit) {
          try {
            eval = ModelTestApiServiceImpl
                .evalOneUtterrance(web2nlUrl, orionUrl, apiKey, modelId, transcription, testModelType);

            // should use String[]
            String[] oneResult = new String[13];
            oneResult[0] = transcription;
            oneResult[1] = intent;
            oneResult[2] = count;
            // collect result

            List<IntentScore> scores = eval.getIntents();
            int i = 0;
            for (IntentScore oneScore : scores) {
              String oneIntent = oneScore.getIntent();
              double scoreValue = oneScore.getScore().doubleValue();
              oneResult[3 + i * 2] = oneIntent;
              oneResult[4 + i * 2] = Double.toString(scoreValue);
              i += 1;
              if (i == 5) {
                break;
              }
            }
            results.add(oneResult);
            break;
          } catch (ApplicationException e) {
            break;
          } catch (ApiException e) {
            if (retry == retryLimit) {
              log.error(
                  "Exceeding retry limit for evaluating utterrance - " + modelId + " - " + apiKey
                      + " - " + transcription);
              break;
            }
            Thread.sleep((long) (Math.random() * 600));
          }
          retry += 1;
        }
        fields = reader.readNext();
      }
      log.info("Batch test - {} - {} - {} records takes {} ", projectId, testId, lineCnt,
          System.currentTimeMillis() - startTime);
      String resultPath = dumpResult(projectId, testId, results);
      mtb.setResult_file(resultPath);
      mtb.setStatus(EvaluationResponse.StatusEnum.SUCCESS.toString());
      log.info("Stored batch test result - {} - {} - {}  ", projectId, testId, resultPath);
    } catch (FileNotFoundException e) {
      mtb.setStatus(EvaluationResponse.StatusEnum.FAILED.toString());
      log.error("export file not found for - " + testId + " - " + uniqueTrascriptsFile, e);
    } catch (IOException e) {
      mtb.setStatus(EvaluationResponse.StatusEnum.FAILED.toString());
      log.error("failed to read export file - " + testId + " - " + uniqueTrascriptsFile, e);
    } catch (InterruptedException e) {
      log.error("InterruptedException occur", e);
      Thread.currentThread().interrupt();
    }

    mtb.update();
  }

  private String dumpResult(String projectId, String testId, List<String[]> results)
      throws IOException {

    FileSystem fileSystem = FileSystems.getDefault();
    Path root = fileSystem.getPath(repositoryRoot, MODEL_TEST_DIR + "/" + projectId);

    CSVFileUtil.createIfNotExistsRootDirectoy(root);

    String columnMappedFilename = testId + ".csv";
    Path columnMappedFilePath = fileSystem.getPath(root.toString(), columnMappedFilename);

    try (final CSVWriter columnMappedFileWriter = new CSVWriter(
            new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(columnMappedFilePath.toString()),
                    StandardCharsets.UTF_8)))) {

      //Write column Headers
      List<String> headersList = new ArrayList<>();
      // Transcription|Actual Intent| Predicted Intent 1| Predicted intent1 score| Predicted..
      headersList.add("transcription");
      headersList.add("Rollup Intent (RUI)");
      headersList.add("count");
      headersList.add("Predicted RUI 1");
      headersList.add("score1");
      headersList.add("Predicted RUI 2");
      headersList.add("score2");
      headersList.add("Predicted RUI 3");
      headersList.add("score3");
      headersList.add("Predicted RUI 4");
      headersList.add("score4");
      headersList.add("Predicted RUI 5");
      headersList.add("score5");
      columnMappedFileWriter.writeNext(headersList.toArray(new String[headersList.size()]));

      for (String[] colList : results) {
        log.info("Remove::::RedisMessageSubscriber::dumpResult::transcripts: {}", colList[0]);
        columnMappedFileWriter.writeNext(colList);
      }

    } catch (IOException ioe) {
      log.warn("Failed to write - {}", columnMappedFilePath.toString());
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                      ErrorMessage.FILE_ACCESS_MESSAGE))
              .build(), ioe);
    }

    return (columnMappedFilePath.toString());
  }
}
