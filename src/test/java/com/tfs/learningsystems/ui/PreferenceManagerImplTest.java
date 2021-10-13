package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.auth.AuthValidationBaseTest;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.PreferencesBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.VectorizerBO;
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

import static org.junit.Assert.assertEquals;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PreferenceManagerImplTest extends AuthValidationBaseTest {

    private ClientBO testClient;
    private ProjectBO testProject;
    private ProjectBO testProject1;
    private PreferencesBO testPreference;
    private VectorizerBO testVectorizer;
    private PreferencesBO testPreferenceUse;

    @Autowired
    @Qualifier("preferenceManagerBean")
    private PreferenceManager preferenceManager;

    private String modelTechnology = "n-gram";

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

        testProject1 = ModelUtils.getTestProjectObject(testClient.getId(), currentUserId, name);
        testProject1.create();

        testVectorizer = ModelUtils.getVectorizer(modelTechnology);
        testVectorizer.create();

        testPreferenceUse = ModelUtils.getPreference(testClient.getId(), testClient.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreferenceUse.create();

        testPreference = ModelUtils.getPreference(testClient.getId(), testProject.getId().toString(), level, Constants.VECTORIZER_TYPE, testVectorizer.getId());
        testPreference.create();
    }

    @After
    public void tearDown() {
        testProject.delete();
        testProject1.delete();
        testClient.delete();
        testVectorizer.delete();
        testPreference.delete();
        testPreferenceUse.delete();
    }

    @Test
    public void testGetPreferenceByLevelAndAttributeSuccessModel() {
        PreferencesBO preferencesBO;
        preferencesBO = preferenceManager.getPreferenceByLevelTypeAndAttribute(testClient.getId().toString(), clientLevel, testPreference.getType(), testPreferenceUse.getAttribute().toString(), false);
        assertEquals(preferencesBO, testPreferenceUse);
    }

    @Test
    public void testGetPreferenceByLevelAndAttributeSuccessClient() {
        PreferencesBO preferencesBO;
        preferencesBO = preferenceManager.getPreferenceByLevelTypeAndAttribute(testClient.getId().toString(), level, testPreference.getType(), testPreference.getAttribute().toString(), false);
        assertEquals(preferencesBO, testPreference);
    }

    @Test
    public void testGetPreferenceByLevelAndAttributeSuccessClientIncludeDeleted() {
        PreferencesBO preferencesBO;
        testPreference.setStatus(PreferencesBO.STATUS_DISABLED);
        testPreference.update();
        preferencesBO = preferenceManager.getPreferenceByLevelTypeAndAttribute(testClient.getId().toString(), level, testPreference.getType(), testPreference.getAttribute().toString(), true);
        assertEquals(preferencesBO, testPreference);
        testPreference.setStatus(PreferencesBO.STATUS_ENABLED);
        testPreference.update();
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetPreferenceByLevelAndAttributeFailure() {
        testPreference.setStatus(PreferencesBO.STATUS_DISABLED);
        testPreference.update();
        preferenceManager.getPreferenceByLevelTypeAndAttribute(testClient.getId().toString(), level, testPreference.getType(), testPreference.getAttribute().toString(), false);
        testPreference.setStatus(PreferencesBO.STATUS_ENABLED);
        testPreference.update();
    }

    @Test
    public void testGetPreferencesSuccess() {
        List<PreferencesBO> preferenceList = new ArrayList<>();
        preferenceList.add(testPreferenceUse);
        preferenceList.add(testPreference);
        List<PreferencesBO> testList = preferenceManager.getAllPreferences(testClient.getId().toString(), false);
        assertEquals(testList, preferenceList);
    }

    @Test
    public void testGetPreferencesSuccessShowDeletedTrue() {
        List<PreferencesBO> preferenceList = new ArrayList<>();
        testPreference.setStatus(PreferencesBO.STATUS_DISABLED);
        testPreference.update();
        preferenceList.add(testPreferenceUse);
        preferenceList.add(testPreference);
        List<PreferencesBO> testList = preferenceManager.getAllPreferences(testClient.getId().toString(), true);
        assertEquals(testList, preferenceList);
        testPreference.setStatus(PreferencesBO.STATUS_ENABLED);
        testPreference.update();
    }

    @Test (expected = InvalidRequestException.class)
    public void testGetPreferencesFailure() {
        testPreference.setStatus(PreferencesBO.STATUS_DISABLED);
        testPreferenceUse.setStatus(PreferencesBO.STATUS_DISABLED);
        testPreference.update();
        testPreferenceUse.update();
        preferenceManager.getAllPreferences(testClient.getId().toString(), false);
        testPreference.setStatus(PreferencesBO.STATUS_ENABLED);
        testPreferenceUse.setStatus(PreferencesBO.STATUS_ENABLED);
        testPreference.update();
        testPreferenceUse.update();
    }

    @Test
    public void testAddPreferenceSuccessClientPriority() {
        PreferencesBO preferencesBO;
        preferencesBO = preferenceManager.addPreference(testClient.getId().toString(), Constants.VECTORIZER_TYPE, testProject1.getId().toString(), testVectorizer.getId(), level, false);
        assertEquals(preferencesBO.getLevel(), level);
        assertEquals(preferencesBO.getAttribute(), testProject1.getId().toString());
        assertEquals(preferencesBO.getType(), Constants.VECTORIZER_TYPE);
        preferencesBO.delete();
    }

    @Test
    public void testAddPreferenceSuccessModelPriority() {
        PreferencesBO preferencesBO;
        preferencesBO = preferenceManager.addPreference(testClient.getId().toString(), Constants.VECTORIZER_TYPE, testProject1.getId().toString(), testVectorizer.getId(), level, true);
        assertEquals(preferencesBO.getLevel(), level);
        assertEquals(preferencesBO.getAttribute(), testProject1.getId().toString());
        assertEquals(preferencesBO.getType(), Constants.VECTORIZER_TYPE);
        preferencesBO.delete();
    }

    @Test
    public void testAddPreferenceSuccessClient() {
        PreferencesBO preferencesBO;
        testPreferenceUse.setStatus(PreferencesBO.STATUS_DISABLED);
        testPreferenceUse.update();
        preferencesBO = preferenceManager.addPreference(testClient.getId().toString(), Constants.VECTORIZER_TYPE, testClient.getId().toString(), testVectorizer.getId(), clientLevel, true);
        assertEquals(preferencesBO.getLevel(), clientLevel);
        assertEquals(preferencesBO.getAttribute(), testClient.getId().toString());
        assertEquals(preferencesBO.getType(), Constants.VECTORIZER_TYPE);
        preferencesBO.delete();
        testPreferenceUse.setStatus(PreferencesBO.STATUS_ENABLED);
        testPreferenceUse.update();
    }

    @Test (expected = InvalidRequestException.class)
    public void testUpdatePreferenceFailure() {
        PatchRequest patchRequest = new PatchRequest();
        preferenceManager.updatePreferences(testClient.getId().toString(), "0", patchRequest);
    }

    @Test
    public void testUpdatePreferenceSuccess() {
        PreferencesBO preferencesBO;
        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument1 = new PatchDocument();
        patchDocument1.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument1.setPath("/" + "type");
        patchDocument1.setValue("testType");
        patchRequest.add(patchDocument1);
        String oldType = testPreference.getType();
        preferencesBO = preferenceManager.updatePreferences(testClient.getId().toString(), testPreference.getId().toString(), patchRequest);
        assertEquals(preferencesBO.getType(), "testType");
        testPreference.setType(oldType);
        testPreference.update();
    }
}
