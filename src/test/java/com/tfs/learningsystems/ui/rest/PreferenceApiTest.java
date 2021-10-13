package com.tfs.learningsystems.ui.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.PreferencesBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.VectorizerBO;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.ConfigManager;
import com.tfs.learningsystems.ui.FileManager;
import com.tfs.learningsystems.ui.ModelManager;
import com.tfs.learningsystems.ui.ValidationManager;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.nlmodel.model.dao.ModelDao;
import com.tfs.learningsystems.util.CommonLib;
import com.tfs.learningsystems.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class PreferenceApiTest {
    @Inject
    @Qualifier("modelManagerBean")
    private ModelManager modelManager;

    @Inject
    @Qualifier("validationManagerBean")
    private ValidationManager validationManager;

    @Inject
    @Qualifier("fileManagerBean")
    private FileManager fileManager;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ModelDao modelDao;

    private String basePath;

    // private MockRestServiceServer mockServer;

    public String clientId;
    public String projectId;
    public String datasetId;
    public String userId = "UnitTest@247.ai";
    public String cid;
    private String modelTechnology = "use";
    private String modelLevel = "model";
    private String clientLevel = "client";

    @Autowired
    public void setBasePath(AppConfig appConfig) {
        this.basePath = appConfig.getRestUrlPrefix() + "v1/";
    }

    @Before
    public void setUp() throws ScriptException {
        CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());
    }

    @Test
    public void testCreatePreference() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();

        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        VectorizerBO vectorizer = ModelUtils.getVectorizer(modelTechnology);
        vectorizer.create();
        PreferencesBO preference = ModelUtils.getPreference(client.getId(), client.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, vectorizer.getId());

        long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;


        ResponseEntity<PreferencesBO> entity = this.restTemplate
                .postForEntity(basePath + "preference?clientId="+ client.getId().toString() + "&setDefault=true",
                        preference, PreferencesBO.class);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        PreferencesBO addedPreference = entity.getBody();
        assertEquals(preference.getLevel(), addedPreference.getLevel());
        assertTrue(timeStamp < addedPreference.getCreatedAt());
        addedPreference.delete();
        vectorizer.delete();
        proj.delete();
        client.delete();
    }

    @Test
    public void testGetPreferenceByTypeLevelAndAttribute() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + System.currentTimeMillis() % 10000000;

        ClientBO client = ModelUtils.getTestClientObject(name);
        client.create();

        ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), userId, name);
        project.create();

        VectorizerBO vectorizer = ModelUtils.getVectorizer(modelTechnology);
        vectorizer.create();

        PreferencesBO preference = ModelUtils.getPreference(client.getId(), client.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, vectorizer.getId());
        preference.create();
        ResponseEntity<PreferencesBO> entity =
                this.restTemplate
                        .getForEntity(basePath +
                                        "preference/clients/"+ client.getId().toString()+ "?level="+ preference.getLevel() + "&attribute=" + preference.getAttribute() + "&type=" + Constants.VECTORIZER_TYPE,
                                PreferencesBO.class);
        PreferencesBO returnedPreference = entity.getBody();
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(preference.getId(), returnedPreference.getId());
        assertEquals(preference.getType(), returnedPreference.getType());
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        returnedPreference.delete();
        vectorizer.delete();
        project.delete();
        client.delete();
    }

    @Test
    public void testGetAllPreferences() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + System.currentTimeMillis() % 10000000;

        ClientBO client = ModelUtils.getTestClientObject(name);
        client.create();

        ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), userId, name);
        project.create();

        VectorizerBO vectorizer = ModelUtils.getVectorizer(modelTechnology);
        vectorizer.create();

        PreferencesBO preference1 = ModelUtils.getPreference(client.getId(), client.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, vectorizer.getId());
        preference1.create();

        PreferencesBO preference2 = ModelUtils.getPreference(client.getId(), project.getId().toString(), modelLevel, Constants.VECTORIZER_TYPE, vectorizer.getId());
        preference2.create();

        ResponseEntity<String> entity =
                this.restTemplate
                        .getForEntity(basePath + "preference?clientId=" + client.getId().toString() + "&includeDeleted=false",
                                String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        preference1.delete();
        preference2.delete();
        vectorizer.delete();
        project.delete();
        client.delete();
    }

    @Test
    public void testUpdatePreferences() throws JsonProcessingException {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + System.currentTimeMillis() % 10000000;

        ClientBO client = ModelUtils.getTestClientObject(name);
        client.create();

        ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), userId, name);
        project.create();

        VectorizerBO vectorizer = ModelUtils.getVectorizer(modelTechnology);
        vectorizer.create();

        PreferencesBO preference = ModelUtils.getPreference(client.getId(), client.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, vectorizer.getId());
        preference.create();

        PatchRequest patchRequest = new PatchRequest();
        PatchDocument patchDocument = new PatchDocument();
        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
        patchDocument.setPath("/status");
        patchDocument.setValue(PreferencesBO.STATUS_DISABLED);
        patchRequest.add(patchDocument);

        ObjectMapper mapper = new ObjectMapper();
        String jsonPatch = mapper.writeValueAsString(patchRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/json-patch+json"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, headers);

        String url = basePath + "preference?clientId=" + client.getId().toString() + "&id=" + preference.getId().toString();
        ResponseEntity<PreferencesBO> entity =
                this.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, PreferencesBO.class);
        PreferencesBO patchedPreference = entity.getBody();
        assertThat(entity.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(patchedPreference.getStatus(), is(equalTo(PreferencesBO.STATUS_DISABLED)));
        patchedPreference.delete();
        vectorizer.delete();
        project.delete();
        client.delete();
    }
}
