package com.tfs.learningsystems.util;

import java.util.regex.Pattern;

public class DuplicateHandler {

  private static Pattern dup = Pattern.compile("\\b(\\w+)(\\s*\\1\\b)+");
  private static Pattern dupExcludeClass = Pattern.compile("\\b((?!_class)\\w+)(\\s*\\1\\b)+");
  private static Pattern dupOnlyWordClass = Pattern.compile("\\b(_class\\w+)(\\s*\\1\\b)+");

  protected static String mergeConsecutiveDuplicates(String sentence) {
    /*
     * this is to remove consecutive duplicates like "this this". This is done after the stopword
     * removal because the duplicate words can be separated by some stop word eg: "this uh um this"
     * This will also merge duplicate words like "_class_number _class_number
     */
    String consecutiveDupRemoved = dup.matcher(sentence).replaceAll("$1");
    return consecutiveDupRemoved;
  }

  protected static String mergeConsecutiveDuplicatesExcludingClass(String sentence) {
    /*
     * this is to remove consecutive duplicates like "this this". This is done after the stopword
     * removal because the duplicate words can be separated by some stop word eg: "this uh um this".
     * This would not merge duplicates like "_class_number _class_number"
     */
    String consecutiveDupRemoved = dupExcludeClass.matcher(sentence).replaceAll("$1");
    return consecutiveDupRemoved;
  }

  protected static String mergeConsecutiveDuplicatesOnlyWordClass(String sentence) {
    /*
     * this is to remove consecutive duplicates like "_class_number _class_number". This is done after the stopword
     * removal because the duplicate words can be separated by some stop word eg: "this uh um this"
     */
    String consecutiveDupRemoved = dupOnlyWordClass.matcher(sentence).replaceAll("$1");
    return consecutiveDupRemoved;
  }


  public static String mergeConsecutiveDuplicates(String sentence, boolean mergeNonClassWords,
      boolean mergeClassWords) {
    String result = sentence;
    if (mergeNonClassWords && mergeClassWords) {
      result = mergeConsecutiveDuplicates(sentence);
    } else if (mergeNonClassWords) {
      result = mergeConsecutiveDuplicatesExcludingClass(sentence);
    } else if (mergeClassWords) {
      result = mergeConsecutiveDuplicatesOnlyWordClass(sentence);
    }
    return result;
  }
}
