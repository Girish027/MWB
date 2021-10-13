package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.db.PreferencesBO;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Service
@Path("/v1")
@Consumes({"application/json"})
@io.swagger.annotations.Api(description = "the Preference API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-08-15T00:03:23.342-07:00")
public class PreferenceApi {

    private final PreferenceApiService preferenceApiService;

    @Autowired
    public PreferenceApi(PreferenceApiService preferenceApiService) {
        this.preferenceApiService = preferenceApiService;
    }

    @POST
    @Path("/preference")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Create a Preference", notes = "", response = PreferencesBO.class, tags = {
            "preferences"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Preference added", response = PreferencesBO.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid request to create a Preference", response = Error.class),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = Error.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
    public Response createPreference(
            @ApiParam(value = "clientId" , required = true, example = "156") @QueryParam("clientId") String clientId,
            @ApiParam(value = "Preference object" , example = "{\"level\":\"model\", \"type\":\"USE\", \"value\": \"2\", \"attribute\": \"2\"}") PreferencesBO preference,
            @ApiParam(value = "set Default", example = "true/false") @QueryParam("setDefault") boolean setDefault,
            @Context UriInfo uriInfo)
            throws NotFoundException {
        ApiParameterSanitizer.sanitize(clientId);
        return preferenceApiService.createPreference(clientId, preference, setDefault, uriInfo);
    }

    @PATCH
    @Path("/preference")
    @Consumes({"application/json-patch+json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Updates a Preference ", notes = "Modifies one or more attributes of a preference", response = PreferencesBO.class, tags = {
            "preferences"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = PreferencesBO.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid request", response = java.lang.Error.class),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = java.lang.Error.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = java.lang.Error.class)})
    public Response updatePreferences(
            @ApiParam(value = "The clientId of the preference to be updated", required = true, example = "159") @QueryParam("clientId") String clientId,
            @ApiParam(value = "The id of the preference to be updated", required = true, example = "159") @QueryParam("id") String id,
            @ApiParam(value = "Actual Patch Request", required = true) PatchRequest jsonPatch)
            throws NotFoundException {
        ApiParameterSanitizer.sanitize(clientId);
        ApiParameterSanitizer.sanitize(id);
        return preferenceApiService.updatePreferences(clientId, id, jsonPatch);
    }

    @GET
    @Path("/preference/clients/{clientId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Metadata about a specific preference", notes = "", response = PreferencesBO.class, tags = {
            "preferences"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = PreferencesBO.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid preference parameters", response = Error.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
    public Response getPreferenceByLevelTypeAndAttribute(
            @ApiParam(value = "The clientId of the preference", required = true, example = "159") @PathParam("clientId") String clientId,
            @ApiParam(value = "The level of the preference", required = true, example = "159") @QueryParam("level") String level,
            @ApiParam(value = "The type of the preference", required = true, example = "159") @QueryParam("type") String type,
            @ApiParam(value = "The attribute of the preference", required = true, example = "159") @QueryParam("attribute") String attribute,
            @ApiParam(value = "deleted status of the preference") @QueryParam("includeDeleted") boolean includeDeleted)
            throws NotFoundException {
        ApiParameterSanitizer.sanitize(clientId);
        ApiParameterSanitizer.sanitize(level);
        ApiParameterSanitizer.sanitize(type);
        ApiParameterSanitizer.sanitize(attribute);
        return preferenceApiService.getPreferenceByLevelTypeAndAttribute(clientId, level, type, attribute, includeDeleted);
    }

    @GET
    @Path("/preference")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Metadata about all preferences", notes = "", response = PreferencesBO.class, tags = {
            "preferences"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = PreferencesBO.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid preference parameters", response = Error.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
    public Response getAllPreferences(
            @ApiParam(value = "The clientId of the preference to be updated", required = true, example = "159") @QueryParam("clientId") String clientId,
            @ApiParam(value = "deleted status of the preference to be updated") @QueryParam("includeDeleted") boolean includeDeleted)
            throws NotFoundException {
        return preferenceApiService.getAllPreferences(clientId, includeDeleted);
    }
}
