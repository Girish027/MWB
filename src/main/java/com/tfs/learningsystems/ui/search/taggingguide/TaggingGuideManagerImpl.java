/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.db.TaggingGuideImportStatBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.ContentManager;
import com.tfs.learningsystems.ui.ProjectManager;
import com.tfs.learningsystems.ui.ValidationManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.search.taggingguide.model.ClassificationDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappedResponse;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelection;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideStagedImportResponse;
import com.tfs.learningsystems.util.ErrorMessage;
import com.tfs.learningsystems.util.CSVFileUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.IDCreatorUtil;
import com.tfs.learningsystems.util.TextUtil;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Qualifier("taggingGuideManager")
@Slf4j
public class TaggingGuideManagerImpl implements TaggingGuideManager {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Autowired
  @Qualifier("contentManagerBean")
  private ContentManager contentManager;

  @Autowired
  @Qualifier("taggingGuideColumnsManager")
  private TaggingGuideColumnsManager taggingGuideColumnsManager;

  @Autowired
  @Qualifier("taggingGuideImportStatsManager")
  private TaggingGuideImportStatsManager taggingGuideImportStatsManager;


  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;


  @Autowired
  private IDCreatorUtil idCreatorUtil;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;


  private Path getProjectFilePath(String projectId, String fileName) throws IOException {
    FileSystem fileSystem = FileSystems.getDefault();
    String repositoryRoot = appConfig.getFileUploadRepositoryRoot();
    Path projectRoot = fileSystem.getPath(repositoryRoot, projectId);
    return fileSystem.getPath(projectRoot.toString(), fileName);
  }

  private void stageValidIntents(List<String[]> validTags, String filePath)
      throws IOException {
    if (validTags != null) {
      try (final CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
        for (String[] tagFields : validTags) {
          writer.writeNext(tagFields);
        }
      }
    }
  }

  private void stageInvalidIntents(List<String> missingTags, String filePath)
      throws IOException {
    if (missingTags != null) {
      try (final CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
        for (String tag : missingTags) {
          String[] fields = new String[]{tag};
          writer.writeNext(fields);
        }
      }
    }
  }

