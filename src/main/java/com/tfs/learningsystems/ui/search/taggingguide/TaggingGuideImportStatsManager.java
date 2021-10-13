/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide;

import com.tfs.learningsystems.db.TaggingGuideImportStatBO;

public interface TaggingGuideImportStatsManager {

  public TaggingGuideImportStatBO getLatestStatsForProject(String projectId);

  public TaggingGuideImportStatBO getStatsById(String id);
}
