package com.tfs.learningsystems.ui.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.testutil.ModelBuilderUtilTest;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.ConfigManager;
import com.tfs.learningsystems.ui.ElasticApiBaseTest;
import com.tfs.learningsystems.ui.FileManager;
import com.tfs.learningsystems.ui.ModelManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.*;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState;
import com.tfs.learningsystems.ui.nlmodel.model.dao.ModelDao;
import com.tfs.learningsystems.util.CommonLib;
import com.tfs.learningsystems.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class ModelsApiTest extends ElasticApiBaseTest {

    @Inject
    @Qualifier("modelManagerBean")
    private ModelManager modelManager;

    @Autowired
    @Qualifier("elasticTestUtils")
    private ElasticSearchTestUtils esTestUtils;

    @Inject
    @Qualifier("fileManagerBean")
    private FileManager fileManager;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ModelDao modelDao;

    private MockRestServiceServer mockServer;

    public String clientId;
    public String projectId;
    public String datasetId;
    public String userId = "UnitTest@247.ai";
    public String cid;
    public String modelAccuracy= "0.88";
    public String modelWeightedFScore= "0.87";
    private String modelUUID = "a5d666e6-f062-4f2f-9001-3da5b92ac74f";
    private String token = "f36fff00-b573-4da5-bca2-213a071f5c1f";
    private String modelTechnology = "n-gram";
    private String modelLevel = "model";

    @Before
    public void setUp() throws IOException {
        super.setUp();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        try {
            this.configManager.reloadDefaultConfig(appConfig.getEnglishConfigArchiveFilename(),
                    Constants.DEFAULT_EN_CONFIG_NAME, "en");

            CommonLib.Auth.addAccessToken(restTemplate);
            this.esTestUtils.refreshAllIndices();
            clientId = Integer.toString(super.clientDetail.getId());
            cid = super.clientDetail.getCid();
            projectId = Integer.toString(super.projectDetail.getId());
            datasetId = Integer.toString(super.datasetDetail.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    private void addingDataToES() {
        int clientId = Integer.valueOf(this.clientId).intValue();
        int projectId = Integer.valueOf(this.projectId).intValue();
        int datasetId = Integer.valueOf(this.datasetId).intValue();
        this.setProjectDatasetTransformed();
        List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();

        List<TranscriptionDocumentForIndexing> makeResHash1Documents = this.esTestUtils
                .getMakeReservationHash1Documents("1", clientId, projectId, datasetId);
        allDocuments.addAll(makeResHash1Documents);

        List<TranscriptionDocumentForIndexing> makeResHash2Documents = this.esTestUtils
                .getMakeReservationHash2Documents("1", clientId, projectId, datasetId);
        allDocuments.addAll(makeResHash2Documents);

        List<TranscriptionDocumentForIndexing> pointsbalanceDocuments =
                this.esTestUtils.getPointsBalanceDocuments("1", clientId, projectId, datasetId);
        allDocuments.addAll(pointsbalanceDocuments);

        String indexUrl = String.format("%scontent/%d/projects/%d/datasets/%d/index", basePath,
                clientId, projectId, datasetId);
        log.info("ES URL: " + indexUrl);
        ResponseEntity<Void> indexEntity =
                super.restTemplate.postForEntity(indexUrl, allDocuments, Void.class);
        assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

        this.esTestUtils.refreshNltoolsIndex();

        AddIntentRequest addIntentRequest = new AddIntentRequest();
        addIntentRequest.setUsername("john.doe");
        addIntentRequest.setIntent("reservation-make");
        List<String> transcriptionHashList = new ArrayList<>();
        for (TranscriptionDocumentForIndexing allDocument : allDocuments) {
            transcriptionHashList.add(allDocument.getTranscriptionHash());
        }

        addIntentRequest.setTranscriptionHashList(transcriptionHashList);

        String tagUrl =
                String.format("%scontent/%d/tag", basePath, projectId);
        ResponseEntity<UpdateIntentResponse> entity = super.restTemplate.postForEntity(tagUrl,
                addIntentRequest, UpdateIntentResponse.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());

        this.esTestUtils.refreshNltoolsIndex();
    }

    @Test
    public void testConfigureModel() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), proj.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
        ModelBO model = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);

        ResponseEntity<ModelBO> entity = super.restTemplate
                .postForEntity(basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models",
                        model, ModelBO.class);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        ModelBO addedModel = entity.getBody();
        assertEquals(proj.getModelVersion(), addedModel.getVersion());
        assertTrue(timeStamp < addedModel.getCreatedAt());
        addedModel.delete();
        proj.delete();
        client.delete();
        testVectorizer.delete();
        testPreference.delete();
    }

    @Test
    public void testConfigureModelForDefault() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), proj.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        Mockito.doReturn(testPreference).when(this.preferenceManager)
                .getPreferenceByLevelTypeAndAttribute(proj.getClientId().toString(), Constants.PREFERENCE_MODEL_LEVEL, Constants.VECTORIZER_TYPE, proj.getId().toString(),false);

        long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
        ModelBO model = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);

        ResponseEntity<ModelBO> entity = super.restTemplate
                .postForEntity(basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models" + "?toDefault=true" + "&modelTechnology=n-gram",
                        model, ModelBO.class);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        ModelBO addedModel = entity.getBody();
        assertEquals(proj.getModelVersion(), addedModel.getVersion());
        assertTrue(timeStamp < addedModel.getCreatedAt());
        addedModel.delete();
        proj.delete();
        client.delete();
        testVectorizer.delete();
        testPreference.delete();
    }

    @Test
    public void testConfigureModelForDefaultForCreatePreference() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), proj.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        Mockito.doThrow(InvalidRequestException.class).when(this.preferenceManager)
                .getPreferenceByLevelTypeAndAttribute(proj.getClientId().toString(), Constants.PREFERENCE_MODEL_LEVEL, Constants.VECTORIZER_TYPE, proj.getId().toString(),false);

        Mockito.doReturn(testPreference).when(this.preferenceManager)
                .addPreference(proj.getClientId().toString(), testPreference.getType(), testPreference.getAttribute(), testVectorizer.getId() ,Constants.PREFERENCE_MODEL_LEVEL, true);

        long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
        ModelBO model = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);

        ResponseEntity<ModelBO> entity = super.restTemplate
                .postForEntity(basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models" + "?toDefault=true" + "&modelTechnology=n-gram",
                        model, ModelBO.class);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        ModelBO addedModel = entity.getBody();
        assertEquals(proj.getModelVersion(), addedModel.getVersion());
        assertTrue(timeStamp < addedModel.getCreatedAt());
        addedModel.delete();
        proj.delete();
        client.delete();
        testVectorizer.delete();
        testPreference.delete();
    }

    @Test
    public void testConfigureSpeechModelWithoutDigitalHostedUrl() {

        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), proj.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();

        StringBuilder digitalUrl = new StringBuilder(appConfig.getOrionURL())
                .append(Constants.FORWARD_SLASH)
                .append(modelUUID)
                .append(Constants.FORWARD_SLASH)
                .append(Constants.DIGITAL_MODEL.toLowerCase());

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
        ModelBO model = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        model.setModelType(Constants.DIGITAL_SPEECH_MODEL);
        model.setModelId(modelUUID);

        ResponseEntity<ModelBO> entity = super.restTemplate
                .postForEntity(basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models",
                        model, ModelBO.class);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        ModelBO addedModel = entity.getBody();

        assertEquals(proj.getModelVersion(), addedModel.getVersion());
        assertTrue(timeStamp < addedModel.getCreatedAt());
        assertEquals(addedModel.getDigitalHostedUrl(), digitalUrl.toString());
        addedModel.delete();
        proj.delete();
        client.delete();
        testPreference.delete();
        testVectorizer.delete();
    }

    @Test
    public void testConfigureSpeechModelWithDigitalHostedUrl() {

        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), proj.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();

        String digitalUrl = "https://digital.com/digital";

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
        ModelBO model = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        model.setModelType(Constants.DIGITAL_SPEECH_MODEL);
        model.setModelId(modelUUID);
        model.setDigitalHostedUrl(digitalUrl);

        ResponseEntity<ModelBO> entity = super.restTemplate
                .postForEntity(basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models",
                        model, ModelBO.class);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        ModelBO addedModel = entity.getBody();

        assertEquals(proj.getModelVersion(), addedModel.getVersion());
        assertTrue(timeStamp < addedModel.getCreatedAt());
        assertEquals(addedModel.getDigitalHostedUrl(), digitalUrl);
        addedModel.delete();
        proj.delete();
        client.delete();
        testPreference.delete();
        testVectorizer.delete();
    }

    @Test
    public void testConfigureSpeechModelWithDigitalSpeechModelIdUpdate() {

        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), proj.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));


        ModelBO digitalModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        digitalModel.setModelId(modelUUID);
        digitalModel.create();

        ModelBO model = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        model.setModelType(Constants.DIGITAL_SPEECH_MODEL);
        model.setModelId(modelUUID);

        ResponseEntity<ModelBO> entity = super.restTemplate
                .postForEntity(basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models",
                        model, ModelBO.class);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        ModelBO addedModel = entity.getBody();

        ModelBO digitalModelDup = modelManager.getDigitalModelByModelId(modelUUID);

        assertEquals(digitalModelDup.getSpeechModelId(), addedModel.getId().toString());
        addedModel.delete();
        digitalModel.delete();
        digitalModelDup.delete();
        proj.delete();
        client.delete();
        testPreference.delete();
        testVectorizer.delete();
    }

    @Test
    public void testDeletePrevSpeechOnCreatingNewSpeechModel() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), proj.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        ModelBO digitalModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        digitalModel.setModelId(modelUUID);
        digitalModel.create();

        ModelBO speechModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        speechModel.setModelType(Constants.DIGITAL_SPEECH_MODEL);
        speechModel.setModelId(modelUUID);
        speechModel.create();

        digitalModel.setSpeechModelId(speechModel.getId().toString());
        digitalModel.update();

        Mockito.doReturn(digitalModel).when(this.validationManager).
                validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(digitalModel.getId()), Constants.MODEL_DB_ID);

        Mockito.doReturn(speechModel).when(this.validationManager).
                validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(speechModel.getId()), Constants.MODEL_DB_ID);


        ModelJobQueueBO modelJobState = new ModelJobQueueBO();
        modelJobState.setModelId(digitalModel.getModelId());
        modelJobState.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobState.setToken(digitalModel.getModelId());
        modelJobState.setStartedAt(System.currentTimeMillis());
        modelJobState.create();

        ModelBO model = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        model.setModelType(Constants.DIGITAL_SPEECH_MODEL);
        model.setModelId(modelUUID);

        ResponseEntity<ModelBO> speechEntity = super.restTemplate
                .postForEntity(basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models",
                        model, ModelBO.class);

        assertEquals(HttpStatus.CREATED, speechEntity.getStatusCode());
        ModelBO addedSpeechModel = speechEntity.getBody();

        ModelBO digitalModelDup = modelManager.getDigitalModelByModelId(modelUUID);

        assertEquals(digitalModelDup.getSpeechModelId(), addedSpeechModel.getId().toString());

        addedSpeechModel.delete();
        digitalModelDup.delete();
        digitalModel.delete();
        modelJobState.delete();
        proj.delete();
        client.delete();
        testPreference.delete();
        testVectorizer.delete();
    }

    @Test
    public void testShouldDeleteModelsFromModelJobQueueTable() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        ModelBO digitalModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        digitalModel.setModelId(modelUUID);
        digitalModel.setModelType(Constants.DIGITAL_MODEL);
        digitalModel.create();

        ModelBO speechModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        speechModel.setModelType(Constants.DIGITAL_SPEECH_MODEL);
        speechModel.setModelId(modelUUID);
        speechModel.create();

        digitalModel.setSpeechModelId(speechModel.getId().toString());
        digitalModel.update();

        Mockito.doReturn(digitalModel).when(this.validationManager).
                validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(digitalModel.getId()), Constants.MODEL_DB_ID);

        Mockito.doReturn(speechModel).when(this.validationManager).
                validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(speechModel.getId()), Constants.MODEL_DB_ID);

        ModelJobQueueBO modelJobStateDigitalModel = new ModelJobQueueBO();
        modelJobStateDigitalModel.setModelId(modelUUID);
        modelJobStateDigitalModel.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobStateDigitalModel.setToken(token);
        modelJobStateDigitalModel.setModelType(digitalModel.getModelType());
        modelJobStateDigitalModel.setStartedAt(System.currentTimeMillis());
        modelJobStateDigitalModel.create();

        ModelJobQueueBO modelJobStateSpeechModel = new ModelJobQueueBO();
        modelJobStateSpeechModel.setModelId(modelUUID);
        modelJobStateSpeechModel.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobStateSpeechModel.setToken(token);
        modelJobStateSpeechModel.setModelType(speechModel.getModelType());
        modelJobStateSpeechModel.setStartedAt(System.currentTimeMillis());
        modelJobStateSpeechModel.create();

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body("{\"status\": \"model deleted successfully!!\"}"));

        String url = basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models/" + digitalModel.getId().toString();
        ResponseEntity<Void> entity = super.restTemplate
                .exchange(url, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(ModelJobQueueBO.FLD_MODEL_ID, speechModel.getModelId());
        paramMap.put(Constants.MODEL_TYPE, Constants.DIGITAL_SPEECH_MODEL);
        ModelJobQueueBO speechModelJobQueueBO = new ModelJobQueueBO();
        speechModelJobQueueBO = speechModelJobQueueBO.findOne(paramMap);
        assertNull(speechModelJobQueueBO);

        Map<String, Object> paramMap1 = new HashMap<>();
        paramMap1.put(ModelJobQueueBO.FLD_MODEL_ID, digitalModel.getModelId());
        paramMap1.put(Constants.MODEL_TYPE, Constants.DIGITAL_MODEL);
        ModelJobQueueBO digitalModelJobQueueBO = new ModelJobQueueBO();
        digitalModelJobQueueBO = digitalModelJobQueueBO.findOne(paramMap1);
        assertNull(digitalModelJobQueueBO);

        proj.delete();
        client.delete();
        digitalModel.delete();
        speechModel.delete();
        modelJobStateDigitalModel.delete();
        modelJobStateSpeechModel.delete();
    }

    @Test
    public void testDeleteModelById() throws InterruptedException {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();
        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        ModelBO digitalModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        digitalModel.setModelId(modelUUID);
        digitalModel.create();

        Mockito.doReturn(digitalModel).when(this.validationManager).
                validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(digitalModel.getId()), Constants.MODEL_DB_ID);

        ModelJobQueueBO modelJobState = new ModelJobQueueBO();
        modelJobState.setModelId(digitalModel.getModelId());
        modelJobState.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobState.setToken(digitalModel.getModelId());
        modelJobState.setStartedAt(System.currentTimeMillis());
        modelJobState.create();

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body("{\"status\": \"model deleted successfully!!\"}"));

        String url = basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models/" + digitalModel.getId().toString();

        ResponseEntity<Void> entity = super.restTemplate
                .exchange(url, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        proj.delete();
        client.delete();
        digitalModel.delete();
        modelJobState.delete();
    }

    // To Do item - Need to change exception throwing currently
    @Test
    public void testDeleteModelByIdCheckForPreviewModel() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        ModelBO digitalModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        digitalModel.setModelId(modelUUID);
        digitalModel.create();

        proj.setPreviewModelId(digitalModel.getId().toString());
        proj.update();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        Mockito.doReturn(digitalModel).when(this.validationManager)
                .validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(digitalModel.getId()), Constants.MODEL_DB_ID);

        Mockito.doThrow(new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(), "can_not_delete", "Can not be deleted"))).when(this.validationManager)
                .validatePreviewAndLiveModel(String.valueOf(client.getId()),
                        String.valueOf(proj.getId()), String.valueOf(digitalModel.getId()));

        ModelJobQueueBO modelJobState = new ModelJobQueueBO();
        modelJobState.setModelId(digitalModel.getModelId());
        modelJobState.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobState.setToken(digitalModel.getModelId());
        modelJobState.setStartedAt(System.currentTimeMillis());
        modelJobState.create();

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST));

        String url = basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models/" + digitalModel.getId().toString();
        ResponseEntity<Void> entity = super.restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

        proj.delete();
        client.delete();
        modelJobState.delete();
        digitalModel.delete();
    }

