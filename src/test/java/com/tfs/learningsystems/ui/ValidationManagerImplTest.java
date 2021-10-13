package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.auth.AuthValidationBaseTest;
import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.ui.model.Client;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.model.DatasetDetailsModel;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.error.AlreadyExistsException;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ValidationManagerImplTest extends AuthValidationBaseTest {

    private ClientBO testClient;
    private MwbItsClientMapBO mwbItsClientMapBO;
    private ProjectBO testProject;
    private DatasetBO testDataset;
    private ProjectBO testProject1;
    private DatasetBO testDataset1;
    private ModelBO testModel;
    private JobBO jobDetail;
    private PatchRequest patchRequest;
    private TaskEventBO task;
    private VectorizerBO testVectorizer;
    private PreferencesBO testPreference;
    private PreferencesBO testPreference1;
    private PreferencesBO testPreference2;
    private ProjectBO testProject2;
    private ProjectBO testProject3;
    private ProjectBO testProject4;
    private String modelTechnology = "n-gram";
    private String clientLevel = "client";
    private String modelLevel = "model";

    @Autowired
    @Qualifier("validationManagerBean")
    private ValidationManager validationManager;

    @Autowired
    @Qualifier("projectManagerBean")
    private ProjectManager projectManager;

    @Autowired
    @Qualifier("vectorizerManagerBean")
    private VectorizerManager vectorizerManager;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
        String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        String currentUserId = "UnitTest@247.ai";
        String itsClientId = "Dish";
        String itsAppId = "default";

        this.testClient = ModelUtils.getTestClientObject(name);
        this.testClient.create();


        mwbItsClientMapBO = ModelUtils.getTestMwbItsClientMapBO(testClient.getId(), itsClientId, itsAppId);
        mwbItsClientMapBO.create();

        testProject = ModelUtils.getTestProjectObject(testClient.getId(), currentUserId, name);
        testProject.create();

        testDataset = ModelUtils.getTestDatasetObject(testClient.getId(), testProject.getId(), name);
        testDataset.create();

        name = name + "1";

        testProject1 = ModelUtils.getTestProjectObject(testClient.getId(), currentUserId, name);
        testProject1.create();

        testDataset1 = ModelUtils.getTestDatasetObject(testClient.getId(), testProject1.getId(), name);
        testDataset1.create();

        testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        testProject2 = ModelUtils.getTestProjectObject(testClient.getId(), currentUserId, name);
        testProject2.create();

        testProject3 = ModelUtils.getTestProjectObject(testClient.getId(), currentUserId, name);
        testProject3.create();

        testProject4 = ModelUtils.getTestProjectObject(testClient.getId(), currentUserId, name);
        testProject4.create();

        testPreference = ModelUtils.getPreference(testClient.getId(), testClient.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();

        testPreference1 = ModelUtils.getPreference(testClient.getId(), testProject2.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference1.create();

        testPreference2 = ModelUtils.getPreference(testClient.getId(), testProject3.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference2.create();
        jobDetail = new JobBO();
        jobDetail.setProjectId(testProject1.getId());
        jobDetail.setDatasetId(testDataset1.getId());
        jobDetail.create();

        task = new TaskEventBO();
        task.setJobId(jobDetail.getId());
        task.setStatus(TaskEventBO.Status.COMPLETED);
        task.setTask(TaskEventBO.TaskType.INDEX);
        task.create();

        projectManager.addDatasetProjectMapping(testClient.getId().toString(),
                testProject1.getId().toString(), testDataset1.getId().toString(), "");

        testModel = ModelUtils.getModelObject(testProject.getId());
        testModel.setModelType(Constants.DIGITAL_SPEECH_MODEL);
        testModel.create();
        testModel.setModelId(testModel.getId().toString());
        testModel.update();

        patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.DEPLOYABLE_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
    }

    @After
    public void tearDown() {
        testProject.delete();
        testModel.delete();
        mwbItsClientMapBO.delete();
        testClient.delete();
        testDataset.delete();
        projectManager.removeDatasetProjectMapping(testClient.getId().toString(),
                testDataset1.getId().toString(),
                testProject1.getId().toString(),"");
        task.delete();
        jobDetail.delete();
        testDataset1.delete();
        testProject1.delete();
        testVectorizer.delete();
        testPreference.delete();
        testPreference1.delete();
        testPreference2.delete();
        testProject2.delete();
        testProject3.delete();
        testProject4.delete();
    }

    @Test
    public void testValidateProjectId() {
        ProjectBO projectBO = validationManager.validateProjectId(testProject.getId().toString());
        assertEquals(projectBO, testProject);
    }

    @Test (expected = NotFoundException.class)
    public void testValidateProjectIdNotFound() {
        validationManager.validateProjectId(testProject.getId().toString() + 1);
    }

    @Test
    public void testValidateClient() {
        ClientBO clientBO = validationManager.validateClient(testClient.getId().toString());
        assertEquals(clientBO, testClient);
    }

    @Test (expected = BadRequestException.class)
    public void testValidateGlobalProjectNameBadRequestException() {
        validationManager.validateGlobalProjectName(ProjectBO.Type.GLOBAL.toString(), testProject.getName());
    }

    @Test (expected = BadRequestException.class)
    public void testValidateNodeProjectNameBadRequestException() {
        validationManager.validateGlobalProjectName(ProjectBO.Type.NODE.toString(), "Root_Intent");
    }

    @Test (expected = Test.None.class)
    public void testValidateNodeProjectNameNoException() {
        validationManager.validateGlobalProjectName(ProjectBO.Type.NODE.toString(), testProject.getName());
    }

    @Test (expected = Test.None.class)
    public void testValidateGlobalProjectNameNoException() {
        validationManager.validateGlobalProjectName(ProjectBO.Type.GLOBAL.toString(), "Root_Intent");
    }

    @Test
    public void testValidateClientAndDataset() {
        DatasetBO datasetBO = validationManager.validateClientAndDataset(testClient.getId().toString(), testDataset.getId().toString());
        assertEquals(datasetBO, testDataset);
    }

    @Test (expected = NotFoundException.class)
    public void testValidateClientAndDatasetNotFound() {
        validationManager.validateClientAndDataset(testClient.getId().toString() + 1 , testDataset.getId().toString() + 1 );
    }

    @Test
    public void testValidateProjectDatasetEntry() {
        Boolean projectDataset = validationManager.validateProjectDatasetEntry(testProject1.getId().toString() , testDataset1.getId().toString(), false);
        assertEquals(true, projectDataset);
    }

    @Test (expected = ForbiddenException.class)
    public void testValidateProjectDatasetEntryWithExternalUser() {
        testDataset1.setSource(DatasetBO.Source.A);
        testDataset1.update();
        validationManager.validateProjectDatasetEntry(testProject1.getId().toString() , testDataset1.getId().toString(), false);
        testDataset1.setSource(DatasetBO.Source.E);
        testDataset1.update();
    }

    @Test (expected = BadRequestException.class)
    public void testValidateProjectTransformedStatusBadRequest() {
        validationManager.validateProjectTransformedStatus(testProject.getId().toString());
    }

    @Test
    public void testValidateProjectTransformedStatus() {
        List<DatasetBO> datasetBOList = validationManager.validateProjectTransformedStatus(testProject1.getId().toString());
        assertEquals(1, datasetBOList.size());
        assertNotNull(datasetBOList.get(0));
    }

    @Test (expected = NotFoundException.class)
    public void testValidateProjectDatasetEntryNotFoundCase1() {
        Boolean projectDataset = validationManager.validateProjectDatasetEntry(testProject.getId().toString() , testDataset.getId().toString(), false);
        assertEquals(false, projectDataset);
    }

    @Test (expected = NotFoundException.class)
    public void testValidateProjectDatasetEntryNotFound() {
        validationManager.validateProjectDatasetEntry(testProject.getId().toString() + 1 , testDataset.getId().toString() + 1, false);
    }

    @Test (expected = NotFoundException.class)
    public void testValidateClientProjectDataSetNotFound() {
        validationManager.validateClientProjectDataSet(testClient.getId().toString(), testProject.getId().toString() + 1 ,
                testDataset.getId().toString() + 1 );
    }

    @Test
    public void testValidateClientProjectDataSet() {
        DatasetDetailsModel datasetDetailsModel = validationManager.validateClientProjectDataSet(testClient.getId().toString(),
                testProject1.getId().toString(), testDataset1.getId().toString());
        assertNotNull(datasetDetailsModel);
    }

    @Test (expected = Test.None.class)
    public void testIfProjectNameChangePatchRequest() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setPath("/" + Constants.PROJECT_NAME);
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setValue("root_intent");
        patchRequest.add(patchDocument);
        validationManager.ifProjectNameChangePatchRequest(testClient.getId().toString(),
                ProjectBO.Type.GLOBAL.toString(), patchRequest);
    }

    @Test (expected = Test.None.class)
    public void testValidatePatchCallWithValidProjectField() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setPath("/" + Constants.PROJECT_NAME);
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setValue("ABC");
        patchRequest.add(patchDocument);
        validationManager.validatePatchCall(patchRequest, Constants.PROJECT_FIELDS);
    }

    @Test (expected = ForbiddenException.class)
    public void testValidatePatchCallWithInValidProjectField() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setPath("/" + Constants.DEPLOY2_MODULE_TYPE);
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setValue(ProjectBO.Type.NODE.toString());
        patchRequest.add(patchDocument);
        validationManager.validatePatchCall(patchRequest, Constants.PROJECT_FIELDS);
    }

    @Test (expected = Test.None.class)
    public void testValidatePatchCallWithValidDatasetField() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setPath("/" + Constants.DESCRIPTION);
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setValue("ABC");
        patchRequest.add(patchDocument);
        validationManager.validatePatchCall(patchRequest, Constants.DATASET_FIELDS);
    }

    @Test (expected = ForbiddenException.class)
    public void testValidatePatchCallWithInValidDatasetField() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setPath("/source");
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setValue(DatasetBO.Source.E.toString());
        patchRequest.add(patchDocument);
        validationManager.validatePatchCall(patchRequest, Constants.DATASET_FIELDS);
    }

    @Test
    public void testIfValidClientProjectDatasetEntry() {
        Boolean projectDatasetEntry = validationManager.ifValidClientProjectDatasetEntry(testProject.getId().toString() , testDataset.getId().toString());
        assertEquals(false, projectDatasetEntry);
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateDeployableModelWithInvalidProjectId() {
        validationManager.validateDeployableModel(testClient.getId().toString(), testProject.getId().toString() + 1,
                "123");
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateDeployableModelWithInvalidClientId() {
        validationManager.validateDeployableModel(testClient.getId().toString() + 1, testProject.getId().toString(),
                "123");
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateDeployableModelWithDeployedModelId() {
        testProject.setDeployableModelId(testModel.getId());
        testProject.update();
        validationManager.validateDeployableModel(testClient.getId().toString(), testProject.getId().toString(),
                testModel.getId().toString());
    }

    @Test (expected = Test.None.class)
    public void testValidateDeployableModelWithNonDeployedModelId() {
        testProject.setDeployableModelId(testModel.getId());
        testProject.update();
        validationManager.validateDeployableModel(testClient.getId().toString(), testProject.getId().toString(),
                testModel.getId().toString() + 1);
    }

    @Test
    public void testValidateClientAndProjectValid() {
        ProjectBO projectBO = validationManager.validateClientAndProject(testClient.getId().toString(), testProject.getId().toString());
        assertEquals(projectBO, testProject);
    }

    @Test (expected = NotFoundException.class)
    public void testValidateClientAndProjectNotFound() {
        validationManager.validateClientAndProject(testClient.getId().toString(), testProject.getId().toString() + 1);
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateClientAndProjectInvalidRequestException() {
        validationManager.validateClientAndProject(testClient.getId().toString() + 1, testProject.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateClientInvalidRequest() {
        validationManager.validateClient(testClient.getId().toString() + 1);
    }

    @Test
    public void testValidateUserWithExternalSourceAndAdminGroup() {
        this.setClientAdminInUserGroup();
        Boolean flag = validationManager.validateUser(DatasetBO.Source.E.toString());
        assertEquals(true, flag);
    }

    @Test
    public void testValidateUserWithInternalSourceAndAdminGroup() {
        this.setClientAdminInUserGroup();
        Boolean flag = validationManager.validateUser(DatasetBO.Source.I.toString());
        assertEquals(true, flag);
    }

    @Test (expected = ForbiddenException.class)
    public void testValidateUserWithAgentSourceAndAdminGroup() {
        this.setClientAdminInUserGroup();
        validationManager.validateUser(DatasetBO.Source.A.toString());
    }

    @Test
    public void testValidateUserWithExternalSourceAndExternalUser() {
        this.setUserIdOnDetailsMap("dummyUserId");
        this.setDetailsMap("user@test.ai", "EXTERNAL", "user@test-inc.com");
        Boolean flag = validationManager.validateUser(DatasetBO.Source.E.toString());
        assertEquals(true, flag);
    }

    @Test (expected = ForbiddenException.class)
    public void testValidateUserWithInternalSourceAndExternalUser() {
        this.setUserIdOnDetailsMap("dummyUserId");
        this.setDetailsMap("user@test.ai", "EXTERNAL", "user@test-inc.com");
        Boolean flag = validationManager.validateUser(DatasetBO.Source.I.toString());
        assertEquals(true, flag);
    }

    @Test (expected = ForbiddenException.class)
    public void testValidateUserWithAgentSourceAndExternalUser() {
        this.setUserIdOnDetailsMap("dummyUserId");
        this.setDetailsMap("user@test.ai", "EXTERNAL", "user@test-inc.com");
        Boolean flag = validationManager.validateUser(DatasetBO.Source.A.toString());
        assertEquals(true, flag);
    }

    @Test (expected = ForbiddenException.class)
    public void testValidateUserWithExternalSourceAndInternalUser() {
        this.setUserIdOnDetailsMap("dummyUserId");
        this.setDetailsMap("user@247.ai", "INTERNAL","user@247-inc.com");
        validationManager.validateUser(DatasetBO.Source.E.toString());
    }

    @Test
    public void testValidateUserWithInternalSourceAndInternalUser() {
        this.setUserIdOnDetailsMap("dummyUserId");
        this.setDetailsMap("user@247.ai", "INTERNAL","user@247-inc.com");
        Boolean flag = validationManager.validateUser(DatasetBO.Source.I.toString());
        assertEquals(true, flag);
    }

    @Test
    public void testValidateUserWithAgentSourceAndInternalUser() {
        this.setUserIdOnDetailsMap("dummyUserId");
        this.setDetailsMap("user@247.ai", "INTERNAL","user@247-inc.com");
        Boolean flag = validationManager.validateUser(DatasetBO.Source.A.toString());
        assertEquals(true, flag);
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateProjectIdForModelWithDigitalSpeechModel() {
        validationManager.validateProjectIdForModel(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateProjectIdForModelWithInValidProjectId() {
        testModel.setProjectId(-1);
        testModel.update();
        validationManager.validateProjectIdForModel(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateProjectIdForModelWithInValidModelId() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" + Constants.DEPLOYABLE_MODEL_ID);
        patchDocument.setValue(testModel.getId() + 1);
        patchRequest.add(patchDocument);

        validationManager.validateProjectIdForModel(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = Test.None.class)
    public void testValidateProjectIdForModelWithNullModelId() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" + Constants.DEPLOYABLE_MODEL_ID);
        patchDocument.setValue(null);
        patchRequest.add(patchDocument);

        validationManager.validateProjectIdForModel(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = ForbiddenException.class)
    public void testValidateProjectIdForModel() {
        testModel.setModelType(Constants.DIGITAL_MODEL);
        testModel.update();
        validationManager.validateProjectIdForModel(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test
    public void testValidateRolesByClientIdAdminGroup() {
        this.setClientAdminInUserGroup();
        Boolean flag = validationManager.validateRolesByClientId(testClient.getId().toString());
        assertEquals(true, flag);
    }

    @Test
    public void testValidateRolesByClientIdOperatorRole() {
        List<String> roles = new ArrayList<>();
        roles.add(Constants.OPERATOR_ROLE);
        this.setMockClientDetails(mwbItsClientMapBO.getItsClientId(), mwbItsClientMapBO.getItsAppId(), roles);
        this.setUserIdOnDetailsMap("dummyUserId");
        Boolean flag = validationManager.validateRolesByClientId(mwbItsClientMapBO.getId().toString());
        assertEquals(true, flag);
    }

    @Test
    public void testValidateRolesByClientIdDeveloperRole() {
        List<String> roles = new ArrayList<>();
        roles.add(Constants.DEVELOPER_ROLE);
        this.setMockClientDetails(mwbItsClientMapBO.getItsClientId(), mwbItsClientMapBO.getItsAppId(), roles);
        this.setUserIdOnDetailsMap("dummyUserId");
        Boolean flag = validationManager.validateRolesByClientId(mwbItsClientMapBO.getId().toString());
        assertEquals(false, flag);
    }

    @Test
    public void testValidateProjectAndStart(){
        ProjectBO projectBO = validationManager.validateProjectAndStart(testProject.getId().toString());
        assertNotNull(projectBO);
    }

    @Test (expected = NotFoundException.class)
    public void testValidateProjectAndStartNotFound() {
        validationManager.validateProjectAndStart(testProject.getId().toString() + 1 );
    }

    @Test
    public void testValidateModelCreate() {
        ModelBO modelBO = validationManager.validateModelCreate(testClient.getId().toString(), testProject.getId().toString(), testModel);
        assertEquals(modelBO, testModel);
    }

    @Test (expected = BadRequestException.class)
    public void testValidateModelCreateWithModelNull() {
        validationManager.validateModelCreate(testClient.getId().toString(), testProject.getId().toString(), null);
    }

    @Test (expected = BadRequestException.class)
    public void testValidateModelCreateWithCidNull() {
        ModelBO testModel = ModelUtils.getModelObject(testProject.getId());
        testModel.create();
        testModel.setCid(null);
        testModel.update();
        validationManager.validateModelCreate(testClient.getId().toString(), testProject.getId().toString(), testModel);
        testModel.delete();
    }

    @Test
    public void testValidateClientCreate() {
        String itsAppId = "GeneralApp";
        String itsAccountId = "caponeAccount";
        boolean isVertical = false;
        Client client = ModelUtils.getTestClient("TestClient", testClient.getId().toString(), itsAppId, itsAccountId, isVertical );
        Boolean validateClientCreate = validationManager.validateClientCreate(client);
        assertEquals(true, validateClientCreate);
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidatePreviewAndLiveModelWithEmptyProject() {
        validationManager.validatePreviewAndLiveModel(testClient.getId().toString(), testProject.getId().toString() + 1, testModel.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidatePreviewModelDeleteCheck() {
        testProject.setPreviewModelId(testModel.getId().toString());
        testProject.update();
        validationManager.validatePreviewAndLiveModel(testClient.getId().toString(), testProject.getId().toString(), testModel.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateLiveModelDeleteCheck() {
        testProject.setLiveModelId(testModel.getId().toString());
        testProject.update();
        validationManager.validatePreviewAndLiveModel(testClient.getId().toString(), testProject.getId().toString(), testModel.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateDeployableModelDeleteCheck() {
        testProject.setDeployableModelId(testModel.getId());
        testProject.update();
        validationManager.validateDeployableModel(testClient.getId().toString(), testProject.getId().toString(), testModel.getId().toString());
    }

    @Test (expected = Test.None.class)
    public void testValidateDeployableModelNotExist() {
        testProject.setDeployableModelId(testModel.getId() + 1);
        testProject.update();
        validationManager.validateDeployableModel(testClient.getId().toString(), testProject.getId().toString(), testModel.getId().toString());
    }

    @Test (expected = Test.None.class)
    public void testPreviewModelId() {
        this.setClientAdminInUserGroup();
        ModelBO testModel = ModelUtils.getModelObject(testProject.getId());
        testModel.setModelType(Constants.DIGITAL_MODEL);
        testModel.create();
        testModel.setModelId(testModel.getId().toString());
        testModel.update();
        ModelJobQueueBO modelJobQueueBO = new ModelJobQueueBO();
        modelJobQueueBO.setModelId(testModel.getId().toString());
        modelJobQueueBO.setToken(testModel.getId().toString());
        modelJobQueueBO.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobQueueBO.setStartedAt(System.currentTimeMillis());
        modelJobQueueBO.setModelType(testModel.getModelType());
        modelJobQueueBO.create();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.PREVIEW_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
        testModel.delete();
    }

    @Test (expected = Test.None.class)
    public void testLiveModelId() {
        this.setClientAdminInUserGroup();
        ModelBO testModel = ModelUtils.getModelObject(testProject.getId());
        testModel.setModelType(Constants.DIGITAL_MODEL);
        testModel.create();
        testModel.setModelId(testModel.getId().toString());
        testModel.update();
        ModelJobQueueBO modelJobQueueBO = new ModelJobQueueBO();
        modelJobQueueBO.setModelId(testModel.getId().toString());
        modelJobQueueBO.setToken(testModel.getId().toString());
        modelJobQueueBO.setStatus(ModelJobQueueBO.Status.COMPLETED);
        modelJobQueueBO.setStartedAt(System.currentTimeMillis());
        modelJobQueueBO.setModelType(testModel.getModelType());
        modelJobQueueBO.create();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.LIVE_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
        Boolean flag = validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
        assertEquals(true, flag);
        testModel.delete();
    }

    @Test (expected = Test.None.class)
    public void testLiveModelIdNullCase() {
        this.setClientAdminInUserGroup();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.LIVE_MODEL_ID);
        patchDocument.setValue(null);
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = Test.None.class)
    public void testPreviewModelIdNullCase() {
        this.setClientAdminInUserGroup();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.PREVIEW_MODEL_ID);
        patchDocument.setValue(null);
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testInValidLiveModelId() {
        this.setClientAdminInUserGroup();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.LIVE_MODEL_ID);
        patchDocument.setValue(testModel.getId()+1);
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testModelFailedStatusLiveValidation() {
        this.setClientAdminInUserGroup();
        ModelBO testModel = ModelUtils.getModelObject(testProject.getId());
        testModel.setModelType(Constants.DIGITAL_MODEL);
        testModel.create();
        testModel.setModelId(testModel.getId().toString());
        testModel.update();
        ModelJobQueueBO modelJobQueueBO = new ModelJobQueueBO();
        modelJobQueueBO.setModelId(testModel.getId().toString());
        modelJobQueueBO.setToken(testModel.getId().toString());
        modelJobQueueBO.setStatus(ModelJobQueueBO.Status.FAILED);
        modelJobQueueBO.setStartedAt(System.currentTimeMillis());
        modelJobQueueBO.setModelType(testModel.getModelType());
        modelJobQueueBO.create();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.LIVE_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
        testModel.delete();
        modelJobQueueBO.delete();
    }

    @Test (expected = InvalidRequestException.class)
    public void testModelFailedStatusPreviewValidation() {
        this.setClientAdminInUserGroup();
        ModelBO testModel = ModelUtils.getModelObject(testProject.getId());
        testModel.setModelType(Constants.DIGITAL_MODEL);
        testModel.create();
        testModel.setModelId(testModel.getId().toString());
        testModel.update();
        ModelJobQueueBO modelJobQueueBO = new ModelJobQueueBO();
        modelJobQueueBO.setModelId(testModel.getId().toString());
        modelJobQueueBO.setToken(testModel.getId().toString());
        modelJobQueueBO.setStatus(ModelJobQueueBO.Status.FAILED);
        modelJobQueueBO.setStartedAt(System.currentTimeMillis());
        modelJobQueueBO.setModelType(testModel.getModelType());
        modelJobQueueBO.create();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.PREVIEW_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
        testModel.delete();
        modelJobQueueBO.delete();
    }

    @Test (expected = InvalidRequestException.class)
    public void testNonDigitalLiveModelIdCheck() {
        this.setClientAdminInUserGroup();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.LIVE_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testNonDigitalPreviewModelIdCheck() {
        this.setClientAdminInUserGroup();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.PREVIEW_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testInValidPreviewModelId() {
        this.setClientAdminInUserGroup();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.PREVIEW_MODEL_ID);
        patchDocument.setValue(testModel.getId()+1);
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidLiveModelIdInValidProjectId() {
        this.setClientAdminInUserGroup();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.LIVE_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString() + 1);
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidPreviewModelIdInValidProjectId() {
        this.setClientAdminInUserGroup();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.PREVIEW_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString() + 1);
    }

    @Test (expected = InvalidRequestException.class)
    public void testLiveModelIdEmptyCheck() {
        this.setClientAdminInUserGroup();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.LIVE_MODEL_ID);
        patchDocument.setValue("");
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testPreviewModelIdEmptyCheck() {
        this.setClientAdminInUserGroup();
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.PREVIEW_MODEL_ID);
        patchDocument.setValue("");
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = ForbiddenException.class)
    public void testInvalidUserAccessToPreviewModelCheck() {
        this.setUserIdOnDetailsMap("MWB_User");
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.PREVIEW_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = ForbiddenException.class)
    public void testInvalidUserAccessToLiveModelCheck() {
        this.setUserIdOnDetailsMap("MWB_User");
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" +Constants.LIVE_MODEL_ID);
        patchDocument.setValue(testModel.getId());
        patchRequest.add(patchDocument);
        validationManager.validateLiveAndPreviewModelId(testClient.getId().toString(), patchRequest, testProject.getId().toString());
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateDatasetIdsEmptyFailure() {
        List<String> datasetsIds = new ArrayList<>();
        validationManager.validateDatasetIds(datasetsIds);
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateDatasetIdsNullFailure() {
        validationManager.validateDatasetIds(null);
    }

    @Test (expected = Test.None.class)
    public void testValidateDatasetIdsSuccess() {
        List<String> datasetsIds = new ArrayList<>();
        datasetsIds.add("123");
        validationManager.validateDatasetIds(datasetsIds);
    }

    @Test
    public void testValidateProjectCreate() {
        ProjectBO projectBO = validationManager.validateProjectCreate(testClient.getId().toString(), testProject);
        assertEquals(testProject, projectBO);
    }

    @Test (expected = BadRequestException.class)
    public void testValidateProjectCreateWithNullProject() {
        validationManager.validateProjectCreate(testClient.getId().toString(), null);
    }

    @Test (expected = BadRequestException.class)
    public void testValidateProjectCreateWithInvalidClientId() {
        String clsName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
        ClientBO testClient = ModelUtils.getTestClientObject(name);
        testClient.create();
        validationManager.validateProjectCreate(testClient.getId().toString(), testProject);
        testClient.delete();
    }

    @Test
    public void testValidateAndGetModelById() {
        ModelBO modelBO = validationManager.validateAndGetModel(testClient.getId().toString(),
                testProject.getId().toString(), testModel.getId().toString(), "id");
        assertEquals(testModel, modelBO);
    }

    @Test (expected = NotFoundException.class)
    public void testValidateAndGetModelByIdFailure() {
        validationManager.validateAndGetModel(testClient.getId().toString(),
                testProject.getId().toString() + 1, testModel.getId().toString(), "id");
    }

    @Test
    public void testValidateAndGetModelByModelId() {
        ModelBO modelBO =  validationManager.validateAndGetModel(testClient.getId().toString(),
                testProject.getId().toString(), testModel.getId().toString(), "modelId");
        assertEquals(testModel, modelBO);
    }

    @Test (expected = NotFoundException.class)
    public void testValidateAndGetModelByModelIdFailure() {
        validationManager.validateAndGetModel(testClient.getId().toString(),
                testProject.getId().toString(), testModel.getId().toString() + 1, "modelId");
    }

    @Test (expected = AlreadyExistsException.class)
    public void testValidateProjectNameExists() {
        validationManager.validateProjectNameExists(testClient.getId().toString(), testProject.getName());
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidateVectorizerFailure() {
        validationManager.validateVectorizer("0");
    }

    @Test
    public void getVectorizerByIdSuccess() {
        VectorizerBO testVectorizerOne;
        testVectorizerOne = validationManager.validateVectorizer(String.valueOf(testVectorizer.getId()));
        assertEquals(testVectorizer, testVectorizerOne);
        testVectorizerOne.delete();
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidatePreferenceInvalidId() {
        validationManager.validatePreference(testClient.getId().toString(), "0");
    }

    @Test (expected = InvalidRequestException.class)
    public void testValidatePreferenceInvalidClient() {
        validationManager.validatePreference("0", testPreference.getId().toString());
    }

    @Test
    public void testValidatePreference() {
        PreferencesBO testPreferenceOne;
        testPreferenceOne = validationManager.validatePreference(testClient.getId().toString(), String.valueOf(testPreference.getId()));
        assertEquals(testPreference, testPreferenceOne);
        testPreferenceOne.delete();
    }

    @Test (expected = AlreadyExistsException.class)
    public void testValidateLevelTypeAndAttributeModelFailure() {
        validationManager.validateLevelTypeAndAttribute(modelLevel, testProject2.getId().toString(), Constants.VECTORIZER_TYPE, testClient.getId().toString());
    }

    @Test (expected = AlreadyExistsException.class)
    public void testValidateLevelTypeAndAttributeClientFailure() {
        validationManager.validateLevelTypeAndAttribute(clientLevel, testClient.getId().toString(), Constants.VECTORIZER_TYPE, testClient.getId().toString());
    }

    @Test
    public void testValidateLevelTypeAndAttributeClientSuccess() {
        testPreference.setStatus(PreferencesBO.STATUS_DISABLED);
        testPreference.update();
        validationManager.validateLevelTypeAndAttribute(clientLevel, testClient.getId().toString(), Constants.VECTORIZER_TYPE, testClient.getId().toString());
        testPreference.setStatus(PreferencesBO.STATUS_ENABLED);
        testPreference.update();
    }

    @Test
    public void testValidateLevelTypeAndAttributeProjectSuccess() {
        testPreference1.setStatus(PreferencesBO.STATUS_DISABLED);
        testPreference1.update();
        validationManager.validateLevelTypeAndAttribute(modelLevel, testProject2.getId().toString(), Constants.VECTORIZER_TYPE, testClient.getId().toString());
        testPreference1.setStatus(PreferencesBO.STATUS_ENABLED);
        testPreference1.update();
    }

    @Test
    public void testValidateLevelModelSuccess() {
        Boolean result = validationManager.validateLevel(modelLevel, true);
        assertEquals(result, true);
    }

    @Test
    public void testValidateLevelModelFailure() {
        Boolean result = validationManager.validateLevel(modelLevel, false);
        assertEquals(result, false);
    }

    @Test
    public void testValidateLevelClientSuccess() {
        Boolean result = validationManager.validateLevel(clientLevel, true);
        assertEquals(result, true);
    }

    @Test (expected = ForbiddenException.class)
    public void TestPreferencePatchRequestForbiddenFailure() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" + "level");
        patchDocument.setValue(modelLevel);
        patchRequest.add(patchDocument);
        validationManager.PreferencePatchRequest(testClient.getId().toString(), testPreference, patchRequest );
    }

    @Test (expected = InvalidRequestException.class)
    public void TestPreferencePatchRequestVectorizerFailure() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" + "value");
        patchDocument.setValue("0");
        patchRequest.add(patchDocument);
        validationManager.PreferencePatchRequest(testClient.getId().toString(), testPreference, patchRequest );
    }

    @Test (expected = ForbiddenException.class)
    public void TestPreferencePatchRequestFailure() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" + "level");
        patchDocument.setValue(modelLevel);
        patchRequest.add(patchDocument);
        PatchDocument patchDocument1 = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" + "attribute");
        patchDocument.setValue(testPreference2.getAttribute());
        validationManager.PreferencePatchRequest(testClient.getId().toString(), testPreference1, patchRequest );
    }

    @Test (expected = ForbiddenException.class)
    public void TestPreferencePatchRequestClientAttributeFailure() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" + "level");
        patchDocument.setValue(modelLevel);
        patchRequest.add(patchDocument);
        PatchDocument patchDocument1 = new PatchDocument();
        patchDocument1.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument1.setPath("/" + "attribute");
        patchDocument1.setValue(testPreference2.getAttribute());
        patchRequest.add(patchDocument1);
        validationManager.PreferencePatchRequest(testClient.getId().toString(), testPreference1, patchRequest );
    }

    @Test (expected = ForbiddenException.class)
    public void TestPreferencePatchRequestClientMatchFailure() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" + "level");
        patchDocument.setValue(clientLevel);
        patchRequest.add(patchDocument);
        PatchDocument patchDocument1 = new PatchDocument();
        patchDocument1.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument1.setPath("/" + "attribute");
        patchDocument1.setValue("0");
        patchRequest.add(patchDocument1);
        PatchDocument patchDocument2 = new PatchDocument();
        patchDocument2.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument2.setPath("/" + "client_id");
        patchDocument2.setValue(testClient.getId());
        patchRequest.add(patchDocument2);
        validationManager.PreferencePatchRequest(testClient.getId().toString(), testPreference, patchRequest );
    }

    @Test (expected = ForbiddenException.class)
    public void TestPreferencePatchRequestModelFailure() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument1 = new PatchDocument();
        patchDocument1.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument1.setPath("/" + "attribute");
        patchDocument1.setValue("0");
        patchRequest.add(patchDocument1);
        validationManager.PreferencePatchRequest(testClient.getId().toString(), testPreference1, patchRequest );
    }

    @Test
    public void TestPreferencePatchRequestSuccess() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument1 = new PatchDocument();
        patchDocument1.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument1.setPath("/" + "type");
        patchDocument1.setValue("testType");
        patchRequest.add(patchDocument1);
        validationManager.PreferencePatchRequest(testClient.getId().toString(), testPreference1, patchRequest );
    }

    @Test (expected = InvalidRequestException.class)
    public void TestPreferencePatchRequestVectorizerCheck() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument1 = new PatchDocument();
        patchDocument1.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument1.setPath("/" + "value");
        patchDocument1.setValue(0);
        patchRequest.add(patchDocument1);
        validationManager.PreferencePatchRequest(testClient.getId().toString(), testPreference2, patchRequest );
    }

    @Test
    public void TestValidateProjectandModelID() {
        ModelDeploymentDetailsBO modelDeploymentDetailsBO = validationManager.validateProjectAndModelId(testProject.getId().toString());
    }
}
