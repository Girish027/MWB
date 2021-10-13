package com.tfs.learningsystems.db;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A key structure.
 *
 * Form:
 *
 * [3 bytes TYPE_SIZE][2  byte INSTANCE_SIZE][Random Characters padded to sequence][Sequence]
 */
public class DbId implements Comparable<DbId> {

  private static final String EMPTY_ID_STRING = "00000000000000000000";
  public static final DbId EMPTY = new DbId(EMPTY_ID_STRING);

  // This is the 3 characters at the beginning of the Id
  protected static final int TYPE_SIZE = 3;

  // The part of the Id that is actually generated (and matters)
  private static final int FILL_SIZE = 17;

  // Total size of the Id.  3 + 17
  private static final int KEY_SIZE = 20;

  // The alphabet.
  private static final char[] ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  // This is what we use to fill our key
  private static final char FILLER = '0';


  private final String databaseValue;

  public DbId(String id) {
    this.databaseValue = id;
  }

  @Override
  public String toString() {
    return this.databaseValue;
  }

  @Override
  public int hashCode() {
    return this.databaseValue.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DbId)) {
      return false;
    }

    DbId id = (DbId) o;
    return DbId.equals(id, this);
  }

  public static DbId GenerateId(BusinessObject busObj, int seq) {
    String nextSeq = Long.toString(seq, 36);

    String id = fillRandomString(nextSeq, busObj.getDbPrefix());
    return new DbId(id);
  }

  public boolean isEmpty() {
    return DbId.isEmpty(this);
  }

  public boolean isValid() {
    return DbId.isValid(this);
  }

  public static boolean isEmpty(DbId id) {
    if (id == null) {
      return true;
    }

    return isEmpty(id.toString());
  }

  /**
   * Returns whether the given id is null, or zero length, or equal to the empty id string.
   *
   * @param id the given id
   * @return {@code true} if the given id is null, or zero length, or equal to the empty id string;
   *     {@code false} otherwise.
   */
  public static boolean isEmpty(String id) {
    if (id == null) {
      return true;
    }

    if (id.length() == 0) {
      return true;
    }

    return EMPTY_ID_STRING.equals(id);
  }

  public static boolean equals(DbId a, DbId b) {
    if (a == null && b == null) {
      return true;
    }
    if (isEmpty(a) && isEmpty(b)) {
      return true;
    }

    if (a != null && b != null && a.toString().equals(b.toString())) {
      return true;
    }

    return false;
  }


  public static boolean isValid(DbId dbId) {
    return !isEmpty(dbId) && dbId.toString().length() == KEY_SIZE;
  }

  /**
   * verify if the id string conforms to db id format
   */
  public static boolean isValid(String id) {
    return !isEmpty(id) && id.length() == KEY_SIZE;
  }

  public static String GetKeyPrefix(DbId dbId) {
    if (!isValid(dbId)) {
      return null;
    }
    String id = dbId.toString();
    String prefix = id.substring(0, TYPE_SIZE);

    return prefix;
  }

  /**
   * Pads the sequence with random characters so the Id cannot be guessed
   */
  private static String fillRandomString(String init, String type) {
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

  public static String GetCaseSafeString(DbId id) {
    char[] chars = id.toString().toCharArray();
    StringBuilder sb = new StringBuilder(chars.length + 1);

    int i = 0;

    // Skip the type and instance part
    for (; i < TYPE_SIZE && i < chars.length; i++) {
      sb.append(chars[i]);
    }

    boolean foundSeq = false;

    for (; i < chars.length; i++) {

      char c = chars[i];

      if (foundSeq) {
        sb.append(c);
        continue;
      }

      // If first time running into lowerCase or digit,
      // then we found the sequence part
      if (Character.isLowerCase(c) || Character.isDigit(c)) {
        sb.append("_");
        foundSeq = true;
      }
      sb.append(c);
    }
    return sb.toString();
  }

  @Override
  public int compareTo(DbId dbId) {
    // Assumption: null is before all strings
    if (this.databaseValue == null) {
      return (dbId.databaseValue == null) ? 0 : -1;
    }
    return (dbId.databaseValue == null) ? 1 : this.databaseValue.compareTo(dbId.databaseValue);
  }

  public long calculateSequence() {
    if (!DbId.isValid(this)) {
      return 0;
    }
    char[] chars = this.toString().toCharArray();
    StringBuilder sb = new StringBuilder(chars.length + 1);

    // Skip the type and instance part
    int i = TYPE_SIZE;

    boolean foundSeq = false;

    for (; i < chars.length; i++) {

      char c = chars[i];

      if (foundSeq) {
        sb.append(c);
        continue;
      }

      // If first time running into lowerCase or digit,
      // then we found the sequence part
      if (Character.isLowerCase(c) || Character.isDigit(c)) {
        sb.append(c);
        foundSeq = true;
      }
    }
    return Long.parseLong(sb.toString(), 36);
  }

  //
  // similar to Snowflake, without server ID, since it is used in a single test box.
  //
  enum Generator {
    Instance;

    private static final long Timestamp_Epoch =
        System.currentTimeMillis() - 1000;  // a rather recent point in time
    private static final int Sequence_modular = 4096;    // 2^12

    private int serverId = 0;
    private volatile AtomicLong sequence;

    private Generator() {

      this.sequence = new AtomicLong(0);

    }

    public long getNextNumber() {

      long timeFromEpoch = System.currentTimeMillis() - Timestamp_Epoch;

      long id = timeFromEpoch << 12 |
          (this.sequence.incrementAndGet() % Sequence_modular);
      return (id);
    }
  }

}
