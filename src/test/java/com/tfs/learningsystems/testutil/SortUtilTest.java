package com.tfs.learningsystems.testutil;

import static org.junit.Assert.assertArrayEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentComparators;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.util.SortUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.Test;

public class SortUtilTest {

  @Test
  public void testBuildTranscriptionDocumentDetailComparatorList() {
    List<String> sortByList = new ArrayList<>();
    sortByList.add("count:asc");
    List<Comparator<TranscriptionDocumentDetail>> comparatorList = SortUtil
        .buildTranscriptionDocumentDetailComparatorList(sortByList, Locale.CANADA);
    assertArrayEquals(comparatorList.toArray(), new Comparator[]{
        TranscriptionDocumentComparators.DOCUMENT_COUNT_ASC
    });
  }

  @Test
  public void testSortTranscriptionsByCountASC() {
    final TranscriptionDocumentDetail doc1 = new TranscriptionDocumentDetail()
        .autoTagString("make reservation")
        .documentCount(1578L)
        .textStringForTagging("I would like to make reservation")
        .intent("reservation-make");
    final TranscriptionDocumentDetail doc2 = new TranscriptionDocumentDetail()
        .autoTagString("cancel reservation")
        .documentCount(1149L)
        .textStringForTagging("cancel my reservation")
        .intent("reservation-cancel");
    final TranscriptionDocumentDetail doc3 = new TranscriptionDocumentDetail()
        .autoTagString("vague reservation")
        .documentCount(817L)
        .textStringForTagging("where is my reservation")
        .intent("reservation-vague");
    List<TranscriptionDocumentDetail> documents = new ArrayList<TranscriptionDocumentDetail>() {
      private static final long serialVersionUID = -7575445427985827051L;

      {
        add(doc1);
        add(doc2);
        add(doc3);
      }
    };

    List<TranscriptionDocumentDetail> sortedDocuments = SortUtil
        .sortTranscriptions(documents, new ArrayList<String>() {
          private static final long serialVersionUID = -8427434239520722725L;

          {
            add("count:A");
          }
        });

    assertArrayEquals(sortedDocuments.toArray(), new TranscriptionDocumentDetail[]{
        doc3,
        doc2,
        doc1
    });
  }

  @Test
  public void testSortTranscriptionsByIntentASCAndCountDesc() {
    final TranscriptionDocumentDetail doc1 = new TranscriptionDocumentDetail()
        .autoTagString("make reservation")
        .documentCount(113L)
        .textStringForTagging("I would like to make reservation")
        .intent("reservation-make");
    final TranscriptionDocumentDetail doc2 = new TranscriptionDocumentDetail()
        .autoTagString("cancel reservation")
        .documentCount(113L)
        .textStringForTagging("cancel my reservation")
        .intent("reservation-cancel");
    final TranscriptionDocumentDetail doc3 = new TranscriptionDocumentDetail()
        .autoTagString("vague reservation")
        .documentCount(817L)
        .textStringForTagging("where is my reservation")
        .intent("reservation-vague");
    final TranscriptionDocumentDetail doc4 = new TranscriptionDocumentDetail()
        .autoTagString("payment inquiry")
        .documentCount(100L)
        .textStringForTagging("where is my reservation");
    final TranscriptionDocumentDetail doc5 = new TranscriptionDocumentDetail()
        .autoTagString("cancel reservation, angry customer")
        .documentCount(249L)
        .textStringForTagging("I am frustrated please cancel my reservation")
        .intent("reservation-cancel");
    List<TranscriptionDocumentDetail> documents = new ArrayList<TranscriptionDocumentDetail>() {
      private static final long serialVersionUID = -7575445427985827051L;

      {
        add(doc1);
        add(doc2);
        add(doc3);
        add(doc4);
        add(doc5);
      }
    };

    List<TranscriptionDocumentDetail> sortedDocuments = SortUtil
        .sortTranscriptions(documents, new ArrayList<String>() {
          private static final long serialVersionUID = -8427434239520722725L;

          {
            add("manualTag:asc");
            add("count:desc");
          }
        });

    assertArrayEquals(sortedDocuments.toArray(), new TranscriptionDocumentDetail[]{
        doc5,
        doc2,
        doc1,
        doc3,
        doc4
    });
  }

