package com.tfs.learningsystems.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AspectLogger {

  @Autowired
  private Environment env;

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final Set<String> omitList = Collections.unmodifiableSet(
      new HashSet<String>(Arrays.asList(
          null,
          "",
          "uriInfo",
          "securityContext"
      )));

  private static final int MAX_LENGTH = 1000;

  @Before("execution(* com.tfs.learningsystems.ui.rest.impl.*ApiServiceImpl.*(..))")
  public static void logAPICall(JoinPoint jp) throws Throwable {
    Object[] args = jp.getArgs();
    String[] argNames = ((CodeSignature) jp.getSignature()).getParameterNames();

    StringBuilder logOutput = new StringBuilder("[REST API CALL] ");
    logOutput.append(jp.getSignature().getName());
    logOutput.append(" (" + jp.getSignature().toLongString() + ") ");
    logOutput.append("ARGUMENTS:" + formatArgString(argNames, args));

    log.debug(logOutput.toString());
  }

  @AfterReturning(pointcut = "execution(javax.ws.rs.core.Response com.tfs.learningsystems.ui.rest.impl.*ApiServiceImpl.*(..))",
      returning = "retVal")
  public static void logAPIResponse(Response retVal) throws Throwable {
    StringBuilder logOutput = new StringBuilder("[REST API RESPONSE] ");
    if (retVal == null) {
      logOutput.append("null");
    } else {
      logOutput.append(retVal.toString());
      logOutput.append(" HEADER: " + retVal.getHeaders().toString());
      logOutput.append(" BODY: ");

      String jsonString;
      if (retVal.getEntity() == null) {
        jsonString = "null";
      } else if (HttpServletRequest.class.isInstance(retVal.getEntity()) || InputStream.class
          .isInstance(retVal.getEntity()) || FileDescriptor.class.isInstance(retVal.getEntity())) {
        jsonString = retVal.getEntity().toString();
      } else {
        jsonString = mapper.writeValueAsString(retVal.getEntity());
      }

      if (jsonString.length() > MAX_LENGTH) {
        logOutput.append(jsonString.substring(0, MAX_LENGTH)).append("...}");
      } else {
        logOutput.append(jsonString);
      }
    }

    log.debug(logOutput.toString());
  }

  @Around("execution(* com.tfs.learningsystems.ui.dao.Jdbc*Dao.*(..))")
  public static Object logDAOCall(ProceedingJoinPoint pjp) throws Throwable {

    Object retVal = pjp.proceed();
    Object[] args = pjp.getArgs();
    String[] argNames = ((CodeSignature) pjp.getSignature()).getParameterNames();

    StringBuilder logOutput = new StringBuilder("[DB ACCESS CALL] ");

    logOutput.append(pjp.getSignature().getName());
    logOutput.append(" (" + pjp.getSignature().toString() + ")");
    logOutput.append(" ARGUMENTS:" + formatArgString(argNames, args));
    logOutput
        .append(" RETURNED:" + ((retVal == null) ? "null" : mapper.writeValueAsString(retVal)));

    log.debug(logOutput.toString());

    return retVal;
  }


  @AfterThrowing(pointcut = "execution(* com.tfs.learningsystems.*.*(..))"
      + "|| execution(* com.tfs.learningsystems.ui.model.*.*(..))"
      + "|| execution(* com.tfs.learningsystems.ui.rest.impl.*.*(..))",
      throwing = "ex")
  public void logGenericException(Exception ex) {
    log.error("[EXCEPTION THROWN] " + ex.toString());
    log.error("[STACKTRACE] ", ex);
  }

  public static void logMessage(String type, String method, String message) {
    StringBuilder logOutput = new StringBuilder(type);
    logOutput.append(" ").append(method);
    logOutput.append(" : ").append(message);

    log.debug(logOutput.toString());
  }

  public static void logException(Exception ex) {
    log.error("[EXCEPTION THROWN] " + ex.toString());
    log.error("[STACKTRACE] ", ex);
  }

  private static String formatArgString(String[] argNames, Object[] args) throws Throwable {

    Map<String, String> argMap = new HashMap<String, String>();

    for (int i = 0; i < args.length; i++) {
      String argName = argNames[i];
      if (omitList.contains(argName)) {
        continue;
      }

      String argVal;
      if (HttpServletRequest.class.isInstance(args[i]) || InputStream.class.isInstance(args[i])
          || FileDescriptor.class.isInstance(args[i])) {
        argVal = args[i].toString();
      } else {
        argVal = mapper.writeValueAsString(args[i]);
      }

      argMap.put(argName, argVal);
    }

    String result = "{" + argMap.entrySet().stream()
        .map(Object::toString)
        .collect(Collectors.joining(",")) + "}";

    return result;
  }


}
