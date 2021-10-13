package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.ui.model.PatchRequest;
import java.io.InputStream;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@javax.annotation.Generated(
    value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-10-11T15:06:25.584-04:00")
public abstract class ConfigsApiService {

  public abstract Response addConfig(String name, String cid, String description, String projectId,
      InputStream fileInputStream,
      UriInfo uriInfo) throws NotFoundException;

  public abstract Response getConfigById(String configId) throws NotFoundException;

  public abstract Response getConfigFilesById(String configId) throws NotFoundException;

  public abstract Response listConfigs(Integer limit, Integer startIndex) throws NotFoundException;

  public abstract Response addConfig(final ModelConfigBO config,
      final UriInfo uriInfo);

  public abstract Response getConfigDataById(final String clientId, final String configId);

  public abstract Response getWordClassFromConfig(final String clientId, final String configId, UriInfo uriInfo);

  public abstract Response patchConfig(final String id,
      final PatchRequest jsonPatch);
}
