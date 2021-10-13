package com.tfs.learningsystems.ui.rest;

import java.io.InputStream;
import java.util.List;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-02-21T16:16:07.557-08:00")
public abstract class ModelTestApiService {

  public abstract Response listBatchTests(String clientId, String projectId, String modelId,
      int limit, int startIndex) throws NotFoundException;

  public abstract Response checkStatus(String clientId, String projectId, String modelId,
      String testId) throws NotFoundException;

  public abstract Response evalDatasets(String clientId, String projectId, String modelId,
      String testModelType, List<String> datasets) throws NotFoundException;

  public abstract Response evalTranscriptions(String clientId, String projectId, String modelId,
      String testModelType, List<String> utterances) throws NotFoundException;

  public abstract Response evalSpeechUtterance(String clientId, String projectId, String modelId,
      String fileType, String audioURL, InputStream fileInputStream) throws NotFoundException;
}
