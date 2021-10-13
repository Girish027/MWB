package com.tfs.learningsystems.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class ActionContextTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testInstance() {
    String testId = "test_Cid";
    String testClientName = "testClient";
    ActionContext.init(testId, "session_id", null, "testClient", "mwb-team@247-inc.com",null);
    ActionContext ac = ActionContext.get();
    assertEquals(testId, ac.getClientId());
    assertEquals(testClientName, ActionContext.getName());
    ActionContext.remove();
    ac = ActionContext.get();
    assertNull(ac.getClientId());
  }


  @Test
  public void testRoleReset()
  {
    List<String> userRoles= new LinkedList<>();
    userRoles.add("MWB_ROLE_EXTERNAL");
    ActionContext ac;
    ActionContext.setRoles(userRoles);
    ac = ActionContext.get();
    assertEquals(ac.getRoles(),userRoles);
  }
  
}
