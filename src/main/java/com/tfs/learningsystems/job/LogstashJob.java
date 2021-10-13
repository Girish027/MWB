/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.helper.SearchHelper;
import com.tfs.learningsystems.ui.ContentManager;
import com.tfs.learningsystems.ui.JobManager;
import com.tfs.learningsystems.ui.model.LogstashJobDetail;
import com.tfs.learningsystems.util.CSVFileUtil;
import com.tfs.learningsystems.util.QuartzJobUtil;
import com.tfs.learningsystems.util.TextUtil;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;

@DisallowConcurrentExecution
@Slf4j
public class LogstashJob implements InterruptableJob {


  @Autowired
  private BulkProcessing bulkProcessing;

  @Autowired
  Environment env;

  @Autowired
  ElasticSearchPropertyConfig elasticSearchProps;

  // has the job been interrupted?
  private boolean isInterrupted = false;

  // job name
  private JobKey jobKey = null;

  @Autowired
  @Qualifier("freemarkerConfiguration")
  private freemarker.template.Configuration freemarkerConfiguration;

  @Autowired
  @Qualifier("contentManagerBean")
  private ContentManager contentManager;

  @Autowired
  @Qualifier("jobManagerBean")
  private JobManager jobManager;

  @Autowired
  private SearchHelper searchHelper;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("jsonObjectMapper")
  private ObjectMapper jsonObjectMapper;

  @Autowired
  @Qualifier("elasticSearchClient")
  private org.elasticsearch.client.Client elasticSearchClient;


