/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.ui.model.FileEntryDetail;

public interface FileDao {

  public void deleteFileById(String fileId);

  public FileEntryDetail getFileById(String fileId);
}
