package com.tfs.learningsystems.auth;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ActionContext {

  private static ThreadLocal<ActionContext> actionContextThread = ThreadLocal.withInitial(ActionContext::new);
  private long startTime;
  private String clientId;
  private String sessionId;
  private Integer userId = Integer.valueOf(0);
  private String userEmailId;
  private Integer legacyId;
  private String name;
  private List<String> roles = new LinkedList<>();

  private ActionContext() {
  }

  public static void init(String clientId, String sessionId, Integer legacyId, String name,
      String userEmailId,
      List<String> roles) {
    ActionContext ac = get();

    ac.clientId = clientId;
    ac.sessionId = sessionId;
    ac.startTime = System.currentTimeMillis();
    ac.userId = Integer.valueOf(0); // to be populated
    ac.userEmailId = userEmailId;
    ac.legacyId = legacyId;
    ac.name = name;
    if (roles != null) {
      ac.roles.clear();
      ac.roles.addAll(roles);
    }
  }

  public static void clear() {
    ActionContext ac = get();

    ac.clientId = null;
    ac.legacyId = null;
    ac.name = null;
    ac.sessionId = null;
    ac.userEmailId = null;
    ac.startTime = System.currentTimeMillis();
    ac.userId = Integer.valueOf(0); // to be populated
  }


  static ActionContext get() {
    return actionContextThread.get();
  }

  static void remove() {
    ActionContext ac = get();
    ac.endRequest();
    actionContextThread.remove();
  }

  public static Integer getLegacyClientId() {
    ActionContext ac = get();
    return (ac.legacyId);
  }

  public static long getStartTime() {
    ActionContext ac = get();
    return (ac.startTime);
  }

  public static String getClientId() {
    ActionContext ac = get();
    return (ac.clientId);
  }

  public static String getSessionId() {
    ActionContext ac = get();
    return (ac.sessionId);
  }

  public static Integer getUserId() {
    ActionContext ac = get();
    return (ac.userId);
  }

  public static List<String> getRoles() {
    ActionContext ac = get();
    return (Collections.unmodifiableList(ac.roles));
  }

  public static void setRoles( List<String> userRoles) {
    ActionContext ac = get();
    ac.roles.clear();

    if (userRoles != null) {
      ac.roles.addAll(userRoles);
    }
  }

  public static String getName() {
    ActionContext ac = get();
    return (ac.name);
  }

  public static String getUserEmailId() {
    ActionContext ac = get();
    return ac.userEmailId;
  }

  public static Integer getLegacyId() {
    ActionContext ac = get();
    return ac.legacyId;
  }

  private void endRequest() {
    this.clientId = null;
    this.legacyId = null;
    this.name = null;
    this.userEmailId = null;
    this.startTime = 0L;
  }

}
