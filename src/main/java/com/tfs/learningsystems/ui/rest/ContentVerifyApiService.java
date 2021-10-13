package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.VerifyRequest;
import java.util.List;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-08-21T16:55:37.740-07:00")
public abstract class ContentVerifyApiService {

  public abstract Response verifyIntents(String clientId, String projectId, Integer startIndex,
      Integer limit, List<String> sortBy, VerifyRequest search) throws NotFoundException;

}
