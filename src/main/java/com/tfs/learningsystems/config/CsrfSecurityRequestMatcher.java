package com.tfs.learningsystems.config;

import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CsrfSecurityRequestMatcher implements RequestMatcher {

  private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

  private RegexRequestMatcher unprotectedMatcher =
      new RegexRequestMatcher("/nltools/private/v1/csrftoken", null);

  @Override
  public boolean matches(HttpServletRequest request) {
    boolean matched = true;
    if (allowedMethods.matcher(request.getMethod()).matches()) {
      matched = false;
    } else {
      matched = !unprotectedMatcher.matches(request);
    }

    return matched;
  }

}
