package com.tfs.learningsystems.ui.nlmodel.model;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.ui.ConfigManager;
import com.tfs.learningsystems.ui.DataManagementManager;
import com.tfs.learningsystems.ui.OrionManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class TFSModelJob extends TFSModelJobBase implements Callable<TFSModelJobResult> {

  private String clientId;
  private String projectId;
  private String modelId;
  private List<String> datasetIds;
  private ModelConfigBO configDetail;
  private DataManagementManager dataManagementManager;
  private ConfigManager configManager;
  private RestTemplate restTemplate;
  private OrionManager orionManager;

  private AppConfig appConfig;
  private ModelConfigBO speechConfig;
  private Boolean isUnbundled;
  private String digitalHostedUrl;
  private String modelType;
  private String modelTechnology;
  private String vectorizerVersion;

  @Override
  public TFSModelJobResult call() throws Exception {

    File uploadedFile = null;
    String modelUUID;

    if(datasetIds != null && !datasetIds.isEmpty()){
      uploadedFile = dataManagementManager
              .export(clientId, projectId, datasetIds, null, true, true, true, modelType);
      log.info("File uploaded at: " + uploadedFile.getPath());
    }

    File configFile = configManager.getModelConfigFile(configDetail);
    File speechWordClassFile = null;

    if(speechConfig!=null && modelId != null) {
      speechWordClassFile = configManager.getModelConfigFile(speechConfig);
      modelUUID = orionManager.patchModelToOrion(modelId, speechWordClassFile, isUnbundled, digitalHostedUrl, uploadedFile, null, modelTechnology, vectorizerVersion);
    } else {
      modelUUID = orionManager.postModelToOrion(uploadedFile, configFile, speechWordClassFile, isUnbundled, digitalHostedUrl, modelType, modelTechnology, vectorizerVersion);
    }

    log.info(
            String.format("For project id: %s, Orion started building with UUID: %s",
                    projectId, modelUUID));
    TFSModelJobResult jobResult = new TFSModelJobResult();
    jobResult.setMessage(modelUUID);
    jobResult.setStatus(TFSModelJobState.Status.RUNNING);
    return jobResult;
  }
}
