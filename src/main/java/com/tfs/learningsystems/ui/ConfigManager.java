package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.ui.model.ModelConfigCollection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public interface ConfigManager {

  public void reloadDefaultConfig(String configArchiveLocation, String configId, String language)
      throws
      IOException;

  public void oldReloadDefaultConfig(String configArchiveLocation, String configName,
      String language) throws
      IOException;

  public ModelConfigBO addModelConfig(ModelConfigBO modelConfig,
      final InputStream fileInputStream);

  public ModelConfigBO addModelConfig(ModelConfigBO modelConfig);

  //TODO - Remove after client Id is part of all APIs.
  public ModelConfigBO getModelConfigById(String configId);

  public ModelConfigBO getModelConfigById(final String clientId, String configId);

  //TODO - Remove after client Id is part of all APIs.
  public ModelConfigBO getModelConfigByName(String configName);

  public ModelConfigCollection getModelConfigsByProject(String clientId, String projectId);

  public ModelConfigBO getProjectsLatestModelConfig(String projectId);

  public ModelConfigBO getModelConfigDataById(final String clientId, final String configId);

  public ModelConfigBO getModelConfigDataById(final String configId);

  public ModelConfigBO getModelConfigDataByName(final String configName);

  public File getModelConfigFile(ModelConfigBO modelConfig) throws IOException;

  public ModelConfigBO updateModelConfigById(String configId,
      ModelConfigBO modelConfig);

  public ModelConfigCollection listConfigs(Integer limit, Integer startIndex);

  public Map<String, String> getWordExpansions(ModelConfigBO modelConfig, boolean isReadJSONConfig);

  public Set<String> getStopwords(ModelConfigBO modelConfig, boolean isReadJSONConfig);

  public Set<String> getStemmingExceptions(ModelConfigBO modelConfig, boolean isReadJSONConfig);

  public Map<String, String> getWordClasses(ModelConfigBO modelConfig, boolean isReadJSONConfig);
}
