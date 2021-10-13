package com.tfs.learningsystems.testutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tfs.learningsystems.util.AuthUtil;
import javax.servlet.http.Cookie;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuthUtilTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreateCookie() {
    Cookie cookie1 = AuthUtil.createCookie("username", "testUser", 900, true);
    assertEquals(cookie1.getName(), "username");
    assertEquals(cookie1.getValue(), "testUser");
    assertTrue(cookie1.getSecure());

    Cookie cookie2 = AuthUtil.createCookie("userid", "10", 60, false);
    assertFalse(cookie2.getSecure());
    assertEquals(cookie2.getValue(), "10");
    assertEquals(cookie2.getMaxAge(), 60);
  }

}
