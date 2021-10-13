/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.config.QuartzSchedulerConfig;
import com.tfs.learningsystems.ui.nlmodel.ModelJobExecutionService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = {FreeMarkerAutoConfiguration.class})
@Import({QuartzSchedulerConfig.class})
@EnableConfigurationProperties(AppConfig.class)
public class NlToolsUiApplication {

  public static void main(String[] args) {
    SpringApplication.run(NlToolsUiApplication.class, args);


  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  public ModelJobExecutionService modelJobService() {
    return new ModelJobExecutionService();
  }

}
