package com.tfs.learningsystems.util;

import com.tfs.learningsystems.auth.AuthValidationBaseTest;
import com.tfs.learningsystems.ui.model.ClientDetail;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class CommonUtilsTest extends AuthValidationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Rule
    public TemporaryFolder rootFolder = new TemporaryFolder();

    @Test
    public void sanitizeInput() {
        String badString = "Representative_Request    ";
        String sanitizedString = CommonUtils.sanitize(badString);
        assertEquals("Representative_Request", sanitizedString);
    }

    @Test
    public void testGetUserId() throws Exception {
        String userId = "dummyUserId";
        this.setUserIdOnDetailsMap(userId);
        String receivedUserId = CommonUtils.getUserId();
        assertEquals(userId, receivedUserId);
    }

    @Test
    public void testIsUserExternalTypeWithAuthIsNull() throws Exception {
        assertTrue(CommonUtils.isUserExternalType());
    }

    @Test
    public void testIsUserExternalTypeWithUserTypeNull() throws Exception {
        String userType = null;
        String email = "testup@247.ai";
        String preferredEmail = "testup@247-inc.com";
        this.setDetailsMap(email, userType, preferredEmail);
        assertFalse(CommonUtils.isUserExternalType());
    }

    @Test
    public void testIsUserExternalTypeWithUserTypeAndEmailNull() throws Exception {
        String userType = null;
        String email = null;
        String preferredEmail = "testup@247-inc.com";
        this.setDetailsMap(email, userType, preferredEmail);
        assertFalse(CommonUtils.isUserExternalType());
    }

    @Test
    public void testIsUserExternalTypeWithUserTypeEmailAndPreferredEmailNull() throws Exception {
        String userType = null;
        String email = null;
        String preferredEmail = null;
        this.setDetailsMap(email, userType, preferredEmail);
        assertTrue(CommonUtils.isUserExternalType());
    }

    @Test
    public void testIsUserExternalTypeWithUserTypeInternal() throws Exception {
        String userType = "INTERNAL";
        String email = "testup@247.ai";
        String preferredEmail = "testup@247-inc.com";
        this.setDetailsMap(email, userType, preferredEmail);
        assertFalse(CommonUtils.isUserExternalType());
    }

    @Test
    public void testIsUserExternalTypeWithUserTypeExternal() throws Exception {
        String userType = "EXTERNAL";
        String email = "testup@247.ai";
        String preferredEmail = "testup@247-inc.com";
        this.setDetailsMap(email, userType, preferredEmail);
        assertTrue(CommonUtils.isUserExternalType());
    }

    @Test
    public void testIsUserExternalTypeWithUserTypeNullAndExternalEmail() throws Exception {
        String userType = null;
        String email = "testup@test.com";
        String preferredEmail = "testup@247-inc.com";
        this.setDetailsMap(email, userType, preferredEmail);
        assertTrue(CommonUtils.isUserExternalType());
    }

    @Test
    public void testIsUserExternalTypeWithUserTypeEmailNullAndExternalPreferredEmail() throws Exception {
        String userType = null;
        String email = null;
        String preferredEmail = "testup@test.com";
        this.setDetailsMap(email, userType, preferredEmail);
        assertTrue(CommonUtils.isUserExternalType());
    }

    @Test
    public void testIsUserClientAuthorizedWithClientMapNull() throws Exception {
        Map<String, ClientDetail> clientsMap = null;
        String clientName = "247AI_AISHA";
        assertFalse(CommonUtils.isUserClientAuthorized(clientsMap, clientName));
    }

    @Test
    public void testIsUserClientAuthorizedWithClientMapNotNullTruthy() throws Exception {
        Map<String, ClientDetail>  clientsMap = new HashMap<>();
        clientsMap.put("247AI_AISHA", new ClientDetail());
        String clientName = "247AI_AISHA";
        assertTrue(CommonUtils.isUserClientAuthorized(clientsMap, clientName));
    }

    @Test
    public void testIsUserClientAuthorizedWithClientMapNotNullFalse() throws Exception {
        Map<String, ClientDetail>  clientsMap = new HashMap<>();
        clientsMap.put("247AI_AISHA", new ClientDetail());
        String clientName = "247AI_GENERAL";
        assertFalse(CommonUtils.isUserClientAuthorized(clientsMap, clientName));
    }

    @Test
    public void testIsUserHasCapOneClientAccessTrueWithStar() throws Exception {
        String itsClientId = "Capone";
        this.setAdminUserWithClientAttribute();
        assertTrue(CommonUtils.isUserSpecificClientType(itsClientId));
    }

    @Test
    public void testIsUserHasCapOneClientAccessTrueWithSpecificClient() throws Exception {
        String itsClientId = "Capone";
        this.setSpecificUserWithClientAttribute(Constants.ITS_CAPONE_CLIENT_NAME);
        assertTrue(CommonUtils.isUserSpecificClientType(itsClientId));
    }

    @Test
    public void testIsUserHasCapOneClientAccessFalse() throws Exception {
        String itsClientId = "247ai";
        this.setSpecificUserWithClientAttribute(Constants.ITS_CAPONE_CLIENT_NAME);
        assertFalse(CommonUtils.isUserSpecificClientType(itsClientId));
    }

    @Test
    public void testIsUserHasCapOneClientAccessWithNullClientsMap() throws Exception {
        String itsClientId = "247ai";
        assertFalse(CommonUtils.isUserSpecificClientType(itsClientId));
    }

    @Test
    public void testIsUserHasCapOneClientAccessWithEmptyClientsMap() throws Exception {
        String itsClientId = "247ai";
        this.setEmptyUserWithClientAttribute();
        assertFalse(CommonUtils.isUserSpecificClientType(itsClientId));
    }

    @Test
    public void getQueryParamsValidate() throws Exception {
        String configsApiURL = "test";
        String result = CommonUtils.getQueryParams(configsApiURL);
        assertEquals(null , result);
    }

    @Test
    public void getQueryParamsValidateWithNull() throws Exception {
        String configsApiURL = null;
        String result = CommonUtils.getQueryParams(configsApiURL);
        assertEquals(null , result);
    }

    @Test
    public void getQueryParamsValidateValidCase() throws Exception {
        String configsApiURL = "/nltools/private/v1/clients/111/projects/222/configs";
        String result = CommonUtils.getQueryParams(configsApiURL);
        assertEquals("111" , result);
    }

    @Test (expected = Test.None.class)
    public void testDeleteFilesFromFolderWithValidHours() {
        int hoursBefore = 2;
        CommonUtils.deleteFilesFromTempFolder(hoursBefore);
    }

    @Test
    public void testDeleteFilesWithinTime() throws IOException {
        String subFolderName = "optimizeTempFolder1";
        String configFolderName = "config1";
        File subFolder = rootFolder.newFolder(subFolderName);
        rootFolder.newFolder(subFolderName, configFolderName);
        rootFolder.newFile(subFolderName + File.separator + "trainingOutputs123.zip");
        rootFolder.newFile(subFolderName + File.separator + "final123.model");
        rootFolder.newFile(subFolderName + File.separator + "model_stats123.xlsx");
        rootFolder.newFile(subFolderName + File.separator + configFolderName + File.separator + "config.json");
        int hoursBefore = 0;
        FileFilter fileFilter = new FileFilter(new String[]{"trainingOutputs","final","model_stats","config"}, hoursBefore);
        CommonUtils.deleteFiles(fileFilter, subFolder.toString());
        assertEquals(0, Objects.requireNonNull(subFolder.listFiles()).length);
        subFolder.delete();
    }

    @Test
    public void testDeleteFilesOutsideTime() throws IOException {
        String subFolderName = "optimizeTempFolder2";
        String configFolderName = "config2";
        File subFolder = rootFolder.newFolder(subFolderName);
        rootFolder.newFolder(subFolderName, configFolderName);
        rootFolder.newFile(subFolderName + File.separator + "trainingOutputs123.zip");
        rootFolder.newFile(subFolderName + File.separator + "final123.model");
        rootFolder.newFile(subFolderName + File.separator + "model_stats123.xlsx");
        rootFolder.newFile(subFolderName + File.separator + configFolderName + File.separator + "config.json");
        int hoursBefore = 2;
        FileFilter fileFilter = new FileFilter(new String[]{"trainingOutputs","final","model_stats","config"}, hoursBefore);
        CommonUtils.deleteFiles(fileFilter, subFolder.toString());
        assertEquals(4, Objects.requireNonNull(subFolder.listFiles()).length);
        subFolder.delete();
    }

    @Test
    public void testDeleteFilesBasedOnProvidedFilterTypesOnly() throws IOException {
        String subFolderName = "optimizeTempFolder3";
        String configFolderName = "config3";
        File subFolder = rootFolder.newFolder(subFolderName);
        rootFolder.newFolder(subFolderName, configFolderName);
        rootFolder.newFile(subFolderName + File.separator + "trainingOutputs123.zip");
        rootFolder.newFile(subFolderName + File.separator + "final123.model");
        rootFolder.newFile(subFolderName + File.separator + "model_stats123.xlsx");
        rootFolder.newFile(subFolderName + File.separator + configFolderName + File.separator + "config.json");
        int hoursBefore = 0;
        FileFilter fileFilter = new FileFilter(new String[]{"trainingOutputs"}, hoursBefore);
        CommonUtils.deleteFiles(fileFilter, subFolder.toString());
        assertEquals(3, Objects.requireNonNull(subFolder.listFiles()).length);
        subFolder.delete();
    }

    @Test
    public void testDeleteFilesBasedOnProvidedFilterTypesWithNull() throws IOException {
        String subFolderName = "optimizeTempFolder4";
        String configFolderName = "config4";
        File subFolder = rootFolder.newFolder(subFolderName);
        rootFolder.newFolder(subFolderName, configFolderName);
        rootFolder.newFile(subFolderName + File.separator + "trainingOutputs123.zip");
        rootFolder.newFile(subFolderName + File.separator + "final123.model");
        rootFolder.newFile(subFolderName + File.separator + "model_stats123.xlsx");
        rootFolder.newFile(subFolderName + File.separator + configFolderName + File.separator + "config.json");
        int hoursBefore = 0;
        FileFilter fileFilter = new FileFilter(new String[]{null}, hoursBefore);
        CommonUtils.deleteFiles(fileFilter, subFolder.toString());
        assertEquals(4, Objects.requireNonNull(subFolder.listFiles()).length);
        subFolder.delete();
    }

    @Test
    public void testDeleteFilesCheckWithNoFilesInTempDir() throws IOException {
        String subFolderName = "optimizeTempFolder5";
        String configFolderName = "config5";
        File subFolder = rootFolder.newFolder(subFolderName);
        rootFolder.newFolder(subFolderName, configFolderName);
        int hoursBefore = 0;
        FileFilter fileFilter = new FileFilter(new String[]{"trainingOutputs","final","model_stats","config"}, hoursBefore);
        CommonUtils.deleteFiles(fileFilter, subFolder.toString());
        assertEquals(0, Objects.requireNonNull(subFolder.listFiles()).length);
        subFolder.delete();
    }

    @Test
    public void getSpeedWorkProcessedMapValidate() throws Exception {
        String jsonData = "test";
        String result = CommonUtils.getSpeedWorkProcessedMap(jsonData);
        assertEquals("" , result);
    }

    @Test
    public void getSpeedWorkProcessedMapValidateWithNull() throws Exception {
        String jsonData = null;
        String result = CommonUtils.getSpeedWorkProcessedMap(jsonData);
        assertEquals("" , result);
    }

    @Test
    public void getSpeedWorkProcessedMapValidateValidCase() throws Exception {
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
        String result = CommonUtils.getSpeedWorkProcessedMap(jsonData);
        assertEquals("_class_payment\r\n" +
                "pymt\r\n" +
                "pmt\r\n" +
                "pymnt\r\n" +
                "pyt\r\n\r\n" , result);
    }
}