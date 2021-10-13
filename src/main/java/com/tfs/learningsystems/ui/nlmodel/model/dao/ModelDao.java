package com.tfs.learningsystems.ui.nlmodel.model.dao;

import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModel;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState;
import java.util.ArrayList;
import java.util.List;

public interface ModelDao {

  public List<TFSModel> getModelsForProject(String projectId);

  public TFSModelJobState getModelJobStatus(String id, String modelType);

  public void updateModelJobStatusByModelId(String modelId, String modelType, TFSModelJobState status);

  public void updateModelJobStatusModelTypeByModelIdModelType(String modelId, TFSModelJobState status, String currentModelType);

  public List<TFSModel> getModelsForModelIds( ArrayList<String> strings);
}
