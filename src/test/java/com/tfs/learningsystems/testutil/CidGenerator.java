package com.tfs.learningsystems.testutil;

import com.tfs.learningsystems.db.BusinessObject;
import org.springframework.stereotype.Component;

@Component
public class CidGenerator {

  // The part of the Id that is actually generated (and matters)
  private final int FILL_SIZE = 17;

  // Total size of the Id.  3 + 17
  private final int KEY_SIZE = 20;

  // The alphabet.
  private final char[] ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  // This is what we use to fill our key
  private final char FILLER = '0';

  public String generateTestCId(BusinessObject busObj, int seq) {
    String nextSeq = Long.toString(seq, 36);

    String id = fillRandomString(nextSeq, busObj.getDbPrefix());
    return id;
  }

  private String fillRandomString(String init, String type) {
    StringBuilder sb = new StringBuilder(init);

    // Fill it up
    int originalSize = init.length();
    int fillSize = FILL_SIZE - originalSize;
    for (int i = 0; i < fillSize; i++) {
      int index = (int) (Math.random() * ALPHA.length);
      sb.insert(0, ALPHA[index]);
    }

    fillSize = FILL_SIZE - sb.length();
    for (int i = 0; i < fillSize; i++) {
      sb.insert(0, FILLER);
    }

    // Add the keyType
    sb.insert(0, type);

    String s = sb.toString();
    assert s.length() == KEY_SIZE;
    return s;
  }

}
