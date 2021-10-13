package com.tfs.learningsystems.util;

public class ErrorMessage {

  public static final String SEARCH_UNAVAILABLE = "Search is currently unavailable.";

  public static final String CLIENT_NOT_FOUND = "Failed to find the related client.";

  public static final String PROJECT_NOT_FOUND = "Failed to find the related project.";

  public static final String PROJECT_NOT_TRANSFORMED = "Model does not have a transformed dataset.";

  public static final String PROJECT_DATASET_NOT_ASSOCIATED = "Specified dataset not associated with the model.";

  public static final String NO_TRANSCRIPTIONS_TAGGED = "Invalid transcriptionHashList; No transcriptions were tagged.";

  public static final String SOURCE_TYPE_ERROR = "Source type is not allowed for the user.";

  public static final String DATASET_VIEW_NOT_ALLOWED = "Dataset access is not allowed for the user.";

  public static final String USER_NOT_AUTHORISED = "User is not authorized to perform this operation.";

  public static final String NO_COMMENTS_ADDED = "Invalid transcriptionHashList; No comments were added.";

  public static final String INVALID_SEARCH_QUERY = "Failed while querying elastic search. Search query invalid.";

  public static final String CREATE_CONSTRAINT_VIOLATION = "Constraint violation exception while creating.";

  public static final String INTENT_NOT_FOUND = "Intent not found";

  public static final String FILE_ACCESS_MESSAGE = "Unable to access file system.";

  public static final String MODEL_DOWNLOAD_MESSAGE = "Model not ready yet.";

  public static final String MODEL_TAG_LOAD_ERROR_MESSAGE = "Could not load tag for Client.";

  public static final String MODEL_TAGS_LOAD_ERROR_MESSAGE = "Could not load tags for client.";

  public static final String COLUMN_MAPPING_CONVERT_ERROR = "Cannot convert columnMappings to correct format.";

  public static final String BAD_COLUMN_MAPPING = "Bad Column mapping.";

  public static final String EMPTY_CSV_FILE = "Empty file is uploaded.";

  public static final String NON_UTF8_ERROR_MESSAGE = "File contains non utf-8 characters.";

  public static final String DATASET_VALIDATION = "Dataset has conflicting data. Please refer to the documentation for more details";

  public static final String VALIDATE_ENCODING_ERROR = "Error while performing validation in the uploaded file.";

  public static final String WRITING_FILE_ERROR = "Error in writing the file to the destination.";

  public static final String IMPORTING_FILE_ERROR = "Error in importing the file. Please contact the admin.";

  public static final String GATEWAY_TIMEOUT_ERROR = "Connection error from web2nl to model builder service.";

  public static final String ORION_MODEL_POST_ERROR = "Model building service is currently unavailable. Please contact the admin.";

  public static final String ORION_MODEL_POST_ERROR_24 = "Failed to initiate model for building in the last 24 hours. Please contact the admin.";

  public static final String ADDING_FILE_ERROR = "Error in adding the file. Please contact the admin.";

  public static final String DATASET_ID_MISSING = "Invalid dataset provided. Please try with valid dataset ID.";

  public static final String BACKEND_ERROR = "Something went wrong. Please try again later.";

  public static final String CONNECTION_ERROR = "Unable to connect with deployment module.";

  public static final String INVALID_FILE_ERROR = "Unreadable file or bad file format.";

  public static final String INVALID_TRANSCRIBING_ERROR = "Unable to transcribe audio.";

  public static final String AUDIO_FILE_ERROR = "Audio file is empty or null.";

  public static final String AUDIO_URL_ERROR = "Audio URL is empty or null.";

  public static final String INVALID_GITHUB_INFO_FROM_DEPLOY2 = "Cannot find repository name or github org name from deploy2 module.";

  public static final String INTENT_LENGTH_ERROR = "Dataset intent should not be more than 50 characters.";

  public static final String INVALID_MODELS = "Cannot publish invalid models.";

  public static final String NULLABLE_OR_EMPTY_MODELS = "Cannot publish empty models.";

  public static final String VECTORIZER_NOT_FOUND = "Failed to find Vectorizer";

  public static final String PREFERENCE_NOT_FOUND = "Failed to find Preference";

  public static final String VECTORIZER_NOT_CREATED = "Failed to create Vectorizer";

  public static final String PREFERENCE_NOT_CREATED = "Failed to create Preference";

  public static final String VECTORIZER_NOT_UPDATED = "Failed to update Vectorizer";

  public static final String PREFERENCE_NOT_UPDATED = "Failed to update Preference";

  public static final String METRICS_NOT_FOUND = "Failed to find Metrics";

}
