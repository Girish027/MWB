/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.file;

import com.tfs.learningsystems.ui.search.file.model.FileColumnList;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;

public interface FileColumnsManager {

  public FileColumnList getColumns();

  public void addColumnMappings(String userEmail, FileColumnMappingSelectionList columnMappingList);
}
