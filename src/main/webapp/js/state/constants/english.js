// tagging
export const LABEL_TAG_NO_MATCHING = 'No matching tags.';
export const LABEL_TAG_FOUND = 'Matching tags.';
export const LABEL_TAG_NEW = 'This will create a new tag.';
export const LABEL_TAG_ERROR_CREATE = 'To create a new tag, please follow the topic-goal format.';
export const LABEL_TAG_NO_DISPLAY = 'No tags to display.';
export const tagMessages = [
  { status: 1, label: LABEL_TAG_NO_MATCHING }, // 1 is valid topic-goal but not sent
  { status: 2, label: LABEL_TAG_ERROR_CREATE }, // 2 is an error
  { status: 3, label: LABEL_TAG_FOUND }, // 3 is found suggested tags
  { status: 4, LABEL_TAG_NO_DISPLAY },
]; // 4 no suggest tags
export const ERROR_SERVER_500 = 'The server communication error.';
export const ERROR_SERVER_503 = 'Elastic Search is not available.';
export const ERROR_SERVER_400 = 'Bad request.';
export const ERROR_SERVER_401 = 'Unathorized.';
export const ERROR_SERVER_404 = 'Not found.';
export const ERROR_SERVER_405 = 'Not allowed.';
export const ERROR_SERVER_406 = 'Not acceptable.';
export const ERROR_SERVER_409 = 'Already tagged.';
export const ERROR_SERVER_TOO_MANY = 'Too many requests.';
export const ERROR_SERVER_UNKNOWN = 'Server communication error.';
export const ERROR_SERVER_DOWN = 'Your request did not complete in a timely manner. Please try again.';
export const ERROR_TRANSFORM_FAIL = 'Dataset transformation failed.';


// Display Messages

export const DISPLAY_MESSAGES = {
  deleteTransformationItem: itemDeleted => `Deleted Transformation item: [${JSON.stringify(itemDeleted)}].`,
  tagCreateFail: 'Tag could not be added.',
  tagCreated: 'Tag was successfully added.',
  tagUpdated: 'Tag was successfully updated.',
  tagRemoved: 'Tag was successfully removed.',
  fileImportFail: 'There was a problem importing the file.',
  emptyFile: 'Empty file is uploaded.',
  fileParseFail: 'Error in parsing the data given in the uploaded file, please check documentation for expected format.',
  taggingGuideImportSuccess: validTagCount => `Successfully imported ${validTagCount} tag${validTagCount == 1 ? '' : 's'}. Loading now...`,
  configFileUploaded: name => `Config file '${name}' uploaded.`,
  configAlreadyExists: 'Config already exists.',
  createdSuccess: itemName => `${itemName} created.`,
  createFailed: 'Create Failed - Are you logged in?',
  projectCreated: projectName => `Model ${projectName} created.`,
  projectDemoted: 'Model demoted to node level.',
  projectPromoted: 'Model promoted to global level.',
  projectUpdateFailed: 'Model update failed.',
  projectAlreadyExists: 'Model already exists.',
  projectUpdated: 'Model updated.',
  projectDeleted: projectName => `Model ${projectName} deleted.`,
  projectLatest: clientName => `Switching to latest project for client ${clientName}.`,
  commentUpdated: 'Comment was successfully updated.',
  commentRemoved: 'Comment was successfully removed.',
  granluarTagRemoved: 'Granular tag removed.',
  bulkTagAdded: intent => `Bulk tag "${intent}" added.`,
  bulkTagUpdated: intent => `Bulk tag "${intent}" updated.`,
  bulkTagAddNUpdate: intent => `Bulk tag "${intent}" added and updated.`,
  bulkTagRemoved: 'Bulk tag removed.',
  invalidIntent: intent => `Tag ${intent} is not in a topic-goal format.`,
  modelAlreadyExists: 'Model version already exists.',
  testResultsRecieved: 'Model test results received.',
  testRequestSubmitted: 'Model batch test request submitted.',
  datasetCreated: datasetName => `Dataset ${datasetName} created.`,
  datasetDeleted: 'Dataset successfully deleted.',
  datasetUsed: 'Cannot delete dataset. It is used in a model.',
  datasetAlreadyExists: datasetName => `Dataset ${datasetName} already exists.`,
  datasetCreateError: 'Dataset creation error.',
  datasetOperationNotAllowed: 'Source type is not allowed for the user.',
  uniqueRollupCount: 'Datasets should have more than 1 unique rollup intents.',
  modelCreated: itemName => `Model version ${itemName} created.`,
  modelUpdated: itemName => `Model version ${itemName} updated.`,
  modelDeleted: itemName => `Model version ${itemName} deleted.`,
  markModelFlagSuccess: modelVersion => `Model version ${modelVersion} is marked for deploy.`,
  markModelFlagFailed: modelVersion => `Unable to mark model version ${modelVersion} as deployable.`,
  unmarkModelFlagSuccess: modelVersion => `Model version ${modelVersion} is unmarked for deploy.`,
  unmarkModelFlagFailed: modelVersion => `Unable to unmark model version ${modelVersion} from deployable.`,
  modelAlreadyDeleted: 'Model is already deleted.',
  modelDeleteFailed: 'Model deletion failed.',
  buildModelRequest: 'Build request is submitted.',
  audioFileNotAvailable: 'Something went wrong. Please retry again.',
  speechTestTimeoutMsg: 'Your request timed out. Please retry again',
  modelTestConnectionError: 'Could not process your request at this moment. Please check with the admin.',
  modelTestFailure: 'Could not process your request. Please retry again.',
  audioRecordFail: 'Unable to record audio at this time.',
  micPermissionDenied: 'Permission denied to record audio.',
  fileDownloadFailed: 'Unable to download file.',
  audioNotPlayable: 'Unable to play audio file.',
  evalSpeechFailed: 'Failed to evaluate the utterance.',
  invalidConfigFile: 'Invalid File. Please upload a valid json file.',
  sessionExpired: 'Your session is expired. Please login again.',
};
