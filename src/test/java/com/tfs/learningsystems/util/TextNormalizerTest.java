package com.tfs.learningsystems.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.ConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class TextNormalizerTest {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private ConfigManager configManager;

  @Autowired
  private TextNormalizer textNormalizer;

  @Before
  public void setUp() throws Exception {
    configManager.oldReloadDefaultConfig(appConfig.getOldEnglishConfigArchiveFilename(),
        "system_default_en_0", "en");
    textNormalizer.init("system_default_en_0", false);
  }

  @Test
  public void init() throws Exception {
    textNormalizer.init("system_default_en_0", false);
  }

  @Test
  public void processText() throws Exception {
    String text = "unit tests";
    String processText = textNormalizer.processText(text);
    assertNotEquals(text, processText);
    assertEquals("unit test", processText);
  }

  @Test
  public void processTextRemoveDuplicate() throws Exception {
    String text = "test test";
    assertEquals("test", textNormalizer.processText(text));
  }

  @Test
  public void processTextStem() throws Exception {
    String text = "tests";
    assertEquals("test", textNormalizer.processText(text));
  }

  @Test
  public void processTextExpand() throws Exception {
    String text = "mightn't";
    assertEquals("might not", textNormalizer.processText(text));
  }

  @Test
  public void processTextWordClass() throws Exception {
    String text = "two";
    assertEquals("_class_number", textNormalizer.processText(text));
  }

  @Test
  public void processTextLocation() throws Exception {
    String text = "california";
    assertEquals("_class_location_domestic", textNormalizer.processText(text));
  }


}
