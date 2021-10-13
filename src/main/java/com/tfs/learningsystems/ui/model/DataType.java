/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataType {

  private static Map<String, List<String>> entries = new HashMap<>();
  private static final String[] DEFAULT_FIELDS = new String[]{};

  public static void addValue(String newVal) {
    entries.put(newVal, Arrays.asList(DEFAULT_FIELDS));
  }

  public static void addValue(String newVal, String[] requiredFields) {
    entries.put(newVal, Arrays.asList(requiredFields));
  }

  public static void reloadValues(String input) {
    entries.clear();
    String[] values = input.split("\\R");
    for (String entry : values) {
      String[] fields = entry.split(",");
      if (fields.length > 1) {
        addValue(fields[0].trim(), fields[1].split(";"));
      } else {
        addValue(fields[0].trim());
      }
    }
  }

  private String value;


  DataType(String value) {
    this.value = value;
  }

  public static String[] values() {
    return (String[]) entries.keySet().toArray(new String[entries.size()]);
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }


  public static DataType valueOf(String input) {
    if (input == null || input.isEmpty()) {
      throw new NullPointerException();
    }

    if (!entries.containsKey(input)) {
      throw new IllegalArgumentException(
          "Invalid datatype:" + input + " is not listed as an entry in the datatypes.csv file.");
    }

    return new DataType(input);
  }

  @JsonValue
  public String getName() {
    return this.toString();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj == null) ? false : (this.toString().equals(obj.toString())) ? true : false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @JsonIgnore
  public static List<String> requiredFields(String datatype) {
    if (entries.containsKey(datatype)) {
      return entries.get(datatype);
    } else {
      return Arrays.asList(DEFAULT_FIELDS);
    }

  }

}

//import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets DataType
 */
//public enum DataType {
//  
//  SPEECH("SPEECH"),
//
//  AIVA("AIVA"),
//
//  OMNICHANNEL("OMNICHANNEL");
//
//  private String value;
//
//  DataType(String value) {
//    this.value = value;
//  }
//
//  @Override
//  public String toString() {
//    return String.valueOf(value);
//  }
//  
//  @JsonValue
//  public String getName() {
//      return this.name();
//  }
//}


