package com.tfs.learningsystems.testutil;

import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.ClientBO.State;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.ui.model.AddCommentRequest;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.Locale;
import com.tfs.learningsystems.ui.model.ModelConfigCollection;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.SearchRequestFilter;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.Vertical;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelection;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

public class IntegrationUtilTest {

  @Autowired
  private TestRestTemplate restTemplate;


  private static ClientBO getTestClientBO(String name, boolean isVertical) {
    ClientBO client = new ClientBO();
    client.setName(name);
    client.setDescription("For integration Test");
    client.setAddress("123 Fake St. San Francisco, CA, 94110, USA");
    client.setIsVertical(isVertical);
    client.setState(State.ENABLED);
    return client;
  }

  public static ClientBO getTestClientObject(String name) {
    return (getTestClientBO(name, false));
  }

  public static ProjectBO getTestProjectObject(String cId, int clientId, String userId,
      String projectName) {
    ProjectBO project = new ProjectBO();
    project.setName(projectName);
    project.setDescription("Project for Integration Test");
    project.setClientId(clientId);
    project.setCid(cId);
    project.setOwnerId(userId);
    project.setVertical(Vertical.valueOf("FINANCIAL").toString());
    project.setState(ProjectBO.State.ENABLED);
    project.setLocale(Locale.EN_US);
    return project;
  }

  public static DatasetBO getTestDatasetObject(int clientId, int projectId, String datasetName, String uri) {
    DatasetBO dataset = new DatasetBO();
    dataset.setClientId(Integer.toString(clientId));
    dataset.setProjectId(Integer.toString(projectId));
    dataset.setName(datasetName);
    dataset.setDataType(DataType.valueOf("AIVA").getName());
    dataset.setDescription("Data set for integration test");
    dataset.setUri(uri);
    dataset.setLocale(Locale.EN_US);
    return dataset;
  }

  public static AddDatasetRequest addDatasetReq(DatasetBO dataset, Integer projectID) {
    AddDatasetRequest addDatasetRequest = new AddDatasetRequest();
    addDatasetRequest.setDataset(dataset);
    addDatasetRequest.setProjectId(Integer.toString(projectID));
    addDatasetRequest.setAutoTagDataset(true);
    return addDatasetRequest;
  }


  public static ModelBO getModelObject(String cid, int projectId, String configId, int datasetId,

      String currentUserId, String modelType) {
    ModelBO model = new ModelBO();
    model.setName("Test model");
    model.setDescription("Model for integration test");
    model.setProjectId(projectId);
    if (configId != null) {
      model.setConfigId(Integer.parseInt(configId));
    }
    model.setUserId(currentUserId);
    model.setDatasetIds(Collections.singletonList(Integer.toString(datasetId)));
    model.setVersion(1);
    model.setCid(cid);
    model.setDigitalHostedUrl("");
    model.setCreatedAt(Calendar.getInstance().getTimeInMillis() - 1);
    model.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
    model.setModelType(modelType);
    return model;
  }

