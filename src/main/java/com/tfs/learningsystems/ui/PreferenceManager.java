/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.PreferencesBO;
import com.tfs.learningsystems.ui.model.PatchRequest;
import java.util.List;

/**
 * @author samirthesh
 */
public interface PreferenceManager {

  /**
   * creates preferences record in Preferences Table
   * @return newPreference added
   */
  public PreferencesBO addPreference(String clientId, String type, String attribute, Integer value,
                                     String level, Boolean setDefault);

  /**
   * List all preferences for the clientId provided
   * @param showDeleted whether to count soft deleted items
   * @return List of preferences for the requested client
   */
  public List<PreferencesBO> getAllPreferences(String clientId, Boolean showDeleted);

  /**
   * updates preference record in Preferences Table for the given id
   * @return updatedPreference
   */
  public PreferencesBO updatePreferences(String clientId, String id, PatchRequest patchRequest);

  /**
   * updates preference record in Preferences Table for the given level, attribute
   * @param includeDeleted whether to count soft deleted item
   * @return matched Preference
   */
  public PreferencesBO getPreferenceByLevelTypeAndAttribute(String clientId, String level, String type,
                                                            String attribute, Boolean includeDeleted);

}
