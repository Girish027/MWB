package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.SearchRequestFilter;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.model.VerifiedTranscriptionDocument;
import com.tfs.learningsystems.ui.model.VerifiedTranscriptionsResponse;
import com.tfs.learningsystems.ui.model.VerifyRequest;
import com.tfs.learningsystems.ui.model.VerifyRequestFilter;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.util.SortUtil;
import com.tfs.learningsystems.util.TextNormalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class ContentVerifyManagerImpl implements ContentVerifyManager {

  private static final double TAG_SUGGESTION_THREASHOLD = 0.70;
  private static final String MISSING_TAG = "missing-tag";
  @Autowired
  @Qualifier("searchManagerBean")
  private SearchManager searchManager;
  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;
  @Autowired
  private TextNormalizer textNormalizer;

  @Override
  public VerifiedTranscriptionsResponse verifyIntents(final String clientId, final String projectId,
      final VerifyRequest request, int startIndex, int limit,
      List<String> sortBy) {

    final VerifiedTranscriptionsResponse response =
        new VerifiedTranscriptionsResponse();

    final TranscriptionDocumentDetailCollection uniqueTranscriptions;
    final VerifyRequestFilter filter = request.getFilter();

    Locale projectLocale = this.projectManager.getProjectLocale(projectId);

    try {

      final SearchRequest searchRequest = new SearchRequest();
      final SearchRequestFilter requestFilter =
          new SearchRequestFilter().tagged(filter.getOnlyTagged())
              .untagged(false);
      searchRequest.setFilter(requestFilter);

      List<String> datasetIds = filter.getDatasetIds();
      List<String> sortByList = new ArrayList<>();

      sortByList.add("count:desc");

      uniqueTranscriptions = searchManager
          .getFilteredTranscriptions(clientId, projectId, datasetIds, 0, -1,
              null, sortByList, searchRequest, projectLocale);

    } catch (QueryNodeException e1) {
      log.warn("invalid search query - {} - {} ", projectId, e1.getLocalizedMessage());
      throw new InvalidRequestException(
          new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              "invalid_search_query", e1.getLocalizedMessage()));
    }
    final MultiValueMap<String, VerifiedTranscriptionDocument> map =
        new LinkedMultiValueMap<>();

    String configName = "system_default_en_0";
    textNormalizer.init(configName, false);

    // Build map of documents keyed on normalized form
    for (TranscriptionDocumentDetail transcriptionDocumentDetail : uniqueTranscriptions
        .getTranscriptionList()) {

      VerifiedTranscriptionDocument doc =
          new VerifiedTranscriptionDocument();

      doc.setDocumentCount(
          transcriptionDocumentDetail.getDocumentCount());

      if (!transcriptionDocumentDetail.getIntents().isEmpty()) {
        for (String intent : transcriptionDocumentDetail.getIntents()
            .values()) {
          if (intent != null) {
            if (intent.contains(",")) {
              doc.getIntents().addAll(Arrays.asList(intent.split(",")));
            } else {
              doc.addIntentsItem(intent);
            }
          }
        }
      }

      String textStringForTagging =
          transcriptionDocumentDetail.getTextStringForTagging();

      String normalziedForm = textNormalizer.processText(
          textStringForTagging == null ? "" : textStringForTagging);

      doc.setNormalizedForm(normalziedForm);
      doc.setTranscriptionHash(
          transcriptionDocumentDetail.getTranscriptionHash());
      doc.setTextStringForTagging(textStringForTagging);

      if (StringUtils.isEmpty(transcriptionDocumentDetail) &&
          StringUtils.isEmpty(transcriptionDocumentDetail.getIntent()) &&
          transcriptionDocumentDetail.getIntent().contains(",")) {
        for (String intent : Arrays
            .asList(transcriptionDocumentDetail.getIntent()
                .split(","))) {
          try {
            VerifiedTranscriptionDocument newDoc = doc.clone();
            newDoc.setIntent(intent);
            newDoc.addIntentsItem(intent);
            map.add(normalziedForm, newDoc);
          } catch (CloneNotSupportedException e) {
            log.error("failed to create new doc", e);
          }
        }

      } else {
        doc.setIntent(transcriptionDocumentDetail.getIntent());
        map.add(normalziedForm, doc);
      }
    }

    int normalizedFormGroup = 0;
    final Map<String, Long> intentCounts = new HashMap<>();
    final List<VerifiedTranscriptionDocument> list = new ArrayList<>();
    final Set<VerifiedTranscriptionDocument> conflicts = new HashSet<>();
    Long total;

    for (Map.Entry<String, List<VerifiedTranscriptionDocument>> entry : map
        .entrySet()) {

      intentCounts.clear();
      total = 0L;
      // Build intent counts
      for (VerifiedTranscriptionDocument document : entry.getValue()) {
        String intent = document.getIntent() == null ?
            MISSING_TAG :
            document.getIntent();
        Long intentTotal = intentCounts.get(intent);
        Long documentCount = document.getDocumentCount();
        if (intentTotal == null) {
          intentTotal = documentCount;
        } else {
          intentTotal += documentCount;
        }
        intentCounts.put(intent, intentTotal);
        total += documentCount;
      }

      // Only process documents with multiple intents
      if (intentCounts.size() > 1) {
        String suggestedIntent = null;
        intentCounts.keySet().remove(MISSING_TAG);

        // Find suggested intent tag based on counts
        for (Map.Entry<String, Long> longEntry : intentCounts
            .entrySet()) {
          if ( total > 0 && (longEntry.getValue() / (double) total)
              > TAG_SUGGESTION_THREASHOLD) {
            suggestedIntent = longEntry.getKey();
            break;
          }
        }

        // Finish each document and test if it has a conflicting intent (ie, doesn't match suggested intent)
        for (VerifiedTranscriptionDocument document : entry
            .getValue()) {
          document.getIntents().addAll(intentCounts.keySet());
          document.setNormalizedFormGroup(normalizedFormGroup);
          if (suggestedIntent != null) {
            document.setSuggestedIntent(suggestedIntent);
            if (!suggestedIntent
                .equalsIgnoreCase(document.getIntent())) {
              document.setIntentConflict(true);
              conflicts.add(document);
            }
          } else {
            document.setIntentConflict(true);
            conflicts.add(document);
          }
          list.add(document);
        }
        normalizedFormGroup++;
      }

    }

    // Only keep documents with conflicting intents
    if (filter.getOnlyConflicts()) {
      list.retainAll(conflicts);
    }

    int size = list.size();
    if (!list.isEmpty() && startIndex < size) {

      // Always last sort by count desc, unless already sorting by count
      if (!sortBy.contains("count:desc") && !sortBy
          .contains("count:asc")) {
        sortBy.add("count:desc");
      }
      SortUtil.sortVarifiedTranscriptions(list, sortBy, projectLocale);
      int endIndex = startIndex + limit;
      endIndex = endIndex > size ? size : endIndex;
      response.setTranscriptionList(list.subList(startIndex, endIndex));
    }
    response.setTotal((long) size);
    response.setLimit(limit);
    response.setStartIndex(startIndex);
    return response;
  }
}
