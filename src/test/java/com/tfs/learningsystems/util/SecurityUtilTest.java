package com.tfs.learningsystems.util;

import com.tfs.learningsystems.exceptions.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.assertEquals;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SecurityUtilTest {

  @Autowired private TestRestTemplate restTemplate;

  @Before
  public void setUp() throws Exception {
    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());
  }

  @Test
  public void byteToHexValid() {
    byte[] bytes = {10, 2, 15, 11};
    String convertedValue = SecurityUtil.byteToHex(bytes);
    assertEquals("0a020f0b", convertedValue);
  }

  @Test
  public void toMD5() throws ApplicationException {
    String input = Constants.INPUT_SECURITY_UTIL_HASH_VALUE;
    String convertedValue = SecurityUtil.toMD5(input);
    assertEquals("7fc56270e7a70fa81a5935b72eacbe29", convertedValue);
  }

  @Test
  public void checkMD5Hash() {
    String hash = Constants.INPUT_SECURITY_UTIL_HASH_VALUE;
    String key = Constants.INPUT_SECURITY_UTIL_HASH_VALUE;
    Boolean chekHashStatus = SecurityUtil.checkMD5Hash(hash, key);
    assertEquals(false, chekHashStatus);
  }

  @Test
  public void toSHA1() {
    String input = Constants.INPUT_SECURITY_UTIL_HASH_VALUE;
    String convertedValue = SecurityUtil.toSHA1(input);
    assertEquals("6dcd4ce23d88e2ee9568ba546c007c63d9131c1b", convertedValue);
  }
}