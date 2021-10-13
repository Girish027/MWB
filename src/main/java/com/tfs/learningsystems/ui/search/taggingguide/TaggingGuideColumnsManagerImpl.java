/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide;

import com.tfs.learningsystems.db.TaggingGuideColumnBO;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumn;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.taggingguide.model.dao.TaggingGuideColumnsDao;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("taggingGuideColumnsManager")
@Slf4j
public class TaggingGuideColumnsManagerImpl implements TaggingGuideColumnsManager {

  @Inject
  @Qualifier("taggingGuideColumnsDao")
  private TaggingGuideColumnsDao columnsDao;

  @Override
  public void seedColumns(List<TaggingGuideColumn> columns) {

    for (TaggingGuideColumn taggingGuideColumn : columns) {
      TaggingGuideColumnBO column = new TaggingGuideColumnBO();
      column = column.findOne(TaggingGuideColumnBO.FLD_NAME, taggingGuideColumn.getName());
      if (column == null || column.getId() == null) {
        column = new TaggingGuideColumnBO();
        column.setName(taggingGuideColumn.getName());
        column.setDisplayName(taggingGuideColumn.getDisplayName());
        column.setRequired(taggingGuideColumn.getRequired());
        column.create();
        continue;
      }
      if (column.getName().equals(taggingGuideColumn.getName()) ||
          column.getRequired() != taggingGuideColumn.getRequired()) {
        column.setDisplayName(taggingGuideColumn.getDisplayName());
        column.setRequired(taggingGuideColumn.getRequired());
        column.update();
      }else{
        column.setDisplayName(taggingGuideColumn.getDisplayName());
        column.update();

      }
    }

  }

  @Override
  public void addColumnMappings(String projectId,
      TaggingGuideColumnMappingSelectionList columnMappingList) {
    columnsDao.addMappings(projectId, columnMappingList);
  }

  @Override
  public TaggingGuideColumnMappingSelectionList getColumnMappings(String userEmail,
      String projectId) {
    try {
      return columnsDao.getMappings(userEmail, projectId);
    } catch (Exception e) {
      log.error("Failed to get column mapping", e);
    }
    return null;
  }

  @Override
  public TaggingGuideColumnList getColumns() {
    TaggingGuideColumnBO columnBO = new TaggingGuideColumnBO();
    List<TaggingGuideColumnBO> all = columnBO.list(new HashMap<String, Object>(), null);
    TaggingGuideColumnList columnList = new TaggingGuideColumnList();
    columnList.addAll(all);
    return (columnList);
  }

}
