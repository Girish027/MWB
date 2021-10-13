package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.auth.AuthValidationBaseTest;
import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.TaskEvent;
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

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DatasetManagerImplTest extends AuthValidationBaseTest {

    private static String currentUserId = "UnitTest@247.ai";
    private static String itsClientId = "Dish";
    private static String itsAppId = "default";
    private ClientBO testClient;
    private MwbItsClientMapBO mwbItsClientMapBO;
    private ProjectBO testProject;
    private DatasetBO testDataset;
    private ProjectBO testProject1;
    private DatasetBO testDataset1;
    private ModelBO testModel;
    private JobBO jobDetail;
    private TaskEventBO task;

    @Inject
    @Qualifier("datasetManagerBean")
    private DatasetManager datasetManager;

    @Autowired
    @Qualifier("projectManagerBean")
    private ProjectManager projectManager;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
        String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

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
        testDataset1.setTransformationStatus(TaskEvent.Status.COMPLETED.toString());
        testDataset1.setTransformationTask(TaskEventBO.TaskType.INDEX.toString());
        testDataset1.create();

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
    }

    @Test
    public void testGetDatasetById() {
        DatasetBO datasetBO = datasetManager.getDatasetById(testDataset.getId().toString());
        assertEquals(datasetBO, testDataset);
    }

    @Test
    public void testGetDatasetByProjectIdClientIdDatasetId() {
        DatasetBO datasetBO = datasetManager.getDatasetById(testClient.getId().toString(),
                testProject1.getId().toString(), testDataset1.getId().toString());
        assertEquals(datasetBO, testDataset1);
    }

    @Test
    public void testGetDatasetByProjectIdClientIdDatasetIdWithNullJob() {
        DatasetBO datasetBO = datasetManager.getDatasetById(testClient.getId().toString(),
                testProject.getId().toString(), testDataset.getId().toString());
        testDataset.setTransformationStatus(TaskEvent.Status.NULL.toString());
        assertEquals(datasetBO, testDataset);
    }

    @Test
    public void testGetDatasetSourceMapByDatasetIds() {
        List<String> datasetIds = new ArrayList<>();
        datasetIds.add(testDataset.getId().toString());
        datasetIds.add(testDataset1.getId().toString());
        Map<Integer, String> datasetSourceMap = datasetManager.getDatasetSourceMapByDatasetIds(datasetIds);
        assertEquals(testDataset.getSource(), datasetSourceMap.get(testDataset.getId()));
        assertEquals(testDataset1.getSource(), datasetSourceMap.get(testDataset1.getId()));
    }
}

