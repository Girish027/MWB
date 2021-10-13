package com.tfs.learningsystems.auth;

import static org.junit.Assert.assertEquals;

import com.tfs.learningsystems.exceptions.AppErrorCodes;
import com.tfs.learningsystems.exceptions.HandledException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AppErrorCodesTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testErrorCodes() {
    try {
      throw new HandledException(AppErrorCodes.AppErrorCode.InvalidDeveloperKey,
          "Test message here");
    } catch (HandledException e) {
      assertEquals(AppErrorCodes.AppErrorCode.InvalidDeveloperKey, e.getAppErrorCode());
    }
  }

  @Test
  public void testDecodeErrorCode() {
    String errorCodeStr = AppErrorCodes.AppErrorCode.InactiveDeveloperKey.getCode();
    AppErrorCodes.AppErrorCode code = AppErrorCodes.AppErrorCode.of(errorCodeStr);
    assertEquals(AppErrorCodes.AppErrorCode.InactiveDeveloperKey, code);
  }

}
