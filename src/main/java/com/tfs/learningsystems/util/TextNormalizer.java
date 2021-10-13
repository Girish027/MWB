package com.tfs.learningsystems.util;

import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.ui.ConfigManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tartarus.snowball.ext.PorterStemmer;

@Component
@Slf4j
public class TextNormalizer {

  private Map<String, String> wordExpansions;
  private Set<String> stopwords;
  private Set<String> stemmingExceptions;
  private Map<String, String> wordClasses;
  private Pattern stopwordPattern;
  private boolean isInitialized = false;


  @Autowired
  private ConfigManager configManager;


  public void init(String configName, boolean isReadJSONConfig) {
    ModelConfigBO config = configManager.getModelConfigDataByName(configName);
    wordExpansions = configManager.getWordExpansions(config, isReadJSONConfig);
    stopwords = configManager.getStopwords(config, isReadJSONConfig);
    stemmingExceptions = configManager.getStemmingExceptions(config, isReadJSONConfig);
    wordClasses = configManager.getWordClasses(config, isReadJSONConfig);
    stopwordPattern = this.compileStopwordRegex(stopwords);
    isInitialized = true;
  }

  public String processText(final String text) {

    if (!isInitialized) {
      return text;
    }

    // Replacing word expansions and contractions
    List<String> words = this.replaceContractions(text);

    // Stopword removal
    words = removeStopwords(words);

    List<String> processedWords = new ArrayList<>();

    for (String word : words) {

      // Word Classes
      if (word.startsWith("_class")) {
        processedWords.add(word);
        continue;
      }

      String stem = null;
      // Check the processed words list
      if (this.wordClasses.containsKey(word)) {
        stem = this.wordClasses.get(word);
      } else {

        // Check the stemming exception word list
        if (this.stemmingExceptions.contains(word)) {
          processedWords.add(word);
          continue;
        }

        stem = this.stemWord(word);

        if (!word.equals(stem)) {
          // Checking whether the stem can be further stemmed
          if (this.wordClasses.containsKey(stem)) {
            stem = this.wordClasses.get(stem);
          }
          this.wordClasses.put(word, stem);
        } else {
          this.wordClasses.put(word, stem);
        }

      }
      processedWords.add(stem);
    }

    // Stopword removal for the stemmed version
    processedWords = removeStopwords(processedWords);

    return processedWords.isEmpty() ? "" : String.join(" ", processedWords);
  }

  /**
   * Stem a single word tries to look up the word in the AllWords HashMap If the word is not found
   * it is stemmed with WordNet and put into AllWords
   *
   * @param word word to be stemmed
   * @return stemmed word
   */
  private String stemWord(String word) {
    PorterStemmer stemmer = new PorterStemmer();
    stemmer.setCurrent(word);
    if (stemmer.stem()) {
      return stemmer.getCurrent();
    } else {
      return word;
    }
  }

  private List<String> replaceContractions(String text) {

    if (!text.contains("'")) {
      return Arrays.asList(text.toLowerCase().split("\\s+"));
    }

    List<String> expanded = new ArrayList<>();

    for (String word : text.toLowerCase().split("\\s+")) {
      if (!word.contains("'")) {
        expanded.add(word);
      } else {
        if (this.wordExpansions.containsKey(word)) {
          expanded.addAll(Arrays.asList(
              this.wordExpansions.get(word).split("\\s+")));
        } else {
          if ((word.split("'").length > 1)) {
            // Example : words like "'sub" and "'" need to be filtered here. This was added for chat data
            for (String w : word.split("'")) {
              if (!Objects.equals(w, "'") && !w.isEmpty()) {
                expanded.add(w);
                break;
              }
            }
          } else {
            expanded.add(word);
          }

        }
      }
    }
    return expanded;
  }

  /**
   * Compile stopword regex
   */
  private Pattern compileStopwordRegex(Set<String> stopwords) {
    ArrayList<String> stpWrds = new ArrayList<>();
    stpWrds.addAll(stopwords);
    Collections.sort(stpWrds);
    Collections.reverse(stpWrds);
    String prefix = "";
    StringBuilder pttrn = new StringBuilder("\\b(?:");
    for (String s : stpWrds) {
      pttrn.append(prefix);
      prefix = "|";
      pttrn.append(s);
    }
    pttrn.append(")'?s?\\b");
    return Pattern.compile(pttrn.toString());
  }

  // Remove stopwords - inclusing seqeunces of words
  private List<String> removeStopwords(List<String> words) {
    String text = String.join(" ", words);
    List<String> stopwordsRemoved = new ArrayList<>();
    String stopwordRemoved =
        this.stopwordPattern.matcher(text.toLowerCase()).replaceAll("")
            .trim();
    String processedSentence = DuplicateHandler
        .mergeConsecutiveDuplicates(stopwordRemoved, true, false);
    stopwordsRemoved.addAll(Arrays.asList(processedSentence.split("\\s+")));
    return stopwordsRemoved;
  }
}
