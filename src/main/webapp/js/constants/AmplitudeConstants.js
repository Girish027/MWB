
const amplitudeConstants = {

  // ToolId for Modeling Work Bench
  TOOL_ID: 'MWB',

  // List of Events
  LOGIN_EVENT: 'LoginUser',
  LOGOUT_EVENT: 'LogoutUser',
  SELECT_CLIENT_EVENT: 'SelectClientApp',
  CREATE_DATASET_EVENT: 'CreateDataset',
  CREATE_DATASET_COMPLETED_EVENT: 'CreateDatasetCompleted',
  CREATE_DATASET_FAILED_EVENT: 'CreateDatasetFailed',
  DELETE_DATASET_EVENT: 'DeleteDataset',
  DELETE_DATASET_COMPLETED_EVENT: 'DeleteDatasetCompleted',
  DELETE_DATASET_FAILED_EVENT: 'DeleteDatasetFailed',
  CREATE_MODEL_EVENT: 'CreateModel',
  CREATE_MODEL_COMPLETED_EVENT: 'CreateModelCompleted',
  CREATE_MODEL_FAILED_EVENT: 'CreateModelFailed',
  DELETE_MODEL_EVENT: 'DeleteModel',
  DELETE_MODEL_COMPLETED_EVENT: 'DeleteModelCompleted',
  DELETE_MODEL_FAILED_EVENT: 'DeleteModelFailed',
  TEST_SPEECH_MODEL_EVENT: 'TestSpeechModel',
  TEST_SPEECH_MODEL_COMPLETED_EVENT: 'TestSpeechModelCompleted',
  TEST_SPEECH_MODEL_FAILED_EVENT: 'TestSpeechModelFailed',
  TEST_DIGITAL_MODEL_EVENT: 'TestDigitalModel',
  TEST_DIGITAL_MODEL_COMPLETED_EVENT: 'TestDigitalModelCompleted',
  DIGITAL_MODEL_TEST_FAILED_EVENT: 'TestDigitalModelFailed',
  DOWNLOAD_MODEL_EVENT: 'DownloadModel',
  IMPORT_TAGGING_GUIDE_EVENT: 'ImportTaggingGuide',
  IMPORT_TAGGING_GUIDE_COMPLETED_EVENT: 'ImportTaggingGuideCompleted',
  IMPORT_TAGGING_GUIDE_FAILED_EVENT: 'ImportTaggingGuideFailed',
  RUN_BATCH_TEST_EVENT: 'RunBatchTest',
};

export default amplitudeConstants;
