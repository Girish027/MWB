package com.tfs.learningsystems.ui.rest.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.config.HttpConfig;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ModelTestBatchBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.ClientManager;
import com.tfs.learningsystems.ui.ModelManager;
import com.tfs.learningsystems.ui.ProjectManager;
import com.tfs.learningsystems.ui.ValidationManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.*;
import com.tfs.learningsystems.ui.model.EvaluationResponse.StatusEnum;
import com.tfs.learningsystems.ui.model.error.BadFileException;
import com.tfs.learningsystems.ui.model.error.TranscribingException;
import com.tfs.learningsystems.ui.rest.ApiException;
import com.tfs.learningsystems.ui.rest.ApiResponseMessage;
import com.tfs.learningsystems.ui.rest.ModelTestApiService;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import com.tfs.learningsystems.util.redis.RedisMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Component
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-02-21T16:16:07.557-08:00")
@Slf4j
@Qualifier("modelTestBean")
public class ModelTestApiServiceImpl extends ModelTestApiService {

  private static JedisPool pool;
  @Autowired
  ApplicationContext context;
  @Qualifier("appConfig")
  @Autowired
  private AppConfig appConfig;
  @Autowired
  private RedisMessagePublisher redisMessagePublisher;
  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;
  @Autowired
  @Qualifier("modelManagerBean")
  private ModelManager modelManager;
  @Autowired
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;
  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  private static String getOrionUrl(String modelId, String testModelType, String orionUrl) {
    String orionUrlForModel = orionUrl + "/" + modelId;
    if(testModelType.equals(Constants.DIGITAL_MODEL)) {
          orionUrlForModel = orionUrlForModel + "/digital";
    }
    return orionUrlForModel;
  }

  public static UtteranceEvaluation evalOneUtterrance(String web2nlUrl, String orionUrl,
      String apiKey, String modelId, String one, String testModelType)
      throws ApplicationException, ApiException {
    //
    // curl -X GET --header 'Accept: application/json' 'http://stable.api.sv2.247-inc.net/v1/classifier/intents?q=how%20are%20you&modelurl=http%3A%2F%2Fstable.api.sv2.247-inc.net%2Fv1%2Fmodelbuilder%2F4f070e68-374e-4cdf-87e9-4bff7d4ba8ed&maxintents=5&applythresholds=true&confidential=false&verbose=true&api_key=V19v9MP3zX3VapNZ'
    //
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(HttpConfig.createSecureTransport());

    Map<String, String> variables = new HashMap<>();

    if (StringUtils.isEmpty(web2nlUrl) ||
        StringUtils.isEmpty(apiKey)) {
      throw (new ApplicationException(
          "The server is not not configured for web2nl. Please set tfs.web2nl.host, tfs.web2nl.api.key"));
    }
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

    String sid = ActionContext.getSessionId();
    if (org.apache.commons.lang3.StringUtils.isEmpty(sid)) {
      //
      // for async batch job, there's no session id
      //
      sid = modelId;
    }

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(web2nlUrl)
        .queryParam("modelurl", getOrionUrl(modelId, testModelType, orionUrl))
        .queryParam("maxintents", "5")
        .queryParam("sessionid", sid)
        .queryParam("requestid", sid)
        .queryParam("applythresholds", "true")
        .queryParam("confidential", "false")
        .queryParam("q", one)
        .queryParam("verbose", "true")               // to get transformations
        .queryParam("api_key", apiKey);

    HttpEntity<?> entity = new HttpEntity<>(headers);

    log.info("evaluate intents for - {}", one);
    try {
      ResponseEntity<JsonNode> response = restTemplate.getForEntity(
          builder.build().encode().toUri(),
          JsonNode.class);

      log.info(" Json node response - {}", response);

      if (HttpStatus.OK == response.getStatusCode()) {
        JsonNode res = response.getBody();
        JsonNode intents = res.findValue("intents");
        JsonNode entities = res.findValue("entities");
        JsonNode transforms = res.findValue("transforms");
        UtteranceEvaluation eval = new UtteranceEvaluation()
            .utterance(one)
            .transformations(transforms)
            .entities(entities);
        Iterator<JsonNode> it = intents.elements();
        while (it.hasNext()) {
          JsonNode oneNode = it.next();
          IntentScore is = new IntentScore();
          is.score(BigDecimal.valueOf(oneNode.get("score").asDouble()));
          is.setIntent(oneNode.get("className").asText());
          eval.addIntentsItem(is);
        }
        return (eval);
      }
      throw (new ApiException(response.getStatusCodeValue(),
          "failed to send evaluation - " + modelId + " - " + one));
    } catch (Exception e) {
      log.error("failed to evaluate one utterance - " + modelId, e);
      throw (new ApplicationException(e));
    }
  }

