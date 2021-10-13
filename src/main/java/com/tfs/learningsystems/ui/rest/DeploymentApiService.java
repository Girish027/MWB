package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.ProjectModelRequest;
import java.util.List;
import javax.ws.rs.core.Response;

public abstract class DeploymentApiService {

  public abstract Response publishModel(String clientId, List<ProjectModelRequest> projectModels,
      String tag) throws NotFoundException;

  public abstract Response getClientTags(String clientId);

  public abstract Response getClientTag(String clientId, String tagName);
}

