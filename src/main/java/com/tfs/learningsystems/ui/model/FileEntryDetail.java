/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * FileEntryDetail
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FileEntryDetail extends FileEntry {

  private String id;
  private String uri;
  private Long createdAt;
  private Long modifiedAt;
}

