package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.db.PreferencesBO;
import com.tfs.learningsystems.ui.model.PatchRequest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-08-15T00:03:23.342-07:00")
public abstract class PreferenceApiService {
    public abstract Response createPreference(String clientId, PreferencesBO preferencesBO, Boolean setDefault, UriInfo uriInfo) throws NotFoundException;

    public abstract Response getPreferenceByLevelTypeAndAttribute(String clientId, String level, String type,
                                                              String attribute, Boolean includeDeleted) throws NotFoundException;

    public abstract Response getAllPreferences(String clientId, Boolean includeDeleted) throws NotFoundException;

    public abstract Response updatePreferences(String clientId, String id, PatchRequest jsonPatch) throws NotFoundException;
}
