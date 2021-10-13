/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class TaggedTranscriptionObject {

  String hash;
  String id;
  String classificationId;
  String documentType;
  String datasetId;
  long docCount;
}
