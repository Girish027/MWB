/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.FileBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.model.FileEntryDetail;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.file.model.FileStagedImportResponse;
import java.io.InputStream;
import org.quartz.SchedulerException;

public interface FileManager {

  public FileStagedImportResponse importFile(InputStream inputStream, String userName,
      String datatype) throws Exception;

  public FileBO generateUserSelectedColumnsFile(String userId, String token, boolean ignoreFirstRow,
      FileColumnMappingSelectionList columnMappingList) throws ApplicationException;

  public FileBO addFile(InputStream inputStream, String username, String datatype)
      throws Exception;

  public FileEntryDetail renameFile(String oldName, String newName, String username);

  public void deleteFileById(String fileId) throws SchedulerException;

  public FileEntryDetail getFileById(String fileId);

  public FileEntryDetail getFileByNameAndUser(String name, String username);

  public FileEntryDetail getFilesByUser(String username, int startIndex, int count);

  public FileEntryDetail getFiles(int startIndex, int count);
}
