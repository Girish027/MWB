package com.tfs.learningsystems.util;

import com.tfs.learningsystems.exceptions.ApplicationException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class IDCreatorUtil {

  private static final String NUMERIC_STRING = "0123456789";
  private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz0123456789";
  private static final String ALPHA_STRING = "abcdefghijklmnopqrstuvwxyz";

  private static String generateString(String input, String algorithm, int maxlength)
      throws NoSuchAlgorithmException {
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
    byte[] bytes = messageDigest.digest(input.getBytes());
    BigInteger integer = new BigInteger(1, bytes);
    String result = integer.toString(16);
    while (result.length() > maxlength) {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }


  public static String getHash256(String text) {
    try {
      return org.apache.commons.codec.digest.DigestUtils.sha256(text).toString();


    } catch (Exception ex) {
      log.warn("[IDCreatorUtil] : Issue while creating ID");
      return "";
    }
  }


  public String createDocumentId1(int length) {

    return this.randomAlphaNumeric(length);
  }

  public String createIntentRutagId(String intent, String rutag, int length) {

    StringBuilder idBuilder = new StringBuilder();
    try {
      if (!StringUtils.isEmpty(intent)) {
        idBuilder.append(generateString(intent, "SHA-1", 10));
        //  idBuilder.append(getHash256(intent) );

      }
      if (!StringUtils.isEmpty(rutag)) {
        idBuilder.append(generateString(rutag, "SHA-1", 10));
        //  idBuilder.append(getHash256(rutag) );
      }

    } catch (NoSuchAlgorithmException e) {
      String message = String.format(
          "Exception while creating Id %s", e.getMessage());
      log.error("Id creation Error ", message);
      throw new ApplicationException(message, e);
    }

    int idLenght = length - idBuilder.length();
    idBuilder.append(randomAlphaNumeric(idLenght > 0 ? idLenght : 0));

    return idBuilder.toString();

  }

  public String randomAlphaNumeric(int count) {

    StringBuilder builder = new StringBuilder();
    while (count-- != 0) {
      int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
      builder.append(ALPHA_NUMERIC_STRING.charAt(character));
    }
    return builder.append(getCurrentDateTimeMS()).toString();
  }

  public String randomText(int count) {

    StringBuilder builder = new StringBuilder();
    while (count-- != 0) {
      int character = (int) (Math.random() * ALPHA_STRING.length());
      builder.append(ALPHA_STRING.charAt(character));
    }
    return builder.toString();
  }

  public String getCurrentDateTimeMS() {

    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
    String datetime = ft.format(dNow);
    return datetime;
  }

  public Integer randomNumber(int count) {

    StringBuilder builder = new StringBuilder();
    while (count-- != 0) {
      int character = (int) (Math.random() * NUMERIC_STRING.length());
      builder.append(NUMERIC_STRING.charAt(character));
    }
    return Integer.parseInt(builder.toString());
  }

  public String randomNumberStr(int count) {

    StringBuilder builder = new StringBuilder();
    while (count-- != 0) {
      int character = (int) (Math.random() * NUMERIC_STRING.length());
      builder.append(NUMERIC_STRING.charAt(character));
    }
    return builder.toString();
  }

}
