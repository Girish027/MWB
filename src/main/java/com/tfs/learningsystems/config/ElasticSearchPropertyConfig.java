/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Data
@Configuration
@ConfigurationProperties
@PropertySources({
    @PropertySource(value = "classpath:/elasticsearch-${spring.profiles.active}.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "file:/tools/config/current/elasticsearch.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "${elastic.properties}", ignoreResourceNotFound = true)
})

public class ElasticSearchPropertyConfig {

  private int apiPort;
  private int transportPort;
  private int searchPageSize;
  private int scrollTimeValue;
  private int searchActionGetTimeout;
  private int totalBulkProcessingActions;
  private int bulkProcessingFlushInterval;
  private int bulkProcessingAwaitCloseTime;
  private int bulkProcessingConcurrentRequests;
  private int bulkProcessingBackoffInitialDelay;
  private int bulkProcessingBackoffTotalRetries;
  private int cardinalityPrecisionThreshold;
  private int maxClauseCount;
  private int termsQueryChunkSize;
  private long maxIndexingWaitTime;

  private String tagLabel;
  private String nltoolsIndexName;
  private String nltoolsIndexAlias;
  private String nltoolsIndexSchemaVersion;
  private String nltoolsIndexSchemaVersionLabel;
  private String dateFormat;
  private String clusterName;
  private String taggedAtLabel;
  private String commentedAtLabel;
  private String taggedByLabel;
  private String commentedByLabel;
  private String deletedAtLabel;
  private String deletedByLabel;
  private String clientIdLabel;
  private String projectIdLabel;
  private String datasetIdLabel;
  private String numTokensLabel;
  private String quartzJobGroup;
  private String documentTypeLabel;
  private String autoTagCountLabel;
  private String autoTagStringLabel;
  private String indexSourceLocation;
  private String transportPingTimeout;
  private String transcriptionLabel;
  private String transcriptionHashLabel;
  private String transcriptionOriginalLabel;
  private String uuidLabel;
  private String taggedDocumentIdLabel;
  private String deletedDocumentIndexType;
  private String defaultDocumentIndexType;
  private String commentLabel;
  private String importedIntentLabel;
  private String taggingGuideRUTagLabel;
  private String taggingGuideCommentsLabel;
  private String taggingGuideKeywordsLabel;
  private String taggingGuideExamplesLabel;
  private String taggingGuideDescriptionLabel;

  private String classificationIndexName;
  private String classificationIndexAlias;

  private String classificationDataLabel;
  private String granularIntentLabel;
  private String classificationIdLabel;

  private String recordIdLabel;
  private String jobIdLabel;
  private String mergedAutoTagsLabel;
  private String hasCustomTagLabel;
  private String suggestedIntentTagsLabel;
  private String filenameLabel;
  private String sessionIdLabel;
  private String dateLabel;
  private String verticalLabel;
  private String datatypeLabel;
  private String autoTagLabel;

  private List<String> hosts = new ArrayList<String>();
}
