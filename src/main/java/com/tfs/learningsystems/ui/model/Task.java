/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import java.util.HashMap;

public class Task {

  public enum Name {
    CATEGORIZE("CATEGORIZE"),
    INDEX("INDEX");

    private String value;

    Name(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  private static final HashMap<Name, String> taskDescription = new HashMap<Name, String>();

  static {
    taskDescription.put(Name.CATEGORIZE, "Clean and Catagorize data");
    taskDescription.put(Name.INDEX, "Index data into ElasticSearch");
  }

  public static String description(Name task) {
    if (taskDescription.containsKey(task)) {
      return taskDescription.get(task);
    }
    return null;
  }

  private static final HashMap<Name, Name> taskSequence = new HashMap<Name, Name>();

  static {
    taskSequence.put(Name.CATEGORIZE, Name.INDEX);
  }

  public static Task.Name getNextTask(Name task) {
    if (taskSequence.containsKey(task)) {
      return taskSequence.get(task);
    }
    return null;
  }
}
