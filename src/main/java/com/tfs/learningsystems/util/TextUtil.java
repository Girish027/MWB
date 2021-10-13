/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.util;

import com.ibm.icu.text.Normalizer2;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

  private TextUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static final Pattern intentPattern = Pattern.compile("(\\w+)-(\\w+)",
      Pattern.UNICODE_CHARACTER_CLASS);

  public static String toCanonical(String input) {
    String normalizedText = null;

    if (input != null) {
      Normalizer2 normalizer = Normalizer2.getNFKCCasefoldInstance();
      normalizedText = normalizer.normalize(input);
    }

    return normalizedText;
  }

  public static boolean isIntentValid(String intent) {
    if (intent == null || intent.trim().isEmpty() || intent.length() > 50) {
      return false;
    }

    Matcher matcher = intentPattern.matcher(intent);
    return matcher.matches();
  }


  public static String removeNonBMPCharacters(final String input) {
    StringBuilder strBuilder = new StringBuilder();
    input.codePoints().forEach(i -> {
      if (Character.isSupplementaryCodePoint(i)) {
        strBuilder.append("?");
      } else {
        strBuilder.append(Character.toChars(i));
      }
    });
    return strBuilder.toString();
  }


}
