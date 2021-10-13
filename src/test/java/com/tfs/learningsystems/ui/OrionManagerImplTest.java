package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.auth.AuthValidationBaseTest;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState;
import com.tfs.learningsystems.util.CommonLib;
import com.tfs.learningsystems.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class OrionManagerImplTest extends AuthValidationBaseTest {

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Autowired
    @Qualifier("orionManagerBean")
    private OrionManager orionManager;

    private String modelUUID = "a5d666e6-f062-4f2f-9001-3da5b92ac74f";

    private String modelType = "classifier";

    private String modelTechnology = "tensorflow";

    private String vectorizerVersion = "1.1";

    private String digitalHostedUrl = "https://digitalUrl.com/digital";

    private Boolean isUnbundled = true;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        try {
            this.configManager.reloadDefaultConfig(appConfig.getEnglishConfigArchiveFilename(),
                    Constants.DEFAULT_ENGLISH_CONFIG_ID, "en");

            CommonLib.Auth.addAccessToken(restTemplate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetDigitalModelFromOrion() throws IOException {

        Path path = Paths.get("src/test/resources/unit_test_case.model");

        byte[] data = Files.readAllBytes(path);
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID + "/digital"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(data));
        File file = orionManager.getDigitalModelFromOrion(modelUUID);
        assertTrue(FileUtils.contentEquals(new File("src/test/resources/unit_test_case.model"), file));
    }

    @Test
    public void testGetSpeechModelFromOrion() throws IOException {

        Path path = Paths.get("src/test/resources/unit_test_case.model");

        byte[] data = Files.readAllBytes(path);
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID + "/slm"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(data));
        File file = orionManager.getSpeechModelFromOrion(modelUUID);
        assertTrue(FileUtils.contentEquals(new File("src/test/resources/unit_test_case.model"), file));
    }

    @Test
    public void testGetBuiltModelFromOrion() throws IOException {

        Path path = Paths.get("src/test/resources/unit_test_case.model");
        byte[] data = Files.readAllBytes(path);
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(data));
        File file = orionManager.getBuiltModelFromOrion(modelUUID);
        assertTrue(FileUtils.contentEquals(new File("src/test/resources/unit_test_case.model"), file));
    }

    @Test
    public void testGetBuildModelStatsFromOrionForInternal() throws IOException {

        this.setUserIdOnDetailsMap("dummyUserId");
        this.setDetailsMap("user@247.ai", "INTERNAL","user@247-inc.com");
        Path path = Paths.get("src/test/resources/model_stats.xlsx");
        byte[] data = Files.readAllBytes(path);
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID + "/statistics"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(data));
        File file = orionManager.getBuildModelStatsFromOrion(modelUUID);
        assertTrue(FileUtils.contentEquals(new File("src/test/resources/model_stats.xlsx"), file));
    }

    @Test
    public void testGetBuildModelStatsFromOrionForExternal() throws IOException {

        Path path = Paths.get("src/test/resources/model_stats.xlsx");
        byte[] data = Files.readAllBytes(path);
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID + "/externalStatistics"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(data));
        File file = orionManager.getBuildModelStatsFromOrion(modelUUID);
        assertTrue(FileUtils.contentEquals(new File("src/test/resources/model_stats.xlsx"), file));
    }

    @Test
    public void testGetModelTrainingOutputsFromOrion() throws IOException {

        Path path = Paths.get("src/test/resources/trainingOutputs.zip");
        byte[] data = Files.readAllBytes(path);
        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID + "/training-outputs"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(data));
        File file = orionManager.getModelTrainingOutputsFromOrion(modelUUID);
        assertTrue(FileUtils.contentEquals(new File("src/test/resources/trainingOutputs.zip"), file));
    }

    @Test
    public void testDeleteBuiltModelFromOrion() throws IOException {

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK));
        Boolean isDeleted = orionManager.deleteBuiltModelFromOrion(modelUUID);
        assertTrue(isDeleted);
    }

    @Test
    public void testGetModelBuildingDigitalStatusSuccess() throws IOException {

        String endAt = "1593014941766";

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID + "/status"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body("{\"status\": \"" + Constants.ORION_COMPLETE_STATUS +
                        "\",\"endedAt\": \""+ endAt +"\"}"));

        TFSModelJobState modelJobState = orionManager.getModelBuildingStatus(modelUUID);
        assertEquals(modelJobState.getModelUUID(), modelUUID);
        assertEquals( Constants.ORION_COMPLETE_STATUS, modelJobState.getStatusMessage());
        assertEquals(TFSModelJobState.Status.COMPLETED, modelJobState.getStatus());
    }

    @Test
    public void testGetModelBuildingSpeechStatusSuccess() throws IOException {

        String endAt = "1593014941766";

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID + "/status"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body("{\"status\": \"" + Constants.ORION_SPEECH_COMPLETE_STATUS +
                        "\",\"endedAt\": \""+ endAt +"\"}"));

        TFSModelJobState modelJobState = orionManager.getModelBuildingStatus(modelUUID);
        assertEquals(modelJobState.getModelUUID(), modelUUID);
        assertEquals( Constants.ORION_SPEECH_COMPLETE_STATUS, modelJobState.getStatusMessage());
        assertEquals(TFSModelJobState.Status.COMPLETED, modelJobState.getStatus());
    }

    @Test
    public void testGetModelBuildingStatusFailure() throws IOException {

        String endAt = "1593014941766";

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID + "/status"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body("{\"status\": \"" + Constants.ORION_FAILED_STATUS +
                        "\",\"endedAt\": \""+ endAt +"\"}"));

        TFSModelJobState modelJobState = orionManager.getModelBuildingStatus(modelUUID);
        assertEquals(modelJobState.getModelUUID(), modelUUID);
        assertEquals(Constants.ORION_FAILED_STATUS, modelJobState.getStatusMessage());
        assertEquals(TFSModelJobState.Status.FAILED, modelJobState.getStatus());

    }

    @Test
    public void testPatchModelToOrionSuccess() {

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID +
                "?isUnbundled=" + isUnbundled.toString() +"&digitalHostedUrl=" + digitalHostedUrl + "&modelType=" + modelType
                + "&modelTechnology=" + modelTechnology + "&vectorizerVersion=" + vectorizerVersion))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PATCH))
                .andExpect(MockRestRequestMatchers.queryParam("isUnbundled", isUnbundled.toString()))
                .andExpect(MockRestRequestMatchers.queryParam("digitalHostedUrl", digitalHostedUrl))
                .andExpect(MockRestRequestMatchers.queryParam("modelType", modelType))
                .andExpect(MockRestRequestMatchers.queryParam("modelTechnology", modelTechnology))
                .andExpect(MockRestRequestMatchers.queryParam("vectorizerVersion", vectorizerVersion.toString()))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.ACCEPTED).body("{\"link\": \"" + appConfig.getOrionURL() + "/" + modelUUID + "\"}"));

        String modelUUIDRecieved = orionManager.patchModelToOrion(modelUUID, new File("src/test/resources/word_classes.txt"), isUnbundled,
                digitalHostedUrl, new File("src/test/resources/test-input.csv"), modelType, modelTechnology, vectorizerVersion);
        assertEquals(modelUUIDRecieved, modelUUID);

    }

    @Test (expected = InvalidRequestException.class)
    public void testPatchModelToOrionFailure() {

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() + "/" + modelUUID +
                "?isUnbundled=" + isUnbundled.toString() +"&digitalHostedUrl=" + digitalHostedUrl +"&modelType=" + modelType
                + "&modelTechnology=" + modelTechnology + "&vectorizerVersion=" + vectorizerVersion))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PATCH))
                .andExpect(MockRestRequestMatchers.queryParam("isUnbundled", isUnbundled.toString()))
                .andExpect(MockRestRequestMatchers.queryParam("digitalHostedUrl", digitalHostedUrl))
                .andExpect(MockRestRequestMatchers.queryParam("modelType", modelType))
                .andExpect(MockRestRequestMatchers.queryParam("modelTechnology", modelTechnology))
                .andExpect(MockRestRequestMatchers.queryParam("vectorizerVersion", vectorizerVersion.toString()))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        orionManager.patchModelToOrion(modelUUID, new File("src/test/resources/word_classes.txt"), isUnbundled,
                digitalHostedUrl, new File("src/test/resources/test-input.csv"), modelType, modelTechnology, vectorizerVersion);
    }

    @Test
    public void testPostModelToOrionSuccess() {

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() +
                "?digitalHostedUrl=" + digitalHostedUrl +"&modelType=" + modelType + "&modelTechnology=" + modelTechnology
                + "&vectorizerVersion=" + vectorizerVersion + "&isUnbundled=" + isUnbundled.toString()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.queryParam("isUnbundled", isUnbundled.toString()))
                .andExpect(MockRestRequestMatchers.queryParam("modelType", modelType))
                .andExpect(MockRestRequestMatchers.queryParam("modelTechnology", modelTechnology))
                .andExpect(MockRestRequestMatchers.queryParam("vectorizerVersion", vectorizerVersion.toString()))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.ACCEPTED).body("{\"link\": \"" + appConfig.getOrionURL() + "/" + modelUUID + "\"}"));

        String modelUUIDRecieved = orionManager.postModelToOrion(new File("src/test/resources/test-input.csv"),
                new File("src/test/resources/speechConfig.json"),
                new File("src/test/resources/word_classes.txt"),
                isUnbundled,
                digitalHostedUrl,
                modelType,
                modelTechnology,
                vectorizerVersion);
        assertEquals(modelUUIDRecieved, modelUUID);
    }

    @Test (expected = InvalidRequestException.class)
    public void testPostModelToOrionFailure() {

        mockServer.expect(MockRestRequestMatchers.requestTo(appConfig.getOrionURL() +
                "?digitalHostedUrl=" + digitalHostedUrl +"&modelType=" + modelType + "&modelTechnology=" + modelTechnology
                + "&vectorizerVersion=" + vectorizerVersion + "&isUnbundled=" + isUnbundled.toString()))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.queryParam("isUnbundled", isUnbundled.toString()))
                .andExpect(MockRestRequestMatchers.queryParam("modelType", modelType))
                .andExpect(MockRestRequestMatchers.queryParam("modelTechnology", modelTechnology))
                .andExpect(MockRestRequestMatchers.queryParam("vectorizerVersion", vectorizerVersion.toString()))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        orionManager.postModelToOrion(new File("src/test/resources/test-input.csv"),
                new File("src/test/resources/speechConfig.json"),
                new File("src/test/resources/word_classes.txt"),
                isUnbundled,
                digitalHostedUrl,
                modelType,
                modelTechnology,
                vectorizerVersion);
    }

}