  @Test
  public void testSortTranscriptionsByTextStringWithLocaleDESC() throws JsonProcessingException {
    final TranscriptionDocumentDetail doc1 = new TranscriptionDocumentDetail()
        .autoTagString("cote")
        .documentCount(1578L)
        .textStringForTagging("cote")
        .intent("reservation-make");
    final TranscriptionDocumentDetail doc2 = new TranscriptionDocumentDetail()
        .autoTagString("coté")
        .documentCount(1149L)
        .textStringForTagging("coté")
        .intent("reservation-cancel");
    final TranscriptionDocumentDetail doc3 = new TranscriptionDocumentDetail()
        .autoTagString("côte")
        .documentCount(317L)
        .textStringForTagging("côte")
        .intent("reservation-vague");
    final TranscriptionDocumentDetail doc4 = new TranscriptionDocumentDetail()
        .autoTagString("côté")
        .documentCount(817L)
        .textStringForTagging("côté")
        .intent("reservation-vague");
    List<TranscriptionDocumentDetail> documents = new ArrayList<TranscriptionDocumentDetail>() {
      private static final long serialVersionUID = -7575445427985827051L;

      {
        add(doc1);
        add(doc2);
        add(doc3);
        add(doc4);
      }
    };

    Locale french = Locale.forLanguageTag("fr-FR");
    List<TranscriptionDocumentDetail> sortedDocuments = SortUtil
        .sortTranscriptions(documents, new ArrayList<String>() {
          private static final long serialVersionUID = -8427434239520722725L;

          {
            add("uniqueTextString:DESC");
          }
        }, french);

    // Order specified by https://en.wikipedia.org/wiki/Alphabetical_order Unicode® Technical Standard #10
    TranscriptionDocumentDetail[] expected = new TranscriptionDocumentDetail[]{
        doc4,
        doc2,
        doc3,
        doc1
    };

    assertArrayEquals(expected, sortedDocuments.toArray());
  }

  @Test
  public void testSortTranscriptionsByIntentWithLocaleDESC() throws JsonProcessingException {
    final TranscriptionDocumentDetail doc1 = new TranscriptionDocumentDetail()
        .autoTagString("cote")
        .documentCount(1578L)
        .textStringForTagging("cote")
        .intent("cote-test");
    final TranscriptionDocumentDetail doc2 = new TranscriptionDocumentDetail()
        .autoTagString("coté")
        .documentCount(1149L)
        .textStringForTagging("coté")
        .intent("coté-test");
    final TranscriptionDocumentDetail doc3 = new TranscriptionDocumentDetail()
        .autoTagString("côte")
        .documentCount(317L)
        .textStringForTagging("côte")
        .intent("côte-test");
    final TranscriptionDocumentDetail doc4 = new TranscriptionDocumentDetail()
        .autoTagString("côté")
        .documentCount(817L)
        .textStringForTagging("côté")
        .intent("côté-test");
    final TranscriptionDocumentDetail doc5 = new TranscriptionDocumentDetail()
        .autoTagString("côté")
        .documentCount(817L)
        .textStringForTagging("côté");
    List<TranscriptionDocumentDetail> documents = new ArrayList<TranscriptionDocumentDetail>() {
      private static final long serialVersionUID = -7575445427985827051L;

      {
        add(doc1);
        add(doc2);
        add(doc3);
        add(doc4);
        add(doc5);
      }
    };

    Locale french = Locale.forLanguageTag("fr-FR");
    List<TranscriptionDocumentDetail> sortedDocuments = SortUtil
        .sortTranscriptions(documents, new ArrayList<String>() {
          private static final long serialVersionUID = -8427434239520722725L;

          {
            add("manualTag:DESC");
          }
        }, french);

    // Order specified by https://en.wikipedia.org/wiki/Alphabetical_order Unicode® Technical Standard #10
    TranscriptionDocumentDetail[] expected = new TranscriptionDocumentDetail[]{
        doc4,
        doc2,
        doc3,
        doc1,
        doc5
    };

    assertArrayEquals(expected, sortedDocuments.toArray());
  }
}
