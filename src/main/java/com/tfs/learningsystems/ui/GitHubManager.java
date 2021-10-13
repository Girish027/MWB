package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.MwbItsClientMapBO;
import com.tfs.learningsystems.ui.model.TFSProjectModel;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.kohsuke.github.GHRelease;


public interface GitHubManager {

  public void createModelConfigs(String clientId, List<ModelBO> modelsToDeploy,
      Map<Integer, byte[]> projectModelFiles, Map<Integer, byte[]> projectConfigFiles,
      List<TFSProjectModel> projectModelsinResponse) throws IOException;

  public GHRelease checkOrCreateRepositories(MwbItsClientMapBO mwbItsClientMapBO, String clientId,
      Map<Integer, String> validProjects, Map<Integer, byte[]> modelFiles,
      Map<Integer, byte[]> configFiles,String gitHubBaseOrg, String repositoryName, StringBuilder modelsSummary,
      String tag,String userEmail);


}
