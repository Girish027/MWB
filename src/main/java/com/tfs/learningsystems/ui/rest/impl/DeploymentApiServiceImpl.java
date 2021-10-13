package com.tfs.learningsystems.ui.rest.impl;


import com.tfs.learningsystems.ui.ModelManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.ProjectModelRequest;
import com.tfs.learningsystems.ui.rest.DeploymentApiService;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import java.util.List;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeploymentApiServiceImpl extends DeploymentApiService {

  @Autowired
  @Qualifier("modelManagerBean")
  private ModelManager modelManager;

  @Override
  public Response publishModel(String clientId, List<ProjectModelRequest> projectModels, String tag)
      throws NotFoundException {

      String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
      if (currentUserEmail == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      return Response.ok().entity(modelManager
          .publishModel(clientId, projectModels, tag, currentUserEmail)).build();
  }

  @Override
  public Response getClientTags(String clientId) {
    try {

      return Response.ok().entity(modelManager
          .getClientTags(clientId)).build();

    } catch (Exception e) {

      log.error("failed to load tags ", e);
      throw new ServerErrorException(Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                  ErrorMessage.MODEL_TAGS_LOAD_ERROR_MESSAGE))
          .build());
    }

  }

  @Override
  public Response getClientTag(String clientId, String tagName) {
    try {

      return Response.ok().entity(modelManager
          .getClientTagDetails(clientId, tagName)).build();

    } catch (Exception e) {

      log.error("failed to load tag ", e);
      throw new ServerErrorException(Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                  ErrorMessage.MODEL_TAG_LOAD_ERROR_MESSAGE))
          .build());
    }

  }


}

