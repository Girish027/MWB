/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.config;

import freemarker.core.UndefinedOutputFormat;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class FreemarkerConfig {

  @Value("${freemarker.template-dir-prefix}")
  private String templatesDir;

  @Bean
  @Scope("singleton")
  public freemarker.template.Configuration freemarkerConfiguration()
      throws IOException, TemplateException {
    freemarker.template.Configuration cfg = new freemarker.template.Configuration(
        freemarker.template.Configuration.VERSION_2_3_25);
    cfg.setOutputFormat(UndefinedOutputFormat.INSTANCE);
    cfg.setClassForTemplateLoading(this.getClass(), templatesDir);
    //cfg.setDirectoryForTemplateLoading(new File(templatesDir));
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(true);
    return cfg;
  }

}
