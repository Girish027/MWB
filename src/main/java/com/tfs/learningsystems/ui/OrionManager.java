package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState;
import java.io.File;

/**
 * Created by huzefa.siyamwala on 8/22/17.
 */
public interface OrionManager {

  public void reinit();

  public String postModelToOrion(File inputFile, File configFile, File speechWordClassFile, Boolean isUnbundled, String digitalHostedUrl, String modelType, String modelTechnology, String vectorizerVersion);

  public String patchModelToOrion(String modelUUID, File speechWordClassFile, Boolean isUnbundled, String digitalHostedUrl, File inputFile, String modelType, String modelTechnology, String vectorizerVersion);

  public TFSModelJobState getModelBuildingStatus(String modelUUID);

  public File getBuiltModelFromOrion(String modelUUID);

  public File getDigitalModelFromOrion(String modelUUID);

  public File getSpeechModelFromOrion(String modelUUID);

  public File getBuildModelStatsFromOrion(String modelUUID);

  public File getModelTrainingOutputsFromOrion(String modelUUID);

  public Boolean deleteBuiltModelFromOrion(String modelUUID);

  public byte[] getBuiltModelByteStreamFromOrion(String modelUUID);

}
