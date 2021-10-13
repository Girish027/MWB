package com.tfs.learningsystems.auth.filter;

import com.tfs.learningsystems.auth.AuthValidationBaseTest;
import com.tfs.learningsystems.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class AuthValidationFilterTest extends AuthValidationBaseTest {

    @Autowired
    AuthValidationFilter authValidationFilter;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetTestAuth() throws Exception {
        OAuth2Authentication auth = authValidationFilter.getTestAuth(this.httpServletRequest, null);
        Map<String, Object> authorizationInfoMap = (HashMap<String, Object>) ((OAuth2AuthenticationDetails) (auth
                    .getDetails())).getDecodedDetails();
        Map<String, String> userGroups = (Map<String, String>) authorizationInfoMap.get(Constants.OKTA_USER_GROUPS);
        Map<String, String> claimDataMap = new HashMap<>();
        claimDataMap.put(Constants.MWB_ROLE_CLIENTADMIN, Constants.MWB_ROLE_CLIENTADMIN);
        assertNotNull(auth);
        assertEquals(userGroups, claimDataMap);
    }
}
