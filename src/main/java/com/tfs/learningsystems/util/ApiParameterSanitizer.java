package com.tfs.learningsystems.util;

import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class ApiParameterSanitizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiParameterSanitizer.class);

  public static void sanitize(String val) {
    if (val == null) {
      return;
    }
    PolicyFactory sanitizer = Sanitizers.FORMATTING
        .and(Sanitizers.BLOCKS)
        .and(Sanitizers.STYLES)
        .and(Sanitizers.TABLES);

    String cleanVal = sanitizer.sanitize(val);
    if (!val.equals(cleanVal)) {
      LOGGER.info("Sanitize- '{}' - '{}'", val, cleanVal);
      val = cleanVal;
      throw new InvalidRequestException(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), null,
                      "Bad Request API Parameters"));
    }
  }


  public static void sanitizeMapListValues(Map<String, List<String>> map) {

    if (map == null) {
      return;
    }

    PolicyFactory sanitizer = Sanitizers.FORMATTING
        .and(Sanitizers.BLOCKS)
        .and(Sanitizers.STYLES)
        .and(Sanitizers.TABLES);

    for (List<String> list : map.values()) {
      if (list == null || list.isEmpty()) {
        throw new InvalidRequestException(
            new Error(Response.Status.BAD_REQUEST.getStatusCode(), null,
                "Dataset ids cannot be null or empty"));
      }

      for (String mapValue : list) {
        String cleanVal = sanitizer.sanitize(mapValue);
        if (!mapValue.equals(cleanVal)) {
          LOGGER.info("Sanitize- '{}' - '{}'", mapValue, cleanVal);
          mapValue = cleanVal;
        }

      }
    }

  }

  public static void sanitizeSet(Set<String> set) {

    if (set == null) {
      return;
    }

    PolicyFactory sanitizer = Sanitizers.FORMATTING
        .and(Sanitizers.BLOCKS)
        .and(Sanitizers.STYLES)
        .and(Sanitizers.TABLES);

    for (String key : set) {
      if (StringUtils.isEmpty(key)) {
        throw new InvalidRequestException(
            new Error(Response.Status.BAD_REQUEST.getStatusCode(), null,
                "Dataset ids cannot be null or empty"));
      }

      String cleanVal = sanitizer.sanitize(key);
      if (!key.equals(cleanVal)) {
        LOGGER.info("Sanitize- '{}' - '{}'", key, cleanVal);
        key = cleanVal;
      }


    }

  }

}
