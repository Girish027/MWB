package com.tfs.learningsystems.util;

import com.tfs.learningsystems.ui.model.DocumentComparatorFactory;
import com.tfs.learningsystems.ui.model.SortOrderingDirection;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.TranscriptionOrderingField;
import com.tfs.learningsystems.ui.model.VerifiedTranscriptionDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TagOrderingField;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SortUtil {

  public static List<Comparator<TranscriptionDocumentDetail>> buildTranscriptionDocumentDetailComparatorList(
      List<String> sortByList, Locale locale) {
    List<Comparator<TranscriptionDocumentDetail>> comparatorList = new ArrayList<>();
    if (sortByList != null && !sortByList.isEmpty()) {
      for (String sortByField : sortByList) {
        String[] sortByTokens = sortByField.split(":");
        if (sortByTokens.length == 2) {
          TranscriptionOrderingField sortField =
              TranscriptionOrderingField.lookup(sortByTokens[0]);
          SortOrderingDirection sortDirection =
              SortOrderingDirection.lookup(sortByTokens[1]);
          if (sortField != null && sortDirection != null) {
            Comparator<TranscriptionDocumentDetail> comparator =
                DocumentComparatorFactory.getComparator(sortField,
                    sortDirection, locale);
            if (comparator != null) {
              comparatorList.add(comparator);
            }
          }
        }
      }
    }
    return comparatorList;
  }

  public static List<Comparator<VerifiedTranscriptionDocument>> buildVerifiedTranscriptionDocumentComparatorList(
      List<String> sortByList, Locale locale) {
    List<Comparator<VerifiedTranscriptionDocument>> comparatorList = new ArrayList<>();
    if (sortByList != null && !sortByList.isEmpty()) {
      for (String sortByField : sortByList) {
        String[] sortByTokens = sortByField.split(":");
        if (sortByTokens.length == 2) {
          TranscriptionOrderingField sortField =
              TranscriptionOrderingField.lookup(sortByTokens[0]);
          SortOrderingDirection sortDirection =
              SortOrderingDirection.lookup(sortByTokens[1]);
          if (sortField != null && sortDirection != null) {
            Comparator<VerifiedTranscriptionDocument> comparator =
                DocumentComparatorFactory.getVerifiedDocComparator(sortField,
                    sortDirection, locale);
            if (comparator != null) {
              comparatorList.add(comparator);
            }
          }
        }
      }
    }
    return comparatorList;
  }

  public static List<Comparator<TaggingGuideDocumentDetail>> buildTaggingGuideDocumentComparatorList(
      List<String> sortByList, Locale locale) {
    List<Comparator<TaggingGuideDocumentDetail>> comparatorList = new ArrayList<>();
    if (sortByList != null && !sortByList.isEmpty()) {
      for (String sortByField : sortByList) {
        String[] sortByTokens = sortByField.split(":");
        if (sortByTokens.length == 2) {
          TagOrderingField sortField =
              TagOrderingField.lookup(sortByTokens[0]);
          SortOrderingDirection sortDirection =
              SortOrderingDirection.lookup(sortByTokens[1]);
          if (sortField != null && sortDirection != null) {
            Comparator<TaggingGuideDocumentDetail> comparator =
                DocumentComparatorFactory.getTaggingGuideDocumentComparator(sortField,
                    sortDirection, locale);
            if (comparator != null) {
              comparatorList.add(comparator);
            }
          }
        }
      }
    }
    return comparatorList;
  }

  public static List<TranscriptionDocumentDetail> sortTranscriptions(
      List<TranscriptionDocumentDetail> documents, List<String> sortByList) {
    return sortTranscriptions(documents, sortByList, Locale.getDefault());
  }

  public static List<TranscriptionDocumentDetail> sortTranscriptions(
      List<TranscriptionDocumentDetail> documents, List<String> sortByList, Locale locale) {

    List<Comparator<TranscriptionDocumentDetail>> orderingComparators =
        buildTranscriptionDocumentDetailComparatorList(sortByList, locale);

    if (orderingComparators != null && !orderingComparators.isEmpty()) {
      Comparator<TranscriptionDocumentDetail> comparator = orderingComparators.get(0);
      int index = 1;
      while (index < orderingComparators.size()) {
        comparator = comparator.thenComparing(orderingComparators.get(index));
        index += 1;
      }

      Collections.sort(documents, comparator);
    }
    return documents;
  }

  public static List<VerifiedTranscriptionDocument> sortVarifiedTranscriptions(
      List<VerifiedTranscriptionDocument> documents, List<String> sortByList, Locale locale) {

    List<Comparator<VerifiedTranscriptionDocument>> orderingComparators =
        buildVerifiedTranscriptionDocumentComparatorList(sortByList, locale);

    if (orderingComparators != null && !orderingComparators.isEmpty()) {
      Comparator<VerifiedTranscriptionDocument> comparator = orderingComparators.get(0);
      int index = 1;
      while (index < orderingComparators.size()) {
        comparator = comparator.thenComparing(orderingComparators.get(index));
        index += 1;
      }

      Collections.sort(documents, comparator);
    }
    return documents;
  }

  public static List<TaggingGuideDocumentDetail> sortTaggingGuideDocuments(
      List<TaggingGuideDocumentDetail> documents, List<String> sortByList, Locale locale) {

    List<Comparator<TaggingGuideDocumentDetail>> orderingComparators =
        buildTaggingGuideDocumentComparatorList(sortByList, locale);

    if (orderingComparators != null && !orderingComparators.isEmpty()) {
      Comparator<TaggingGuideDocumentDetail> comparator = orderingComparators.get(0);
      int index = 1;
      while (index < orderingComparators.size()) {
        comparator = comparator.thenComparing(orderingComparators.get(index));
        index += 1;
      }

      Collections.sort(documents, comparator);
    }
    return documents;
  }


}
