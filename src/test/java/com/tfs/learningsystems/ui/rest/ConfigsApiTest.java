package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.*;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.testutil.ModelBuilderUtilTest;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.ConfigManager;
import com.tfs.learningsystems.ui.ElasticApiBaseTest;
import com.tfs.learningsystems.ui.ModelManager;
import com.tfs.learningsystems.ui.nlmodel.model.dao.ModelDao;
import com.tfs.learningsystems.util.CommonLib;
import com.tfs.learningsystems.util.Constants;
import java.io.IOException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j

public class ConfigsApiTest extends ElasticApiBaseTest {
    @Inject
    @Qualifier("modelManagerBean")
    private ModelManager modelManager;

    @Autowired
    @Qualifier("elasticTestUtils")
    private ElasticSearchTestUtils esTestUtils;

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
    public String userId = "UnitTest@247.ai";
    public String cid;

    @Before
    public void setUp() throws IOException {
        super.setUp();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        try {
            this.configManager.reloadDefaultConfig(appConfig.getEnglishConfigArchiveFilename(), Constants.DEFAULT_ENGLISH_CONFIG_ID, "en");
            CommonLib.Auth.addAccessToken(restTemplate);
            this.esTestUtils.refreshAllIndices();
            clientId = Integer.toString(super.clientDetail.getId());
            cid = super.clientDetail.getCid();
            projectId = Integer.toString(super.projectDetail.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetWordClassFromConfig() throws Exception {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

        ClientBO client = ModelUtils.setTestClient(name, cid);
        client.create();
        ProjectBO proj = ModelUtils.setTestProject(client.getId(), userId, name, cid);
        proj.create();

        Mockito.doReturn(proj).when(this.validationManager).
        validateClientAndProject(String.valueOf(proj.getClientId()),
        String.valueOf(proj.getId()));

        String jsonData = "{\n" +
                "  \"version\": \"1.0.0\",\n" +
                "  \"name\": \"spinnerTest-cfg\",\n" +
                "  \"case-normalization\",\n" +
                "    {\n" +
                "      \"wordclass-substitutions\": {\n" +
                "        \"type\": \"wordclass-subst-regex\",\n" +
                "        \"mappings\": {\n" +
                "        \"/\\\\b(?:pymnt|pymt|pyt|pmt)'?s?\\\\b/i\": \"_class_payment\"\n" +
                "      \t}\n" +
                "      }\n" +
                "    }\n" +
                "}";

        ModelConfigBO modelConfig = ModelBuilderUtilTest.getDummyModelConfig(name, userId, cid, proj.getId().toString(), jsonData);
        modelConfig.create();
        int configId = modelConfig.getId();

        String buildUrl = basePath + "clients/" + clientId + "/configs/" + configId;
        ResponseEntity<String> response = super.restTemplate.getForEntity(buildUrl, String.class);
        mockServer.verify();

        JSONObject responseJsonData = new JSONObject(response.getBody());

        assertEquals(responseJsonData.get("configFile"), "_class_payment\r\n" +
                "pymt\r\n" +
                "pmt\r\n" +
                "pymnt\r\n" +
                "pyt\r\n\r\n");

        proj.delete();
        client.delete();
    }
}
