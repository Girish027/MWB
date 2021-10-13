/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide;

import com.tfs.learningsystems.db.TaggingGuideImportStatBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@Qualifier("taggingGuideImportStatsManager")
@Slf4j
public class TaggingGuideImportStatsManagerImpl implements TaggingGuideImportStatsManager {

  @Override
  public TaggingGuideImportStatBO getStatsById(String id) {
    TaggingGuideImportStatBO bo = new TaggingGuideImportStatBO();
    return (bo.findOne(id));
  }

  @Override
  public TaggingGuideImportStatBO getLatestStatsForProject(String projectId) {
    TaggingGuideImportStatBO bo = new TaggingGuideImportStatBO();
    Map<String, Object> params = new HashMap<>();
    params.put(TaggingGuideImportStatBO.FLD_PROJECT_ID, projectId);

    Sort sort = Sort.by(Sort.Direction.DESC,
        new String[]{TaggingGuideImportStatBO.FLD_IMPORTED_AT});

    bo = bo.findOne(params, sort);
    return (bo);
  }

}