  private static String encodeFileToBase64Binary(String fileName)
      throws IOException {

    File file = new File(fileName);
    byte[] bytes = loadFile(file);
    byte[] encoded = Base64.encodeBase64(bytes);
    String encodedString = new String(encoded, StandardCharsets.UTF_8);

    return encodedString;
  }

  private static byte[] loadFile(File file) throws IOException {
    InputStream is = new FileInputStream(file);

    long length = file.length();
    if (length > Integer.MAX_VALUE) {
      // File is too large
      log.error("File is too large - " + file.getName());
      return ("File is too large".getBytes());
    }
    byte[] bytes = new byte[(int) length];

    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length
        && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
      offset += numRead;
    }

    if (offset < bytes.length) {
      log.error("Could not completely read file " + file.getName());
      throw new IOException("Could not completely read file " + file.getName());
    }

    is.close();
    return bytes;
  }

  private static BatchTestInfo getBatchTestInfo(@NotNull ModelTestBatchBO batchTest)
      throws NotFoundException {
    BatchTestInfo info = new BatchTestInfo();
    // TODO implement batch test name. Till then, use BatchTest_<Timestamp>
    String batchTestName = "BatchTest_" + batchTest.getCreatedAt();
    String requestPayload = batchTest.getRequestPayload();
    info.batchTestName(batchTestName)
        .testId(batchTest.getId())
        .type(BatchTestInfo.TypeEnum.DATASETS)
        .status(BatchTestInfo.StatusEnum.valueOf(batchTest.getStatus().toUpperCase()))
        .requestPayload(requestPayload)
        .createdAt(batchTest.getCreatedAt().toString());
    return info;
  }

  public JedisPool getJedisPool() throws NotFoundException {
    if (pool == null) {
      synchronized (ModelTestApiServiceImpl.class) {
        // check again as multiple threads can reach above step
        if (pool == null) {
          String host = appConfig.getRedisHost();
          int port = appConfig.getRedisPort();
          log.info("Building connection pool to Redis server {}:{}", host, port);
          pool = new JedisPool(new JedisPoolConfig(), host, port);
        }
      }
    }
    return (pool);
  }

  @Override
  public Response listBatchTests(String clientId, String projectId, String modelId, int pageSize,
      int startIndex) throws NotFoundException {

    // validate projectId and model Id before proceeding

    ModelBO model = validationManager.validateAndGetModel(clientId, projectId, modelId, "modelId");

    // Get the existing batch tests for the model
    ModelTestBatchBO mtbo = new ModelTestBatchBO();
    List<ModelTestBatchBO> batchTestResults = new ArrayList<ModelTestBatchBO>();

    Map<String, Object> conditions = new HashMap<>();
    conditions.put(ModelTestBatchBO.FLD_PROJECT_ID, projectId);
    conditions.put(ModelTestBatchBO.FLD_MODEL_ID, modelId);
    conditions.put(ModelTestBatchBO.FLD_CLIENT_ID, clientId);

    Sort sort = Sort.by(Sort.Direction.DESC, new String[]{"createdAt"});
    batchTestResults = mtbo.page(conditions, startIndex, pageSize, sort);

    log.info("Parsing through batch tests for model Id - " + modelId);
    List<BatchTestInfo> batchTestInfo = new ArrayList<BatchTestInfo>();
    for (ModelTestBatchBO batchTest : batchTestResults) {
      batchTestInfo.add(getBatchTestInfo(batchTest));
    }

    log.info("Constructing batch test results response object for model Id" + modelId);

    BatchTestResultsResponse batchTestResponse = new BatchTestResultsResponse();
    batchTestResponse
        .projectId(projectId)
        .modelId(modelId)
        .modelName(model.getName())
        .modelVersion(model.getVersion().toString())
        .modelDescription(model.getDescription())
        .batchTestInfo(batchTestInfo);
    return Response.ok().entity(batchTestResponse).build();
  }


  @Override
  public Response checkStatus(String clientId, String projectId, String modelId, String testId)
      throws NotFoundException {
    EvaluationResponse evalResponse = new EvaluationResponse();
    evalResponse.setProjectId(projectId);
    evalResponse.setModelId(modelId);
    evalResponse.setTestId(testId);
    evalResponse.setType(EvaluationResponse.TypeEnum.DATASETS);

    ModelTestBatchBO mtb = new ModelTestBatchBO();
    mtb = mtb.findOne(testId);

    if (mtb == null ||
        mtb.getId() == null || mtb.getClientId() == null ||
        mtb.getProjectId() == null ||
        !clientId.equals(Long.toString(mtb.getClientId())) ||
        !projectId.equals(Long.toString(mtb.getProjectId())) ||
        !modelId.equals(mtb.getModelId()) ||
        !testId.equals(mtb.getId())) {
      log.error("Found ModelTestBatchDao record, but failed to match the test set.");
      evalResponse.setStatus(EvaluationResponse.StatusEnum.FAILED);
      evalResponse.setMessage("Failed to match the test set.");
      return Response.serverError()
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Failed to match the test set."))
          .build();
    }

    EvaluationResponse.StatusEnum status = EvaluationResponse.StatusEnum
        .valueOf(mtb.getStatus().toUpperCase());
    evalResponse.setStatus(status);
    if (status != null) {
      log.info("Model test status - {} - {} - {} - {}", projectId, modelId, testId, status);
    }
    if (EvaluationResponse.StatusEnum.SUCCESS == status) {
      String resultFilePath = mtb.getResult_file();
      String content = null;
      log.info("Reading test result file - {}", resultFilePath);
      try {
        content = encodeFileToBase64Binary(resultFilePath);
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, content)).build();
      } catch (IOException e) {
        log.error("Failed to read result file for model testing, - ", e);
        return Response.serverError().entity(
            new ApiResponseMessage(ApiResponseMessage.ERROR, "Failed to read the result file"))
            .build();
      }
    } else if (EvaluationResponse.StatusEnum.FAILED == status) {
      return Response.ok()
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Batch test failed")).build();
    }

    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "")).build();
  }

  @Override
  public Response evalDatasets(String clientId, String projectId, String modelId,
      String testModelType, List<String> datasets) throws NotFoundException {
    EvaluationResponse evalResponse = new EvaluationResponse();
    evalResponse.setProjectId(projectId);
    evalResponse.setModelId(modelId);
    evalResponse.setType(EvaluationResponse.TypeEnum.DATASETS);

    // get the project with the given projectId for the given clientId
    ProjectBO project = new ProjectBO();
    Map<String, Object> conditions = new HashMap<>();
    conditions.put(ProjectBO.FLD_CLIENT_ID, clientId);
    conditions.put(ProjectBO.FLD_PROJECT_ID, projectId);
    project = project.findOne(conditions);

    if (project == null || project.getId() == null) {
      log.error("Failed to find the related project for evaluating model - {} - {} ", projectId,
          modelId);
      evalResponse.setStatus(EvaluationResponse.StatusEnum.FAILED);
      return Response.serverError().entity(evalResponse).build();
    }

    ModelTestBatchBO mtb = new ModelTestBatchBO();
    mtb.setModelId(modelId);
    mtb.setProjectId(Long.parseLong(projectId));
    mtb.setRequestPayload(String.join(",", datasets));
    mtb.setStatus(EvaluationResponse.StatusEnum.QUEUED.toString());
    mtb.setClientId(Long.parseLong(project.getClientId().toString()));
    mtb = mtb.create();
    String testId = mtb.getId();

    try {
      this.redisMessagePublisher.publish(testId + "_" + testModelType);
      evalResponse.setTestId(testId.toString());
      evalResponse.setStatus(EvaluationResponse.StatusEnum.QUEUED);
      log.info("Queued request for evaluating model - {} - {} - {} ", projectId, modelId, testId);
    } catch (Exception e) {
      log.error("Failed to enqueue the test set - " + projectId + " - " + modelId, e);
      mtb.setStatus(EvaluationResponse.StatusEnum.FAILED.toString());
      evalResponse.setMessage("Failed to enqueue the test set.");
    }

    if (!StringUtils.isEmpty(evalResponse.getMessage())) {
      return Response.serverError().entity(evalResponse).build();
    }
    return Response.ok().entity(evalResponse).build();
  }

  public UtteranceEvaluation getResponseFromWebReco(String audioFilePath,String modelURL) {
    try {

      // Rest Call to WebReco service
      RestTemplate restTemplate = new RestTemplate();
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add(Constants.WEBRECO_QUERY_PARAM_AUDIO, new FileSystemResource(audioFilePath));
      map.add(Constants.WEBRECO_QUERY_PARAM_AUTHORIZATION, AuthUtil.getWebRecoAuthHeader(appConfig.getWebrecoClient(),appConfig.getWebrecoClientSecret()));
      map.add(Constants.WEBRECO_QUERY_PARAM_CONFIDENCE_SCORE,Constants.WEBRECO_QUERY_CONFIDENCE_SCORE_VALUE);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
          new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
              headers);

      UriComponentsBuilder builder = UriComponentsBuilder
          .fromHttpUrl(appConfig.getWebrecoUrl());
      builder.queryParam(Constants.WEBRECO_QUERY_PARAM_GRAMMAR, modelURL);

      SSLContextBuilder sslcontext = new SSLContextBuilder();

      sslcontext.loadTrustMaterial(null, new TrustStrategy(){
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
          return true;
        }
      }).build();
      final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
      final HttpClient httpClient = HttpClientBuilder.create()
          .setSSLContext(sslcontext.build()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
          .setRedirectStrategy(new LaxRedirectStrategy())
          .build();

      factory.setHttpClient(httpClient);
      restTemplate.setRequestFactory(factory);

      ResponseEntity<String> result = restTemplate.exchange(
          builder.build().encode().toUri(),
          HttpMethod.POST,
          requestEntity,
          String.class);

      log.info(result.getBody().toString());


      // Parsing XML Response
      SAXBuilder domBuilder = new SAXBuilder();
      String XMLResponse = result.getBody();
      InputStream stream = new ByteArrayInputStream(XMLResponse.getBytes(Constants.DEFAULT_ENCODING));
      Document document = domBuilder.build(stream);
      Element classElement = document.getRootElement();
      String status = classElement.getAttribute("status",Namespace.getNamespace(Constants.MICROSOFT_NAMESPACE)).getValue();

      if(!status.equalsIgnoreCase(HttpStatus.OK.getReasonPhrase())){
        String responseValue = classElement
            .getChild("info", Namespace.getNamespace(Constants.EMMA_NAMESPACE)).getChildText("error",Namespace.getNamespace(Constants.MICROSOFT_NAMESPACE));
        throw new BadFileException(responseValue);
      }

      /// Uninterpreted case
      Element interpretationChild =  classElement
          .getChild("interpretation", Namespace.getNamespace(Constants.EMMA_NAMESPACE));
      if (interpretationChild!=null){
        String value = interpretationChild.getAttributeValue("uninterpreted",Namespace.getNamespace(Constants.EMMA_NAMESPACE));
        if(value==null || value.equalsIgnoreCase("true")) {
          String msg = "Failed while transcibing audio at path: " + audioFilePath;
          log.info(msg);
          throw new TranscribingException(msg);
        }
      }

      Element child = classElement
          .getChild("one-of", Namespace.getNamespace(Constants.EMMA_NAMESPACE))
          .getChild("interpretation", Namespace.getNamespace(Constants.EMMA_NAMESPACE));

      Attribute transcribedText = child
          .getAttribute("tokens", Namespace.getNamespace(Constants.EMMA_NAMESPACE));
      Attribute confidenceScore = child
          .getAttribute("confidence",
              Namespace.getNamespace(Constants.EMMA_NAMESPACE));
      Attribute transcribedTextWithClassNames = child
          .getAttribute("tokens-with-classnames",
              Namespace.getNamespace(Constants.TELLME_NAMESPACE));

      log.info("Trasncription  " + transcribedText.getValue() + " and confidence score: "
          + confidenceScore.getValue());

//      Element intent = child.getChild("intent");
//      List<Element> intentList = intent.getChildren();



      UtteranceEvaluation eval = new UtteranceEvaluation()
          .utterance(transcribedText.getValue())
          .transformations(null)
          .entities(null);

      eval.setRecognitionScore(confidenceScore.getValue());
      if (transcribedTextWithClassNames != null && transcribedTextWithClassNames.getValue() != null)
      {
        log.info("Trasncription with class names " + transcribedTextWithClassNames.getValue());
        eval.setUtteranceWithWordClass(transcribedTextWithClassNames.getValue());
      }

//      for (Element intentElem : intentList) {
//        log.debug("intent:" + intentElem.getChildText("classification") + ", confidence score: "
//            + intentElem.getChildText("confidence").toString());
//        IntentScore is = new IntentScore();
//        is.score(new BigDecimal(intentElem.getChildText("confidence").toString()));
//        is.setIntent(intentElem.getChildText("classification"));
//        eval.addIntentsItem(is);
//      }

      return (eval);
    } catch(BadFileException e){
      throw e;
    } catch(TranscribingException e){
      throw e;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Response evalSpeechUtterance(String clientId, String projectId, String modelId,
      String fileType, String audioURL, InputStream fileInputStream) throws NotFoundException {
    log.info(String.format("Evaluating Speech utterance against model id: %s and file type: %s", modelId, fileType));
    EvaluationResponse evalResponse = new EvaluationResponse();
    Path tempPath = null;
    try {
      if(fileType==null || fileType.isEmpty()){
        log.error(String.format("For client: %s, project: %s, modelId: %s, Filetype is empty or null",
            clientId,projectId,modelId));
        return Response.status(Status.BAD_REQUEST).entity(new Error(Status.BAD_REQUEST.getStatusCode(), null,
            ErrorMessage.INVALID_FILE_ERROR)).build();
      }
      if (fileType.equalsIgnoreCase(Constants.UTTERANCE_FILE_TYPE_AS_LINK)) {
        if(audioURL == null || audioURL.isEmpty()){
          log.error(String.format("For client: %s, project: %s, modelId: %s, Audio URL is empty or null ",
              clientId,projectId,modelId));
          return Response.status(Status.BAD_REQUEST).entity(new Error(Status.BAD_REQUEST.getStatusCode(), null,
              ErrorMessage.AUDIO_URL_ERROR)).build();
        }
        try {
          log.info("Downloading file for single utterance testing from URL: " + audioURL);
          RestTemplate restTemplate = new RestTemplate();
          restTemplate.getMessageConverters()
              .add(new ByteArrayHttpMessageConverter());

          HttpHeaders headers = new HttpHeaders();
          headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
          HttpEntity<String> entity = new HttpEntity<>(headers);

          ResponseEntity<byte[]> response = restTemplate
              .exchange(audioURL, HttpMethod.GET, entity, byte[].class,
                  "1");

          if (response.getStatusCode() == HttpStatus.OK
              && response.getBody() != null) {
            tempPath = Files.createTempFile(UUID.randomUUID().toString(), ".wav");
            log.info("For speech utterance testing, storing input file at " + tempPath.toString());
            Files.write(tempPath, response.getBody());
          } else {
            log.info(String.format(
                "Failed while retreiving audio file from URL %s with status code: %s and response body: %s",
                audioURL, response.getStatusCode(), response.getBody()));
            return Response.status(Status.BAD_REQUEST).build();
          }

        }catch (Exception e){
          log.error(String.format("For client: %s, project: %s, modelId: %s, Failed downloading audio file from url: %s",
              clientId,projectId,modelId,audioURL));
          return Response.status(Status.BAD_REQUEST).entity(new Error(Status.BAD_REQUEST.getStatusCode(), null,
              ErrorMessage.AUDIO_URL_ERROR)).build();
        }
      } else {
        if(fileInputStream == null){
          log.error(String.format("For client: %s, project: %s, modelId: %s, Audio file is empty or null",
              clientId,projectId,modelId));
          return Response.status(Status.BAD_REQUEST).entity(new Error(Status.BAD_REQUEST.getStatusCode(), null,
              ErrorMessage.AUDIO_FILE_ERROR)).build();
        }
        tempPath = Files.createTempFile(UUID.randomUUID().toString(), ".wav");
        log.info("Storing input file at " + tempPath.toString());
        FileUtils.copyInputStreamToFile(fileInputStream, new File(tempPath.toString()));
      }

      ModelBO model = validationManager.validateAndGetModel(clientId, projectId, modelId, "modelId");
      // String speechModelId = model.getSpeechModelId();

      //ModelBO speechModel = validationManager.validateAndGetModel(clientId, projectId, speechModelId, Constants.MODEL_DB_ID);

      String modelURL = null;
      if(model.getModelType().equals(Constants.SPEECH_MODEL)){
        modelURL = appConfig.getOrionURL() + "/" + modelId + "/slm";
      } else {
        modelURL = appConfig.getOrionURL() + "/" + modelId + "/speech";
      }

      evalResponse.setProjectId(projectId);
      evalResponse.setModelId(modelId);
      evalResponse.setType(EvaluationResponse.TypeEnum.UTTERANCES);
      UtteranceEvaluation eval = getResponseFromWebReco(tempPath.toString(),modelURL);
      evalResponse.addEvaluationsItem(eval);
      evalResponse.setStatus(StatusEnum.SUCCESS);

      // This is done so that Front end can play and download the file from external source
      if (fileType.equalsIgnoreCase(Constants.UTTERANCE_FILE_TYPE_AS_LINK)) {
        eval.setUtteranceFileData(encodeFileToBase64Binary(tempPath.toAbsolutePath().toString()));
      }

      return Response.ok().entity(evalResponse).build();
    }catch (BadFileException e){
      log.error(ErrorMessage.INVALID_FILE_ERROR,e);
      return Response.status(Status.BAD_REQUEST).entity(new Error(Status.BAD_REQUEST.getStatusCode(), null,
          ErrorMessage.INVALID_FILE_ERROR)).build();
    }catch (TranscribingException e){
      log.error(ErrorMessage.INVALID_TRANSCRIBING_ERROR,e);
      return Response.status(Status.BAD_REQUEST).entity(new Error(Status.BAD_REQUEST.getStatusCode(), null,
          ErrorMessage.INVALID_TRANSCRIBING_ERROR)).build();
    }catch (Exception e) {
      log.error("Failed while evaluating speech file again webreco",e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }finally {
      if(fileInputStream!=null) {
        try {
          fileInputStream.close();
        } catch (IOException e) {
          log.error("Failed to close input stream", e);
        }
      }
      if (tempPath != null) {
        try {
          Files.deleteIfExists(tempPath);
        } catch (IOException e) {
          log.error("Error deleting temp file", e);
        }
      }
    }
  }


  @Override
  public Response evalTranscriptions(String clientId, String projectId, String modelId,
      String testModelType, List<String> utterances)
      throws NotFoundException {
    EvaluationResponse evalResponse = new EvaluationResponse();
    evalResponse.setProjectId(projectId);
    evalResponse.setModelId(modelId);
    evalResponse.setType(EvaluationResponse.TypeEnum.UTTERANCES);

    String web2nlUrl = appConfig.getWeb2nlUrl().trim();
    String apiKey = appConfig.getWeb2nlApiKey().trim();
    String orionUrl = appConfig.getOrionURL().trim();
    if (StringUtils.isEmpty(web2nlUrl) ||
        StringUtils.isEmpty(apiKey)) {
      log.error(
          "The server is not  configured for web2nl. Please set tfs.web2nl.host, tfs.web2nl.api.key");
      evalResponse.setMessage(
          "The server is not not configured for web2nl. Please set tfs.web2nl.host, tfs.web2nl.api.key");
      return Response.serverError().entity(evalResponse).build();
    }
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

    long startTime = System.currentTimeMillis();

    int retryLimit = 3;
    for (String one : utterances) {
      UtteranceEvaluation eval = null;
      int retry = 0;
      while (retry < retryLimit) {
        try {
          eval = evalOneUtterrance(web2nlUrl, orionUrl, apiKey, modelId, one, testModelType);
          evalResponse.addEvaluationsItem(eval);
          evalResponse.setStatus(StatusEnum.SUCCESS);
          break;
        } catch (ApplicationException e) {
          log.error("failed to evaluate utterance - " + projectId + " - " + modelId + " - " + retry,
              e);
          if(e.getCause() instanceof HttpServerErrorException.GatewayTimeout) {
            return Response.status(Status.GATEWAY_TIMEOUT).entity(
                    new Error(Status.GATEWAY_TIMEOUT.getStatusCode(), null,
                            ErrorMessage.GATEWAY_TIMEOUT_ERROR)).build();
          }
          evalResponse.setMessage(e.getMessage());
          break;
        } catch (ApiException e) {
          log.error("failed to evaluate utterance, sleep and retry - " + projectId + " - " + modelId
              + " - " + retry, e);
          try {
            Thread.sleep((long) (Math.random() * 600));
          } catch (InterruptedException e1) {
            log.error(
                "Failed to pause for evaluating utterrance - " + modelId + " - " + retry + " - "
                    + apiKey + " - " + one);
          }
        }
        retry += 1;
      }
      if (!StringUtils.isEmpty(evalResponse.getMessage())) {
        break;
      }
    }
    if (!StringUtils.isEmpty(evalResponse.getMessage())) {
      log.info("Failed utterance test - {} - {} - {} takes {}", projectId, modelId,
          utterances.size(), System.currentTimeMillis() - startTime);
      return Response.serverError().entity(evalResponse).build();
    }
    log.info("Finish utterance test - {} - {} - {} takes {}", projectId, modelId, utterances.size(),
        System.currentTimeMillis() - startTime);
    return Response.ok().entity(evalResponse).build();
  }
}
