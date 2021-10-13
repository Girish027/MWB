/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide;

import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumn;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;
import java.util.List;

public interface TaggingGuideColumnsManager {

  public TaggingGuideColumnList getColumns();

  public void seedColumns(List<TaggingGuideColumn> columns);

  public TaggingGuideColumnMappingSelectionList getColumnMappings(String userId, String projectId);

  public void addColumnMappings(String projectId,
      TaggingGuideColumnMappingSelectionList columnMappingList);
}
