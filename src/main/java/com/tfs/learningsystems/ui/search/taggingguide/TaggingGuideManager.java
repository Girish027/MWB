/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide;

import com.tfs.learningsystems.db.TaggingGuideImportStatBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappedResponse;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideStagedImportResponse;
import java.io.InputStream;

public interface TaggingGuideManager {

  public TaggingGuideStagedImportResponse saveGuide(String projectId, InputStream fileInputStream,
      String userId) throws ApplicationException;

  public TaggingGuideColumnMappedResponse addColumnMappingsAndGetStats(String projectId,
      String userId, String token, boolean ignoreFirstRow,
      TaggingGuideColumnMappingSelectionList columnMappings) throws ApplicationException;

  public TaggingGuideImportStatBO commitImportGuide(String projectId, String userId, String token);

  public void abortImportGuide(String projectId, String token);
}
