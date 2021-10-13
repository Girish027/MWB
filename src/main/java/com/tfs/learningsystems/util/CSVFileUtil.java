/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.exceptions.EmptyCSVFileException;
import com.tfs.learningsystems.exceptions.InvalidCSVFileEncodingException;
import com.tfs.learningsystems.exceptions.InvalidCSVFileException;
import com.tfs.learningsystems.ui.model.DatasetIntentDetail;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelection;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

@Slf4j
public class CSVFileUtil {

  @Data
  @JsonSerialize
  public static class CSVFileInfo {

    private String[] columns;
    private long validRowCount;
  }

  private CSVFileUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static CSVFileInfo validateAndGetColumns(String csvPath) throws ApplicationException {
    int rowCount = 0;
    String[] columns = null;
    try (CSVReader reader = new CSVReader(
        new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8),
        CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,
        CSVParser.NULL_CHARACTER)) {

      String[] nextLine = null;
      while ((nextLine = reader.readNext()) != null) {
        if (rowCount == 0) {
          columns = nextLine;
        }
        boolean validRow =
            Arrays.stream(nextLine).anyMatch(c -> c != null && !Constants.BLANK_STRING.equals(c));
        if (validRow) {
          rowCount++;
        }
      }
      if (columns == null || columns.length == 0) {
        throw new EmptyCSVFileException(ErrorMessage.EMPTY_CSV_FILE);
      }

    } catch (FileNotFoundException fnfe) {
      throw new ApplicationException("File '" + csvPath + "' not found", fnfe);
    } catch (IOException ioe) {
      throw new ApplicationException(ioe);
    }

