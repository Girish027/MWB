package com.tfs.learningsystems.ui.model;

import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ProjectBO;

public class DatasetDetailsModel {

  private ClientBO clientBO = null;

  private ProjectBO projectBO = null;

  private DatasetBO datasetBO = null;

  public ClientBO getClientBO() {
    return clientBO;

  }

  public void setClientBO(ClientBO clientBO) {
    this.clientBO = clientBO;
  }

  public ProjectBO getProjectBO() {
    return projectBO;
  }

  public void setProjectBO(ProjectBO projectBO) {
    this.projectBO = projectBO;
  }

  public DatasetBO getDatasetBO() {
    return datasetBO;
  }

  public void setDatasetBO(DatasetBO datasetBO) {
    this.datasetBO = datasetBO;
  }


}
