package com.tfs.learningsystems.auth.protocol.handler;

import com.tfs.learningsystems.config.AppConfig;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class OAUth2SuccessHandler implements AuthenticationSuccessHandler {

  @Autowired
  private AppConfig appConfig;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response, Authentication authentication)
      throws IOException {

  }

}
