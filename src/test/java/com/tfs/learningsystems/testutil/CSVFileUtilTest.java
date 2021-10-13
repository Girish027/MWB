/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.testutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.opencsv.CSVWriter;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.exceptions.EmptyCSVFileException;
import com.tfs.learningsystems.exceptions.InvalidCSVFileEncodingException;
import com.tfs.learningsystems.exceptions.InvalidCSVFileException;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelection;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.util.CSVFileUtil;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.apache.commons.io.input.BOMInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;

import javax.ws.rs.BadRequestException;

public class CSVFileUtilTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test(expected = EmptyCSVFileException.class)
  public void testValidateAndGetColumnsEmptyCSVFileException()
      throws IOException, ApplicationException {
    File file = null;
    try {
      file = File.createTempFile("ebay-empty", ".csv");
      CSVWriter writer = null;
      try {
        writer = new CSVWriter(new FileWriter(file), ',');
        writer.flush();
        CSVFileUtil
            .validateAndGetColumns(FileSystems.getDefault().getPath(file.toString()).toString());
      } finally {
        if (writer != null) {
          writer.close();
        }
      }
    } finally {
      if (file != null) {
        file.deleteOnExit();
      }
    }
  }

  @Test
  public void testValidateAndGetColumnsFileNotFoundException()
      throws IOException, ApplicationException {
    String fakeFileName = "no-such-file.csv";
    expectedException.expect(ApplicationException.class);
    expectedException.expectMessage("File 'no-such-file.csv' not found");
    CSVFileUtil.validateAndGetColumns(FileSystems.getDefault().getPath(fakeFileName).toString());
  }

  @Test (expected = EmptyCSVFileException.class)
  public void testValidateWithRecordsCheckAndEmptyCSV() throws IOException, ApplicationException {
    String fileName1 = "workshop_with_no_data.csv";
    long validRowCount = 1L;
    CSVFileUtil.validate(new ClassPathResource(fileName1).getFile().getPath(), validRowCount);
  }

  @Test (expected = FileNotFoundException.class)
  public void testValidateWithRecordsCheckAndFileNotFound() throws IOException, ApplicationException {
    String fileName1 = "workshop_with_no_data1.csv";
    long validRowCount = 1L;
    CSVFileUtil.validate(new ClassPathResource(fileName1).getFile().getPath(), validRowCount);
  }

  @Test
  public void testValidateWithRecordsCheck() throws IOException, ApplicationException {
    String fileName1 = "test-input.csv";
    String fileName2 = "test-input-missing1record.csv";
    long validRowCount = CSVFileUtil.validateAndGetColumns(new ClassPathResource(fileName1).getFile().getPath())
            .getValidRowCount();
    expectedException.expect(InvalidCSVFileException.class);
    expectedException.expectMessage(
            "Incorrect number of rows, expected: " + validRowCount + ", found: " + (validRowCount - 1));
    CSVFileUtil.validate(new ClassPathResource(fileName2).getFile().getPath(), validRowCount);
  }

  @Test
  public void testValidateWithRecordsCheckWEscapeChar() throws IOException, ApplicationException {
    String fileName1 = "test-input.csv";
    String fileName2 = "test-input-escapechar.csv";
    long validRowCount = CSVFileUtil
        .validateAndGetColumns(new ClassPathResource(fileName1).getFile().getPath())
        .getValidRowCount();
    try {
      Path tempFile = Files.createTempFile("unit-test", ".csv");
      tempFile.toFile().deleteOnExit();
      CSVFileUtil.writeFile(new ClassPathResource(fileName2).getInputStream(), tempFile.toString());
      CSVFileUtil.validate(tempFile.toString(), validRowCount);
    } catch (InvalidCSVFileException e) {
      fail(String
          .format("Failed reading correct number of rows from file - %s \nerror : %s", fileName2,
              e.getMessage()));
    }
  }

  @Test (expected = Test.None.class)
  public void testValidateWithRequiredColumn() throws Exception {
    String fileName1 = "test-input.csv";
    String fileName2 = "test-input-missing1record.csv";

    List<String> requiredColumns = new ArrayList<>();
    requiredColumns.add("transcription");
    CSVFileUtil.validate(new ClassPathResource(fileName2).getFile().getPath(), requiredColumns);
  }

  @Test (expected = BadRequestException.class)
  public void testValidateWithRequiredColumnWithBadRequest() throws Exception {
    String fileName2 = "test-input-missing1record.csv";
    List<String> requiredColumns = new ArrayList<>();
    requiredColumns.add("transcription");
    requiredColumns.add("ruIntent");
    CSVFileUtil.validate(new ClassPathResource(fileName2).getFile().getPath(), requiredColumns);

  }

  @Test (expected = EmptyCSVFileException.class)
  public void testValidateWithRequiredColumnWithEmptyCsv() throws Exception {
    String fileName2 = "workshop_with_no_data.csv";
    List<String> requiredColumns = new ArrayList<>();
    requiredColumns.add("transcription");
    CSVFileUtil.validate(new ClassPathResource(fileName2).getFile().getPath(), requiredColumns);
  }

  @Test (expected = FileNotFoundException.class)
  public void testValidateWithRequiredColumnWithFileNotFound() throws Exception {
    String fileName2 = "test-input-missing1record1.csv";
    List<String> requiredColumns = new ArrayList<>();
    requiredColumns.add("transcription");
    CSVFileUtil.validate(new ClassPathResource(fileName2).getFile().getPath(), requiredColumns);
  }

  @Test (expected = Test.None.class)
  public void testValidate() throws Exception {
    String fileName2 = "test-input-missing1record.csv";
    CSVFileUtil.validate(new ClassPathResource(fileName2).getFile().getPath());
  }

  @Test (expected = EmptyCSVFileException.class)
  public void testValidateWithEmptyCsv() throws Exception {
    String fileName2 = "workshop_with_no_data.csv";
    CSVFileUtil.validate(new ClassPathResource(fileName2).getFile().getPath());
  }

  @Test (expected = FileNotFoundException.class)
  public void testValidateWithFileNotFound() throws Exception {
    String fileName2 = "test-input-missing1record1.csv";
    CSVFileUtil.validate(new ClassPathResource(fileName2).getFile().getPath());
  }

  @Test (expected = Test.None.class)
  public void testValidateEncoding() throws Exception {
    String fileName2 = "test-input-missing1record.csv";
    InputStream inputStream = new FileInputStream(new ClassPathResource(fileName2).getFile());
    int bytesToRead = 4096;
    BOMInputStream bOMInputStream = new BOMInputStream(inputStream);
    PushbackInputStream pInputStream = new PushbackInputStream(bOMInputStream, bytesToRead);
    CSVFileUtil.validateEncoding(pInputStream, bytesToRead);
  }

  @Test (expected = InvalidCSVFileEncodingException.class)
  public void testValidateEncodingIso() throws Exception {
    String fileName2 = "test-input-iso-8859-1.csv";
    InputStream inputStream = new FileInputStream(new ClassPathResource(fileName2).getFile());
    int bytesToRead = 4096;
    BOMInputStream bOMInputStream = new BOMInputStream(inputStream);
    PushbackInputStream pInputStream = new PushbackInputStream(bOMInputStream, bytesToRead);
    CSVFileUtil.validateEncoding(pInputStream, bytesToRead);
  }

  @Test
  public void testGetDefaultColumnMappings() throws Exception {
    FileColumnMappingSelectionList fileColumnMappingSelections = new FileColumnMappingSelectionList();
    FileColumnMappingSelection column = new FileColumnMappingSelection();
    column.setId("1");
    column.setColumnName("rutag");
    column.setDisplayName("Rollup Intent");
    column.setColumnIndex("0");
    fileColumnMappingSelections.add(column);
    column = new FileColumnMappingSelection();
    column.setId("7");
    column.setColumnName("transcription");
    column.setDisplayName("Transcription");
    column.setColumnIndex("1");
    fileColumnMappingSelections.add(column);
    FileColumnMappingSelectionList result = CSVFileUtil.getDefaultColumnMappings();
    assertEquals(fileColumnMappingSelections, result);
  }

  @Test (expected = BadRequestException.class)
  public void testValidateIntentForNullIntent()
          throws Exception {
    CSVFileUtil.validateIntent(null);
  }

  @Test (expected = BadRequestException.class)
  public void testValidateIntentForBiggerIntent()
          throws Exception {
    CSVFileUtil.validateIntent("dsdsfdsfdffdffffsfdfFDSFSfsDFSSSDFSDFSFDSFSDFDSFDFDFSDFSFSDF");
  }

  @Test (expected = Test.None.class)
  public void testValidateIntent()
          throws Exception {
    CSVFileUtil.validateIntent("ValidIntent");
  }

  @Test
  public void testValidateRUCountTruthy()
          throws Exception {
    Boolean value = CSVFileUtil.validateRUCount("oldIntent", "newIntent");
    assertEquals(true, value);
  }

  @Test
  public void testValidateRUCountFalse()
          throws Exception {
    Boolean value = CSVFileUtil.validateRUCount("oldIntent", "oldIntent");
    assertEquals(false, value);
  }

  @Test
  public void testValidateRUCountNullPreIntent()
          throws Exception {
    Boolean value = CSVFileUtil.validateRUCount(null, "oldIntent");
    assertEquals(false, value);
  }

  @Test (expected = BadRequestException.class)
  public void testUploadedDatasetValidationFailure() throws Exception {
    Map<String, String> mapOnTranscriptionAndRutag = new HashMap<>();
    String transcription = "cancel my reservation";
    String rutag = "Reservation_Update";
    mapOnTranscriptionAndRutag.put(transcription, rutag);
    CSVFileUtil.datasetValidation(mapOnTranscriptionAndRutag, transcription, rutag + "123");
  }

  @Test (expected = Test.None.class)
  public void testUploadedDatasetValidationSuccess() throws Exception {
    Map<String, String> mapOnTranscriptionAndRutag = new HashMap<>();
    String transcription = "cancel my reservation";
    String rutag = "Reservation_Update";
    mapOnTranscriptionAndRutag.put(transcription, rutag);
    CSVFileUtil.datasetValidation(mapOnTranscriptionAndRutag, transcription+ "123", rutag);
  }

  @Test (expected = Test.None.class)
  public void testUploadedDatasetAddSuccess() throws Exception {
    Map<String, String> mapOnTranscriptionAndRutag = new HashMap<>();
    String transcription = "cancel my reservation";
    String rutag = "Reservation_Update";
    CSVFileUtil.datasetValidation(mapOnTranscriptionAndRutag, transcription, rutag);
  }

  @Test (expected = Test.None.class)
  public void testUploadedDatasetTranscriptionNullSuccess() throws Exception {
    Map<String, String> mapOnTranscriptionAndRutag = new HashMap<>();
    String transcription = "cancel my reservation";
    String rutag = "Reservation_Update";
    mapOnTranscriptionAndRutag.put(transcription, rutag);
    CSVFileUtil.datasetValidation(mapOnTranscriptionAndRutag, null, rutag);
  }

  @Test (expected = Test.None.class)
  public void testUploadedDatasetRutagNullSuccess() throws Exception {
    Map<String, String> mapOnTranscriptionAndRutag = new HashMap<>();
    String transcription = "cancel my reservation";
    String rutag = "Reservation_Update";
    mapOnTranscriptionAndRutag.put(transcription, rutag);
    CSVFileUtil.datasetValidation(mapOnTranscriptionAndRutag, transcription, null);
  }

  @Test (expected = Test.None.class)
  public void testUploadedDatasetEmptyMapTranscriptionNullSuccess() throws Exception {
    Map<String, String> mapOnTranscriptionAndRutag = new HashMap<>();
    String rutag = "Reservation_Update";
    CSVFileUtil.datasetValidation(mapOnTranscriptionAndRutag, null, rutag);
  }

  @Test (expected = Test.None.class)
  public void testUploadedDatasetEmptyMapRutagNullSuccess() throws Exception {
    Map<String, String> mapOnTranscriptionAndRutag = new HashMap<>();
    String transcription = "cancel my reservation";
    CSVFileUtil.datasetValidation(mapOnTranscriptionAndRutag, transcription, null);
  }

  @Test (expected = Test.None.class)
  public void testUploadedDatasetRutagEmptySuccess() throws Exception {
    Map<String, String> mapOnTranscriptionAndRutag = new HashMap<>();
    String transcription = "cancel my reservation";
    String rutag = "Reservation_Update";
    mapOnTranscriptionAndRutag.put(transcription, rutag);
    CSVFileUtil.datasetValidation(mapOnTranscriptionAndRutag, transcription, "");
  }

  @Test
  public void testResolveConflictWithOldDocAsExternalAndNewDocAsInternal() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("Old_Query");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("New_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(13);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.E.toString(), DatasetBO.Source.I.toString());
    assertEquals(oldDoc, doc);
  }

  @Test
  public void testResolveConflictWithOldDocAsInternalAndNewDocAsExternal() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("Old_Query");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("New_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(13);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.I.toString(), DatasetBO.Source.E.toString());
    assertEquals(newDoc, doc);
  }

  @Test
  public void testResolveConflictWithOldDocAsAgentAndNewDocAsExternal() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("Old_Query");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("New_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(13);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.A.toString(), DatasetBO.Source.E.toString());
    assertEquals(newDoc, doc);
  }

  @Test
  public void testResolveConflictWithSourceAsNull() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("Old_Query");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("New_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(13);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, null, null);
    assertEquals(newDoc, doc);
  }

  @Test
  public void testResolveConflictWithSameDocs() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("Old_Query");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("Old_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(12);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.I.toString(), DatasetBO.Source.I.toString());
    assertEquals(oldDoc, doc);
  }

  @Test
  public void testResolveConflictWithSameDocsWithNullSource() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("Old_Query");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("Old_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(12);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, null, null);
    assertEquals(oldDoc, doc);
  }

  @Test (expected = BadRequestException.class)
  public void testResolveConflictWithSameSourceAndDatasetIdsDifferentIntent() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("Old_Query");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("New_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(12);
    newDoc.setDatasetIds(newDatasets);

    CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.E.toString(), DatasetBO.Source.E.toString());
  }

  @Test (expected = BadRequestException.class)
  public void testResolveConflictWithSameDifferentIntent() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("Old_Query");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("New_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(12);
    newDoc.setDatasetIds(newDatasets);

    CSVFileUtil.resolveConflict(oldDoc, newDoc, null, null);
  }

  @Test
  public void testResolveConflictWithSameSourceDifferentIntent() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("Old_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(12);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.I.toString(), DatasetBO.Source.I.toString());
    assertEquals(newDoc, doc);
  }

  @Test
  public void testResolveConflictWithSameSourceEmptyIntent() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("        ");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("Old_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(12);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.I.toString(), DatasetBO.Source.I.toString());
    assertEquals(newDoc, doc);
  }

  @Test
  public void testResolveConflictWithSameSourceFirstIntentNull() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag(null);
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("Old_Query");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(12);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.I.toString(), DatasetBO.Source.I.toString());
    assertEquals(newDoc, doc);
  }

  @Test
  public void testResolveConflictWithSameSourceSecondIntentNull() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("New_Intent");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag(null);
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(12);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.I.toString(), DatasetBO.Source.I.toString());
    assertEquals(oldDoc, doc);
  }

  @Test
  public void testResolveConflictWithSameSourceBothIntentNull() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag(null);
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag(null);
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(14);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.I.toString(), DatasetBO.Source.E.toString());
    assertEquals(newDoc, doc);
  }

  @Test
  public void testResolveConflictWithSameSourceBothBlankNull() throws ApplicationException {
    TranscriptionDocumentDetail oldDoc = new TranscriptionDocumentDetail();
    oldDoc.setRutag("");
    Set<Integer> oldDatasets = new HashSet<>();
    oldDatasets.add(12);
    oldDoc.setDatasetIds(oldDatasets);

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    newDoc.setRutag("");
    Set<Integer> newDatasets = new HashSet<>();
    newDatasets.add(14);
    newDoc.setDatasetIds(newDatasets);

    TranscriptionDocumentDetail doc = CSVFileUtil.resolveConflict(oldDoc, newDoc, DatasetBO.Source.A.toString(), DatasetBO.Source.E.toString());
    assertEquals(newDoc, doc);
  }
}

