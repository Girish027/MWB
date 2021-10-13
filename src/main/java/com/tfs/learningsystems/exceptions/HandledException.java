package com.tfs.learningsystems.exceptions;

import com.tfs.learningsystems.exceptions.AppErrorCodes.AppErrorCode;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class HandledException extends RuntimeException {

  private static final long serialVersionUID = -1258502702214493093L;

  private final AppErrorCode appErrorCode;
  private final String debugInfo;

  public HandledException(String message) {
    super(message);
    this.appErrorCode = null;
    this.debugInfo = "";
  }

  public HandledException(AppErrorCode code, String message) {
    super(message);
    this.appErrorCode = code;
    this.debugInfo = "";
  }

  public String getDebugInfo() {
    return this.debugInfo;
  }

  public AppErrorCode getAppErrorCode() {
    if (this.appErrorCode == null) {
      return AppErrorCode.UnknownError;
    }
    return this.appErrorCode;
  }

  // this should be only called when no api version is available
  private String getApiMessage() {
    if (this.appErrorCode != null) {
      return this.appErrorCode.getMessage();
    } else if (!StringUtils.isEmpty(getMessage())) {
      return getMessage();
    } else {
      return getAppErrorCode().getMessage();
    }
  }

  protected Map<String, Object> getParams() {
    return new HashMap<>();
  }

}
