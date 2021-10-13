package com.tfs.learningsystems.config;

import com.tfs.learningsystems.logging.AspectLogger;
import com.tfs.learningsystems.ui.ClientManager;
import com.tfs.learningsystems.ui.ConfigManager;
import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.Vertical;
import com.tfs.learningsystems.util.Constants;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.annotation.IntegrationComponentScan;



@Configuration
@IntegrationComponentScan
public class ResourceFileWatcher
    implements ApplicationListener<ApplicationReadyEvent> {

  @Autowired
  private Environment env;

  @Autowired
  private AppConfig appConfig;

  @Inject
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Autowired
  private ConfigManager configManager;



  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    try {
      Vertical.reloadValues(new String(Files.readAllBytes(Paths.get(
          appConfig.getResourceFileLocation() + "/verticals.csv")), StandardCharsets.UTF_8));
      DataType.reloadValues(new String(Files.readAllBytes(Paths.get(
          appConfig.getResourceFileLocation() + "/datatypes.csv")), StandardCharsets.UTF_8));

      if (!env.acceptsProfiles("test")) {
        // TODO : when we have other language configs create those defaults as well
        String language = Locale.ENGLISH.getLanguage();
        configManager.reloadDefaultConfig(appConfig.getEnglishConfigArchiveFilename(),
            Constants.DEFAULT_EN_CONFIG_NAME, language);

        //Adding dafaultconfig to be used for consistency report
        configManager.oldReloadDefaultConfig(appConfig.getOldEnglishConfigArchiveFilename(),
            "system_default_en_0", language);
      }
    } catch (IOException e) {
      AspectLogger.logException(e);
    }
  }


}