    CSVFileInfo csvFileInfo = new CSVFileInfo();
    csvFileInfo.setColumns(columns);
    csvFileInfo.setValidRowCount(rowCount - 1);
    log.debug("Number of records found in file '{}' : {}", csvPath, csvFileInfo.getValidRowCount());
    return csvFileInfo;
  }

  public static void validate(String csvPath) throws ApplicationException {
    try (CSVReader reader = new CSVReader(
        new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8),
        CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,
        CSVParser.NULL_CHARACTER)) {
      String[] columns = reader.readNext();
      if (columns == null || columns.length == 0) {
        throw new EmptyCSVFileException(ErrorMessage.EMPTY_CSV_FILE);
      }
    } catch (FileNotFoundException fnfe) {
      throw new ApplicationException("File '" + csvPath + "' not found", fnfe);
    } catch (IOException ioe) {
      throw new ApplicationException(ioe);
    }
  }

  public static TranscriptionDocumentDetail resolveConflict(TranscriptionDocumentDetail oldDoc, TranscriptionDocumentDetail newDoc,
                                     String oldDatasetSource, String newDatasetSource) {

    String oldRuTag = oldDoc.getRutag();
    Integer oldDatasetId = oldDoc.getDatasetIds().iterator().next();
    DatasetIntentDetail oldDatasetIntentDetail = new DatasetIntentDetail(oldRuTag, oldDatasetSource, oldDatasetId);

    String newRuTag = newDoc.getRutag();
    Integer newDatasetId = newDoc.getDatasetIds().iterator().next();
    DatasetIntentDetail newDatasetIntentDetail = new DatasetIntentDetail(newRuTag, newDatasetSource, newDatasetId);

    if(!oldDatasetIntentDetail.equals(newDatasetIntentDetail)) {
      return resolvedConflictForIntent(oldDoc, newDoc, oldDatasetSource, newDatasetSource);
    }
    return oldDoc;
  }

  public static void createIfNotExistsRootDirectoy(Path root) throws IOException {
    try {
      if (!root.toFile().exists()) {
        if(System.getProperty("os.name").contains("Windows")){
          Files.createDirectory(root);
        }else {
          Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr-----");
          FileAttribute<Set<PosixFilePermission>> fileAttributes =
                  PosixFilePermissions.asFileAttribute(permissions);
          Files.createDirectory(root, fileAttributes);
        }
      }
    } catch (IOException ioe) {
      String message = "Error creating the project root directory: " + root.toString();
      log.error("Error creating the user root directory:" + root.toString(), ioe);
      throw new IOException(message);
    }
  }

  private static TranscriptionDocumentDetail resolvedConflictForIntent(TranscriptionDocumentDetail oldDoc, TranscriptionDocumentDetail newDoc,
                                                String oldDatasetSource,
                                                String newDatasetSource) {
    if(oldDoc.getRutag() != null && !oldDoc.getRutag().trim().equals(Constants.BLANK_STRING)
            && newDoc.getRutag() != null && !newDoc.getRutag().trim().equals(Constants.BLANK_STRING)) {
      if(oldDatasetSource != null && newDatasetSource != null && !oldDatasetSource.equals(newDatasetSource)) {
        return resolveBySourceOrder(oldDoc, newDoc, oldDatasetSource, newDatasetSource);
      } else {
        return resolveByDatasetId(oldDoc, newDoc);
      }
    }
    return checkEmptyOrNull(oldDoc, newDoc);
  }

  private static TranscriptionDocumentDetail resolveByDatasetId(TranscriptionDocumentDetail oldDoc, TranscriptionDocumentDetail newDoc) {

    Integer oldDatasetId = oldDoc.getDatasetIds().iterator().next();
    String oldIntent = oldDoc.getRutag();

    Integer newDatasetId = newDoc.getDatasetIds().iterator().next();
    String newIntent = newDoc.getRutag();

    if(oldDatasetId < newDatasetId) {
      return newDoc;
    } else if(oldDatasetId.equals(newDatasetId) && !oldIntent.equals(newIntent)) {
      log.error(ErrorMessage.DATASET_VALIDATION);
      Error error = ErrorUtil.createError(Response.Status.BAD_REQUEST.getStatusCode(),
              ErrorCode.CONFLICTING_DATASET,
              ErrorMessage.DATASET_VALIDATION);
      throw new BadRequestException(
              Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }
    return oldDoc;
  }

  private static TranscriptionDocumentDetail resolveBySourceOrder(TranscriptionDocumentDetail oldDoc,
                                                                  TranscriptionDocumentDetail newDoc,
                                                                  String oldDatasetSource, String newDatasetSource) {
    if(oldDatasetSource.equals(DatasetBO.Source.A.toString())) {
      if(newDatasetSource.equals(DatasetBO.Source.E.toString())) {
        return newDoc;
      }
    } else if(oldDatasetSource.equals(DatasetBO.Source.I.toString())) {
      return newDoc;
    }
    return oldDoc;
  }

  private static TranscriptionDocumentDetail checkEmptyOrNull(TranscriptionDocumentDetail doc1,
                                                              TranscriptionDocumentDetail doc2) {
    if(doc1.getRutag() != null && !doc1.getRutag().trim().equals(Constants.BLANK_STRING)) {
      return doc1;
    }
    return doc2;
  }

  public static void datasetValidation(Map<String, String> utteranceIntentMap,
                                       String utterance, String ruTag) {
    if(utterance != null && ruTag != null && !utterance.equals(Constants.BLANK_STRING) && !ruTag.equals(Constants.BLANK_STRING)) {
      if (utteranceIntentMap.containsKey(utterance) && !utteranceIntentMap.get(utterance).equals(ruTag)) {
        log.error(ErrorMessage.DATASET_VALIDATION);
        Error error = ErrorUtil.createError(Response.Status.BAD_REQUEST.getStatusCode(),
                ErrorCode.CONFLICTING_DATASET,
                "Dataset has conflicting intent for utterance '"+ utterance + "'. Please upload non-conflicting data.");
        throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST).entity(error).build());
      }
      utteranceIntentMap.put(utterance, ruTag);
    }
  }

  public static void validateIntent(String intent) throws ApplicationException {
    if (intent == null || intent.length() > 50) {
      Error error = ErrorUtil.createError(Response.Status.BAD_REQUEST.getStatusCode(),
              ErrorCode.INVALID_INTENT_LENGTH,
              ErrorMessage.INTENT_LENGTH_ERROR);
      throw new BadRequestException(
              Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }
  }

  public static boolean validateRUCount(String preIntent, String currIntent) throws ApplicationException {
    return preIntent != null && !preIntent.equals(currIntent);
  }

  public static void validate(String csvPath, Long numOfRecords) throws ApplicationException {
    try (CSVReader reader = new CSVReader(
        new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8),
        CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,
        CSVParser.NULL_CHARACTER)) {

      List<String[]> rows = reader.readAll();
      if (rows == null || rows.isEmpty()) {
        throw new EmptyCSVFileException(ErrorMessage.EMPTY_CSV_FILE);
      }

      int rowCount = 0;
      for (String[] nextLine : rows) {
        boolean validRow =
            Arrays.stream(nextLine).anyMatch(c -> c != null && !Constants.BLANK_STRING.equals(c));
        if (validRow) {
          rowCount++;
        }
      }

      // don't count header
      int size = rowCount - 1;
      log.debug("Number of records found in file '{}' : {}", csvPath, size);
      if (size != numOfRecords) {
        throw new InvalidCSVFileException(
            "Incorrect number of rows, expected: " + numOfRecords + ", found: " + size);
      }

    } catch (FileNotFoundException fnfe) {
      throw new ApplicationException("File '" + csvPath + "' not found", fnfe);
    } catch (IOException ioe) {
      throw new ApplicationException(ioe);
    }
  }

  public static void validate(String csvPath, List<String> requiredColumns)
      throws Exception {
    try (CSVReader reader = new CSVReader(
        new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8),
        CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,
        CSVParser.NULL_CHARACTER)) {

      String[] columns = reader.readNext();
      if (columns == null || columns.length == 0) {
        throw new EmptyCSVFileException(ErrorMessage.EMPTY_CSV_FILE);
      } else {
        Set<String> nonExistingColumns = new HashSet<>();
        Set<String> fileColumnSet = new HashSet<>(Arrays.asList(columns));
        if (requiredColumns != null) {
          for (String column : requiredColumns) {
            if (!fileColumnSet.contains(column)) {
              nonExistingColumns.add(column);
            }
          }
        }

        if (!nonExistingColumns.isEmpty()) {
          String message = String.format(
              "Data file columns [%s] missing [%s] columns from the required columns [%s]",
              String.join(",", columns), String.join(",", nonExistingColumns),
              String.join(",", requiredColumns));
          log.error(message);
          throw new BadRequestException(message);
        }
      }

    } catch (FileNotFoundException e) {
      log.error("File '" + csvPath + "' is not found", e);
      throw new FileNotFoundException("File '" + csvPath + "' is not found");
    }
  }

  /**
   * Read first 4k bytes and validate input stream is of proper UTF-8 encoding. testing encoding
   * isn't 100% accurate so can have false positives.
   */
  public static void validateEncoding(PushbackInputStream inputStream, int bytesToRead)
          throws Exception {
    try {
      byte[] headerBytes = new byte[bytesToRead];
      int read = inputStream.read(headerBytes);
      if (read > -1) {
        Charset[] charsets = new Charset[]{UTF_8};
        EnsureEncoding ensureEncoding = new EnsureEncoding(charsets);
        ensureEncoding.decode(headerBytes);
        inputStream.unread(headerBytes, 0, read);
      }
    } catch (IllegalStateException e) {
      log.error(ErrorMessage.NON_UTF8_ERROR_MESSAGE, e);
      throw new InvalidCSVFileEncodingException(ErrorMessage.NON_UTF8_ERROR_MESSAGE, e);
    } catch (Exception e) {
      log.error(ErrorMessage.VALIDATE_ENCODING_ERROR, e);
      throw new Exception(ErrorMessage.VALIDATE_ENCODING_ERROR, e);
    }
  }

  public static void writeFile(InputStream inputStream, String destPath) throws IOException {
    log.debug("Writing to file {}", destPath);
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(inputStream,
            StandardCharsets.UTF_8));
        Writer writer = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(destPath),
                StandardCharsets.UTF_8));
        final CSVReader csvReader = new CSVReader(reader,
            CSVParser.DEFAULT_SEPARATOR,
            CSVParser.DEFAULT_QUOTE_CHARACTER,
            CSVParser.NULL_CHARACTER);
        final CSVWriter csvWriter = new CSVWriter(writer)) {

      String[] nextLine;
      while ((nextLine = csvReader.readNext()) != null) {
        // Don't write invalid blank lines
        boolean validRow =
            Arrays.stream(nextLine).anyMatch(c -> c != null && !Constants.BLANK_STRING.equals(c));
        if (validRow) {
          String[] sanitizedLine = Arrays.stream(nextLine)
              .map(c -> c.replaceAll("\uFEFF", Constants.BLANK_STRING))
              .map(c -> c.replace("\\", " "))
              .collect(Collectors.toList())
              .toArray(new String[0]);
          csvWriter.writeNext(sanitizedLine);
        }
      }
    } catch (IOException e) {
      log.error(ErrorMessage.WRITING_FILE_ERROR, e);
      throw new IOException(ErrorMessage.WRITING_FILE_ERROR);
    }
  }

  public static FileColumnMappingSelectionList getDefaultColumnMappings() {

    FileColumnMappingSelectionList result = new FileColumnMappingSelectionList();
    FileColumnMappingSelection column = new FileColumnMappingSelection();
    column.setId("1");
    column.setColumnName("rutag");
    column.setDisplayName("Rollup Intent");
    column.setColumnIndex("0");
    result.add(column);
    column = new FileColumnMappingSelection();
    column.setId("7");
    column.setColumnName("transcription");
    column.setDisplayName("Transcription");
    column.setColumnIndex("1");
    result.add(column);
    return result;
  }
}
