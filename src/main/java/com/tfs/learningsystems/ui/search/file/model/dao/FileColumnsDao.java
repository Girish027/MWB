/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.file.model.dao;

import com.tfs.learningsystems.ui.search.file.model.FileColumnList;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import java.util.List;

public interface FileColumnsDao {

  public List<String> getColumnNames();

  public FileColumnList getColumns();

  public void addMappings(String userEmail, FileColumnMappingSelectionList columnMappings);
}
