package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.db.VectorizerBO;
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
@io.swagger.annotations.Api(description = "the Vectorizer API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-08-15T00:03:23.342-07:00")
public class VectorizerApi {

    private final VectorizerApiService vectorizerApiService;

    @Autowired
    public VectorizerApi(VectorizerApiService vectorizerApiService) {
        this.vectorizerApiService = vectorizerApiService;
    }

    @POST
    @Path("/vectorizer")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Create a Vectorizer", notes = "", response = VectorizerBO.class, tags = {
            "vectorizers"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Vectorizer added", response = VectorizerBO.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid request to create a Vectorizer", response = Error.class),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = Error.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
    public Response createVectorizer(
            @ApiParam(value = "Embedding Type" , required = true, example = "USE or nGram") @QueryParam("embeddingType") String embeddingType,
            @ApiParam(value = "Version", example = "1.0 , 2.0") @QueryParam("version") String version,
            @Context UriInfo uriInfo)
            throws NotFoundException {
        ApiParameterSanitizer.sanitize(embeddingType);
        ApiParameterSanitizer.sanitize(version);
        return vectorizerApiService.createVectorizer(embeddingType, version, uriInfo);
    }

    @PATCH
    @Path("/vectorizer")
    @Consumes({"application/json-patch+json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Updates a Vectorizer ", notes = "Modifies one or more attributes of a vectorizer", response = VectorizerBO.class, tags = {
            "vectorizers"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = VectorizerBO.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid request", response = java.lang.Error.class),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = java.lang.Error.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = java.lang.Error.class)})
    public Response updateVectorizers(
            @ApiParam(value = "The id of the vectorizer to be updated", required = true, example = "159") @QueryParam("id") String id,
            @ApiParam(value = "Actual Patch Request", required = true) PatchRequest jsonPatch)
            throws NotFoundException {
        ApiParameterSanitizer.sanitize(id);
        return vectorizerApiService.updateVectorizers(id, jsonPatch);
    }

    @GET
    @Path("/vectorizer/{id}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Metadata about a specific vectorizer", notes = "", response = VectorizerBO.class, tags = {
            "vectorizers"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = VectorizerBO.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid vectorizer id", response = Error.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
    public Response getVectorizerById(
            @ApiParam(value = "The id of vectorizer", required = true, example = "159") @PathParam("id") String id)
            throws NotFoundException {
        ApiParameterSanitizer.sanitize(id);
        return vectorizerApiService.getVectorizerById(id);
    }

    @GET
    @Path("/vectorizer")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Metadata about all vectorizers", notes = "", response = VectorizerBO.class, tags = {
            "vectorizers"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = VectorizerBO.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid vectorizer id", response = Error.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
    public Response getAllVectorizers()
            throws NotFoundException {
        return vectorizerApiService.getAllVectorizers();
    }

    @GET
    @Path("/vectorizer/clients/{clientId}/projects/{projectId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Metadata about a specific vectorizer", notes = "", response = VectorizerBO.class, tags = {
            "vectorizers"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = VectorizerBO.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid API parameters", response = Error.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
    public Response getVectorizerByClientProject(
            @ApiParam(value = "The clientId of vectorizer", required = true, example = "159") @PathParam("clientId") String clientId,
            @ApiParam(value = "The projectId of vectorizer", required = true, example = "159") @PathParam("projectId") String projectId)
            throws NotFoundException {
        ApiParameterSanitizer.sanitize(clientId);
        ApiParameterSanitizer.sanitize(projectId);
        return vectorizerApiService.getVectorizerByClientProject(clientId, projectId);
    }

    @GET
    @Path("/vectorizer/type/{technology}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Metadata about a specific vectorizer by technology", notes = "", response = VectorizerBO.class, tags = {
            "vectorizers"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = VectorizerBO.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid API parameters", response = Error.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
    public Response getVectorizerByTechnology(
            @ApiParam(value = "The technology of vectorizer", required = true, example = "n-gram") @PathParam("technology") String technology)
            throws NotFoundException {
        ApiParameterSanitizer.sanitize(technology);
        return vectorizerApiService.getVectorizerByTechnology(technology);
    }
}
