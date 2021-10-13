package com.tfs.learningsystems.auth.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
public class InternalAuthValidationFilter extends GenericFilterBean {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;

    HttpSession session = httpServletRequest.getSession(false);
    if (session == null) {
      httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      log.debug("Session is valid so following through...");
    }
    chain.doFilter(httpServletRequest, httpServletResponse);
  }

}
