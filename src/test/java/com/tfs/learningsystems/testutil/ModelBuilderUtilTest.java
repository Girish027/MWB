package com.tfs.learningsystems.testutil;

import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModel;
import java.util.Calendar;
import java.util.Collections;

/**
 * Created by huzefa.siyamwala on 8/23/17.
 */
public class ModelBuilderUtilTest {


  public static TFSModel getDummyModelConfig() {
    TFSModel model = new TFSModel();
    model.setName("test model");
    model.setProjectId("1234");
    model.setConfigId("1");
    model.setUserId("1234");
    model.setDatasetIds(Collections.singletonList("1234"));
    model.setVersion(1);
    model.setCreatedAt(Calendar.getInstance().getTimeInMillis() - 1);
    model.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
    return model;
  }

  public static ModelBO getDummyModel(String name, String cid, String projectId, String datasetId,
      String userId, String accuracy, String weightedFScore) {
    ModelBO model = new ModelBO();
    model.setName(name);
    model.setCid(cid);
    model.setProjectId(Integer.parseInt(projectId));
    model.setConfigId(1);
    model.setUserId(userId);
    model.setDatasetIds(Collections.singletonList(datasetId));
    model.setVersion(1);
    model.setCreatedAt(Calendar.getInstance().getTimeInMillis() - 1);
    model.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
    model.setModelAccuracy(accuracy);
    model.setModelWeightedFScore(weightedFScore);
    return model;
  }

  public static ModelConfigBO getDummyModelConfig(String name, String userId, String cid, String projectId, String configFile) {
    ModelConfigBO modelConfig = new ModelConfigBO();
    modelConfig.setName(name);
    modelConfig.setUserId(userId);
    modelConfig.setCid(cid);
    modelConfig.setDescription("Default en Model Config");
    modelConfig.setProjectId(Integer.parseInt(projectId));
    modelConfig.setCreatedAt(Calendar.getInstance().getTimeInMillis() - 1);
    modelConfig.setConfigFile(configFile);
    modelConfig.setCreatedAt(Calendar.getInstance().getTimeInMillis() - 1);
    modelConfig.setModifiedAt(Calendar.getInstance().getTimeInMillis());
    return modelConfig;
  }
}
