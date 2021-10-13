/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import lombok.Data;

@Data
@JsonSerialize
public class TaggingGuideImportStats {

  String id;
  String projectId;
  String importedBy;
  long importedAt;
  int validTagCount;
  List<String> missingTags;
  List<String> invalidTags;
}