//    To Do item - Need to change exception throwing currently
    @Test
    public void testDeleteModelByIdCheckForLiveModel() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        ModelBO digitalModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        digitalModel.setModelId(modelUUID);
        digitalModel.create();

        proj.setLiveModelId(digitalModel.getId().toString());
        proj.update();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        Mockito.doReturn(digitalModel).when(this.validationManager)
                .validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(digitalModel.getId()), Constants.MODEL_DB_ID);

        Mockito.doThrow(new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(), "can_not_delete", "Can not be deleted"))).when(this.validationManager)
                .validatePreviewAndLiveModel(String.valueOf(client.getId()),
                        String.valueOf(proj.getId()), String.valueOf(digitalModel.getId()));

        ModelJobQueueBO modelJobState = new ModelJobQueueBO();
        modelJobState.setModelId(digitalModel.getModelId());
        modelJobState.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobState.setToken(digitalModel.getModelId());
        modelJobState.setStartedAt(System.currentTimeMillis());
        modelJobState.create();

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST));

        String url = basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models/" + digitalModel.getId().toString();
        ResponseEntity<Void> entity = super.restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

        proj.delete();
        client.delete();
        modelJobState.delete();
        digitalModel.delete();
    }

//    To Do item - Need to change exception throwing currently
    @Test
    public void testDeleteModelByIdCheckForDeployableModel() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        ModelBO digitalModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        digitalModel.setModelId(modelUUID);
        digitalModel.create();

        proj.setDeployableModelId(digitalModel.getId());
        proj.update();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        Mockito.doReturn(digitalModel).when(this.validationManager)
                .validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(digitalModel.getId()), Constants.MODEL_DB_ID);

        Mockito.doThrow(new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(), "can_not_delete", "Can not be deleted"))).when(this.validationManager)
                .validatePreviewAndLiveModel(String.valueOf(client.getId()),
                        String.valueOf(proj.getId()), String.valueOf(digitalModel.getId()));

        ModelJobQueueBO modelJobState = new ModelJobQueueBO();
        modelJobState.setModelId(digitalModel.getModelId());
        modelJobState.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobState.setToken(digitalModel.getModelId());
        modelJobState.setStartedAt(System.currentTimeMillis());
        modelJobState.create();

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST));

        String url = basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models/" + digitalModel.getId().toString();
        ResponseEntity<Void> entity = super.restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

        proj.delete();
        client.delete();
        modelJobState.delete();
        digitalModel.delete();
    }

    @Test
    public void testDeleteSpeechModelByDigitalId() throws InterruptedException {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        ModelBO digitalModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        digitalModel.setModelId(modelUUID);
        digitalModel.create();

        ModelJobQueueBO modelJobState = new ModelJobQueueBO();
        modelJobState.setModelId(digitalModel.getModelId());
        modelJobState.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobState.setToken(digitalModel.getModelId());
        modelJobState.setStartedAt(System.currentTimeMillis());
        modelJobState.create();

        ModelBO speechModel = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        speechModel.setModelType(Constants.DIGITAL_SPEECH_MODEL);
        speechModel.setModelId(modelUUID);
        speechModel.create();

        digitalModel.setSpeechModelId(speechModel.getId().toString());
        digitalModel.update();

        Mockito.doReturn(digitalModel).when(this.validationManager).
                validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(digitalModel.getId()), Constants.MODEL_DB_ID);

        Mockito.doReturn(speechModel).when(this.validationManager).
                validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(speechModel.getId()), Constants.MODEL_DB_ID);

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body("{\"status\": \"model deleted successfully!!\"}"));

        String url = basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models/" + digitalModel.getId().toString();

        ResponseEntity<Void> entity = super.restTemplate
                .exchange(url, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        proj.delete();
        client.delete();
        modelJobState.delete();
        digitalModel.delete();
        speechModel.delete();
    }

    @Test
    public void testModelVersion() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name1 = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name1, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name1, cid);
        proj.create();

        VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), proj.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();

        Mockito.doReturn(proj).when(this.validationManager)
                .validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        long timeStamp1 = Calendar.getInstance().getTimeInMillis() - 1;
        ModelBO model1 = ModelBuilderUtilTest.getDummyModel(name1, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        ResponseEntity<ModelBO> entity1 = super.restTemplate
                .postForEntity(basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models",
                        model1, ModelBO.class);
        ModelBO addedModel1 = entity1.getBody();
        Integer version1 = addedModel1.getVersion();

        assertEquals(proj.getModelVersion(), version1);
        assertTrue(timeStamp1 < addedModel1.getCreatedAt());

        addedModel1.delete();

        String name2 = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
        ModelBO model2 = ModelBuilderUtilTest.getDummyModel(name2, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        ResponseEntity<ModelBO> entity2 = super.restTemplate
                .postForEntity(basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models",
                        model2, ModelBO.class);
        ModelBO addedModel2 = entity2.getBody();

        assertEquals(proj.getModelVersion(), addedModel2.getVersion());
        assertTrue(version1 < addedModel2.getVersion());
        addedModel1.delete();
        addedModel2.delete();
        proj.delete();
        client.delete();
        testPreference.delete();
        testVectorizer.delete();
    }

    @Test
    public void testForNoModelsWithinProject() {
        String buildUrl =
                basePath + "clients/" + clientId + "/projects/" + "dummyprojectid" + "/models";
        ResponseEntity<Void> queryEntity = super.restTemplate.
                getForEntity(buildUrl, Void.class);
        assertEquals(HttpStatus.OK, queryEntity.getStatusCode());
        assertNull(queryEntity.getBody());
    }

    @Test
    public void testPatchModel() throws Exception {

        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), proj.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();

        Mockito.doReturn(proj).when(this.validationManager).
                validateClientAndProject(String.valueOf(proj.getClientId()),
                        String.valueOf(proj.getId()));

        long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
        ModelBO model = ModelBuilderUtilTest.getDummyModel(name, cid, proj.getId().toString(), datasetId, userId, modelAccuracy, modelWeightedFScore);
        ResponseEntity<ModelBO> entity = super.restTemplate
                .postForEntity(basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models",
                        model, ModelBO.class);
        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        ModelBO addedModel = entity.getBody();
        assertTrue(timeStamp < addedModel.getCreatedAt());

        Mockito.doReturn(addedModel).when(this.validationManager).
                validateAndGetModel(String.valueOf(client.getId()), String.valueOf(proj.getId()),
                        String.valueOf(addedModel.getId()), Constants.MODEL_DB_ID);

        String newDescription = "new" + addedModel.getDescription();

        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/description");
        patchDocument.setValue(newDescription);
        patchRequest.add(patchDocument);

        ModelBO patchedModel = null;
        ObjectMapper mapper = new ObjectMapper();

        String jsonPatch = mapper.writeValueAsString(patchRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/json-patch+json"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, headers);

        String url = basePath + "clients/" + client.getId().toString() + "/projects/" + proj.getId().toString() + "/models/" + addedModel.getId().toString();
        ResponseEntity<ModelBO> patchEntity =
                super.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, ModelBO.class);

        patchedModel = patchEntity.getBody();
        Integer patchedId = patchedModel.getId();

        assertEquals(addedModel.getId(), patchedId);
        assertEquals(newDescription, patchedModel.getDescription());
        patchedModel.delete();
        proj.delete();
        client.delete();
        testPreference.delete();
        testVectorizer.delete();
    }

    @Test
    public void testForAllAvailableModelsWithinProject() throws Exception {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        DatasetBO addDataset = ModelUtils.setTestDataset(this.clientDetail.getId(), this.projectDetail.getId(), name, cid);
        String file = "test-input.csv";
        FileBO addFile = fileManager
                .addFile(new ClassPathResource(file).getInputStream(), userId, addDataset.getDataType());
        String urlStr = basePath + "files/" + addFile.getFileId();
        addDataset.setUri(urlStr);
        addDataset.create();

        ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
        projectDatasetBO.setDatasetId(addDataset.getId());
        projectDatasetBO.setProjectId(this.projectDetail.getId());
        projectDatasetBO.setCid(cid);
        projectDatasetBO.create();

        ModelBO model = ModelBuilderUtilTest
                .getDummyModel(name, cid, projectId, addDataset.getId().toString(), userId, modelAccuracy, modelWeightedFScore);
        model.create();

        String buildUrl =
                basePath + "clients/" + clientId + "/projects/" + projectId + "/models";
        ResponseEntity<String> queryEntity = super.restTemplate.getForEntity(buildUrl, String.class);
        assertEquals(HttpStatus.OK, queryEntity.getStatusCode());
        model.delete();
        projectDatasetBO.delete();
        addDataset.delete();
    }


    @Ignore
    @Test
    public void testForModelQueuing() throws Exception {

        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
        Mockito.doReturn(this.projectDetail).when(this.projectManager).getProjectById(anyString());

        ModelBO model = ModelBuilderUtilTest.getDummyModel(name, cid, projectId, datasetId, userId, modelAccuracy, modelWeightedFScore);
        model.create();
        String modelId = model.getId().toString();

        Mockito.doReturn(model).when(this.validationManager).validateAndGetModel(clientId, null, modelId, Constants.MODEL_DB_ID);
        Mockito.doReturn(this.projectDetail).when(this.validationManager).validateClientAndProject(clientId, projectId);

        String orionResponse = "{\"link\":\"http://dummy/v1/models/a5d666e6-f062-4f2f-9001-3da5b92ac74f\"}";
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.ACCEPTED).body(orionResponse));


        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/a5d666e6-f062-4f2f-9001-3da5b92ac74f/status"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body("{\"status\": \"model1 created successfully!!\"}"));


        Path path = Paths.get("src/test/resources/unit_test_case.model");
        byte[] data = Files.readAllBytes(path);
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/a5d666e6-f062-4f2f-9001-3da5b92ac74f/digital"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(data));

        /// Starting to build the model
        String buildUrl = basePath + "clients/" + clientId + "/models/" + modelId + "/build";
        ResponseEntity<Void> buildEntity = super.restTemplate
                .postForEntity(buildUrl, null, Void.class);
        assertEquals(HttpStatus.ACCEPTED, buildEntity.getStatusCode());

        // Getting the model status
        buildUrl = basePath + "clients/" + clientId + "/projects/" + projectId + "/models/" + modelId + "/status";
        ResponseEntity<TFSModelJobState> statusEntity = super.restTemplate
                .getForEntity(buildUrl, TFSModelJobState.class);
        assertEquals(HttpStatus.OK, statusEntity.getStatusCode());
        assertEquals(TFSModelJobState.Status.RUNNING, statusEntity.getBody().getStatus());

        TFSModelJobState modelJobState = new TFSModelJobState();
        modelJobState.setStatus(TFSModelJobState.Status.COMPLETED);
        modelJobState.setEndedAt(System.currentTimeMillis());
        modelDao.updateModelJobStatusByModelId(model.getModelId(), model.getModelType(), modelJobState);

        // Getting the model status
        buildUrl = basePath + "clients/" + clientId + "/projects/" + projectId + "/models/" + modelId + "/download";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = super.restTemplate.exchange(buildUrl, HttpMethod.GET, entity,
                byte[].class,"1");
        mockServer.verify();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        FileUtils.writeByteArrayToFile(new File ("src/test/resources/unit_test_case_received.model"), response.getBody());
        assertTrue(FileUtils.contentEquals(new File("src/test/resources/unit_test_case_received.model"),
                new File("src/test/resources/unit_test_case.model")));
        model.delete();
    }

    @Test
    public void testForModelQueuingWithInvalidId(){
        /// Starting to build the model
        String buildUrl = basePath + "clients/" + clientId + "models/" + "dummy" + "/build";
        ResponseEntity<Void> buildEntity = super.restTemplate
                .postForEntity(buildUrl, null, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, buildEntity.getStatusCode());
    }

    @Test
    public void testForOrionServiceFailure() throws Exception{

        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
        Mockito.doReturn(this.projectDetail).when(this.projectManager).getProjectById(anyString());

        ModelBO model = ModelBuilderUtilTest.getDummyModel(name, cid, projectId, datasetId, userId, modelAccuracy, modelWeightedFScore);
        model.create();
        String modelId = model.getId().toString();

        Mockito.doReturn(model).when(this.validationManager).validateAndGetModel(clientId, null, modelId, Constants.MODEL_DB_ID);
        Mockito.doReturn(this.projectDetail).when(this.validationManager).validateClientAndProject(clientId, projectId);

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        /// Starting to build the model
        String buildUrl = basePath + "clients/" + clientId + "models/" + modelId + "/build";
        ResponseEntity<Void> buildEntity = super.restTemplate
                .postForEntity(buildUrl, null, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, buildEntity.getStatusCode());
        model.delete();
    }

    @Test
    public void testDownloadStatsFailure() throws Exception{
        Mockito.doReturn(this.projectDetail).when(this.projectManager).getProjectById(anyString());
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ModelBO tfsModel = ModelBuilderUtilTest.getDummyModel(name, cid, projectId, datasetId, userId, modelAccuracy, modelWeightedFScore);
        tfsModel.setModelId(UUID.randomUUID().toString());
        tfsModel.create();
        String modelId = tfsModel.getId().toString();

        Mockito.doReturn(tfsModel).when(this.validationManager).validateAndGetModel(clientId, null, modelId, Constants.MODEL_DB_ID);
        Mockito.doReturn(this.projectDetail).when(this.validationManager).validateClientAndProject(clientId, projectId);

        ModelJobQueueBO modelJobState = new ModelJobQueueBO();
        modelJobState.setModelId(tfsModel.getModelId());
        modelJobState.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobState.setToken(tfsModel.getModelId());
        modelJobState.setStartedAt(System.currentTimeMillis());
        modelJobState.create();

        String body = "{\"reason\" : \"model id not found\"}";
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/"
                + tfsModel.getModelId() +"/statistics"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body.getBytes(Charset.defaultCharset())));

        /// Starting to build the model
        String buildUrl = basePath + "clients/" + clientId + "/projects/" + projectId + "/models/" +  modelId + "/statistics";
        ResponseEntity<Error> buildEntity = super.restTemplate
                .getForEntity(buildUrl, Error.class);
        mockServer.verify();
        assertEquals(HttpStatus.BAD_REQUEST, buildEntity.getStatusCode());
        tfsModel.delete();
        modelJobState.delete();

    }

    @Test
    public void testDownloadStatsSuccess() throws Exception{
        Mockito.doReturn(this.projectDetail).when(this.projectManager).getProjectById(anyString());
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ModelBO tfsModel = ModelBuilderUtilTest.getDummyModel(name, cid, projectId, datasetId, userId, modelAccuracy, modelWeightedFScore);
        tfsModel.setModelId(UUID.randomUUID().toString());
        tfsModel.create();
        String modelId = tfsModel.getId().toString();

        ModelJobQueueBO modelJobState = new ModelJobQueueBO();
        modelJobState.setModelId(tfsModel.getModelId());
        modelJobState.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobState.setToken(tfsModel.getModelId());
        modelJobState.setStartedAt(System.currentTimeMillis());
        modelJobState.create();


        Path path = Paths.get("src/test/resources/model_stats.xlsx");
        byte[] data = Files.readAllBytes(path);
        HttpHeaders orionHeaders = new HttpHeaders();
        orionHeaders.setContentDispositionFormData("attachment", "model_stats.xlsx");
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/"
                + tfsModel.getModelId() +"/statistics"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(data).headers(orionHeaders));

        /// Starting to build the model
        String buildUrl = basePath + "clients/" + clientId + "/projects/" + projectId + "/models/" +  modelId + "/statistics";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = super.restTemplate.exchange(buildUrl, HttpMethod.GET, entity,
                byte[].class,"1");
        mockServer.verify();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Path tempFile = Files.createTempFile("recieved_stats", "xlsx");

        FileUtils.writeByteArrayToFile(tempFile.toFile(), response.getBody());
        assertTrue(FileUtils.contentEquals(tempFile.toFile(), path.toFile()));
        modelJobState.delete();
        tfsModel.delete();
    }

    @Test
    public void testDownloadTrainingOutputsSuccess() throws Exception{
        Mockito.doReturn(this.projectDetail).when(this.projectManager).getProjectById(anyString());
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ModelBO tfsModel = ModelBuilderUtilTest.getDummyModel(name, cid, projectId, datasetId, userId, modelAccuracy, modelWeightedFScore);
        tfsModel.setModelId(UUID.randomUUID().toString());
        tfsModel.create();
        String modelId = tfsModel.getId().toString();

        ModelJobQueueBO modelJobState = new ModelJobQueueBO();
        modelJobState.setModelId(tfsModel.getModelId());
        modelJobState.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobState.setToken(tfsModel.getModelId());
        modelJobState.setStartedAt(System.currentTimeMillis());
        modelJobState.create();


        Path path = Paths.get("src/test/resources/trainingOutputs.zip");
        byte[] data = Files.readAllBytes(path);
        HttpHeaders orionHeaders = new HttpHeaders();
        orionHeaders.setContentDispositionFormData("attachment", "trainingOutputs.zip");
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/"
                + tfsModel.getModelId() +"/training-outputs"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(data).headers(orionHeaders));

        /// Starting to build the model
        String buildUrl = basePath + "clients/" + clientId + "/projects/" + projectId + "/models/" +  modelId + "/training-outputs";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = super.restTemplate.exchange(buildUrl, HttpMethod.GET, entity,
                byte[].class,"1");
        mockServer.verify();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Path tempFile = Files.createTempFile("recieved_trainingOutputs", "zip");

        FileUtils.writeByteArrayToFile(tempFile.toFile(), response.getBody());
        assertTrue(FileUtils.contentEquals(tempFile.toFile(), path.toFile()));

        modelJobState.delete();
        tfsModel.delete();
    }

