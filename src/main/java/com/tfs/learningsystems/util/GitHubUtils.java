package com.tfs.learningsystems.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GitHubUtils {

  private GitHubUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static String getTagForCurrentDate() {
    Date date = new Date(System.currentTimeMillis());
    DateFormat format = new SimpleDateFormat(Constants.CALENDAR_FORMAT_YYYYMMDDHHMMSS);
    format.setTimeZone(TimeZone.getTimeZone(Constants.UTC));
    return format.format(date);
  }

}