  public static String getJsonWithTags(int tags) throws IOException {
    ClassPathResource resource = new ClassPathResource("file_columns.csv");
    File file = resource.getFile();
    List<List<String>> lines = new ArrayList<>();
    Scanner scanner;
    String json_str = null;
    try {
      scanner = new Scanner(file);
      while (scanner.hasNext()) {
        String line = scanner.nextLine();
        String[] values = line.split(",");
        lines.add(Arrays.asList(values));
      }
      scanner.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    json_str = "["
            + "{\"id\":\"1\",\"columnName\":\""+ lines.get(1).get(0) +"\",\"columnIndex\":\"1\",\"displayName\":\""+ lines.get(1).get(2) +"\"}"
            + "]";

    switch (tags) {
      case 1:
        json_str = "["
                + "{\"id\":\"1\",\"columnName\":\""+ lines.get(1).get(0) +"\",\"columnIndex\":\"1\",\"displayName\":\""+ lines.get(1).get(2) +"\"},"
                + "{\"id\":\"4\",\"columnName\":\""+ lines.get(4).get(0) +"\",\"columnIndex\":\"4\",\"displayName\":\""+ lines.get(4).get(2) +"\"},"
                + "{\"id\":\"3\",\"columnName\":\""+ lines.get(3).get(0) +"\",\"columnIndex\":\"0\",\"displayName\":\""+ lines.get(3).get(2) +"\"}"
                + "]";
        break;
      case 2:
        break;
    }
    return json_str;
  }


  public static AddIntentRequest bulkTagUpdate(List<TranscriptionDocumentDetail> transcriptionList,
      String userId) {

    AddIntentRequest addIntentRequest = new AddIntentRequest();
    addIntentRequest.setIntent("test-intent");
    addIntentRequest.setRutag("test-rutag");
    addIntentRequest.setUsername(userId);
    List<String> transcriptionHashList_tag = new ArrayList<>();
    for (int l = 0; l < transcriptionList.size(); l++) {
      String transcription_hash = transcriptionList.get(l).getTranscriptionHash();
      transcriptionHashList_tag.add(transcription_hash);
    }
    addIntentRequest.setTranscriptionHashList(transcriptionHashList_tag);
    return addIntentRequest;
  }

  public static AddIntentRequest bulkUntagUpdate(
      List<TranscriptionDocumentDetail> transcriptionList, String userId) {
    AddIntentRequest deleteIntentRequest = new AddIntentRequest();
    deleteIntentRequest.setUsername(userId);
    List<String> transcriptionHashList_tag = new ArrayList<>();
    for (int l = 0; l < transcriptionList.size(); l++) {
      String transcription_hash = transcriptionList.get(l).getTranscriptionHash();
      transcriptionHashList_tag.add(transcription_hash);
    }
    deleteIntentRequest.setTranscriptionHashList(transcriptionHashList_tag);
    return deleteIntentRequest;
  }

  public static AddCommentRequest commentUpdate(
      List<TranscriptionDocumentDetail> transcriptionList, String userId, String comment) {
    AddCommentRequest addCommentRequest = new AddCommentRequest();
    addCommentRequest.setComment(comment);
    addCommentRequest.setUsername(userId);
    List<String> transcriptionHashList = new ArrayList<>();
    for (int l = 0; l < transcriptionList.size(); l++) {
      String transcription_hash = transcriptionList.get(l).getTranscriptionHash();
      transcriptionHashList.add(transcription_hash);
    }
    addCommentRequest.setTranscriptionHashList(transcriptionHashList);
    return addCommentRequest;
  }

  public static SearchRequest searchRequest(List<String> datasets) {
    SearchRequest search = new SearchRequest();
    SearchRequestFilter filter = new SearchRequestFilter();
    filter.setDatasets(datasets);
    search.setFilter(filter);
    return search;
  }

  public static SearchRequest getTranscript(List<String> datasets, int tag, String query,
      Boolean comment) {
    SearchRequest searchTran = new SearchRequest();
    searchTran.query(query);
    SearchRequestFilter filterTran = new SearchRequestFilter();
    filterTran.setDatasets(datasets);
    switch (tag) {
      //Untagged transcripts
      case 1:
        filterTran.setTagged(false);
        filterTran.setUntagged(true);
        break;
      //Tagged transcripts
      case 2:
        filterTran.setTagged(true);
        filterTran.setUntagged(false);
        break;
      //All transcripts
      case 3:
        filterTran.setTagged(false);
        filterTran.setUntagged(false);
        break;
    }
    filterTran.setHasComment(comment);
    searchTran.setFilter(filterTran);
    return searchTran;
  }

  public static SearchRequest getDatasetTranscript(int tag, String query, Boolean comment) {
    SearchRequest searchTran = new SearchRequest();
    searchTran.query(query);
    SearchRequestFilter filterTran = new SearchRequestFilter();
    switch (tag) {
      //Untagged transcripts
      case 1:
        filterTran.setTagged(false);
        filterTran.setUntagged(true);
        break;
      //Tagged transcripts
      case 2:
        filterTran.setTagged(true);
        filterTran.setUntagged(false);
        break;
      //All transcripts
      case 3:
        filterTran.setTagged(false);
        filterTran.setUntagged(false);
        break;
    }
    filterTran.setHasComment(comment);
    searchTran.setFilter(filterTran);
    return searchTran;
  }

  public static Map<String, String> testTaggingGuide(Object o) throws IOException {
    StringWriter writer_untag = new StringWriter();
    ObjectMapper mapper_getTaggingGuide_untag = new ObjectMapper();
    mapper_getTaggingGuide_untag.writeValue(writer_untag, o);
    String tagging_guide_value = writer_untag.toString();
    tagging_guide_value = tagging_guide_value.replace("\"", "");
    tagging_guide_value = tagging_guide_value.substring(2, tagging_guide_value.length() - 2);
    String[] keyValuePair = tagging_guide_value.split(",");
    Map<String, String> taggingMap = new HashMap<>();
    for (String pairs : keyValuePair) {
      String[] entry_tagging_guide = pairs.split(":");
      taggingMap.put(entry_tagging_guide[0].trim(), entry_tagging_guide[1].trim());
    }
    return taggingMap;
  }

  public static AddIntentRequest addNewIntent(List<TranscriptionDocumentDetail> transcriptionList,
      String userId, String intent) {
    AddIntentRequest newIntentRequest = new AddIntentRequest();
    newIntentRequest.setIntent(intent);
    newIntentRequest.setUsername(userId);
    List<String> transcriptionHashList_tag = new ArrayList<>();
    for (int l = 0; l < transcriptionList.size(); l++) {
      String transcription_hash = transcriptionList.get(l).getTranscriptionHash();
      transcriptionHashList_tag.add(transcription_hash);
    }
    newIntentRequest.setTranscriptionHashList(transcriptionHashList_tag);
    return newIntentRequest;
  }

  public static List<String> decodeBatchTestMessage(String message) throws IOException {

    Map<String, String> resultMap = new HashMap<String, String>();
    ObjectMapper mapperObj = new ObjectMapper();
    resultMap = mapperObj.readValue(message, new TypeReference<HashMap<String, String>>() {
    });
    byte[] decoded = Base64.decodeBase64(resultMap.get("message"));
    String str = new String(decoded);
    str = str.replace("\"", "");
    List<String> ls = new ArrayList<>();
    String[] lines = str.split("\\r?\\n");
    for (String line : lines) {
      ls.add(line);
    }
    List<String> ls2 = new ArrayList<>();
    String[] words = ls.get(1).split(",");
    for (String word : words) {
      ls2.add(word);
    }
    return ls2;
  }

  public static HttpEntity<LinkedMultiValueMap<String, Object>> fileImport(String userId) {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("Integration_Test.csv"));
    map.add("username", userId);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
        map, headers);
    return requestEntity;
  }

  public static HttpEntity<LinkedMultiValueMap<String, Object>> fileImportLargeFile(String userId) {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("Integration_Test_1500k.csv"));
    map.add("username", userId);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
        map, headers);
    return requestEntity;
  }

  public static HttpEntity<LinkedMultiValueMap<String, Object>> fileImportNoTag(String userId) {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("Integration_Test_NoIntent_NoRutag.csv"));
    map.add("username", userId);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
        map, headers);
    return requestEntity;
  }

  public static FileColumnMappingSelectionList fileColMapping(String json_str)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    FileColumnMappingSelectionList fileColumnMappingSelections = mapper
        .readValue(json_str, FileColumnMappingSelectionList.class);
    return fileColumnMappingSelections;
  }

  public static TaggingGuideDocument addTagInTaggingGuide() {
    TaggingGuideDocument add_tag = new TaggingGuideDocument();
    add_tag.setIntent("new-tag");
    add_tag.setRutag("new-rutag");
    add_tag.setKeywords("tag test");
    add_tag.setComments("Added new tag in tagging guide");
    return add_tag;
  }

  public static HttpEntity<String> updateTagInTaggingGuide(String path, String value)
      throws JsonProcessingException {
    ArrayList<PatchDocument> patchRequest = new PatchRequest();
    PatchDocument patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath(path);
    patchDocument.setValue(value);
    patchRequest.add(patchDocument);

    ObjectMapper mapping = new ObjectMapper();
    String jsonPatch = mapping.writeValueAsString(patchRequest);
    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.valueOf("application/json-patch+json"));
    HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, header);
    return httpEntity;
  }

  public static List<TaggingGuideDocumentDetail> taggingGuideDocument(Object o)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    List<TaggingGuideDocumentDetail> taggingGuideDocumentList = mapper.readValue(jsonStr,
        mapper.getTypeFactory()
            .constructCollectionType(List.class, TaggingGuideDocumentDetail.class));
    return taggingGuideDocumentList;
  }

  public static List<String> addUtterance() {
    List<String> uttrances = new LinkedList<>();
    uttrances.add("representative");
    return uttrances;
  }

  public static HttpEntity<LinkedMultiValueMap<String, Object>> configImport(String filename,
      String desc, Integer projectID, String file) {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("name", filename);
    map.add("description", desc);
    map.add("projectId", projectID);
    map.add("file", new ClassPathResource(file));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);
    return requestEntity;
  }

  public static void testDatasetExport() throws IOException {
    String dirpath = "build/tmp/jobs";
    File mainDir = new File(dirpath);
    File[] files = mainDir.listFiles();
    if (files.length == 0) {
      System.out.print("empty");
    } else {
      Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
      for (File file : files) {
        //List dir and sub-dir of client folder
        File latestDir = new File(file.getPath());
        File[] filesPresent = latestDir.listFiles();
        if (filesPresent.length == 0) {
          System.out.println("No files present in client directory");
        } else {
          for (File fileName : filesPresent) {  // looping through files in the client directory
            File projDirPath = new File(fileName.getPath());
            File[] csvfiles = projDirPath.listFiles();
            if (csvfiles.length == 0) {
              System.out.println("No files present in project directory");
            } else {
              for (File csv : csvfiles) {
                if (csv.getName().equalsIgnoreCase("output.csv")) {
                  FileReader filereader = new FileReader(csv);
                  CSVReader csvReader = new CSVReader(filereader);
                  List<String[]> allData = csvReader.readAll();
                  for (String[] record : allData) {
                    System.out.println("Intent : " + record[0]);
                    System.out.println("Transcription : " + record[1]);
                    System.out.println("Original Transcription : " + record[2]);
                    System.out.println("Granular Intent : " + record[3]);
                    System.out.println("File name : " + record[4]);
                    System.out.println("Transcription hash : " + record[5]);
                    System.out.println("Comments : " + record[6]);
                    System.out.println("---------------------------");
                  }
                }//if
              }
            }
          }
          break;
        }
        break;
      }
    }
  }

  public static List<String> testGetReportFields(Object o) throws IOException {
    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(writer, o);
    String reportFields = writer.toString();
    reportFields = reportFields.replace("\"", "");
    reportFields = reportFields.substring(1, reportFields.length() - 1);
    List<String> ls = new ArrayList<>();
    String[] lines = reportFields.split("},");
    for (String line : lines) {
      ls.add(line);
    }
    return ls;
  }

  public static Integer getClientId(Object o)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    String str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    str = str.replace("\"", "");
    str = str.substring(3, str.length() - 3);
    Integer clientId = 0;
    ArrayList<String> aList = new ArrayList(Arrays.asList(str.split("}, \\{")));
    for (int i = 0; i < aList.size(); i++) {
      boolean clientFound = aList.get(i).contains("ModelingWorkbench");
      if (clientFound == true) {
        String[] keyValuePair = aList.get(i).split(",");
        Map<String, String> clientMap = new HashMap<>();
        for (String pairs : keyValuePair) {
          String[] entry = pairs.split(":");
          clientMap.put(entry[0].trim(), entry[1].trim());
        }
        clientId = Integer.valueOf(clientMap.get("id"));
      }
    }
    return clientId;
  }


  public static String getCId(Object o)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    String str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    str = str.replace("\"", "");
    str = str.substring(3, str.length() - 3);
    String cId = null;
    ArrayList<String> aList = new ArrayList(Arrays.asList(str.split("}, \\{")));
    for (int i = 0; i < aList.size(); i++) {
      boolean clientFound = aList.get(i).contains("ModelingWorkbench");
      if (clientFound == true) {
        String[] keyValuePair = aList.get(i).split(",");
        Map<String, String> clientMap = new HashMap<>();
        for (String pairs : keyValuePair) {
          String[] entry = pairs.split(":");
          clientMap.put(entry[0].trim(), entry[1].trim());
        }
        cId = clientMap.get("cid");
      }
    }
    return cId;
  }

  public static String getDefaultConfigId(ResponseEntity<ModelConfigCollection> getProjectConfig) {
    String configID = null;
    for (int i = 0; i < getProjectConfig.getBody().size(); i++) {
      if (getProjectConfig.getBody().get(i).getName().equalsIgnoreCase("system_default_en")) {
        configID = getProjectConfig.getBody().get(i).getId().toString();
        break;
      }
    }
    assertNotNull(configID);
    return configID;
  }

}