  private List<String[]> readStagedIntents(Path filePath) throws IOException {
    List<String[]> validTagRows = new ArrayList<>();
    if (Files.exists(filePath, LinkOption.NOFOLLOW_LINKS)) {
      try (final CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filePath.toString()),
              StandardCharsets.UTF_8))) {
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
          validTagRows.add(nextLine);
        }
      }
    } else {
      log.warn("File does not exists {}", filePath.toString());
    }

    return validTagRows;
  }

  @Override
  public TaggingGuideStagedImportResponse saveGuide(String projectId, InputStream inputStream,
      String userEmail) throws ApplicationException {

    validationManager.validateProjectId(projectId);

    List<String[]> rows = new ArrayList<>();
    TaggingGuideStagedImportResponse stagedImportResponse =
        new TaggingGuideStagedImportResponse();

    String fsName = UUID.randomUUID().toString();
    String fileSystemName = fsName + ".csv";

    String repositoryRoot = appConfig.getFileUploadRepositoryRoot();
    FileSystem fileSystem = FileSystems.getDefault();
    Path projectRoot = fileSystem.getPath(repositoryRoot, projectId);

    try {
      if (!projectRoot.toFile().exists()) {
        if (System.getProperty("os.name").contains("Windows")) {
          Files.createDirectory(projectRoot);
        } else {
          Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr-----");
          FileAttribute<Set<PosixFilePermission>> fileAttributes =
              PosixFilePermissions.asFileAttribute(permissions);
          Files.createDirectory(projectRoot, fileAttributes);
        }
      }
    } catch (IOException ioe) {
      String message = "Error creating project root directory: " + projectRoot.toString();
      log.error("Error creating user root directory", ioe);
      throw new ApplicationException(message);
    }

    Path filePath = fileSystem.getPath(projectRoot.toString(), fileSystemName);
    stagedImportResponse.setFileSystemPath(filePath.toString());
    stagedImportResponse.setToken(fsName);

    try {
      CSVFileUtil.writeFile(inputStream, filePath.toString());
    } catch (IOException e) {
      log.error("Error writing file to the directory", e);
      throw new ApplicationException(e);
    }

    try {
      String fileType = Files.probeContentType(filePath);
      log.info("The file type of the uploaded file {} is {}", filePath.toString(),
          fileType);
    } catch (IOException e) {
      String message = "Error probing the mime type of the file ";
      log.error(message, e);
      throw new ApplicationException(message);
    }

    try (final CSVReader reader =
        new CSVReader(new InputStreamReader(new FileInputStream(filePath.toString()),
                StandardCharsets.UTF_8))) {

      int count = 0;
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null && count < 11) {
        rows.add(nextLine);
        count += 1;
      }
    } catch (Exception e) {
      log.error("", e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                  ErrorMessage.FILE_ACCESS_MESSAGE))
          .build(), e);
    }

    TaggingGuideColumnList columnList = taggingGuideColumnsManager.getColumns();
    stagedImportResponse.setColumns(columnList);
    stagedImportResponse.setPreviewData(rows);

    return stagedImportResponse;
  }

  @Override
  public TaggingGuideColumnMappedResponse addColumnMappingsAndGetStats(String projectId,
      String userEmail, String token, boolean ignoreFirstRow,
      TaggingGuideColumnMappingSelectionList columnMappingList)
      throws ApplicationException {

    for (TaggingGuideColumnMappingSelection columnMapping : columnMappingList) {
      columnMapping.setUserId(userEmail);
    }

    taggingGuideColumnsManager.addColumnMappings(projectId, columnMappingList);

    String filename = token + ".csv";
    Path filePath;
    try {
      filePath = this.getProjectFilePath(projectId, filename);
    } catch (IOException ioe) {
      log.error("Failed to add mapping and get states - ", ioe);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                  ErrorMessage.FILE_ACCESS_MESSAGE))
          .build(), ioe);
    }

    int index = 0;
    for (TaggingGuideColumnMappingSelection column : columnMappingList) {
      if ("intent".equals(column.getColumnName())) {
        index = Integer.valueOf(column.getColumnIndex());
      }
    }
    List<String> danglingIntents = new ArrayList<>();
    List<String[]> validIntents = new ArrayList<>();
    List<String> invalidIntents = new ArrayList<>();

    TaggingGuideColumnMappedResponse columnMappedResponse =
        new TaggingGuideColumnMappedResponse();
    columnMappedResponse.setToken(token);
    columnMappedResponse.setFileSystemPath(filePath.toString());

    String validIntentsStagingFileName =
        String.format("%s_%s", token, Constants.TAGGING_GUIDE_VALID_TAGS_FNAME_SUFFIX);
    String invalidIntentsStagingFileName =
        String.format("%s_%s", token, Constants.TAGGING_GUIDE_INVALID_TAGS_FNAME_SUFFIX);
    String missingIntentsStagingFileName =
        String.format("%s_%s", token, Constants.TAGGING_GUIDE_MISSING_TAGS_FNAME_SUFFIX);

    try (final CSVReader reader =
        new CSVReader(new InputStreamReader(new FileInputStream(filePath.toString()),
                StandardCharsets.UTF_8))) {

      int count = 0;
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        if (ignoreFirstRow && count == 0) {
          count += 1;
          continue;
        }

        String intent = nextLine[index];
        if (TextUtil.isIntentValid(intent)) {
          validIntents.add(nextLine);
        } else if (intent != null && !"".equals(intent)) {
          invalidIntents.add(intent);
        }

        count += 1;
      }
    } catch (Exception e) {
      log.error("Failed to add mapping and get stats, reading file  - " + filePath.toString(), e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                  ErrorMessage.FILE_ACCESS_MESSAGE))
          .build(), e);
    }

    String clientId = null;
    Set<String> systemIntents =
        contentManager.getProjectIntents(clientId, projectId);

    final int intentIndex = index;
    if (!systemIntents.isEmpty()) {
      Set<String> newIntents = new HashSet<>(
          validIntents.stream().map(tokens -> tokens[intentIndex]).collect(Collectors.toList()));
      for (String intent : systemIntents) {
        if (!newIntents.contains(intent)) {
          danglingIntents.add(intent);
        }
      }
    }

    if (!danglingIntents.isEmpty()) {
      columnMappedResponse.setMissingTags(danglingIntents);
      try {
        Path missingIntentsStagingFilePath =
            this.getProjectFilePath(projectId, missingIntentsStagingFileName);
        this.stageInvalidIntents(danglingIntents,
            missingIntentsStagingFilePath.toString());
      } catch (IOException e) {
        log.error("Failed to add mapping and get stats, invalid dangling intents", e);
        throw new ServerErrorException(Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                null, "Could not save missing intents from the tagging guide"))
            .build(), e);
      }
    }

    if (!validIntents.isEmpty() || !danglingIntents.isEmpty()) {
      try {
        Path validIntentsStagingFilePath =
            this.getProjectFilePath(projectId, validIntentsStagingFileName);
        this.stageValidIntents(validIntents,
            validIntentsStagingFilePath.toString());
      } catch (IOException e) {
        log.error("Failed to add mapping and get stats, valid intents ", e);
        throw new ServerErrorException(Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                null, "Could not stage valid intents from the tagging guide"))
            .build(), e);
      }
      columnMappedResponse.setValidTagCount(validIntents.size());
    }

    if (!invalidIntents.isEmpty()) {
      try {
        Path invalidIntentsStagingFilePath =
            this.getProjectFilePath(projectId, invalidIntentsStagingFileName);
        this.stageInvalidIntents(invalidIntents,
            invalidIntentsStagingFilePath.toString());
      } catch (IOException e) {
        log.error("Failed to add mapping and get stats, invalid intents ", e);
        throw new ServerErrorException(Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                null, "Could not save invalid intents from the tagging guide"))
            .build(), e);
      }
    }
    columnMappedResponse.setInvalidTags(invalidIntents);

    return columnMappedResponse;
  }

  @Override
  public TaggingGuideImportStatBO commitImportGuide(String projectId, String userEmail,
      String token) {
    String validIntentsStagingFileName =
        String.format("%s_%s", token, Constants.TAGGING_GUIDE_VALID_TAGS_FNAME_SUFFIX);
    String invalidIntentsStagingFileName =
        String.format("%s_%s", token, Constants.TAGGING_GUIDE_INVALID_TAGS_FNAME_SUFFIX);
    String missingIntentsStagingFileName =
        String.format("%s_%s", token, Constants.TAGGING_GUIDE_MISSING_TAGS_FNAME_SUFFIX);

    TaggingGuideImportStatBO importStats = new TaggingGuideImportStatBO();
    String clientId = null;

    try {
      List<ClassificationDocument> validTags = new ArrayList<>();
      Path validIntentsStagingFilePath =
          this.getProjectFilePath(projectId, validIntentsStagingFileName);
      Path invalidIntentsStagingFilePath =
          this.getProjectFilePath(projectId, invalidIntentsStagingFileName);
      Path missingIntentsStagingFilePath =
          this.getProjectFilePath(projectId, missingIntentsStagingFileName);
      List<String[]> validIntentsRaw = this.readStagedIntents(validIntentsStagingFilePath);
      List<String[]> invalidIntentsRaw =
          this.readStagedIntents(invalidIntentsStagingFilePath);
      List<String[]> missingIntentsRaw =
          this.readStagedIntents(missingIntentsStagingFilePath);

      List<String> invalidIntents = invalidIntentsRaw.stream().map(tokens -> tokens[0])
          .collect(Collectors.toList());
      List<String> missingIntents = missingIntentsRaw.stream().map(tokens -> tokens[0])
          .collect(Collectors.toList());
      importStats.setInvalidTags(invalidIntents);
      importStats.setMissingTags(missingIntents);
      importStats.setValidTagCount(validIntentsRaw.size());
      importStats.setProjectId(Integer.parseInt(projectId));
      String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
      String classfificationDataLabel = elasticSearchProps.getClassificationDataLabel();
      String intentLabel = elasticSearchProps.getTagLabel();
      String rutagLabel = elasticSearchProps.getTaggingGuideRUTagLabel();

      String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
      String clientIdLabel = elasticSearchProps.getClientIdLabel();
      String projectIdLabel = elasticSearchProps.getProjectIdLabel();

      TaggingGuideColumnMappingSelectionList columnMappings = taggingGuideColumnsManager
          .getColumnMappings(userEmail, projectId);

      String rutag = null;

      String intent = null;

      ArrayList<String> ruTagDocs = new ArrayList<>();
      ArrayList<String> intentDocs = new ArrayList<>();

      loadClassificationIds(clientId, projectId,
          intentDocs, ruTagDocs);
      for (String[] intentTokens : validIntentsRaw) {
        BeanWrapper tagRowWrapper = new BeanWrapperImpl(new ClassificationDocument());
        intent = null;
        rutag = null;
        for (TaggingGuideColumnMappingSelection mapping : columnMappings) {
          int index = Integer.parseInt(mapping.getColumnIndex());
          if (mapping.getColumnName().equalsIgnoreCase(rutagLabel)) {
            rutag = intentTokens[index].trim();
            tagRowWrapper.setPropertyValue(classfificationDataLabel,
                TextUtil.removeNonBMPCharacters(rutag));
            continue;
          }
          if (mapping.getColumnName().equalsIgnoreCase(intentLabel)) {
            intent = intentTokens[index].trim();
            tagRowWrapper.setPropertyValue(granularIntentLabel,
                TextUtil.removeNonBMPCharacters(intent));
            continue;
          }

          tagRowWrapper.setPropertyValue(mapping.getColumnName(), intentTokens[index].trim());
        }
        tagRowWrapper.setPropertyValue(classificationIdLabel,
            idCreatorUtil.createIntentRutagId(intent, rutag, 25));
        tagRowWrapper.setPropertyValue(clientIdLabel, clientId == null ? null : clientId);
        tagRowWrapper.setPropertyValue(projectIdLabel, projectId == null ? null : projectId);

        if (!StringUtils.isEmpty(intent) && !intentDocs.contains(intent)) {
          ClassificationDocument tagRow = (ClassificationDocument) tagRowWrapper
              .getWrappedInstance();
          validTags.add(tagRow);
        } else if (StringUtils.isEmpty(intent) && !StringUtils.isEmpty(rutag) && !ruTagDocs
            .contains(rutag)) {
          ClassificationDocument tagRow = (ClassificationDocument) tagRowWrapper.getWrappedInstance();
          validTags.add(tagRow);
        }
      }

      log.debug("Indexing total {} valid tags, user: {}", validTags.size());
      contentManager.indexClassification(null, projectId, validTags, missingIntents);

      long currentMillis = java.time.Instant.now().toEpochMilli();
      importStats.setImportedBy(userEmail);
      importStats.setImportedAt(currentMillis);
      if (!missingIntents.isEmpty()) {

        contentManager.deleteIntentsByIntentListForProject(null, projectId, userEmail,
            missingIntents);
      }
      importStats.create();
    } catch (Exception e) {
      String message = "Failed committing tagging guide";
      log.error("{}", message, e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              message))
          .build(), e);
    }
    return importStats;
  }

  public void loadClassificationIds(String clientId, String projectId,
      List<String> intentClassIds,
      List<String> ruTagClassIds) {

    log.info("Starting loadClassificationIds");

    List<ClassificationDocument> classificationList;

    classificationList = contentManager.getClassificationsForProject(clientId, projectId);

    for (ClassificationDocument classification : classificationList) {

      if (!StringUtils.isEmpty(classification.getGranularIntent())) {
        intentClassIds.add(classification.getGranularIntent());
      } else {
        ruTagClassIds.add(classification.getClassification());
      }
    }

    log.info("At the end of loadClassificationIds");

  }

  @Override
  public void abortImportGuide(String projectId, String token) {
    String filename = token + ".csv";
    String validIntentsStagingFileName =
        String.format("%s_%s", token, Constants.TAGGING_GUIDE_VALID_TAGS_FNAME_SUFFIX);
    String invalidIntentsStagingFileName =
        String.format("%s_%s", token, Constants.TAGGING_GUIDE_INVALID_TAGS_FNAME_SUFFIX);
    String missingIntentsStagingFileName =
        String.format("%s_%s", token, Constants.TAGGING_GUIDE_MISSING_TAGS_FNAME_SUFFIX);
    try {
      String fileSystemPath = this.getProjectFilePath(projectId, filename).toString();
      Path validIntentsStagingFilePath =
          this.getProjectFilePath(projectId, validIntentsStagingFileName);
      Path invalidIntentsStagingFilePath =
          this.getProjectFilePath(projectId, invalidIntentsStagingFileName);
      Path missingIntentsStagingFilePath =
          this.getProjectFilePath(projectId, missingIntentsStagingFileName);
      Files.deleteIfExists(missingIntentsStagingFilePath);
      Files.deleteIfExists(invalidIntentsStagingFilePath);
      Files.deleteIfExists(validIntentsStagingFilePath);

      log.info("Deleting the uploaded tagging guide {}", fileSystemPath);
      Files.deleteIfExists(FileSystems.getDefault().getPath(fileSystemPath));
    } catch (Exception e) {
      log.error("Failed to abort import guide ", e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              "Could not delete the staged artifacts of the tagging guide"))
          .build(), e);
    }
  }

}
