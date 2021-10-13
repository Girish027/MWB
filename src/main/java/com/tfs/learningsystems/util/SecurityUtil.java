/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.util;

import com.tfs.learningsystems.exceptions.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

@Slf4j
public class SecurityUtil {

  private SecurityUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static String toSHA1(String input) throws ApplicationException {
    String sha1 = "";
    try {
      MessageDigest crypt = MessageDigest.getInstance(Constants.SECURITY_TYPE_SHA);
      crypt.reset();
      crypt.update(input.getBytes(StandardCharsets.UTF_8));
      sha1 = byteToHex(crypt.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new ApplicationException(e);
    }

    return sha1;
  }

  public static String toMD5(String input) throws ApplicationException {
    String md5 = "";
    try {
      MessageDigest md = MessageDigest.getInstance(Constants.SECURITY_TYPE_MD5);
      md.update(input.getBytes());
      md5 = byteToHex(md.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new ApplicationException(e);
    }
    return md5;
  }

  public static Boolean checkMD5Hash(String hash, String key) {
    if (hash.equals(toMD5(key)))
      return Boolean.TRUE;
    return Boolean.FALSE;
  }

  public static String byteToHex(final byte[] hash) {
    String result = null;
    try(Formatter formatter  = new Formatter()) {
      for (byte b : hash) {
        formatter.format("%02x", b);
      }
      result = formatter.toString();
    } catch (Exception e) {
      log.error("Error while byteToHex conversion", e);
    }
    return result;
  }
}
