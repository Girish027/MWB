/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashSet;
import java.util.Objects;

public class Vertical {

  private static HashSet<String> entries = new HashSet<String>();

  public static void addValue(String newVal) {
    entries.add(newVal);
  }

  public static void reloadValues(String input) {
    entries.clear();
    String[] values = input.split("\\R");
    for (String s : values) {
      addValue(s);
    }
  }

  private String value;


  Vertical(String value) {
    this.value = value;
  }

  public static String[] values() {
    return (String[]) entries.toArray(new String[entries.size()]);
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }


  public static Vertical valueOf(String input) {
    if (input == null || input.isEmpty()) {
      throw new NullPointerException();
    }

//       if(!entries.contains(input)){
//           throw new IllegalArgumentException("Invalid vertical:" + input + " is not listed as an entry in the verticals.csv file.");
//       }

    return new Vertical(input);
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

}

//import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets Vertical
 */
//public enum Vertical {
//  
//  FINANCIAL("FINANCIAL"),
//  
//  HEALTHCARE("HEALTHCARE"),
//  
//  RETAIL("RETAIL"),
//  
//  TECHNOLOGY("TECHNOLOGY"),
//  
//  TELCO("TELCO"),
//  
//  TRAVEL("TRAVEL"),
//  
//  UTILITIES("UTILITIES"),
//  
//  OTHER("OTHER");
//
//  private String value;
//
//  Vertical(String value) {
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


