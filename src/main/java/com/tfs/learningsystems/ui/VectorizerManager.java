/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.VectorizerBO;
import com.tfs.learningsystems.ui.model.PatchRequest;

import java.util.List;

/**
 * @author jkarpala
 */
public interface VectorizerManager {

  /**
   * creates vectorizer record in Preferences Table
   * @return newVectorizer added
   */
  public VectorizerBO addVectorizer(String type, String version);

  /**
   * get vectorizer based on id
   * @return vectorizer
   */
  public VectorizerBO getVectorizerById(String vectorizerId);

  /**
   * get vectorizer based on model technology
   * @return vectorizer
   */
  public VectorizerBO getLatestVectorizerByTechnology(String modelTechnology);

  /**
   * List all vectorizers
   * @return List of vectorizers
   */
  public List<VectorizerBO> getAllVectorizers();

  /**
   * updates vectorizer record in vectorizer Table for the given id
   * @return updatedVectorizer
   */
  public VectorizerBO updateVectorizers(String id, PatchRequest patchRequest);

  /**
   * get vectorizer record in vectorizer Table for the given clientid and projectId
   * @return vectorizer
   */
  public VectorizerBO getVectorizerByClientProject(String clientId, String projectId);

  }
