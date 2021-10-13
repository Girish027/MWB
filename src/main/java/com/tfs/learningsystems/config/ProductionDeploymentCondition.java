package com.tfs.learningsystems.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ProductionDeploymentCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String deployProfile = context.getEnvironment().getProperty("deploy.profile");
    return "production".equalsIgnoreCase(deployProfile);
  }

}
