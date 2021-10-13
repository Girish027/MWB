package com.tfs.learningsystems.ui.model.config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"case-normalization", "input-match", "regex-modify",
    "caseSensitiveStem", "regex-word-class-substitution",
    "caseSensitiveWordClassSubstitution",
    "whitespace-normalization", "removeHTMLEncoding",
    "removeEmails", "removeURL", "removeNumber"})
public class Transformations {

  @JsonProperty("case-normalization")
  private Boolean caseNormalization;
  @JsonProperty("input-match")
  private List<InputMatch> inputMatch = null;
  @JsonProperty("regex-modify")
  private List<RegexModify> regexModify = null;
  @JsonProperty("caseSensitiveStem")
  private Boolean caseSensitiveStem;
  @JsonProperty("regex-word-class-substitution")
  private List<RegexWordClassSubstitution> regexWordClassSubstitution = null;
  @JsonProperty("caseSensitiveWordClassSubstitution")
  private Boolean caseSensitiveWordClassSubstitution;
  @JsonProperty("whitespace-normalization")
  private Boolean whitespaceNormalization;
  @JsonProperty("removeHTMLEncoding")
  private Boolean removeHTMLEncoding;
  @JsonProperty("removeEmails")
  private Boolean removeEmails;
  @JsonProperty("removeURL")
  private Boolean removeURL;
  @JsonProperty("removeNumber")
  private Boolean removeNumber;
  @JsonIgnore
  private Map<String, Object> additionalProperties =
      new HashMap<String, Object>();

  @JsonProperty("case-normalization")
  public Boolean getCaseNormalization() {
    return caseNormalization;
  }

  @JsonProperty("case-normalization")
  public void setCaseNormalization(Boolean caseNormalization) {
    this.caseNormalization = caseNormalization;
  }

  @JsonProperty("input-match")
  public List<InputMatch> getInputMatch() {
    return inputMatch;
  }

  @JsonProperty("input-match")
  public void setInputMatch(List<InputMatch> inputMatch) {
    this.inputMatch = inputMatch;
  }

  @JsonProperty("regex-modify")
  public List<RegexModify> getRegexModify() {
    return regexModify;
  }

  @JsonProperty("regex-modify")
  public void setRegexModify(List<RegexModify> regexModify) {
    this.regexModify = regexModify;
  }

  @JsonProperty("caseSensitiveStem")
  public Boolean getCaseSensitiveStem() {
    return caseSensitiveStem;
  }

  @JsonProperty("caseSensitiveStem")
  public void setCaseSensitiveStem(Boolean caseSensitiveStem) {
    this.caseSensitiveStem = caseSensitiveStem;
  }

  @JsonProperty("regex-word-class-substitution")
  public List<RegexWordClassSubstitution> getRegexWordClassSubstitution() {
    return regexWordClassSubstitution;
  }

  @JsonProperty("regex-word-class-substitution")
  public void setRegexWordClassSubstitution(
      List<RegexWordClassSubstitution> regexWordClassSubstitution) {
    this.regexWordClassSubstitution = regexWordClassSubstitution;
  }

  @JsonProperty("caseSensitiveWordClassSubstitution")
  public Boolean getCaseSensitiveWordClassSubstitution() {
    return caseSensitiveWordClassSubstitution;
  }

  @JsonProperty("caseSensitiveWordClassSubstitution")
  public void setCaseSensitiveWordClassSubstitution(
      Boolean caseSensitiveWordClassSubstitution) {
    this.caseSensitiveWordClassSubstitution =
        caseSensitiveWordClassSubstitution;
  }

  @JsonProperty("whitespace-normalization")
  public Boolean getWhitespaceNormalization() {
    return whitespaceNormalization;
  }

  @JsonProperty("whitespace-normalization")
  public void setWhitespaceNormalization(Boolean whitespaceNormalization) {
    this.whitespaceNormalization = whitespaceNormalization;
  }

  @JsonProperty("removeHTMLEncoding")
  public Boolean getRemoveHTMLEncoding() {
    return removeHTMLEncoding;
  }

  @JsonProperty("removeHTMLEncoding")
  public void setRemoveHTMLEncoding(Boolean removeHTMLEncoding) {
    this.removeHTMLEncoding = removeHTMLEncoding;
  }

  @JsonProperty("removeEmails")
  public Boolean getRemoveEmails() {
    return removeEmails;
  }

  @JsonProperty("removeEmails")
  public void setRemoveEmails(Boolean removeEmails) {
    this.removeEmails = removeEmails;
  }

  @JsonProperty("removeURL")
  public Boolean getRemoveURL() {
    return removeURL;
  }

  @JsonProperty("removeURL")
  public void setRemoveURL(Boolean removeURL) {
    this.removeURL = removeURL;
  }

  @JsonProperty("removeNumber")
  public Boolean getRemoveNumber() {
    return removeNumber;
  }

  @JsonProperty("removeNumber")
  public void setRemoveNumber(Boolean removeNumber) {
    this.removeNumber = removeNumber;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("caseNormalization", caseNormalization)
        .append("inputMatch", inputMatch)
        .append("regexModify", regexModify)
        .append("caseSensitiveStem", caseSensitiveStem)
        .append("regexWordClassSubstitution",
            regexWordClassSubstitution)
        .append("caseSensitiveWordClassSubstitution",
            caseSensitiveWordClassSubstitution)
        .append("whitespaceNormalization", whitespaceNormalization)
        .append("removeHTMLEncoding", removeHTMLEncoding)
        .append("removeEmails", removeEmails)
        .append("removeURL", removeURL)
        .append("removeNumber", removeNumber)
        .append("additionalProperties", additionalProperties)
        .toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(whitespaceNormalization)
        .append(inputMatch).append(removeEmails).append(removeNumber)
        .append(regexWordClassSubstitution).append(regexModify)
        .append(caseSensitiveStem).append(additionalProperties)
        .append(caseNormalization).append(removeHTMLEncoding)
        .append(caseSensitiveWordClassSubstitution).append(removeURL)
        .toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof Transformations) == false) {
      return false;
    }
    Transformations rhs = ((Transformations) other);
    return new EqualsBuilder()
        .append(whitespaceNormalization, rhs.whitespaceNormalization)
        .append(inputMatch, rhs.inputMatch)
        .append(removeEmails, rhs.removeEmails)
        .append(removeNumber, rhs.removeNumber)
        .append(regexWordClassSubstitution,
            rhs.regexWordClassSubstitution)
        .append(regexModify, rhs.regexModify)
        .append(caseSensitiveStem, rhs.caseSensitiveStem)
        .append(additionalProperties, rhs.additionalProperties)
        .append(caseNormalization, rhs.caseNormalization)
        .append(removeHTMLEncoding, rhs.removeHTMLEncoding)
        .append(caseSensitiveWordClassSubstitution,
            rhs.caseSensitiveWordClassSubstitution)
        .append(removeURL, rhs.removeURL).isEquals();
  }

}
