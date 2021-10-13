package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.ui.model.VerifiedTranscriptionsResponse;
import com.tfs.learningsystems.ui.model.VerifyRequest;
import java.util.List;

public interface ContentVerifyManager {

  public VerifiedTranscriptionsResponse verifyIntents(String clientId, String projectId,
      VerifyRequest request, int startIndex, int limit, List<String> sortBy);
}
