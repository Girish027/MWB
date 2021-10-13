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
import org.thymeleaf.util.StringUtils;

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
@Qualifier("preferenceManagerBean")
public class PreferenceManagerImpl implements PreferenceManager {

  @Autowired
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Inject
  @Qualifier("vectorizerManagerBean")
  private VectorizerManager vectorizerManager;

  @Inject
  private JsonConverter jsonConverter;

    @Override
    public PreferencesBO addPreference(String clientId, String type, String attribute, Integer value,
                                       String level, Boolean setDefault) {

        log.info("adding preference for clientId:{}", clientId);
        PreferencesBO preferences = new PreferencesBO();
        validationManager.validateClient(clientId);
        validationManager.validateLevelTypeAndAttribute(level, attribute, type, clientId);
        preferences.setType(type);
        preferences.setValue(value);
        VectorizerBO vectorizerBO;

        // check to use latest parameter values or the existing client level values
        if (type.equals(Constants.VECTORIZER_TYPE)) {
            validationManager.validateVectorizer(String.valueOf(value));
            Boolean levelValidation = validationManager.validateLevel(level, setDefault);
            if (!levelValidation) {
                vectorizerBO = vectorizerManager.getVectorizerByClientProject(clientId, null);
                preferences.setValue(vectorizerBO.getId());
            }
        }
        preferences.setAttribute(attribute);
        preferences.setLevel(level);
        preferences.setClient_id(Integer.parseInt(clientId));
        preferences.create();
        return preferences;
    }

    @Override
    public PreferencesBO getPreferenceByLevelTypeAndAttribute(String clientId, String level, String type,
                                                          String attribute, Boolean includeDeleted) {

        PreferencesBO preferencesBO = new PreferencesBO();
        Map<String, Object> param = new HashMap<>();
        if (!StringUtils.isEmpty(clientId)) {
          param.put(PreferencesBO.FLD_CLIENT_ID, clientId);
          param.put(PreferencesBO.FLD_LEVEL, level);
          param.put(PreferencesBO.FLD_ATTRIBUTE, attribute);
          param.put(PreferencesBO.FLD_TYPE, type);
        }
        if(!includeDeleted) {
            param.put(PreferencesBO.FLD_STATUS, PreferencesBO.STATUS_ENABLED);
        }
        preferencesBO = preferencesBO.findOne(param);
        // check for preference record existence for the given parameters
        if (preferencesBO == null || preferencesBO.getId() == null) {
            log.error("---- level:{} level preference --- clientId:{}" + " ---- type:{}" +
                    "------ attribute:{} not found", level, clientId, type, attribute);
            throw new InvalidRequestException(
                    new Error(Response.Status.BAD_REQUEST.getStatusCode(), "preference_not_found",
                            ErrorMessage.PREFERENCE_NOT_FOUND));
        }
        return preferencesBO;
    }

    @Override
    public List<PreferencesBO> getAllPreferences(String clientId, Boolean includeDeleted) {

        log.info("getting preferences for clientId:{}", clientId);
        PreferencesBO preferencesBO = new PreferencesBO();
        Map<String, Object> param = new HashMap<>();
        if (!StringUtils.isEmpty(clientId)) {
            param.put(PreferencesBO.FLD_CLIENT_ID, clientId);
        }
        // check to include deleted records or not
        if(!includeDeleted) {
            param.put(PreferencesBO.FLD_STATUS, PreferencesBO.STATUS_ENABLED);
        }
        List<PreferencesBO> preferences = preferencesBO.list(param, null);
        if (preferences.size() == 0)
        {
            log.error("preference --- clientId:{} not found", clientId);
            throw new InvalidRequestException(
                    new Error(Response.Status.BAD_REQUEST.getStatusCode(), "preference_not_found",
                            ErrorMessage.PREFERENCE_NOT_FOUND));
        }
        return preferences;
    }

    @Override
    public PreferencesBO updatePreferences(String clientId, String id, PatchRequest patchRequest) {

        log.info("getting preference for clientId:{} ------ id:{}", clientId, id);
        PreferencesBO currentPreference = validationManager.validatePreference(clientId, id);
        validationManager.PreferencePatchRequest(clientId, currentPreference, patchRequest);
        PreferencesBO newPreference = jsonConverter.patch(patchRequest, currentPreference, PreferencesBO.class);
        newPreference.update();
        return (newPreference);
    }
}
