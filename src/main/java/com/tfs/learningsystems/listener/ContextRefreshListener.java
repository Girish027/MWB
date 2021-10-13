/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.listener;

import com.tfs.learningsystems.db.FileColumnBO;
import com.tfs.learningsystems.ui.search.taggingguide.TaggingGuideColumnsManager;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumn;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Profile({"development", "dev_sv2", "qa_sv2", "psr_sv2", "stable_sv2", "staging", "production",
    "staging_gslb", "prod_gslb", "integration", "test"})
@Slf4j
public class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

  @Autowired
  @Qualifier("taggingGuideColumnsManager")
  private TaggingGuideColumnsManager taggingGuideColumnsManager;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    populateTaggingGuideCols();
    populateFileCols();
  }

  private void populateTaggingGuideCols() {
    log.info("Populating tagging guide columns");
    Resource resource = new ClassPathResource("tagging_guide_columns.csv");
    BufferedReader reader;
    try {
      reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
      List<String[]> lineTokensList = reader
          .lines()
          .filter(line -> !line.startsWith("#"))
          .map(line -> line.split(","))
          .filter(lineTokens -> lineTokens.length == 3)
          .collect(Collectors.toList());
      List<TaggingGuideColumn> taggingGuideColumns = new ArrayList<>(lineTokensList.size());
      for (String[] lineTokens : lineTokensList) {
        TaggingGuideColumn taggingGuideColumn = new TaggingGuideColumn();
        taggingGuideColumn.setName(lineTokens[0]);
        taggingGuideColumn.setDisplayName(lineTokens[2]);
        taggingGuideColumn.setRequired(Boolean.valueOf(lineTokens[1]));
        taggingGuideColumns.add(taggingGuideColumn);
      }
      taggingGuideColumnsManager.seedColumns(taggingGuideColumns);
    } catch (IOException e) {
      log.error("Failed seeding tagging guide columns", e);
    }
  }

  private void populateFileCols() {
    log.info("Populating file columns");
    Resource resource = new ClassPathResource("file_columns.csv");
    BufferedReader reader;
    try {
      reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
      List<String[]> lineTokensList = reader
          .lines()
          .filter(line -> !line.startsWith("#"))
          .map(line -> line.split(","))
          .filter(lineTokens -> lineTokens.length == 3)
          .collect(Collectors.toList());
      for (String[] lineTokens : lineTokensList) {
        FileColumnBO fileColumnBO = new FileColumnBO();
        String name = lineTokens[0];
        String displayName = lineTokens[2];
        Boolean isRequired = Boolean.valueOf(lineTokens[1]);
        fileColumnBO = fileColumnBO.findOne(FileColumnBO.FLD_NAME, name);
        if (fileColumnBO == null || fileColumnBO.getId() == null) {
          fileColumnBO = new FileColumnBO();
          fileColumnBO.setName(name);
          fileColumnBO.setDisplayName(displayName);
          fileColumnBO.setRequired(isRequired);
          fileColumnBO.create();
          continue;
        }
        if (fileColumnBO.getName().equals(name) ||
            fileColumnBO.getRequired() != isRequired) {
          fileColumnBO.setDisplayName(displayName);
          fileColumnBO.setRequired(isRequired);
          fileColumnBO.update();
        }else{
          fileColumnBO.setDisplayName(displayName);
          fileColumnBO.update();
        }

      }
    } catch (IOException e) {
      log.error("Failed seeding file columns", e);
    }
  }

}
