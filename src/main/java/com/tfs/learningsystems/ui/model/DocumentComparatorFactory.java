/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;

import com.tfs.learningsystems.ui.search.taggingguide.model.TagOrderingField;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class DocumentComparatorFactory {

  public static Comparator<VerifiedTranscriptionDocument> getVerifiedDocComparator(
      TranscriptionOrderingField sortField, SortOrderingDirection sortDirection,
      Locale locale) {

    final Collator collator = Collator.getInstance(locale);
    collator.setStrength(Collator.SECONDARY);

    switch (sortField) {
      case COUNT:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(VerifiedTranscriptionDocument::getDocumentCount,
              naturalOrder());
        } else {
          return comparing(VerifiedTranscriptionDocument::getDocumentCount,
              reverseOrder());
        }
      case MANUAL_TAG:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(VerifiedTranscriptionDocument::getSortableIntent, nullsLast(collator));
        } else {
          return comparing(VerifiedTranscriptionDocument::getSortableIntent,
              nullsLast(collator.reversed()));
        }
      case UNIQUE_TEXT_STRING:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(VerifiedTranscriptionDocument::getSortableTextString,
              nullsLast(collator));
        } else {
          return comparing(VerifiedTranscriptionDocument::getSortableTextString,
              nullsLast(collator.reversed()));
        }
      case NORMALIZED_FORM:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(VerifiedTranscriptionDocument::getNormalizedForm, nullsLast(collator));
        } else {
          return comparing(VerifiedTranscriptionDocument::getNormalizedForm,
              nullsLast(collator.reversed()));
        }
      case NORMALIZED_FORM_GROUP:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(VerifiedTranscriptionDocument::getNormalizedFormGroup, naturalOrder());
        } else {
          return comparing(VerifiedTranscriptionDocument::getNormalizedFormGroup, reverseOrder());
        }
      case INTENT_CONFLICT:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(VerifiedTranscriptionDocument::getIntentConflict, naturalOrder());
        } else {
          return comparing(VerifiedTranscriptionDocument::getIntentConflict, reverseOrder());
        }
      case SUGGESTED_INTENT:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(VerifiedTranscriptionDocument::getSuggestedIntent, nullsLast(collator));
        } else {
          return comparing(VerifiedTranscriptionDocument::getSuggestedIntent,
              nullsLast(collator.reversed()));
        }
      default:
        // default sort by count
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(VerifiedTranscriptionDocument::getNormalizedFormGroup, naturalOrder());
        } else {
          return comparing(VerifiedTranscriptionDocument::getNormalizedFormGroup, reverseOrder());
        }
    }
  }

  public static Comparator<TranscriptionDocumentDetail> getComparator(
      TranscriptionOrderingField sortField, SortOrderingDirection sortDirection,
      Locale locale) {

    final Collator collator = Collator.getInstance(locale);
    collator.setStrength(Collator.SECONDARY);

    switch (sortField) {
      case COUNT:
        String sortByString = sortField.getValue() + sortDirection.getValue();
        return TranscriptionDocumentComparators.getComparatorFromSortByString(sortByString);
      case MANUAL_TAG:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          if (!Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
            return comparing(TranscriptionDocumentDetail::getSortableIntent, nullsLast(collator));
          } else {
            return comparing(TranscriptionDocumentDetail::getIntent, nullsLast(collator));
          }
        } else {
          if (!Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
            return comparing(TranscriptionDocumentDetail::getSortableIntent,
                nullsLast(collator.reversed()));
          } else {
            return comparing(TranscriptionDocumentDetail::getIntent,
                nullsLast(collator.reversed()));
          }
        }
      case SUGGESTED_TAG:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(TranscriptionDocumentDetail::getAutoTagString,
              nullsLast(collator));
        } else {
          return comparing(TranscriptionDocumentDetail::getAutoTagString,
              nullsLast(collator.reversed()));
        }
      case UNIQUE_TEXT_STRING:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(TranscriptionDocumentDetail::getSortableTextString,
              nullsLast(collator));
        } else {
          return comparing(TranscriptionDocumentDetail::getSortableTextString,
              nullsLast(collator.reversed()));
        }
      case COMMENT:
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return comparing(TranscriptionDocumentDetail::getSortableComment, nullsLast(collator));
        } else {
          return comparing(TranscriptionDocumentDetail::getSortableComment,
              nullsLast(collator.reversed()));
        }
      case CREATED_BY:
        // We aren't storing this value in the document
      case CREATED_AT:
        // We aren't storing this value in the document
      case DELETED_BY:
        // We aren't storing this value in the document
      case DELETED_AT:
        // We aren't storing this value in the document
      default:
        // default sort by count
        if (sortDirection.getValue()
            .equals(SortOrderingDirection.ASC.getValue())) {
          return TranscriptionDocumentComparators.DOCUMENT_COUNT_ASC;
        } else {
          return TranscriptionDocumentComparators.DOCUMENT_COUNT_DESC;
        }
    }
  }

  public static Comparator<TaggingGuideDocumentDetail> getTaggingGuideDocumentComparator(
      TagOrderingField sortField, SortOrderingDirection sortDirection,
      Locale locale) {

    final Collator collator = Collator.getInstance(locale);
    collator.setStrength(Collator.SECONDARY);

    switch (sortField) {
      case COUNT:
      case FREQUENCY:
        if (SortOrderingDirection.ASC.getValue().equals(sortDirection.getValue())) {
          return comparing(TaggingGuideDocumentDetail::getCount, naturalOrder());
        } else {
          return comparing(TaggingGuideDocumentDetail::getCount, reverseOrder());
        }

      case RUTAG:
        if (SortOrderingDirection.ASC.getValue().equals(sortDirection.getValue())) {
          return comparing(TaggingGuideDocumentDetail::getRutag, nullsLast(collator));
        } else {
          return comparing(TaggingGuideDocumentDetail::getRutag, nullsLast(collator.reversed()));
        }

      case COMMENTS:
        if (SortOrderingDirection.ASC.getValue().equals(sortDirection.getValue())) {
          return comparing(TaggingGuideDocumentDetail::getComments, nullsLast(collator));
        } else {
          return comparing(TaggingGuideDocumentDetail::getComments, nullsLast(collator.reversed()));
        }

      case EXAMPLES:
        if (SortOrderingDirection.ASC.getValue().equals(sortDirection.getValue())) {
          return comparing(TaggingGuideDocumentDetail::getExamples, nullsLast(collator));
        } else {
          return comparing(TaggingGuideDocumentDetail::getExamples, nullsLast(collator.reversed()));
        }

      case KEYWORDS:
        if (SortOrderingDirection.ASC.getValue().equals(sortDirection.getValue())) {
          return comparing(TaggingGuideDocumentDetail::getKeywords, nullsLast(collator));
        } else {
          return comparing(TaggingGuideDocumentDetail::getKeywords, nullsLast(collator.reversed()));
        }

      case DESCRIPTION:
        if (SortOrderingDirection.ASC.getValue().equals(sortDirection.getValue())) {
          return comparing(TaggingGuideDocumentDetail::getDescription, nullsLast(collator));
        } else {
          return comparing(TaggingGuideDocumentDetail::getDescription,
              nullsLast(collator.reversed()));
        }

      case TAG:
      case INTENT:
      default:
        if (SortOrderingDirection.ASC.getValue().equals(sortDirection.getValue())) {
          if (!Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
            return comparing(TaggingGuideDocumentDetail::getSortableIntent,
                nullsLast(collator));
          } else {
            return comparing(TaggingGuideDocumentDetail::getIntent,
                nullsLast(collator));
          }
        } else {
          if (!Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
            return comparing(TaggingGuideDocumentDetail::getSortableIntent,
                nullsLast(collator.reversed()));
          } else {
            return comparing(TaggingGuideDocumentDetail::getIntent,
                nullsLast(collator.reversed()));
          }
        }
    }
  }
}
