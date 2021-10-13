/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TranscriptionDocumentComparators {

  public static final Comparator<TranscriptionDocumentDetail> DOCUMENT_COUNT_ASC =
      comparing(TranscriptionDocumentDetail::getDocumentCount, naturalOrder());

  public static final Comparator<TranscriptionDocumentDetail> DOCUMENT_COUNT_DESC =
      comparing(TranscriptionDocumentDetail::getDocumentCount, reverseOrder());

  public static final Comparator<TranscriptionDocumentDetail> SUGGESTED_TAG_ASC =
      comparing(TranscriptionDocumentDetail::getAutoTagString, nullsLast(naturalOrder()));

  public static final Comparator<TranscriptionDocumentDetail> SUGGESTED_TAG_DESC =
      comparing(TranscriptionDocumentDetail::getAutoTagString, nullsLast(reverseOrder()));

  public static final Comparator<TranscriptionDocumentDetail> MANUAL_TAG_ASC =
      comparing(TranscriptionDocumentDetail::getIntent, nullsLast(naturalOrder()));

  public static final Comparator<TranscriptionDocumentDetail> MANUAL_TAG_DESC =
      comparing(TranscriptionDocumentDetail::getIntent, nullsLast(reverseOrder()));

  public static final Comparator<TranscriptionDocumentDetail> TEXT_SRTING_FOR_TAGGING_ASC =
      comparing(TranscriptionDocumentDetail::getSortableTextString, nullsLast(naturalOrder()));

  public static final Comparator<TranscriptionDocumentDetail> TEXT_SRTING_FOR_TAGGING_DESC =
      comparing(TranscriptionDocumentDetail::getSortableTextString, nullsLast(reverseOrder()));

  public static final Comparator<TranscriptionDocumentDetail> COMMENT_ASC =
      comparing(TranscriptionDocumentDetail::getSortableComment, nullsLast(naturalOrder()));

  public static final Comparator<TranscriptionDocumentDetail> COMMENT_DESC =
      comparing(TranscriptionDocumentDetail::getSortableComment, nullsLast(reverseOrder()));

  public static final Map<String, Comparator<TranscriptionDocumentDetail>> orderingComparatorMap = Collections
      .unmodifiableMap(
          new HashMap<String, Comparator<TranscriptionDocumentDetail>>() {
            private static final long serialVersionUID = -4224408637767737149L;

            {
              put(TranscriptionOrderingField.COUNT.getValue()
                      + SortOrderingDirection.ASC.getValue(),
                  DOCUMENT_COUNT_ASC);
              put(TranscriptionOrderingField.COUNT.getValue()
                      + SortOrderingDirection.DESC.getValue(),
                  DOCUMENT_COUNT_DESC);

              put(TranscriptionOrderingField.MANUAL_TAG.getValue()
                      + SortOrderingDirection.ASC.getValue(),
                  MANUAL_TAG_ASC);
              put(TranscriptionOrderingField.MANUAL_TAG.getValue()
                      + SortOrderingDirection.DESC.getValue(),
                  MANUAL_TAG_DESC);

              put(TranscriptionOrderingField.SUGGESTED_TAG.getValue()
                      + SortOrderingDirection.ASC.getValue(),
                  SUGGESTED_TAG_ASC);
              put(TranscriptionOrderingField.SUGGESTED_TAG.getValue()
                      + SortOrderingDirection.DESC.getValue(),
                  SUGGESTED_TAG_DESC);

              put(TranscriptionOrderingField.UNIQUE_TEXT_STRING.getValue()
                      + SortOrderingDirection.ASC.getValue(),
                  TEXT_SRTING_FOR_TAGGING_ASC);
              put(TranscriptionOrderingField.UNIQUE_TEXT_STRING.getValue()
                      + SortOrderingDirection.DESC.getValue(),
                  TEXT_SRTING_FOR_TAGGING_DESC);

              put(TranscriptionOrderingField.COMMENT.getValue()
                      + SortOrderingDirection.ASC.getValue(),
                  COMMENT_ASC);
              put(TranscriptionOrderingField.COMMENT.getValue()
                      + SortOrderingDirection.DESC.getValue(),
                  COMMENT_DESC);
            }
          });

  public static Comparator<TranscriptionDocumentDetail> getComparatorFromSortByString(
      String sortByString) {
    return orderingComparatorMap.get(sortByString);
  }
}
