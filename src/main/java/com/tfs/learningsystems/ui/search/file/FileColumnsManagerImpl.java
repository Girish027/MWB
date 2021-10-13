/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.file;

import com.tfs.learningsystems.ui.search.file.model.FileColumnList;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.file.model.dao.FileColumnsDao;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fileColumnsManager")
@Slf4j
public class FileColumnsManagerImpl implements FileColumnsManager {

  @Inject
  @Qualifier("fileColumnsDao")
  private FileColumnsDao columnsDao;

  @Override
  public void addColumnMappings(String userEmail, FileColumnMappingSelectionList columnMappingList) {
    columnsDao.addMappings(userEmail, columnMappingList);
  }

  @Override
  public FileColumnList getColumns() {
    return columnsDao.getColumns();
  }

}
