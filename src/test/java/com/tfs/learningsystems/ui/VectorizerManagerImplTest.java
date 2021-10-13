package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.auth.AuthValidationBaseTest;
import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VectorizerManagerImplTest extends AuthValidationBaseTest {

    private ClientBO testClient;
    private ProjectBO testProject;
    private PreferencesBO testPreference;
    private VectorizerBO testVectorizer;
    private VectorizerBO testVectorizerUse;
    private PreferencesBO testPreferenceUse;

    @Autowired
    @Qualifier("vectorizerManagerBean")
    private VectorizerManager vectorizerManager;

    private String modelTechnology = "n-gram";

    private String modelTechnologyUse = "use_large";

    private String level = "model";

    private String clientLevel = "client";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
        String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        String currentUserId = "mwb_user";

        this.testClient = ModelUtils.getTestClientObject(name);
        this.testClient.create();

        testProject = ModelUtils.getTestProjectObject(testClient.getId(), currentUserId, name);
        testProject.create();

        testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        testVectorizerUse = ModelUtils.getVectorizer(modelTechnologyUse);
        testVectorizerUse.create();

        testPreferenceUse = ModelUtils.getPreference(testClient.getId(), testClient.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, testVectorizerUse.getId());
        testPreferenceUse.create();

        testPreference = ModelUtils.getPreference(testClient.getId(), testProject.getId().toString(), level, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();
    }

    @After
    public void tearDown() {
        testProject.delete();
        testClient.delete();
        testVectorizer.delete();
        testPreference.delete();
        testVectorizerUse.delete();
        testPreferenceUse.delete();
    }

    @Test (expected = Test.None.class)
    public void testGetLatestVectorizerByTechnologySuccess() {
        vectorizerManager.getLatestVectorizerByTechnology(modelTechnology);
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetLatestVectorizerByTechnologyFailureCase1() {
        testVectorizer.setIsLatest(VectorizerBO.IsLatest.ZERO);
        testVectorizer.update();
        vectorizerManager.getLatestVectorizerByTechnology(testPreference.getType());
        testVectorizer.setIsLatest(VectorizerBO.IsLatest.ONE);
        testVectorizer.update();
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetLatestVectorizerByTechnologyFailureCase2() {
        testPreference.setType("ABC");
        testPreference.update();
        vectorizerManager.getLatestVectorizerByTechnology(testPreference.getType());
        testPreference.setType(modelTechnology);
        testPreference.update();
    }

    @Test
    public void testCreateVectorizerSuccess() {
        VectorizerBO testVectorizerNgram;
        testVectorizerNgram = vectorizerManager.addVectorizer(modelTechnology, null);
        assertEquals(testVectorizerNgram.getType(), modelTechnology);
        assertEquals(testVectorizerNgram.getVersion(), null);
        testVectorizerNgram.delete();
    }

    @Test (expected = InvalidRequestException.class)
    public void getVectorizerByIdFailure() {
        vectorizerManager.getVectorizerById("0");
    }

    @Test
    public void getVectorizerByIdSuccess() {
        VectorizerBO testVectorizerOne;
        testVectorizerOne = vectorizerManager.getVectorizerById(String.valueOf(testVectorizer.getId()));
        assertEquals(testVectorizer, testVectorizerOne);
        testVectorizerOne.delete();
    }

    @Test
    public void getAllVectorizersSuccess() {
        List<VectorizerBO> testList = vectorizerManager.getAllVectorizers();
        assertTrue(testList.size() >= 2);
    }

    @Test
    public void TestUpdateVectorizerSuccess() {
        VectorizerBO testVectorizerOne;
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" + "isLatest");
        patchDocument.setValue(VectorizerBO.IsLatest.ZERO);
        patchRequest.add(patchDocument);
        testVectorizerOne = vectorizerManager.updateVectorizers(String.valueOf(testVectorizer.getId()), patchRequest);
        assertEquals("0", testVectorizerOne.getIsLatest().toString());
        testVectorizer.setIsLatest(VectorizerBO.IsLatest.ONE);
        testVectorizer.update();
    }

    @Test (expected = InvalidRequestException.class)
    public void TestUpdateVectorizerFailure() {
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/" + "isLatest");
        patchDocument.setValue(VectorizerBO.IsLatest.ZERO);
        patchRequest.add(patchDocument);
        vectorizerManager.updateVectorizers("0", patchRequest);
    }

    @Test
    public void TestgetVectorizerByClientProjectSuccessForNonexistentProject() {
        VectorizerBO testVectorizerOne;
        testVectorizer.setIsLatest(VectorizerBO.IsLatest.ONE);
        testVectorizerOne = vectorizerManager.getVectorizerByClientProject(String.valueOf(testClient.getId()), null);
        assertEquals(testVectorizerUse, testVectorizerOne);
    }

    @Test
    public void TestgetVectorizerByClientProjectSuccessForProject() {
        VectorizerBO testVectorizerOne;
        testVectorizerOne = vectorizerManager.getVectorizerByClientProject(String.valueOf(testClient.getId()), String.valueOf(testProject.getId()));
        assertEquals(testVectorizer, testVectorizerOne);
    }
}
