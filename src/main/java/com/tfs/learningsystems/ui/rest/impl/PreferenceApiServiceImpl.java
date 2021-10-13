package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.db.PreferencesBO;
import com.tfs.learningsystems.ui.PreferenceManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.ui.rest.PreferenceApiService;
import com.tfs.learningsystems.util.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Service
@Slf4j
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "20170814T23:59:55.88807:00")
public class PreferenceApiServiceImpl extends PreferenceApiService {
    @Inject
    @Qualifier("preferenceManagerBean")
    private PreferenceManager preferenceManager;

    @Override
    public Response createPreference(String clientId, PreferencesBO preferencesBO, Boolean setDefault, UriInfo uriInfo)
            throws NotFoundException {
        try {
            PreferencesBO preference = preferenceManager.addPreference(clientId, preferencesBO.getType(), preferencesBO.getAttribute(), preferencesBO.getValue(), preferencesBO.getLevel(), setDefault);
            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
            URI locationURI = uriBuilder.path("").build();
            return Response.created(locationURI).entity(preference).build();
        } catch (Exception ex) {
            log.error("Failed to create preference ", ex);
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                            ErrorMessage.PREFERENCE_NOT_CREATED))
                    .build(), ex);
        }
    }

    @Override
    public Response getPreferenceByLevelTypeAndAttribute(String clientId, String level, String type,
                                                     String attribute, Boolean includeDeleted)
            throws NotFoundException {
        try {
            PreferencesBO preference = preferenceManager.getPreferenceByLevelTypeAndAttribute(clientId, level, type, attribute, includeDeleted);
            return Response.ok().entity(preference).build();
        } catch (Exception ex) {
            log.error("Failed to fetch preference with id", ex);
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                            ErrorMessage.PREFERENCE_NOT_FOUND))
                    .build(), ex);
        }
    }

    @Override
    public Response getAllPreferences(String clientId, Boolean includeDeleted)
            throws NotFoundException {
        try {
            List<PreferencesBO> preferences = preferenceManager.getAllPreferences(clientId, includeDeleted);
            return Response.ok().entity(preferences).build();
        } catch (Exception ex) {
            log.error("Failed to fetch all preferences ", ex);
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                            ErrorMessage.PREFERENCE_NOT_FOUND))
                    .build(), ex);
        }
    }

    @Override
    public Response updatePreferences(String clientId, String id, PatchRequest jsonPatch)
            throws NotFoundException {
        try {
            PreferencesBO preference = preferenceManager.updatePreferences(clientId, id, jsonPatch);
            return Response.ok().entity(preference).build();
        } catch (Exception ex) {
            log.error("Failed to update preference with Id", ex);
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                            ErrorMessage.PREFERENCE_NOT_UPDATED))
                    .build(), ex);
        }
    }


}
