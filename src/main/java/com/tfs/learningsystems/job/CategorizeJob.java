/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.JobManager;
import com.tfs.learningsystems.ui.ModelManager;
import com.tfs.learningsystems.ui.model.CategorizeJobDetail;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.EvaluationResponse;
import com.tfs.learningsystems.ui.model.UtteranceEvaluation;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModel;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState.Status;
import com.tfs.learningsystems.ui.rest.impl.ModelTestApiServiceImpl;
import com.tfs.learningsystems.util.CSVFileUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.QuartzJobUtil;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;


@DisallowConcurrentExecution
@Slf4j
public class CategorizeJob implements InterruptableJob {

  // has the job been interrupted?
  private boolean isInterrupted = false;

  // job name
  private JobKey jobKey = null;

  private static final String AUTO_TAG_COLUMN_NAME = "auto_tag";

  private static final String LOGSTASH_JOB_CANCELLED = "Logstash job canceled";

  private static final String INTERRUPTED = "  -- Interrupted... bailing out!";

  @Inject
  @Qualifier("modelTestBean")
  private ModelTestApiServiceImpl modelTestApiService;

  @Inject
  @Qualifier("modelManagerBean")
  private ModelManager modelManager;

  private static final Set<Integer> RETRY_STATUSES;

  static {
    final Set<Integer> set = new HashSet<>();
    set.add(Response.Status.GATEWAY_TIMEOUT.getStatusCode());
    set.add(Response.Status.BAD_GATEWAY.getStatusCode());
    set.add(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    set.add(Response.Status.REQUEST_TIMEOUT.getStatusCode());
    RETRY_STATUSES = Collections.unmodifiableSet(set);
  }

  @Autowired
  private RestTemplate restTemplate;

  @Inject
  @Qualifier("jobManagerBean")
  private JobManager jobManager;

  @Autowired
  Environment env;

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
        log.error("Got interrupted while waiting to finish categorizer job");
        Thread.currentThread().interrupt();
      }
      if (isInterrupted) {
        log.info("--- " + jobKey + INTERRUPTED);
        context.setResult(isInterrupted);
        throw new JobExecutionException(LOGSTASH_JOB_CANCELLED, false);
      }
    } else {

      if (isInterrupted) {
        log.info("--- " + jobKey + INTERRUPTED);
        context.setResult(isInterrupted);
        throw new JobExecutionException(LOGSTASH_JOB_CANCELLED, false);
      }

      ObjectMapper mapper = new ObjectMapper();
      try {
        CategorizeJobDetail categorizeJobDetail = (CategorizeJobDetail) jobDataMap
            .getOrDefault(QuartzJobUtil.KEY_CATEGORIZE_JOB_DETAIL, null);

        HttpComponentsClientHttpRequestFactory requestFactory =
            new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectionRequestTimeout(jobDataMap
            .getInt(QuartzJobUtil.KEY_CATEGORIZER_CONNECTION_REQUEST_TIMEOUT));
        requestFactory.setConnectTimeout(
            jobDataMap.getInt(QuartzJobUtil.KEY_CATEGORIZER_CONNECT_TIMEOUT));
        requestFactory.setReadTimeout(
            jobDataMap.getInt(QuartzJobUtil.KEY_CATEGORIZER_READ_TIMEOUT));
        this.restTemplate.setRequestFactory(requestFactory);

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        String locale = categorizeJobDetail.getDatasetLocale();

        String file = categorizeJobDetail.getCsvFilePath();
        Long numberOfRecords = CSVFileUtil.validateAndGetColumns(file).getValidRowCount();
        List<String> columns = categorizeJobDetail.getColumns();
        String transcriptionColumn = categorizeJobDetail.getTranscriptionColumn();
        String originalTranscriptionColumn = categorizeJobDetail.getOriginalTranscriptionColumn();

        String projectId = categorizeJobDetail.getProjectId();
        String clientId = categorizeJobDetail.getClientId();
        Boolean isModelPredictedIntentRequired = categorizeJobDetail
            .getUseModelForSuggestedCategory();

        map.add("file", new FileSystemResource(file));
        map.add("lang", locale.split("-")[0]);
        map.add("columns", columns);
        map.add("transcriptionColumn", transcriptionColumn);
        map.add("originalTranscriptionColumn", originalTranscriptionColumn);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String url = categorizeJobDetail.getCategorizeServicePath();
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
            new HttpEntity<>(map, headers);
        ResponseEntity<String> result =
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        if (isInterrupted) {
          log.info("--- " + jobKey + INTERRUPTED);
          context.setResult(isInterrupted);
          throw new JobExecutionException(LOGSTASH_JOB_CANCELLED, false);
        }

        if (result.getStatusCode().is2xxSuccessful()) {

          try (PrintStream out = new PrintStream(new FileOutputStream(file), true,
              StandardCharsets.UTF_8.name())) {
            if (Boolean.TRUE.equals(isModelPredictedIntentRequired)) {
              String responseAlteredForSuggestedCategory = replaceSuggestedIntentsWithModelPrediction(
                  result.getBody(), clientId, projectId);
              out.println(responseAlteredForSuggestedCategory);
            } else {
              log.info("Predicting suggested intents via model is not enabled!!");
              out.println(result.getBody());
            }
            log.info("saving cleaned file to: {}", file);
            CSVFileUtil.validate(file, numberOfRecords);

            task.setModifiedAt(Calendar.getInstance().getTimeInMillis());
            task.setRecordsProcessed(numberOfRecords);
            jobManager.updateTaskEvent(task);

          } catch (FileNotFoundException e) {
            throw new JobExecutionException(e);
          } catch (ApplicationException ae) {
            throw new JobExecutionException(ae.getMessage(), ae);
          } catch (UnsupportedEncodingException uee) {
            throw new JobExecutionException(uee.getMessage(), uee);
          }
        } else {
          throw new RestClientResponseException("Rest Client Exception",
              result.getStatusCodeValue(), result.getStatusCode().getReasonPhrase(),
              result.getHeaders(), result.getBody().getBytes(StandardCharsets.UTF_8),
              StandardCharsets.UTF_8);

        }

      } catch (ResourceAccessException rae) {
        throw new JobExecutionException(rae.getMessage(), rae, false);
      } catch (RestClientResponseException rcre) {

        Error error;
        try {
          error = mapper.readValue(rcre.getResponseBodyAsString(), Error.class);
        } catch (IOException e) {
          error = new Error(rcre.getRawStatusCode(), null, rcre.getMessage());
        }

        if (isInterrupted) {
          log.info("--- " + jobKey + INTERRUPTED);
          context.setResult(isInterrupted);
          throw new JobExecutionException(LOGSTASH_JOB_CANCELLED, false);
        }

        // Only retry when the error was something that could change
        if (CategorizeJob.RETRY_STATUSES.contains(rcre.getRawStatusCode())) {
          int currentRetryCount =
              jobDataMap.getInt(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT);
          int maxRetryCount = jobDataMap.getInt(QuartzJobUtil.KEY_MAX_RETRY_COUNT);
          boolean retry = false;
          if (currentRetryCount < maxRetryCount) {
            currentRetryCount++;

            try {
              log.info("Waiting for {} before refiring again, retry number {} of {}",
                  QuartzJobUtil.KEY_DEFAULT_WAIT_TIME_TO_REFIRE,
                  currentRetryCount, maxRetryCount);
              Thread.sleep(QuartzJobUtil.KEY_DEFAULT_WAIT_TIME_TO_REFIRE);
            } catch (InterruptedException e1) {
              log.warn("Woke up from sleep before firing categorize job again");
              Thread.currentThread().interrupt();
            }
            jobDataMap.put(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT, currentRetryCount);
            retry = true;
          } else {
            retry = false;
          }
          throw new JobExecutionException(error.getMessage(), rcre, retry);
        } else {
          throw new JobExecutionException(error.getMessage(), rcre, false);

        }
      } catch (JobExecutionException e) {
        throw (JobExecutionException) e;
      } catch (Exception e) {
          throw new JobExecutionException(e.getMessage(), e, false);
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

  private String replaceSuggestedIntentsWithModelPrediction(String response, String clientId,
      String projectId) throws JobExecutionException {
    try {

      List<TFSModel> modelList = modelManager.listOfModelsForProject(clientId, projectId);

      if (modelList.isEmpty()) {
        log.info("Cannot find any web2nl model created within this project!!");
        return response;
      }
      long startTime = System.currentTimeMillis();
      CSVReader reader = new CSVReaderBuilder(new StringReader(response)).build();
      List<String> allInputTranscriptions = new ArrayList<>();
      int transcriptionColumnNo = 0; // index of the Transcription Column
      String[] header = reader.readNext(); // skip header
      String[] line;
      while ((line = reader.readNext()) != null) {
        allInputTranscriptions.add(line[transcriptionColumnNo]);
      }

      TFSModel lastSuccessfulModel = null;
      // Sorting Model list based on version so we can pick up the latest version
      modelList.sort((o1, o2) -> o2.getVersion() - o1.getVersion());
      // Try to go through the list of all successful built models and using the latest one
      for (TFSModel model : modelList){
        TFSModelJobState jobState = modelManager.getModelStatus(clientId,model.getId());
        if (jobState.getStatus().getValue().equalsIgnoreCase(Status.COMPLETED.toString())) {
          lastSuccessfulModel = model;
          break;
        }
      }
      if (lastSuccessfulModel == null){
        log.info("No successfully built models found for predicting intents");
        return response;
      }

      String modelUUID = lastSuccessfulModel.getModelId();
      //call the test API and retrieve intents
      ///replace it to the auto_tag column
      Response responseModel = modelTestApiService
          .evalTranscriptions(clientId, projectId, modelUUID, Constants.DIGITAL_MODEL , allInputTranscriptions);

      EvaluationResponse evaluationList = (EvaluationResponse) responseModel.getEntity();
      HashMap<String, String> intentMap = new HashMap<>();
      for (UtteranceEvaluation utteranceEvaluation : evaluationList.getEvaluations()) {
        intentMap.put(utteranceEvaluation.getUtterance(),
            utteranceEvaluation.getIntents().get(0).getIntent());
      }

      StringWriter s = new StringWriter();
      CSVWriter writer = new CSVWriter(s, ',');
      writer.writeNext(header);
      reader = new CSVReaderBuilder(new StringReader(response)).build();
      reader.readNext(); // skip header

      int autoTagColumn = Arrays.asList(header).indexOf(AUTO_TAG_COLUMN_NAME);
      while ((line = reader.readNext()) != null) {
        line[autoTagColumn] = intentMap.get(line[0]);
        writer.writeNext(line);
      }
      writer.close();
      long endTime = System.currentTimeMillis() - startTime;
      log.info("Predicting suggested intents via web2nl took " + endTime + " ms");
      return s.toString();
    } catch (Exception e) {
      throw new JobExecutionException(e.getMessage(), e, false);
    }
  }
}