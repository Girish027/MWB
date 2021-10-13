package com.tfs.learningsystems.helper;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.ClientManager;
import com.tfs.learningsystems.ui.ProjectManager;
import com.tfs.learningsystems.util.AuthUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FileUploadHelper {

  @Autowired
  @Qualifier("appConfig")
  private AppConfig appConfig;

  @Autowired
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  public String getFileUploadPath(ProjectBO projectDetail, String userId,
      String category, String fileName, boolean isimport) throws ApplicationException {
    String clientName = null;
    String projectName = null;
    String userName = null;

    final String repositoryRoot = this.appConfig.getFileUploadRepositoryRoot();
    final FileSystem fileSystem = FileSystems.getDefault();

    ClientBO cd = clientManager.getClientById(Integer.toString(projectDetail.getClientId()));
    try {
      clientName = URLEncoder.encode(cd.getName(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("Could not encode the client name to decide on file path", e);
      throw new ApplicationException("Could not encode the client name to decide on file path");
    }

    try {
      projectName = URLEncoder.encode(projectDetail.getName(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("Could not encode the project name to decide on file path", e);
      throw new ApplicationException("Could not encode the project name to decide on file path");
    }

    userName = AuthUtil.getPrincipalFromSecurityContext(null);

    if (category == null || "".equals(category)) {
      category = "general";
    }

    String action = (isimport) ? "import" : "export";
    ///data/clients/Amex/projects/proj1/users/manish/taggingguide/export
    final Path uploadDirPath = fileSystem.getPath(repositoryRoot, "clients", clientName,
        "projects", projectName, "users", userName, category, action);

    if (Files.notExists(uploadDirPath, LinkOption.NOFOLLOW_LINKS)) {
      try {
        if(System.getProperty("os.name").contains("Windows")){
          Files.createDirectories(uploadDirPath);
        }else {
          final Set<PosixFilePermission> permissions =
          PosixFilePermissions.fromString("rwxr-xr-x");
          final FileAttribute<Set<PosixFilePermission>> fileAttributes =
          PosixFilePermissions.asFileAttribute(permissions);
          Files.createDirectories(uploadDirPath, fileAttributes);
        }
      } catch (IOException e) {
        log.error("Failed to create directory - " + uploadDirPath, e);
        throw new ApplicationException(
            "Could not create file upload directory " + uploadDirPath.toString());
      }     
    }

    return uploadDirPath.resolve(fileName).toString();
  }

  public String getFileUploadPath(String projectId, String userId,
      String category, String fileName) throws ApplicationException {
    ProjectBO pd = this.projectManager.getProjectById(projectId);
    return this.getFileUploadPath(pd, userId, category, fileName, true);
  }

}
