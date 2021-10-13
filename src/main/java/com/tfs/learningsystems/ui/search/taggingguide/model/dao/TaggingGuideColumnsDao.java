/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide.model.dao;

import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;

public interface TaggingGuideColumnsDao {

  public void addMappings(String projectdId, TaggingGuideColumnMappingSelectionList columnMappings);

  public TaggingGuideColumnMappingSelectionList getMappings(String userId, String projectId);
}
