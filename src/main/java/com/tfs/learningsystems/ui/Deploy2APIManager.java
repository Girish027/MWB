package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.MwbItsClientMapBO;
import com.tfs.learningsystems.ui.nlmodel.model.TFSDeploy2Module;
import com.tfs.learningsystems.ui.nlmodel.model.TFSDeploy2ModulePackage;
import com.tfs.learningsystems.ui.nlmodel.model.TFSDeploy2Target;
import java.util.List;
import org.kohsuke.github.GHRelease;


public interface Deploy2APIManager {

  public List<TFSDeploy2Module> findModule(String moduleName);

  public Integer createModule(Boolean moduleHidden, String moduleName, String moduleDesc,String gitHubOrgName,
      String repositoryName, Boolean packagePreserveTime, Boolean compress, Boolean deep_package,
      String contactUser);

  public List<TFSDeploy2ModulePackage>  findModulePackages(Integer moduleId, String packageName);

  public Boolean createPackage(String moduleId, String packageName, String userEmail,
      String gitHubTag, String gitHubTagRefType, String tagDetailComments, boolean isGood);

  public String createPackageOrModuleName(MwbItsClientMapBO mwbItsClientMapBO, String creationType, String tagString);

  Integer loadOrCreateConvensionModule( String gitHubOrgName, String repositoryName, String moduleName, String userEmail);

  String loadOrCreateConvensionPackage(MwbItsClientMapBO mwbItsClientMapBO, GHRelease gitHubRelease, Integer moduleId, String moduleName);

  String extractPackageFromModuleAndCheck(TFSDeploy2Module tfsDeploy2Module,
      GHRelease gitHubRelease, String userEmail);
}