//
//    @Test
//    public void testDownloadStatsNotReady() throws Exception{
//        Mockito.doReturn(this.projectDetail).when(this.projectManager).getProjectById(anyString());
//        TFSModel tfsModel =
//                setExperimentation(clientId, projectId, datasetId, userId);
//        tfsModel.setModelId(UUID.randomUUID().toString());
//        String modelId = tfsModel.getId();
//        this.modelDao.updateModelId(modelId, tfsModel.getModelId());
//        TFSModelJobState modelJobState = new TFSModelJobState();
//        modelJobState.setModelId(modelId);
//        modelJobState.setStatus(TFSModelJobState.Status.COMPLETED);
//        modelJobState.setToken(tfsModel.getModelId());
//        modelJobState.setStartedAt(System.currentTimeMillis());
//        this.modelDao.addModelToJobQueue(modelJobState);
//
//        String body = "{\"reason\" : \"model statistics not ready yet\"}";
//        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:8080/v1/modelbuilder/"
//                + tfsModel.getModelId() +"/statistics"))
//                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
//                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body(body.getBytes(Charset.defaultCharset())));
//
//        /// Starting to build the model
//        String buildUrl = basePath + "models/" + modelId + "/statistics";
//        ResponseEntity<Error> buildEntity = super.restTemplate
//                .getForEntity(buildUrl, Error.class);
//        mockServer.verify();
//        assertEquals(HttpStatus.NOT_FOUND, buildEntity.getStatusCode());
//        assertEquals("model statistics not ready yet", buildEntity.getBody().getMessage());
//
//    }

}
