/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Data
@Configuration
@ConfigurationProperties
@PropertySources({
    @PropertySource("classpath:/ingestion.properties"),
    @PropertySource(value = "${ingestion.properties}", ignoreResourceNotFound = true)
})
public class IngestionPropertyConfig {

  private int startingRetryCount;
  private int maxRetryCount;
  private int repeatCount;
  private long startingRetryDelayMs;
  private long startingDelayMs;
  private String triggerNameFormat;
  private String jobNameFormat;
}
