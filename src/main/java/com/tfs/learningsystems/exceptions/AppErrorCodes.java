package com.tfs.learningsystems.exceptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.httpclient.HttpStatus;

/**
 * When our application return errors to the caller, the same HTTP error code might be caused by
 * different reasons. This is place we can map among our application errors and HTTP errors
 *
 * SC_BAD_REQUEST (400) - Client input has failed input validation and is invalid, i.e. invalid json
 *    format, missing field, value not in range
 * SC_UNAUTHORIZED (401) - The client is unauthenticated or session has expired, must login again to
 *    get a new session
 * SC_FORBIDDEN (403) - The user is not allowed to perform an action
 * SC_NOT_FOUND (404) - The client has requested a resource that does not exist
 * SC_UNPROCESSABLE_ENTITY (422) - Client input has passed input validation and is valid, but the
 *    server will not serve the request due to some other issue (i.e inactive org)
 * SC_INTERNAL_SERVER_ERROR (500) - Server-side error
 */
public class AppErrorCodes {

  private static final Set<String> AllCodes = new HashSet<>();
  private final static Map<String, AppErrorCode> CodeToEnum = new HashMap<>(
      AppErrorCode.values().length, 1.0f);
  private static Set<AppErrorCode> NoLogErrors = new HashSet<>();

  static {
    // this is a skeleton for now. To be populated along the way
    NoLogErrors.add(AppErrorCode.ApiNotSupported);
  }

  public static boolean shouldLogError(AppErrorCode code) {
    return !NoLogErrors.contains(code);
  }

  public enum AppErrorCode {
    // Reserved Errors
    SystemError("AIT_1001", HttpStatus.SC_INTERNAL_SERVER_ERROR),
    UnknownError("AIT_1002", HttpStatus.SC_INTERNAL_SERVER_ERROR),
    UnknownVersion("AIT_1003", HttpStatus.SC_BAD_REQUEST),
    UnsupportedVersion("AIT_1004", HttpStatus.SC_NOT_FOUND),

    InvalidDeveloperKey("AIT_1101", HttpStatus.SC_NOT_FOUND), // 400 404
    InactiveDeveloperKey("AIT_1102", HttpStatus.SC_UNPROCESSABLE_ENTITY),
    ApiNotSupported("AIT_1103", HttpStatus.SC_NOT_FOUND);
    private final String code;
    private final int status;

    AppErrorCode(String code, int status) {
      this.code = code;
      this.status = status;
    }

    public String getCode() {
      return this.code;
    }

    public int getStatus() {
      return this.status;
    }

    // NOTE: some of these error codes require parameters to be passed in to substitute into the message
    public String getMessage() {
      //
      // todo: translate the error code to proper message with localization.
      //
      return this.code;
    }

    public static AppErrorCode of(String code) {
      AppErrorCode result = CodeToEnum.get(code);
      if (result == null) {
        throw new IllegalArgumentException("No AppErrorCode exists for " + code);
      }
      return result;
    }

    /**
     * Make sure all unique error codes and unique message labels are provided.
     */
    static {
      for (AppErrorCode code : AppErrorCode.values()) {
        if (AllCodes.contains(code.getCode())) {
          throw new HandledException(code.getCode() + " already defined!!!");
        }
        AllCodes.add(code.getCode());
        CodeToEnum.put(code.getCode(), code);
      }
    }
  }
}
