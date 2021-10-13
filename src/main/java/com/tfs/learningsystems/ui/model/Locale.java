/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

public class Locale {

  public static final String EN_US = "en-US";

  public static final String FR_FR = "fr-FR";

  public static final String DE_DE = "de-DE";

  private static final String[] LOCALES_LIST = new String[]{EN_US, FR_FR, DE_DE};

  public static String[] values() {
    return LOCALES_LIST;
  }
}
