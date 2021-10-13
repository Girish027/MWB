/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.PreferencesBO;
import com.tfs.learningsystems.db.VectorizerBO;
import com.tfs.learningsystems.json.JsonConverter;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author samirthesh
 */
@Slf4j
@Component
@Qualifier("vectorizerManagerBean")
public class VectorizerManagerImpl implements VectorizerManager {

  @Autowired
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Inject
  private JsonConverter jsonConverter;

  @Override
  public VectorizerBO addVectorizer(String type, String version) {

      log.info("adding vectorizer type:{} version:{}",type,version);
      VectorizerBO vectorizer = new VectorizerBO();
      vectorizer.setType(type);
      if (version != null) {
          vectorizer.setVersion(version);
      }
      vectorizer.create();
      return vectorizer;
  }

  public VectorizerBO getVectorizerById(String vectorizerId) {

      log.info("getting vectorizer vectorizerId:{}",vectorizerId);
      VectorizerBO vectorizerBO = new VectorizerBO();
      vectorizerBO = vectorizerBO.findOne(vectorizerId);
      if (vectorizerBO == null || vectorizerBO.getId() == null) {
          log.error("Vectorizer record not found", vectorizerId
          );
          throw new InvalidRequestException(
                  new Error(Response.Status.BAD_REQUEST.getStatusCode(), "vectorizer_not_found",
                          ErrorMessage.VECTORIZER_NOT_FOUND));
      }
      return (vectorizerBO);
  }

  public VectorizerBO getLatestVectorizerByTechnology(String modelTechnology){

      log.info("getting vectorizer for modelTechnology:{}", modelTechnology);
      VectorizerBO vectorizerBO = new VectorizerBO();
      Map<String, Object> conditions = new HashMap<>();
      conditions.put(PreferencesBO.FLD_TYPE, modelTechnology);
      conditions.put(Constants.IS_LATEST, VectorizerBO.IsLatest.ONE.getValue());
      vectorizerBO = vectorizerBO.findOne(conditions);

      if(vectorizerBO == null || vectorizerBO.getId() == null) {
          log.error("Vectorizer record not found", modelTechnology);
          throw new InvalidRequestException(
                  new Error(Response.Status.BAD_REQUEST.getStatusCode(), "vectorizer_not_found",
                          ErrorMessage.VECTORIZER_NOT_FOUND));
      }
      return (vectorizerBO);
  }

  public List<VectorizerBO> getAllVectorizers() {

      log.info("getting VectorizerList");
      VectorizerBO vectorizerBO = new VectorizerBO();
      Map<String, Object> param = new HashMap<>();
      List<VectorizerBO> vectorizers = vectorizerBO.list(param, null);
      return vectorizers;
  }

  public VectorizerBO updateVectorizers(String id, PatchRequest patchRequest) {

      log.info("updating Vectorizer id:{}",id);
      VectorizerBO currentVectorizer = validationManager.validateVectorizer(id);
      VectorizerBO newVectorizer = jsonConverter.patch(patchRequest, currentVectorizer, VectorizerBO.class);
      newVectorizer.update();
      return (newVectorizer);
  }

  @Override
  public VectorizerBO getVectorizerByClientProject(String clientId, String projectId) {

      log.info("getting Vectorizer for clientId:{} ------ projectId:{}",clientId,projectId);
      PreferencesBO preferencesBO = new PreferencesBO();
      Map<String, Object> param = new HashMap<>();
      param.put(PreferencesBO.FLD_CLIENT_ID, clientId);
      param.put(PreferencesBO.FLD_STATUS, PreferencesBO.STATUS_ENABLED);

      List<PreferencesBO> preferences = preferencesBO.list(param, null);
      VectorizerBO vectorizerBO;

      Integer clientVectorizer = null;
      Integer projectVectorizer = null;

      if(!preferences.isEmpty()) {
          for (PreferencesBO preference : preferences) {
              // if the requested project id has an existing record in preference or not
              if ((preference.getAttribute().equals(projectId) && (preference.getLevel().equals(Constants.PREFERENCE_MODEL_LEVEL))
                      && (preference.getType().equals(Constants.VECTORIZER_TYPE)))) {
                  projectVectorizer = preference.getValue();
              }
              // else use client level vectorizer
              else if((preference.getAttribute().equals(clientId) && (preference.getLevel().equals(Constants.PREFERENCE_CLIENT_LEVEL))
                      && (preference.getType().equals(Constants.VECTORIZER_TYPE)))) {
                  clientVectorizer = preference.getValue();
              }
          }

          // in case of both client level and project level existence, project is given priority
          if (projectVectorizer != null) {
              vectorizerBO = getVectorizerById(String.valueOf(projectVectorizer));
          }
          else if (clientVectorizer != null) {
              vectorizerBO = getVectorizerById(String.valueOf(clientVectorizer));
          }
          else {
              vectorizerBO = getLatestVectorizerByTechnology(Constants.NGRAM);
          }
      }
      else {
          vectorizerBO = getLatestVectorizerByTechnology(Constants.NGRAM);
//          log.error("Preference records not found", clientId, projectId
//          );
//          throw new InvalidRequestException(
//              new Error(Response.Status.BAD_REQUEST.getStatusCode(), "preference_not_found",
//                  ErrorMessage.PREFERENCE_NOT_FOUND));
      }
      return vectorizerBO;
  }
}
