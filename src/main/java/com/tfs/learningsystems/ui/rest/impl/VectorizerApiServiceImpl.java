package com.tfs.learningsystems.ui.rest.impl;


import com.tfs.learningsystems.db.VectorizerBO;
import com.tfs.learningsystems.ui.VectorizerManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.ui.rest.VectorizerApiService;
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
public class VectorizerApiServiceImpl extends VectorizerApiService {

    @Inject
    @Qualifier("vectorizerManagerBean")
    private VectorizerManager vectorizerManager;

    @Override
    public Response createVectorizer(String embeddingType, String version, UriInfo uriInfo)
            throws NotFoundException {
        try {
            VectorizerBO vectorizer = vectorizerManager.addVectorizer(embeddingType, version);
            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
            URI locationURI = uriBuilder.path("").build();
            return Response.created(locationURI).entity(vectorizer).build();
        } catch (Exception ex) {
            log.error("Failed to create vectorizer ", ex);
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                            ErrorMessage.VECTORIZER_NOT_CREATED))
                    .build(), ex);
        }
    }

    @Override
    public Response getVectorizerById(String id)
            throws NotFoundException {
        try {
            VectorizerBO vectorizer = vectorizerManager.getVectorizerById(id);
            return Response.ok().entity(vectorizer).build();
        } catch (Exception ex) {
            log.error("Failed to fetch vectorizer with id", ex);
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                            ErrorMessage.VECTORIZER_NOT_FOUND))
                    .build(), ex);
        }
    }

    @Override
    public Response getAllVectorizers()
            throws NotFoundException {
        try {
            List<VectorizerBO> vectorizers = vectorizerManager.getAllVectorizers();
            return Response.ok().entity(vectorizers).build();
        } catch (Exception ex) {
            log.error("Failed to fetch all vectorizers ", ex);
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                            ErrorMessage.VECTORIZER_NOT_FOUND))
                    .build(), ex);
        }
    }

    @Override
    public Response getVectorizerByClientProject(String clientId, String projectId)
            throws NotFoundException {
        try {
            VectorizerBO vectorizer = vectorizerManager.getVectorizerByClientProject(clientId, projectId);
            return Response.ok().entity(vectorizer).build();
        } catch (Exception ex) {
            log.error("Failed to fetch vectorizer with clientId & projectId", ex);
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                            ErrorMessage.VECTORIZER_NOT_FOUND))
                    .build(), ex);
        }
    }

    @Override
    public Response updateVectorizers(String id, PatchRequest jsonPatch)
            throws NotFoundException {
        try {
            VectorizerBO vectorizer = vectorizerManager.updateVectorizers(id, jsonPatch);
            return Response.ok().entity(vectorizer).build();
        } catch (Exception ex) {
            log.error("Failed to update vectorizer with Id", ex);
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                            ErrorMessage.VECTORIZER_NOT_UPDATED))
                    .build(), ex);
        }
    }

    @Override
    public Response getVectorizerByTechnology(String technology)
            throws NotFoundException {
        try {
            VectorizerBO vectorizer = vectorizerManager.getLatestVectorizerByTechnology(technology);
            return Response.ok().entity(vectorizer).build();
        } catch (Exception ex) {
            log.error("Failed to fetch vectorizer with technology", ex);
            throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                            ErrorMessage.VECTORIZER_NOT_FOUND))
                    .build(), ex);
        }
    }
}
