/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.util;

public class QuartzJobUtil {

  public static final String KEY_CURRENT_RERTY_COUNT = "currentRetryCount";
  public static final String KEY_MAX_RETRY_COUNT = "maxRetryCount";
  public static final String KEY_SYSTEM_PATH = "systemPath";
  public static final int KEY_DEFAULT_WAIT_TIME_TO_REFIRE = 60000; // milliseconds (60 seconds)
  public static final String KEY_LOGSTASH_JOB_DETAIL = "logstashJobDetail";
  public static final String KEY_CATEGORIZE_JOB_DETAIL = "categorizeJobDetail";
  public static final String KEY_CURRENT_TASK_DETAIL = "currentTaskDetail";
  public static final String KEY_REMAINING_TASKS = "remainingTasks";
  public static final String KEY_CATEGORIZER_READ_TIMEOUT = "categorizerReadTimeout";
  public static final String KEY_CATEGORIZER_CONNECT_TIMEOUT = "categorizerConnectTimeout";
  public static final String KEY_CATEGORIZER_CONNECTION_REQUEST_TIMEOUT = "categorizerConnectionRequestTimeout";
  public static final String KEY_CLIENT_ID = "clientId";
  public static final String KEY_PROJECT_ID = "projectId";
  public static final String KEY_DATASET_ID = "datasetId";
}
