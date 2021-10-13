/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.FileBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.job.DeleteFileJob;
import com.tfs.learningsystems.ui.dao.FileDao;
import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.FileEntryDetail;
import com.tfs.learningsystems.ui.search.file.FileColumnsManager;
import com.tfs.learningsystems.ui.search.file.model.FileColumnList;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelection;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.file.model.FileStagedImportResponse;
import com.tfs.learningsystems.util.CSVFileUtil;
import com.tfs.learningsystems.util.ErrorMessage;
import com.tfs.learningsystems.util.QuartzJobUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import javax.inject.Inject;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BOMInputStream;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fileManagerBean")
@Slf4j
public class FileManagerImpl implements FileManager {

  @Inject
  @Qualifier("fileDaoBean")
  private FileDao fileDao;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Autowired
  @Qualifier("fileColumnsManager")
  private FileColumnsManager fileColumnsManager;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private Scheduler scheduler;

  @Override
  public FileStagedImportResponse importFile(InputStream inputStream, String userName,
      String datatype) throws Exception {

    String repositoryRoot = appConfig.getFileUploadRepositoryRoot();
    FileSystem fileSystem = FileSystems.getDefault();
    Path root = fileSystem.getPath(repositoryRoot, userName);
    int bytesToRead = 4096;
    try(BOMInputStream bOMInputStream = new BOMInputStream(inputStream);
        PushbackInputStream pInputStream = new PushbackInputStream(bOMInputStream, bytesToRead)
    ) {

      CSVFileUtil.validateEncoding(pInputStream, bytesToRead);

      CSVFileUtil.createIfNotExistsRootDirectoy(root);

      String uuid = UUID.randomUUID().toString();
      String rootString = root.toString();
      String extension = ".csv";
      String fileSystemName = uuid + extension;
      Path fileSystemPath = fileSystem.getPath(rootString, fileSystemName);

      CSVFileUtil.writeFile(pInputStream, fileSystemPath.toString());

      List<String> requiredColumns = DataType.requiredFields(datatype);
      CSVFileUtil.validate(fileSystemPath.toString(), requiredColumns);

      log.info("Importing file SessionId:{}---Clientid:{}---Filename:{}",
          ActionContext.getSessionId(), ActionContext.getClientId(), fileSystemPath);

      FileBO fileEntry = new FileBO();
      fileEntry.setFileId(uuid);
      fileEntry.setName(fileSystemName);
      fileEntry.setUser(userName);
      fileEntry.setSystemName(fileSystemName);
      fileEntry.create();

      List<String[]> rows  = listOfRowsByFile(fileSystemPath);

      FileColumnList columnList = fileColumnsManager.getColumns();

      FileStagedImportResponse stagedImportResponse = new FileStagedImportResponse();
      stagedImportResponse.setFileSystemPath(fileSystemPath.toString());
      stagedImportResponse.setToken(uuid);
      stagedImportResponse.setColumns(columnList);
      stagedImportResponse.setPreviewData(rows);

      return stagedImportResponse;

    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        log.error("Failed to close input stream", e);
      }
    }
  }

  @Override
  public FileBO generateUserSelectedColumnsFile(String userEmail,
      String token, boolean ignoreFirstRow,
      FileColumnMappingSelectionList columnMappingList)
      throws ApplicationException {

    for (FileColumnMappingSelection columnMapping : columnMappingList) {
      columnMapping.setUserId(userEmail);
    }
    fileColumnsManager.addColumnMappings(userEmail, columnMappingList);

    String repositoryRoot = appConfig.getFileUploadRepositoryRoot();
    FileSystem fileSystem = FileSystems.getDefault();
    Path root = fileSystem.getPath(repositoryRoot, userEmail);

    String originalFilename = token + ".csv";
    String columnMappedFilename = token + "-columnmapped.csv";
    Path originalFilePath = fileSystem.getPath(root.toString(), originalFilename);
    Path columnMappedFilePath = fileSystem.getPath(root.toString(), columnMappedFilename);

    try (final CSVWriter columnMappedFileWriter = new CSVWriter(
        new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(columnMappedFilePath.toString()),
            StandardCharsets.UTF_8)));
        final CSVReader originalFileReader = new CSVReader(
            new BufferedReader(new InputStreamReader(
                new FileInputStream(
                    originalFilePath.toString()),
                StandardCharsets.UTF_8)))) {
      //Write column Headers
      List<String> headersList = new ArrayList<>();
      for (FileColumnMappingSelection columnMapping : columnMappingList) {
        headersList.add(columnMapping.getColumnName());
      }
      columnMappedFileWriter.writeNext(headersList.toArray(new String[headersList.size()]));

      if (ignoreFirstRow) {
        originalFileReader.readNext();
      }
      String[] nextLine;
      Map<String, String> mapOnTranscriptionAndRutag = new HashMap<>();

      while ((nextLine = originalFileReader.readNext()) != null) {
        List<String> colList = new ArrayList<>();
        String rutag = null;
        String transcription = null;
        for (FileColumnMappingSelection columnMapping : columnMappingList) {
          int index = Integer.parseInt(columnMapping.getColumnIndex());
          if(columnMapping.getColumnName().equals("rutag")
                  || columnMapping.getColumnName().equals("inheritedIntent")) {
            CSVFileUtil.validateIntent(nextLine[index]);
          }
          if(columnMapping.getColumnName().equals("transcription")) {
            transcription = nextLine[index];
          } else if (columnMapping.getColumnName().equals("rutag")) {
            rutag = nextLine[index];
          }
          colList.add(nextLine[index]);
        }
        CSVFileUtil.datasetValidation(mapOnTranscriptionAndRutag, transcription, rutag);
        columnMappedFileWriter.writeNext(colList.toArray(new String[colList.size()]));
      }
    } catch (IOException ioe) {
      log.warn("Failed to read / write - {} - {}", originalFilePath.toString(),
          columnMappedFilePath.toString());
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                  ErrorMessage.FILE_ACCESS_MESSAGE))
          .build(), ioe);
    }

    FileBO fileEntry = new FileBO();
    fileEntry.setFileId(token + "-columnmapped");
    fileEntry.setName(columnMappedFilename);
    fileEntry.setUser(userEmail);
    fileEntry.setSystemName(columnMappedFilename);
    fileEntry.create();

    return fileEntry;
  }

  @Override
  public FileBO addFile(InputStream inputStream, String username, String datatype)
      throws Exception {
    String repositoryRoot = appConfig.getFileUploadRepositoryRoot();
    FileSystem fileSystem = FileSystems.getDefault();
    Path userRoot = fileSystem.getPath(repositoryRoot, username);
    int bytesToRead = 4096;

    // Validate csv file
    try(BOMInputStream bOMInputStream = new BOMInputStream(inputStream);
        PushbackInputStream pInputStream = new PushbackInputStream(bOMInputStream, bytesToRead)
    ) {
      CSVFileUtil.validateEncoding(pInputStream, bytesToRead);

      CSVFileUtil.createIfNotExistsRootDirectoy(userRoot);

      String uuid = UUID.randomUUID().toString();
      String userRootString = userRoot.toString();
      String extension = ".csv";
      String fileSystemName = uuid + extension;
      Path fileSystemPath = fileSystem.getPath(userRootString, fileSystemName);

      CSVFileUtil.writeFile(pInputStream, fileSystemPath.toString());

      List<String> requiredColumns = DataType.requiredFields(datatype);
      CSVFileUtil.validate(fileSystemPath.toString(), requiredColumns);

      FileBO fileEntry = new FileBO();
      fileEntry.setFileId(uuid);
      fileEntry.setName(fileSystemName);
      fileEntry.setUser(username);
      fileEntry.setSystemName(fileSystemName);
      fileEntry.create();

      return fileEntry;

    } catch (IOException ioe) {
      String message = "Error validating file: " + ioe.getMessage();
      log.error(message, ioe);
      throw new ApplicationException(message);
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        log.error("Failed to close input stream", e);
      }
    }
  }

  @Override
  public void deleteFileById(String fileId) throws SchedulerException {

    FileEntryDetail fileEntry = fileDao.getFileById(fileId);
    if (fileEntry != null) {
      String repositoryRoot = appConfig.getFileUploadRepositoryRoot();
      Path systemPath = FileSystems.getDefault().getPath(repositoryRoot, fileEntry.getUser(),
          fileEntry.getSystemName());
      fileDao.deleteFileById(fileId);
      JobDataMap jobDataMap = new JobDataMap();
      jobDataMap.put(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT, 0);
      jobDataMap.put(QuartzJobUtil.KEY_MAX_RETRY_COUNT, 3);
      jobDataMap.put(QuartzJobUtil.KEY_SYSTEM_PATH, systemPath.toString());
      JobDetail jobDetail = JobBuilder.newJob(DeleteFileJob.class).setJobData(jobDataMap)
          .storeDurably(true).build();

      Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).startNow().build();

      scheduler.scheduleJob(jobDetail, trigger);
    }
  }

  @Override
  public FileEntryDetail renameFile(String oldName, String newName, String username) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileEntryDetail getFileById(String fileId) {
    return fileDao.getFileById(fileId);
  }

  @Override
  public FileEntryDetail getFileByNameAndUser(String name, String username) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileEntryDetail getFilesByUser(String username, int startIndex, int count) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileEntryDetail getFiles(int startIndex, int count) {
    // TODO Auto-generated method stub
    return null;
  }

  private List<String[]> listOfRowsByFile(Path fileSystemPath) {
    List<String[]> rows = new ArrayList<>();
    try (final CSVReader reader = new CSVReader(
            new InputStreamReader(new FileInputStream(fileSystemPath.toString()),
                    StandardCharsets.UTF_8))) {

      int count = 0;
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null && count < 11) {
        rows.add(nextLine);
        count += 1;
      }
    } catch (Exception e) {
      log.error("Error reading file - " + fileSystemPath, e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                      ErrorMessage.FILE_ACCESS_MESSAGE))
              .build(), e);
    }
    return rows;
  }

}