  public void logstashObj(LogstashJobDetail logstashJobDetail, TaskEventBO task) {

    String path = logstashJobDetail.getCsvFilePath();
    try (CSVReader csvReader = new CSVReader(
            new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8),
            CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,
            CSVParser.NULL_CHARACTER)) {

      long startTime = System.currentTimeMillis();

      String indexName = elasticSearchProps.getNltoolsIndexAlias();
      String indexType = elasticSearchProps.getDefaultDocumentIndexType();

      boolean ifHeader = true;
      int headerlength = 0;
      String[] line = csvReader.readNext();
      int numRecords = 0;
      BulkProcessor requestHandler = bulkProcessing.buildBulkProcessor();
      Map<String, Object> mappedObject = new HashMap<>();
      Map<String,Integer> headerObject = new HashMap<>();

      while (line != null && line.length > 0) {
        if (ifHeader) {

          ifHeader = false;
          headerlength = line.length;
          for (int i = 0; i < line.length; i++) {
            headerObject.put(line[i],i);
          }

        } else if (line.length == headerlength) {


          numRecords = numRecords + 1;

          // Mandatory fields
          if (headerObject.containsKey("original_transcription"))
            mappedObject.put(elasticSearchProps.getTranscriptionOriginalLabel(),line[headerObject.get("original_transcription")]);
          if (headerObject.containsKey("transcription"))
            mappedObject.put(elasticSearchProps.getTranscriptionLabel(),line[headerObject.get("transcription")]);
          if (headerObject.containsKey("rutag"))
            mappedObject.put(elasticSearchProps.getTaggingGuideRUTagLabel(),TextUtil.removeNonBMPCharacters(line[headerObject.get("rutag")]));
          if (headerObject.containsKey("inheritedIntent"))
            mappedObject.put(elasticSearchProps.getImportedIntentLabel(), TextUtil.removeNonBMPCharacters(line[headerObject.get("inheritedIntent")]));
          if (headerObject.containsKey("auto_tag"))
            mappedObject.put(elasticSearchProps.getAutoTagStringLabel(),line[headerObject.get("auto_tag")]);
          if (headerObject.containsKey("manual_tag"))
            mappedObject.put(elasticSearchProps.getTagLabel(),line[headerObject.get("manual_tag")]);

          // Custom fields (optional fields)
          if (headerObject.containsKey("merged_auto_tags"))
            mappedObject.put(elasticSearchProps.getMergedAutoTagsLabel(),line[headerObject.get("merged_auto_tags")]);
          if (headerObject.containsKey("has_custom_tag"))
            mappedObject.put(elasticSearchProps.getHasCustomTagLabel(),line[headerObject.get("has_custom_tag")]);
          if (headerObject.containsKey("suggested_intent_tags"))
            mappedObject.put(elasticSearchProps.getSuggestedIntentTagsLabel(),line[headerObject.get("suggested_intent_tags")]);
          if (headerObject.containsKey("filename"))
            mappedObject.put(elasticSearchProps.getFilenameLabel(),line[headerObject.get("filename")]);
          if (headerObject.containsKey("session_id"))
            mappedObject.put(elasticSearchProps.getSessionIdLabel(),line[headerObject.get("session_id")]);
          if (headerObject.containsKey(("date")))
            mappedObject.put(elasticSearchProps.getDateLabel(),line[headerObject.get("date")]);

          // Metadata fields
          mappedObject.put(elasticSearchProps.getClientIdLabel(),logstashJobDetail.getClientId());
          mappedObject.put(elasticSearchProps.getDatasetIdLabel(),logstashJobDetail.getDatasetId());
          mappedObject.put(elasticSearchProps.getProjectIdLabel(),logstashJobDetail.getProjectId());
          mappedObject.put(elasticSearchProps.getJobIdLabel(),task.getJobId());
          mappedObject.put(elasticSearchProps.getVerticalLabel(),logstashJobDetail.getVertical());
          mappedObject.put(elasticSearchProps.getDatatypeLabel(),logstashJobDetail.getDataType());

          String transcription = (String)mappedObject.get(elasticSearchProps.getTranscriptionLabel());
          int numTokens = calculateNoOfTokens(transcription);
          mappedObject.put(elasticSearchProps.getNumTokensLabel(), numTokens);

          String autoTag = (String)mappedObject.get(elasticSearchProps.getAutoTagStringLabel());
          if (autoTag != null) {
            ArrayList<String> autoTagList = new ArrayList<>();
            if (!StringUtils.isEmpty(autoTag)) {
              autoTagList.add(autoTag);
            }
            mappedObject.put(elasticSearchProps.getAutoTagLabel(), autoTagList);
            mappedObject.put(elasticSearchProps.getAutoTagCountLabel(), autoTagList.size());
          }
          mappedObject.put(elasticSearchProps.getDocumentTypeLabel(),"original");
          String transcriptionValue = (String)mappedObject.get(elasticSearchProps.getTranscriptionLabel());

          mappedObject.put(elasticSearchProps.getTranscriptionHashLabel(),transcriptionValue(transcriptionValue));

          requestHandler.add(bulkProcessing.buildIndexRequest(indexName, indexType, mappedObject));


        }
        line = csvReader.readNext();
      }
      requestHandler.close();
      long endTime = System.currentTimeMillis();
      log.info(String.format("For Ingesting original docs: %s records took %s ms",numRecords,endTime-startTime));
    } catch (Exception e) {
      String message = String
              .format("Error occurred while ingesting data into Elasticsearch: %s", e.getMessage());
      log.equals(message);
      throw new ApplicationException(message,e);
    }
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {

    JobDetail jobDetail = context.getJobDetail();
    JobDataMap jobDataMap = jobDetail.getJobDataMap();
    jobKey = jobDetail.getKey();
    context.setResult(isInterrupted);
    TaskEventBO task = (TaskEventBO) jobDataMap
            .getOrDefault(QuartzJobUtil.KEY_CURRENT_TASK_DETAIL, null);

    task.setModifiedAt(Calendar.getInstance().getTimeInMillis());
    task.setMessage("");
    task.setStatus(TaskEventBO.Status.RUNNING);
    task.setErrorCode(TaskEventBO.ErrorCode.OK.toString());

    jobManager.updateTaskEvent(task);

    if (env.acceptsProfiles("test")) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        log.error("Got interrupted while waiting to finish logstash job");
        Thread.currentThread().interrupt();
      }
      if (isInterrupted) {
        log.info("--- " + jobKey + "  -- Interrupted... bailing out!");
        context.setResult(isInterrupted);
        throw new JobExecutionException("Logstash job canceled", false);
      }
    } else {
      try {

        if (isInterrupted) {
          log.info("--- " + jobKey + "  -- Interrupted... bailing out!");
          context.setResult(isInterrupted);
          throw new JobExecutionException("Logstash job canceled", false);
        }

        LogstashJobDetail logstashJobDetail = (LogstashJobDetail) jobDataMap
                .getOrDefault(QuartzJobUtil.KEY_LOGSTASH_JOB_DETAIL,
                        null);

        CSVFileUtil.CSVFileInfo csvFileInfo =
                CSVFileUtil.validateAndGetColumns(logstashJobDetail.getCsvFilePath());

        long validRowCount = csvFileInfo.getValidRowCount();
        Integer clientId = logstashJobDetail.getClientId();
        Integer projectId = logstashJobDetail.getProjectId();
        Integer datasetId = logstashJobDetail.getDatasetId();

        log.info("Transform dataset - csv file - {} ", logstashJobDetail.getCsvFilePath());

        log.info("Transform dataset - start job {}", jobKey.toString());

        logstashObj(logstashJobDetail, task);

        long indexCount;

        indexCount = searchHelper.getTotalNlToolsOriginalDocumentsCount(clientId, projectId,
                datasetId);
        task.setModifiedAt(Calendar.getInstance().getTimeInMillis());
        task.setRecordsProcessed(indexCount);
        jobManager.updateTaskEvent(task);
        long timeSlept = 0;
        while (indexCount < validRowCount
                && timeSlept < logstashJobDetail.getLogstashExecTimeout()) {

          Thread.sleep(1000);
          timeSlept += 1000;

          if (isInterrupted) {
            log.info("--- " + jobKey + "  -- Interrupted... bailing out!");
            context.setResult(isInterrupted);
            throw new JobExecutionException("Logstash job canceled", false);
          }

          indexCount = searchHelper.getTotalNlToolsOriginalDocumentsCount(clientId, projectId,
                  datasetId);
          task.setModifiedAt(Calendar.getInstance().getTimeInMillis());
          task.setRecordsProcessed(indexCount);
          jobManager.updateTaskEvent(task);
        }

        if (timeSlept >= logstashJobDetail.getLogstashExecTimeout() && indexCount == 0) {
          throw new JobExecutionException("Logstash job timed out", false);
        }

        if (indexCount != validRowCount) {
          log.error(String
                  .format("Transform dataset - mismatching count - %s - %s - %s", indexCount,
                          validRowCount, jobKey.toString()));
          throw new JobExecutionException(
                  "Incorrect number of indexed records, expected: " + validRowCount
                          + ", found: " + indexCount,
                  false);
        }
        log.info(String
                .format("Transform dataset - seeding intent - %s - %s - %s", projectId, datasetId,
                        jobKey.toString()));

        contentManager
                .seedIntentDocuments(clientId, projectId, datasetId, logstashJobDetail.getUsername());

      } catch (ApplicationException | InterruptedException ae) {
        log.error("Possibly failed seeding intent-added document types - " + jobKey.toString(),
                ae);
        Thread.currentThread().interrupt();
        throw new JobExecutionException(ae, true);
      }

    }
  }

  /**
   * <p>
   * Called by the <code>{@link Scheduler}</code> when a user interrupts the <code>Job</code>.
   * </p>
   *
   * @return void (nothing) if job interrupt is successful.
   * @throws JobExecutionException if there is an exception while interrupting the job.
   */
  public void interrupt() throws UnableToInterruptJobException {

    log.info("---" + jobKey + "  -- INTERRUPTING --");
    isInterrupted = true;
  }

  public int calculateNoOfTokens(String transription) {
    int numTokens = 0;
    if (transription != null) {
      int i = 0;
      int spaceCount = 0;
      transription = transription.trim();
      while (i < transription.length()) {
        if (transription.charAt(i) == ' ') {
          spaceCount++;
        }
        i++;
      }
      numTokens = spaceCount + 1;
    }
    return numTokens;
  }

  public String transcriptionValue(String transcriptionValue) {
    SecretKeySpec keySpec = new SecretKeySpec("simplekey".getBytes(),
            "HmacSHA1");
    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(keySpec);
      byte[] rawHmac = mac.doFinal(transcriptionValue.getBytes());
      return Hex.encodeHexString(rawHmac);
    } catch (NoSuchAlgorithmException |  InvalidKeyException e) {
      log.error(" Error {} while generating for transaction value {} ", e, transcriptionValue);
    }
    return null;
  }
}
