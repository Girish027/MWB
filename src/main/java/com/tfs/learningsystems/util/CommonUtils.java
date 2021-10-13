package com.tfs.learningsystems.util;

import com.tfs.learningsystems.ui.model.ClientDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CommonUtils {

  public static String sanitize(String str) {
    if (str != null) {
      return str.replaceAll(Constants.NSBP_UNICODE, "").trim();
    }
    return null;
  }

  public static Map<String, Object> getUserAuthDetailsMap() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth != null) {
      return (HashMap<String, Object>)((OAuth2Authentication) auth)
              .getUserAuthentication().getDetails();
    }
    return null;
  }

  public static Map<String, Object> getAuthorizationMap() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth != null) {
      return (HashMap<String, Object>) ((OAuth2AuthenticationDetails) (auth
              .getDetails())).getDecodedDetails();
    }
    return null;
  }

  public static Map<String, String> getUserGroups() {
    Map<String, Object> authorizationInfoMap = getAuthorizationMap();
    if(authorizationInfoMap != null) {
      return (Map<String, String>) authorizationInfoMap.get(Constants.OKTA_USER_GROUPS);
    }
    return null;
  }

  public static Map<String, String> getUserClients() {
    Map<String, Object> authorizationInfoMap = getAuthorizationMap();
    if(authorizationInfoMap != null ) {
      return  (Map<String, String>) authorizationInfoMap.get(Constants.OKTA_USER_CLIENTS);
    }
    return null;
  }

  public static boolean isUserClientAuthorized(Map<String, ClientDetail>  clientsMap, String clientName) {
    return clientsMap != null && clientsMap.get(clientName) != null;
  }

  public static boolean isUserExternalType() {
    String email = null;
    String preferredName = null;
    String userType = null;
    Map<String, Object> detailsMap = getUserAuthDetailsMap();
    String userId = getUserId();
    Map<String, String> userGroups = getUserGroups();
    if(detailsMap != null && !detailsMap.isEmpty()) {
      email = (String) detailsMap.get(Constants.EMAIL);
      preferredName = (String) detailsMap.get(Constants.PREFERRED_USERNAME);
      userType = (String) detailsMap.get(Constants.USER_TYPE);
    }
    if(userId == null && userGroups != null
            && userGroups.containsKey(Constants.MWB_ROLE_CLIENTADMIN)) {
      return false;
    } else if(userType != null) {
      return userType.equals(Constants.EXTERNAL_USER);
    } else if(email != null) {
      return !email.endsWith(Constants.PRIMARY_EMAIL_SUFFIX);
    } else if(preferredName != null) {
      return !preferredName.endsWith(Constants.PREFERRED_EMAIL_SUFFIX);
    }
    return true;
  }

  public static boolean isUserSpecificClientType(String itsClientName) {
    Map<String, String> userClients = getUserClients();
    return userClients != null
            && (userClients.containsKey(Constants.STAR)
            || userClients.containsKey(itsClientName.toUpperCase()));
  }

  public static String getUserId() {
    Map<String, Object> detailsMap = getUserAuthDetailsMap();
    if(detailsMap != null && !detailsMap.isEmpty()) {
      return  (String) detailsMap.get(Constants.USER_SUB);
    }
    return null;
  }

  public static boolean isCBUser() {
    String userId = getUserId();
    Map<String, String> userGroups = getUserGroups();
    return userId == null && userGroups != null && userGroups.containsKey(Constants.MWB_ROLE_CLIENTADMIN);
  }

  public static void deleteFilesFromTempFolder(int hoursBefore){
    FileFilter fileFilter = new FileFilter(new String[]{"trainingOutputs","final","model_stats","config"}, hoursBefore);
    String tmpdir = System.getProperty(Constants.TEMP_FOLDER_SYSTEM_PROPERTY);
    if(tmpdir != null && fileFilter != null){
      CommonUtils.deleteFiles(fileFilter, tmpdir);
    }
  }

  public static void deleteFiles(FileFilter fileFilter, String dirLocation) {
    try {
      File parentDir = new File(dirLocation);
      String[] listOfTextFiles = parentDir.list(fileFilter);
        if (listOfTextFiles != null || listOfTextFiles.length != 0) {
          for (String f : listOfTextFiles) {
            Path filePath = Paths.get(dirLocation + Constants.FORWARD_SLASH + f);
            if (Files.isDirectory(filePath)) {
              deleteFiles(fileFilter, filePath.toString());
              Files.delete(filePath);
            } else {
              Files.delete(filePath);
            }
          }
        }
    } catch (Exception e) {
      log.error("Error while deleting files from provided directory", e);
    }
  }

  // This function is to read request URI and return value of client
  public static String getQueryParams(String url) {
    try {
      String[] clientSplit = url.split(Constants.CLIENTS_SLASH);
      if(clientSplit.length > 1) {
        String[] idSplit = clientSplit[1].split(Constants.FORWARD_SLASH);
        return idSplit[0];
      }
    }catch (Exception e) {
      log.error("Failed to get the clientID from request URL", e);
    }
    return null;
  }

  // This function is to read whole config file data and converts in to word class data
  public static String getSpeedWorkProcessedMap(String jsonDataFile) {
    StringBuilder sb = new StringBuilder();
    String jsonData = jsonDataFile;
    Map<String, Set<String>> finalMap = new HashMap<>();
    Set<String> valueSet = null;

    try {
      log.info("Converting digital config json to wordclass file for speech model building.");
      String[] abc = jsonData.split(Constants.WORDCLASS_SUB_REGEX);
      for (int i = 1; i < abc.length; i++) {
        String istr = abc[i];
        String[] classArr = istr.split(Constants.WORDCLASS_SUB_REGEX_CLASS_PREFIX);
        String finalkey = null;
        for (int j = 0; j < classArr.length; j++) {
          valueSet = new HashSet<>();
          if (j < classArr.length - 1)
            finalkey = Constants.WORDCLASS_SUB_REGEX_CLASS_PREFIX + classArr[j + 1].substring(0, classArr[j + 1].indexOf('"'));
          if (classArr[j].contains("|")) {
            String[] tilledArr = classArr[j].split("\\|");
            if (tilledArr.length > 0)
              for (int k = 0; k < tilledArr.length; k++) {
                if (k == 0) {
                  if (tilledArr[k].contains("?:") && tilledArr[k].length() > 2) {
                    String tempVal = tilledArr[k].substring(tilledArr[k].lastIndexOf("?:"));
                    tempVal = tempVal.replace("?:", "");
                    if (tempVal != null && !tempVal.contains("\\") && !tempVal.contains("?") && !tempVal.contains("{") && !tempVal.contains("%") && !tempVal.isEmpty()) {
                      valueSet.add(tempVal);
                    }
                  }
                } else if (k == tilledArr.length - 1) {
                  String tempVal = tilledArr[k].substring(0, tilledArr[k].indexOf(')'));
                  if (tempVal != null && !tempVal.contains("\\") && !tempVal.contains("?") && !tempVal.contains("{") && !tempVal.contains("%") && !tempVal.isEmpty()) {
                    valueSet.add(tempVal);
                  }
                } else {
                  String tempVal = tilledArr[k];
                  if (tempVal != null && !tempVal.contains("\\") && !tempVal.contains("?") && !tempVal.contains("{") && !tempVal.contains("%") && !tempVal.isEmpty()) {
                    valueSet.add(tempVal);
                  }
                }
              }
          } else {
            if (j < classArr.length - 1 && classArr[j].contains("\":")) {
              String firstHalf = classArr[j].substring(0, classArr[j].lastIndexOf("\":"));
              String tempVal = firstHalf.substring(firstHalf.lastIndexOf('\"') + 1);
              if (tempVal != null && !tempVal.contains("\\") && !tempVal.contains("map") && !tempVal.contains("?") && !tempVal.contains("{") && !tempVal.contains("%") && !tempVal.isEmpty()) {
                valueSet.add(tempVal);
              }
            }
          }
          if (!valueSet.isEmpty()) {
            if (finalMap.containsKey(finalkey)) {
              Set<String> tempSet = finalMap.get(finalkey);
              for (String k : valueSet) {
                tempSet.add(k);
              }
              finalMap.put(finalkey, tempSet);
            } else {
              finalMap.put(finalkey, valueSet);
            }
          }
        }
      }
      for(Map.Entry<String, Set<String>> entry : finalMap.entrySet()) {
        sb.append(entry.getKey());
        sb.append("\r");
        sb.append("\n");
        for(String value : entry.getValue()){
          if(!value.isEmpty()) {
            sb.append(value);
            sb.append("\r");
            sb.append("\n");
          }
        }
        sb.append("\r");
        sb.append("\n");
      }
    } catch (Exception e) {
      log.error("Failed to read config file", e);
    }
    return sb.toString();
  }
}
