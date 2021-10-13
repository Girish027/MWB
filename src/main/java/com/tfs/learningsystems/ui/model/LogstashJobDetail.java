/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import java.util.List;
import lombok.Data;

@Data
public class LogstashJobDetail {

  private int projectId = 0;
  private int datasetId = 0;
  private int clientId = 0;
  private String jobId;
  private String vertical;
  private String dataType;
  private String username;
  private String csvFilePath;
  private String confFilePath;
  private String[] columns;
  private String firstColumnName;
  private long logstashCheckExecTimeout;
  private long logstashExecTimeout;
  private List<String> elasticSearchHosts;
  private String elasticSearchIndexName;
  private String elasticSearchIndexType;
  private String transcriptionEntityColumnName;
  private String originalTranscriptionColumnName;
  private String normalizedTranscriptionColumnName;
}

