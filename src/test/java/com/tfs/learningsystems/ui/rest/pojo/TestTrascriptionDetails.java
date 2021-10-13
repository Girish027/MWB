package com.tfs.learningsystems.ui.rest.pojo;

public class TestTrascriptionDetails {

  public TestTrascriptionDetails(String transcriptionHash) {
    this.transcriptionHash = transcriptionHash;
  }


  public String getFilename() {

    return filename;
  }

  public void setFilename(String filename) {

    this.filename = filename;
  }

  public String getRuTag() {

    return ruTag;
  }

  public void setRuTag(String ruTag) {

    this.ruTag = ruTag;
  }

  public String getDocumentType() {

    return documentType;
  }

  public void setDocumentType(String documentType) {

    this.documentType = documentType;
  }

  public String getIntentTag() {

    return intentTag;
  }

  public void setIntentTag(String intentTag) {

    this.intentTag = intentTag;
  }

  public String getTranscriptionHash() {

    return transcriptionHash;
  }

  public void setTranscriptionHash(String transcriptionHash) {

    this.transcriptionHash = transcriptionHash;
  }

  public String getOrigibalString() {

    return origibalString;
  }

  public void setOrigibalString(String origibalString) {

    this.origibalString = origibalString;
  }

  public String getComments() {

    return comments;
  }

  public void setComments(String comments) {

    this.comments = comments;
  }


  private String comments;

  private String filename;


  private String ruTag;

  private String documentType;


  private String intentTag;


  private String transcriptionHash;


  private String origibalString;

}
