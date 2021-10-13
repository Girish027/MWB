/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tfs.learningsystems.util.CommonUtils;


@JsonSerialize
public class ClassificationTransientDocument {

  private String id;
  private String intent;
  private String rutag;
  private String classificationId;
  private String projectId;
  private String clientId;
  private String description;
  private String examples;
  private String keywords;
  private String comments;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIntent() {
    return CommonUtils.sanitize(intent);
  }

  public void setIntent(String intent) {
    this.intent = CommonUtils.sanitize(intent);
  }

  public String getRutag() {
    return CommonUtils.sanitize(rutag);
  }

  public void setRutag(String rutag) {
    this.rutag = CommonUtils.sanitize(rutag);
  }

  public String getClassificationId() {
    return CommonUtils.sanitize(classificationId);
  }

  public void setClassificationId(String classificationId) {
    this.classificationId = CommonUtils.sanitize(classificationId);
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getDescription() {
    return CommonUtils.sanitize(description);
  }

  public void setDescription(String description) {
    this.description = CommonUtils.sanitize(description);
  }

  public String getExamples() {
    return examples;
  }

  public void setExamples(String examples) {
    this.examples = examples;
  }

  public String getKeywords() {
    return CommonUtils.sanitize(keywords);
  }

  public void setKeywords(String keywords) {
    this.keywords = CommonUtils.sanitize(keywords);
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

}
