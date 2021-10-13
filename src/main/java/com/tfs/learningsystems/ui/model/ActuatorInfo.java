/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ActuatorInfo {

  public final App app;
  public final Git git;
  public final Build build;

  @JsonCreator
  public ActuatorInfo(@JsonProperty("app") App app, @JsonProperty("git") Git git,
      @JsonProperty("build") Build build) {
    this.app = app;
    this.git = git;
    this.build = build;
  }

  public static final class App {

    public final Java java;
    public final String encoding;

    @JsonCreator
    public App(@JsonProperty("java") Java java, @JsonProperty("encoding") String encoding) {
      this.java = java;
      this.encoding = encoding;
    }

    public static final class Java {

      public final String target;
      public final String source;

      @JsonCreator
      public Java(@JsonProperty("target") String target, @JsonProperty("source") String source) {
        this.target = target;
        this.source = source;
      }
    }
  }

  public static final class Git {

    public final Commit commit;
    public final String branch;

    @JsonCreator
    public Git(@JsonProperty("commit") Commit commit, @JsonProperty("branch") String branch) {
      this.commit = commit;
      this.branch = branch;
    }

    public static final class Commit {

      public final long time;
      public final String id;

      @JsonCreator
      public Commit(@JsonProperty("time") long time, @JsonProperty("id") String id) {
        this.time = time;
        this.id = id;
      }
    }
  }

  public static final class Build {

    public final String version;
    public final String artifact;
    public final String name;
    public final String revision;
    public final String group;
    public final long time;
    public final String buildDate;

    @JsonCreator
    public Build(@JsonProperty("version") String version, @JsonProperty("artifact") String artifact,
        @JsonProperty("name") String name, @JsonProperty("revision") String revision,
        @JsonProperty("group") String group, @JsonProperty("time") long time,
        @JsonProperty("buildDate") String buildDate) {
      this.version = version;
      this.artifact = artifact;
      this.name = name;
      this.revision = revision;
      this.group = group;
      this.time = time;
      this.buildDate = buildDate;
    }
  }
}