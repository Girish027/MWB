package com.tfs.learningsystems.testutil;

import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.IDCreatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("dataCreatorUtils")
public class DataCreatorUtils {

  @Autowired
  private IDCreatorUtil idCreatorUtil;

  public AddIntentRequest createRandomIntentRequest() {
    AddIntentRequest addIntentRequest = new AddIntentRequest();
    addIntentRequest.setUsername(createRandomUser());
    addIntentRequest.setIntent(this.createRandomIntent());
    addIntentRequest.setRutag(this.createRandomRutag());
    return addIntentRequest;
  }

  public String createRandomUser() {
    StringBuilder userId = new StringBuilder(idCreatorUtil.randomText(4)).append(Constants.DOT)
        .append(idCreatorUtil.randomText(4));
    return userId.toString();
  }

  public String createRandomIntent() {
    StringBuilder intent = new StringBuilder(idCreatorUtil.randomText(6)).append(Constants.HYPHEN)
        .append(idCreatorUtil.randomText(6));
    return intent.toString();
  }

  public String createRandomIntentLowerCase() {
    StringBuilder intent = new StringBuilder(idCreatorUtil.randomText(6)).append(Constants.HYPHEN)
        .append(idCreatorUtil.randomText(6));
    return intent.toString().toLowerCase();
  }

  public String createRandomrutagLowerCase() {
    StringBuilder userId = new StringBuilder(idCreatorUtil.randomText(6))
        .append(Constants.UNDER_SCORE).append(idCreatorUtil.randomText(6));
    return userId.toString().toLowerCase();
  }

  public String createRandomRutag() {
    StringBuilder rutag = new StringBuilder(idCreatorUtil.randomText(6))
        .append(Constants.UNDER_SCORE).append(idCreatorUtil.randomText(6));
    return rutag.toString();
  }


  public Integer createRandomId(int length) {

    return idCreatorUtil.randomNumber(length);
  }


  public String createRandomIdStr(int length) {

    return idCreatorUtil.randomNumberStr(length);
  }

  public String createRandomStringData() {

    int length = 25;

    StringBuilder text1 = new StringBuilder(idCreatorUtil.randomNumberStr(length / 4))
        .append(Constants.EMPTY_STRING)
        .append(idCreatorUtil.randomNumberStr(length / 4)).append(Constants.EMPTY_STRING)
        .append(idCreatorUtil.randomNumberStr(length / 4)).append(Constants.EMPTY_STRING)
        .append(idCreatorUtil.randomNumberStr(length / 4));

    return text1.toString();
  }


  public TaggingGuideDocument createTaggingGuide(String projectId) {

    String intent = "new-tag";
    String rutag = "new-rutag";

    // String uniqueID = idCreatorUtil.createIntentRutagId(intent, rutag, 25);

    TaggingGuideDocument add_tag = new TaggingGuideDocument();
    add_tag.setIntent(intent);
    add_tag.setRutag(rutag);
    add_tag.setKeywords("tag test");
    add_tag.setComments("Added new tag in tagging guide");
    // add_tag.setClassificationId(uniqueID);
    return add_tag;
  }
}
