/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.file.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import lombok.Data;

@Data
@JsonSerialize
public class FileStagedImportResponse {

  String token;
  String fileSystemPath;
  List<String[]> previewData;
  FileColumnList columns;
}
