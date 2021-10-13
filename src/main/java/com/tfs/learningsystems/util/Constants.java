package com.tfs.learningsystems.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Constants {

  private Constants() {
    throw new IllegalStateException("Utility class");
  }

  public static final String DEFAULT_ENCODING = "UTF-8";

  public static final String SESSION_USERNAME_FIELD = "userName";

  public static final String SESSION_USER_DETAILS = "userDetails";

  public static final String SESSION_USERID_FIELD = "userId";

  public static final String CLIENTS_USERID_FIELD = "userid";

  public static final String SESSION_CLIENTID_FIELD = "clientId";

  public static final String SESSION_ITS_CLIENTID_FIELD = "itsClientId";

  public static final String SESSION_ITS_ACCOUNTID_FIELD = "itsAccountId";

  public static final String SESSION_ITS_APP_ID_FIELD = "itsAppId";

  public static final String SESSION_STD_CLIENT_ID_FIELD = "standardClientId";

  public static final String CONFIG_FILE_SUFFIX = ".config";

  public static final String CONFIG_FILE_DIRECTORY = "config";

  public static final String INPUT_SECURITY_UTIL_HASH_VALUE = "A";

  //Okta Authorization Constants
  public static final String OKTA_USER_CLIENTS = "clients";
  public static final String CLIENTS_SLASH = "clients/";
  public static final String OKTA_USER_GROUPS = "groups";
  public static final String MWB_ROLE_CLIENTADMIN = "CLIENT_ADMIN";
  public static final String MWB_ROLE_EXTERNAL = "MWB_ROLE_EXTERNAL";  // marker group for external users
  public static final String ITS_CAPONE_CLIENT_NAME = "capone";
  public static final String ITS_GROUP = "IAT_INTERNAL";
  public static final String ITS_GROUP_NAME = "ITS_GROUP";
  public static final String STAR = "*";
  public static final String NO_RESTRICTIONS_ROLE = "NO_RESTRICTIONS";
  public static final String PREFERRED_EMAIL_SUFFIX = "@247-inc.com";
  public static final String PRIMARY_EMAIL_SUFFIX = "@247.ai";
  public static final String EMAIL = "email";
  public static final String PREFERRED_USERNAME = "preferred_username";
  public static final String USER_SUB = "sub";
  public static final String USER_TYPE = "tfsUserType";
  public static final String OKTA_USER = "user";
  public static final String EXTERNAL_USER = "EXTERNAL";
  public static final String BOT_FRAMEWORK = "botframework";
  public static final String ADMIN_ROLE = "admin";
  public static final String OPERATOR_ROLE = "operator";
  public static final String DEVELOPER_ROLE = "developer";

  protected static final Map<String, String> API_AUTHORIZATION_MAP = new HashMap<>();

  //ProjectBo Constants
  public static final String PROJECT_NAME = "name";
  public static final String PROJECT_LABEL = "project";

  public static final Map<String, List<String>> ROLE_BASED_DENY_MAP = new HashMap<>();
  protected static final List<String> EXTERNAL_DENIED_RESOURCES = new LinkedList<>();
  public static final String TAGGING_GUIDE_VALID_TAGS_FNAME_SUFFIX = "_valid_tags.csv";
  public static final String TAGGING_GUIDE_INVALID_TAGS_FNAME_SUFFIX = "_invalid_tags.csv";
  public static final String TAGGING_GUIDE_MISSING_TAGS_FNAME_SUFFIX = "_missing_tags.csv";
  public static final String DEFAULT_ENGLISH_CONFIG_ID = "1";
  public static final String ORIGINAL_TRANSCRIPTION_LABEL = "Original Transcription";
  public static final String GRANULAR_INTENT_LABEL = "Granular Intent";
  public static final String ROLL_UP_INTENT_LABEL = "Rollup Intent";
  public static final String FILENAME_LABEL = "Filename";
  public static final String TRANSCRIPTION_HASH_LABEL = "Transcription Hash";
  public static final String COMMENTS_LABEL = "Comments";
  public static final String SOURCE_LABEL = "Source";
  public static final String FILE_UPLOAD_BASE_URI = "https://tagging.247-inc.com:8443/nltools/private/v1/files/";
  public static final String DEFAULT_EN_CONFIG_NAME = "system_default_en";
  public static final String COMMA = ",";
  public static final String AND = "and";
  public static final String NAME = "name";
  public static final String DESCRIPTION = "description";
  public static final String UTTERANCE_FILE_TYPE_AS_LINK = "link";
  public static final String DEPLOYABLE_MODEL_ID = "deployableModelId";
  public static final String PROJECT_FIELDS = "ProjectFields";
  public static final String DATASET_FIELDS = "DatasetFields";
  public static final String PREVIEW_MODEL_ID = "previewModelId";
  public static final String LIVE_MODEL_ID = "liveModelId";

  //CIG
  protected static final Map<String, String> CONFIG_LANGUAGE_ID_MAP = new HashMap<>();
  public static final String TAGGED_KEY = "Tagged";
  public static final String UNTAGGED_KEY = "UnTagged";

  public static final String DECENDING = "decending";
  public static final String DOT = ".";
  public static final String COLON = ":";
  public static final String HYPHEN = "-";
  public static final String UNDER_SCORE = "_";
  public static final String EMPTY_STRING = " ";
  public static final String BLANK_STRING = "";
  public static final String XLSX_EXTENSION = ".xlsx";
  public static final String INTENT_RU_DELIMITER = "%$^";
  public static final String OLD_DEFAULT_EN_CONFIG_NAME = "system_default_en_0";
  public static final String DB_COLUMN_NAME = "name";
  public static final String DB_COLUMN_ID = "id";

  public static final String DB_COLUMN_VERTICAL = "is_vertical";
  public static final String DB_COLUMN_DESCRIPTION = "description";
  public static final String DB_COLUMN_ADDRESS = "address";
  public static final String DB_COLUMN_CREATED_AT = "created_at";
  public static final String DB_COLUMN_MODIFIED_AT = "modified_at";
  public static final String DB_COLUMN_CID = "cid";
  public static final String DB_COLUMN_STATE = "state";
  public static final String DB_COLUMN_REQUIRED = "required";
  public static final String DB_COLUMN_ITS_CLIENT_ID = "its_client_id";
  public static final String DB_COLUMN_ITS_APP_ID = "its_app_id";
  public static final String DB_COLUMN_DISPLAY_NAME = "display_name";
  public static final String DB_COLUMN_COLUMN_ID = "column_id";
  public static final String DB_COLUMN_COLUMN_INDEX = "column_index";
  public static final String DB_COLUMN_USER_ID = "user_id";

  //MODEL TYPES
  public static final String DIGITAL_MODEL = "DIGITAL";
  public static final String DIGITAL_SPEECH_MODEL = "DIGITAL_SPEECH";
  public static final String SPEECH_MODEL = "SPEECH";
  public static final String MODEL_DB_ID = "id";
  public static final String MODEL_UUID_ID = "modelId";
  public static final String MODEL_TYPE = "modelType";
  public static final String MODEL_VERSION = "version";
  public static final String FORWARD_SLASH = "/";
  public static final String MODEL_FILE_SUFFIX = ".model";
  public static final String ROOT_DIRECTORY = "/";
  public static final String MODEL_ACCURACY = "Accuracy";
  public static final String MODEL_STATS_PREFIX = "model_stats";
  public static final String STARTS_WITH_FILENAME = "trainingOutput";
  public static final String TEMP_FOLDER_SYSTEM_PROPERTY = "java.io.tmpdir";
  public static final String TRAINING_LOG_FILE_START_WITH = "Results/Log_";
  public static final String MODEL_AVERAGE_WEIGHTED_F_SCORE = "Average Weighted F_Score: ";
  public static final String LOWER = "LOWER(`";
  public static final String CLOSE_BRACKETS = "`) = :";
  public static final String NSBP_UNICODE = "\u00A0";
  public static final String INVALID_CLIENT = "Invalid  client details for client : ";
  public static final String DATASET_NOT_FOUND_FOR_CLIENT = "Failed to find the related dataset for client - {} , dataset - {} ";
  public static final String DATASET_NOT_FOUND = "dataset_not_found";

  public static final String CONFIG_DIRECTORY_PATH = "/config/";
  public static final String SPACE = " ";
  public static final String MASTER_BRANCH = "master";
  public static final String UTC = "UTC";
  public static final String CALENDAR_FORMAT_YYYYMMDDHHMMSS = "yyyyMMdd'T'HHmmss";
  public static final String DEPLOY2_PACKAGES_URI = "packages";


  public static final String DEPLOY2_MODULE_ID_PARAM = "module_id";

  public static final String DEPLOY2_NAME_PARAM = "name";
  public static final String DEPLOY2_MODULES_URI = "modules";
  public static final String DEPLOY2_MODULE_CREATION_TYPE = "module";
  public static final String DEPLOY2_MODULE_TYPE = "type";
  public static final String DEPLOY2_PACKAGE_CREATION_TYPE = "package";
  public static final String DEPLOY2_MODULE_NAME_DELIMITER = "--";
  public static final String DEPLOY2_USER_PARAM = "user";
  public static final String DEPLOY2_COMMENT = "comment";
  public static final String DEPLOY2_MARK = "mark";
  public static final String DEPLOY2_PACKAGE_GOOD = "good";
  public static final String DEPLOY2_PACKAGE_OBSOLETE = "obsolete";
  public static final String DEPLOY2_PACKAGE_GIT_HUB_REF = "githubRef";
  public static final String DEPLOY2_PACKAGE_GIT_HUB_REF_TYPE = "githubRefType";
  public static final String DEPLOY2_PACKAGE_GIT_HUB_REF_TYPE_TAG_VALUE = "tags";
  public static final String DEPLOY2_TO_ERROR_CODE = "deploy2_error";

  public static final String DEPLOY2_MODULES_HIDDEN = "hidden";
  public static final Object DEPLOY2_MODULE_TYPE_GIT = "github";
  public static final String DEPLOY2_DESC_PARAM = "description";
  public static final String DEPLOY2_GIT_HUB_ORG_PARAM = "githubOrg";
  public static final String DEPLOY2_GIT_HUB_SERVER_PARAM = "githubServer";
  public static final String DEPLOY2_GIT_HUB_REPO_PARAM = "githubRepo";
  public static final String DEPLOY2_MODULE_PRESEERVE_TIME_PARAM = "preserveTime";
  public static final String DEPLOY2_MODULE_COMPRESS_PARAM = "compress";
  public static final String DEPLOY2_MODULE_DEEP_PACKAGE_PARAM = "deepPackage";
  public static final String DEPLOY2_MODULE_CONTACTS = "contacts";
  public static final String DEPLOY2_CLIENTS_STRING = "clients";
  public static final String DEPLOY2_APPLICATION_STRING = "applications";
  public static final String DEPLOY2_MODELS_STRING = "models";
  public static final String DEPLOY2_RESPONSE_BODY_PARAM = "result";

  public static final String WORDCLASS_SUB_REGEX = "wordclass-subst";
  public static final String WORDCLASS_SUB_REGEX_CLASS_PREFIX = "_class_";

  public static final String LINE_SEPARATOR = "line.separator";


  public static final String MICROSOFT_NAMESPACE = "http://www.microsoft.com/xmlns/webreco";
  public static final String EMMA_NAMESPACE = "http://www.w3.org/2003/04/emma";
  public static final String TELLME_NAMESPACE = "http://www.tellme.com/ns/2009/01/emma";

  public static final String ORION_ERROR =  "orion_error";

  public static final String ORION_MODEL_POST_ERROR = "Failed while posting model to Orion!!";

  public static final String ORION_MODEL_PATCH_ERROR = "Failed while updating model to Orion!!";

  public static final String ORION_MODEL_FETCH_ERROR = "Failed while retrieving model status from Orion!!";

  public static final String ORION_FILE_FETCH_ERROR = "Failed while retrieving file from Orion!!";

  public static final String ORION_COMPLETE_STATUS = "Model Created Successfully";

  public static final String ORION_FAILED_STATUS = "Model building failure";

  public static final String ORION_SPEECH_COMPLETE_STATUS = "Successfully created speech model";

  public static final String ORION_COMBINE_COMPLETE_STATUS = "Successfully combined SLM model with SSI model";

  public static final String COMPLETE_STATUS = "COMPLETED";
  public static final String RUNNING_STATUS = "RUNNING";
  public static final String ERROR_STATUS = "ERROR";

  public static final String END_AT = "endedAt";

  public static final String WEBRECO_QUERY_PARAM_AUDIO = "audio";
  public static final String WEBRECO_QUERY_PARAM_AUTHORIZATION = "authorization";
  public static final String WEBRECO_QUERY_PARAM_GRAMMAR = "grammar1";
  public static final String WEBRECO_QUERY_PARAM_CONFIDENCE_SCORE = "confidencelevel";
  public static final String WEBRECO_QUERY_CONFIDENCE_SCORE_VALUE = "0.19";
  public static final String PREFERENCE_CLIENT_LEVEL = "client";
  public static final String PREFERENCE_MODEL_LEVEL = "model";
  public static final String USE_LARGE = "use_large";
  public static final String NGRAM = "n-gram";
  public static final String IS_LATEST = "isLatest";
  public static final String VECTORIZER_TYPE = "Vectorizer";

  static {
    API_AUTHORIZATION_MAP.put("/nltools/private/v1/clients", "MWB_API_CLIENTS");
    API_AUTHORIZATION_MAP.put("/nltools/private/v1/projects", "MWB_API_PROJECTS");
    API_AUTHORIZATION_MAP.put("/nltools/private/v1/resources", "MWB_API_RESOURCES");
    API_AUTHORIZATION_MAP.put("/nltools/private//v1/search", "MWB_API_SEARCH");
    API_AUTHORIZATION_MAP.put("/nltools/private/v1/models", "MWB_API_MODELS");
    API_AUTHORIZATION_MAP.put("/nltools/private/v1/datasets", "MWB_API_DATASETS");
    API_AUTHORIZATION_MAP.put("/nltools/private/v1/content", "MWB_API_CONTENT");
    API_AUTHORIZATION_MAP.put("/nltools/private/v1", "MWB_API_TEST");
  }

  /**
   * All restricted endpoints for EXTERNAL Users should be added in EXTERNAL_DENIED_RESOURCES
   * List in a particular format - httpMethod followed by ':' followed by URI
   * example - "httpMethod:URI"
   */
  static {

    EXTERNAL_DENIED_RESOURCES.add(":/v1/models/\\d+/training-outputs");
    EXTERNAL_DENIED_RESOURCES.add(":/v1/models/\\d+/download");
    EXTERNAL_DENIED_RESOURCES.add(":/v1/configs/\\d+/download");
    EXTERNAL_DENIED_RESOURCES.add(":/v1/models/\\d+/statistics");
    EXTERNAL_DENIED_RESOURCES.add("DELETE:/v1/clients/\\d+/projects/\\d+/models/\\d+");
    EXTERNAL_DENIED_RESOURCES.add("DELETE:/v1/clients/\\d+/projects/\\d+/datasets/\\d+");
    EXTERNAL_DENIED_RESOURCES.add("GET:/v1/clients/\\d+/projects/\\d+/datasets/\\d+/export");
    EXTERNAL_DENIED_RESOURCES.add("GET:/v1/clients/\\d+/projects/\\d+/export");
    EXTERNAL_DENIED_RESOURCES.add("DELETE:/v1/clients/\\d+/projects/\\d+");
    EXTERNAL_DENIED_RESOURCES.add("GET:/v1/clients/\\d+/projects/\\d+/taggingguide/export");
    EXTERNAL_DENIED_RESOURCES.add("DELETE:/v1/content/\\d+/intents/\\w+");
    EXTERNAL_DENIED_RESOURCES.add("PATCH:/v1/content/\\d+/intents/\\w+");
    EXTERNAL_DENIED_RESOURCES.add("POST:/v1/content/\\d+/intents");

    ROLE_BASED_DENY_MAP.put(MWB_ROLE_EXTERNAL, EXTERNAL_DENIED_RESOURCES);

  }

  static {
    CONFIG_LANGUAGE_ID_MAP.put(Locale.ENGLISH.getLanguage(), "1");
  }

  public static final String HOST_HEADER = "Host";
  public static final String DOMAIN_ENDS_WITH = ".247-inc.net";
  public static final String HEALTH_IP = "127.0.0.1:9590";
  public static final String API_ACCESS_TOKEN = "apiAccessToken";
  public static final String SECURITY_TYPE_SHA = "SHA-1";
  public static final String SECURITY_TYPE_MD5 = "MD5";
  public static final long TRAINING_OUTPUT_PURGE_TTL_IN_DAYS = 60;

  //Ingestion API Constants
  public static final String ING_API_LOG_MESSAGE_PREFIX = "Message received from UI :: ";
  public static final String ING_API_LOG_LEVEL_ERROR = "ERROR";
  public static final String ING_API_LOG_LEVEL_WARNING = "WARNING";
}

