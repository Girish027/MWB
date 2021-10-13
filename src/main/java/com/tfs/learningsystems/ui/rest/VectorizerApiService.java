package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.PatchRequest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-08-15T00:03:23.342-07:00")
public abstract class VectorizerApiService {
    public abstract Response createVectorizer(String embeddingType, String version, UriInfo uriInfo) throws NotFoundException;

    public abstract Response getVectorizerById(String id) throws NotFoundException;

    public abstract Response getAllVectorizers() throws NotFoundException;

    public abstract Response getVectorizerByClientProject(String clientId, String projectId) throws NotFoundException;

    public abstract Response updateVectorizers(String id, PatchRequest jsonPatch) throws NotFoundException;

    public abstract Response getVectorizerByTechnology(String technology) throws NotFoundException;

}
