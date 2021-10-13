/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class JsonObjectMapperConfig {

  @Bean
  @Scope("singleton")
  public ObjectMapper jsonObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

}
