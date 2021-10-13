export default {

  LOGOUT: '/logout',
  DOCUMENT_TITLE_EC: 'Model Workbench | Engagement Cloud',
  DOCUMENT_TITLE: 'Model Workbench',
  BOT_FRAMEWORK: 'MODEL WORKBENCH',
  MODEL_WORKBENCH: 'MODEL WORKBENCH',
  BOT_OVERVIEW: 'Overview',
  ITS_GROUP: 'IAT_INTERNAL',
  MANAGE_MODELS_DATASETS: 'Manage Models & Datasets',
  CLIENT_PICK_TITLE: 'Choose Workspace',
  ICON_WORKBENCH: 'icon_modelWorkbench',
  // Separators
  CLIENT_APP_SEPARATOR: ' ',
  EQUALS_SEPARATOR: '=',
  DEFAULT_VALUE: '',
  OBJECT_DEFAULT_VALUE: {},
  COMMA_WHITESPACE_SEPARATOR: ', ',
  COMMA_SEPARATOR: ',',

  // HTTP status
  SC_FORBIDDEN: 403,
  SC_OK: 200,

  // Help Links
  HELP: {
    SEARCH: 'https://docs.247.ai/using-search-function-datasets',
    TRANSFORMATION_LINK: 'https://docs.247.ai/specifying-configuration-options-nl-model',
  },

  // User Config related
  PREFERRED_MAIL_SUFFIX: '@247-inc.com',
  PRIMARY_MAIL_SUFFIX: '@247.ai',
  USER_TYPE_INTERNAL: 'Internal',
  USER_TYPE_EXTERNAL: 'External',
  DATASET_EXTERNAL_FLAG: 'E',
  DATASET_INTERNAL_FLAG: 'I',

  // Config Related
  UPLOADED_CONFIG_NAME: 'Latest Uploaded Config',
  DEFAULT_CONFIG_NAME: 'system_default_en_1',
  ALTERNATE_DEFAULT_CONFIG_NAME: 'system_default_en',

  // Delete Dialog
  DELETE: 'DELETE',

  // Promote Dialog
  PROMOTE: 'PROMOTE',

  // Demote Dialog
  DEMOTE: 'DEMOTE',

  // Dropdown Actions
  DROPDOWN_PROMOTE: 'Promote',
  DROPDOWN_DEMOTE: 'Demote',
  DROPDOWN_EDIT: 'Edit',
  DROPDOWN_DELETE: 'Delete',

  VOLUME: 'Volume',
  ESCALATION: 'Escalation',
  VERSION: (versionNumber) => `Version ${versionNumber}`,

  ROOT_INTENT: 'root_intent',

  // Project Type
  PROJECT_TYPE: {
    GLOBAL: {
      NAME: 'GLOBAL',
      MODELS_NAME: {
        SOCIAL: 'Social',
        SENTIMENT: 'Sentiment',
        YES_NO: 'Yes_No',
        ROOT_INTENT: 'Root_Intent',
      },
      TOOLTIP: 'Standard NLP models used in AIVA applications. Models can be moved from Global to Node Level through the "Demote" process.',
      TAB_TITLE: (count) => `Global (${count})`,
    },
    NODE: {
      NAME: 'NODE',
      TOOLTIP: 'Client and/or app specific NLP models. Models can be moved from Node Level to Global through the "Promote" process.',
      TAB_TITLE: (count) => `Node Models (${count})`,
    },
  },

  // Project Form
  PROJECT_NAME: 'Model Name*',
  PROJECT_DESCRIPTION: 'Model Description',
  VERTICAL_MARKET: 'Vertical Market*',
  Locale: 'Locale*',

  // Model Form
  MODEL_NAME: 'Name',
  MODEL_DESCRIPTION: 'Description',
  VERTICAL: 'Vertical',
  MODEL_LOCALE: 'Locale',

  // Transformation Types
  TRANSFORMATION_TYPES: {
    CASE_NORMALIZATION: 'case-normalization',
    INPUT_MATCH: 'input-match',
    REGEX_REMOVAL: 'regex-removal',
    REGEX_REPLACE: 'regex-replace',
    SPELL_CHECKING: 'spell-checking',
    STEMS_NOCASE: 'stems-nocase',
    STEMS_NOCASE_URL: 'stems-nocase-url',
    STEMS: 'stems',
    STEMS_URL: 'stems-url',
    STOP_WORDS: 'stop-words',
    TRAINING_DATA_STEMS: 'training-data-stems',
    WHITESPACE_NORMALIZATION: 'whitespace-normalization',
    WORDCLASS_SUBST_REGEX: 'wordclass-subst-regex',
    WORDCLASS_SUBST_TEXT: 'wordclass-subst-text',
    TRANSFORMATION_URL: 'url transformation',
  },

  // Validation Regex
  TAG_WORD_REGEX: '([0-9a-z_ªµºß-öø-ƀƃƅƈƌ-ƍƒƕƙ-ƛƞơƣƥƨƪ-ƫƭưƴƶƹ-ƺƽ-ƿǆǉǌǎǐǒǔǖǘǚǜ-ǝǟǡǣǥǧǩǫǭǯ-ǰǳǵǹǻǽǿȁȃȅȇȉȋȍȏȑȓȕȗșțȝȟȡȣȥȧȩȫȭȯȱȳ-ȹȼȿ-ɀɂɇɉɋɍɏ-ʓʕ-ʯͱͳͷͻ-ͽΐά-ώϐ-ϑϕ-ϗϙϛϝϟϡϣϥϧϩϫϭϯ-ϳϵϸϻ-ϼа-џѡѣѥѧѩѫѭѯѱѳѵѷѹѻѽѿҁҋҍҏґғҕҗҙқҝҟҡңҥҧҩҫҭүұҳҵҷҹһҽҿӂӄӆӈӊӌӎ-ӏӑӓӕӗәӛӝӟӡӣӥӧөӫӭӯӱӳӵӷӹӻӽӿԁԃԅԇԉԋԍԏԑԓԕԗԙԛԝԟԡԣա-և٠-٩۰-۹߀-߉०-९০-৯੦-੯૦-૯୦-୯௦-௯౦-౯೦-೯൦-൯๐-๙໐-໙༠-༩၀-၉႐-႙០-៩᠐-᠙᥆-᥏᧐-᧙᭐-᭙᮰-᮹᱀-᱉᱐-᱙ᴀ-ᴫᵢ-ᵷᵹ-ᶚḁḃḅḇḉḋḍḏḑḓḕḗḙḛḝḟḡḣḥḧḩḫḭḯḱḳḵḷḹḻḽḿṁṃṅṇṉṋṍṏṑṓṕṗṙṛṝṟṡṣṥṧṩṫṭṯṱṳṵṷṹṻṽṿẁẃẅẇẉẋẍẏẑẓẕ-ẝẟạảấầẩẫậắằẳẵặẹẻẽếềểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỵỷỹỻỽỿ-ἇἐ-ἕἠ-ἧἰ-ἷὀ-ὅὐ-ὗὠ-ὧὰ-ώᾀ-ᾇᾐ-ᾗᾠ-ᾧᾰ-ᾴᾶ-ᾷιῂ-ῄῆ-ῇῐ-ΐῖ-ῗῠ-ῧῲ-ῴῶ-ῷⁱⁿℊℎ-ℏℓℯℴℹℼ-ℽⅆ-ⅉⅎↄⰰ-ⱞⱡⱥ-ⱦⱨⱪⱬⱱⱳ-ⱴⱶ-ⱼⲁⲃⲅⲇⲉⲋⲍⲏⲑⲓⲕⲗⲙⲛⲝⲟⲡⲣⲥⲧⲩⲫⲭⲯⲱⲳⲵⲷⲹⲻⲽⲿⳁⳃⳅⳇⳉⳋⳍⳏⳑⳓⳕⳗⳙⳛⳝⳟⳡⳣ-ⳤⴀ-ⴥ꘠-꘩ꙁꙃꙅꙇꙉꙋꙍꙏꙑꙓꙕꙗꙙꙛꙝꙟꙣꙥꙧꙩꙫꙭꚁꚃꚅꚇꚉꚋꚍꚏꚑꚓꚕꚗꜣꜥꜧꜩꜫꜭꜯ-ꜱꜳꜵꜷꜹꜻꜽꜿꝁꝃꝅꝇꝉꝋꝍꝏꝑꝓꝕꝗꝙꝛꝝꝟꝡꝣꝥꝧꝩꝫꝭꝯꝱ-ꝸꝺꝼꝿꞁꞃꞅꞇꞌ꣐-꣙꤀-꤉꩐-꩙ﬀ-ﬆﬓ-ﬗ０-９ａ-ｚ]|\ud801[\udc28-\udc4f\udca0-\udca9]|\ud835[\udc1a-\udc33\udc4e-\udc54\udc56-\udc67\udc82-\udc9b\udcb6-\udcb9\udcbb\udcbd-\udcc3\udcc5-\udccf\udcea-\udd03\udd1e-\udd37\udd52-\udd6b\udd86-\udd9f\uddba-\uddd3\uddee-\ude07\ude22-\ude3b\ude56-\ude6f\ude8a-\udea5\udec2-\udeda\udedc-\udee1\udefc-\udf14\udf16-\udf1b\udf36-\udf4e\udf50-\udf55\udf70-\udf88\udf8a-\udf8f\udfaa-\udfc2\udfc4-\udfc9\udfcb\udfce-\udfff])+',

  // ANY_REGEX
  ANY_REGEX: '/./',
  ANY_VALUE: 'any',
  INPUT_PREFIX: '/(?:', // INPUT_PREFIX: '/\\b(?:',
  INPUT_SUFFIX: ')/', // INPUT_SUFFIX: ')\'?s?\\b/i',

  // upload configs
  DROPZONEJS_COMMON_CONFIG: {
    maxFiles: 1,
    paramName: 'file',
    addRemoveLinks: true,
    maxFileSize: 5000000000, // in MB
    acceptedFiles: 'text/csv, application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  },
  TAGGING_GUIDE_UPLOAD_TIMEOUT: 120000,
  DATASET_UPLOAD_TIMEOUT: 600000,
  UPLOAD_DEFAULT_MSG: 'Click to choose a file or drag it here',

  // Speech
  ADD_SPEECH_DIALOG_INST: 'Select datasets to build speech model',

  // Model Types
  DIGITAL_MODEL: 'DIGITAL',
  VOICE_MODEL: 'VOICE',
  DIGITAL_SPEECH_MODEL: 'DIGITAL_SPEECH',
  SPEECH_MODEL: 'SPEECH',
  MODEL_TYPE: {
    DIGITAL: 'Digital',
    DIGITAL_SPEECH: 'Speech + Digital',
    SPEECH: 'Speech',
  },
  TECHNOLOGY:
  {
    DIGITAL_TENSORFLOW: 'TensorFlow',
    DIGITAL_NGRAM: 'n-grams',
    SPEECH_SLM: 'SLM',
    NGRAM: 'n-gram',
    SPEECH_DIGITAL_NGRAM: 'SLM + N-Gram',
    SPEECH_DIGITAL_TENSORFLOW: 'SLM + Tensorflow',
  },
  TENSORFLOW_TYPE:
  {
    use: 'USE',
    use_large: 'USE-Large',
  },
  TENSORFLOW_TYPE_VERSION: (type, version) => `${type}  ${version} `,
  TEST_MODEL_RADIO_LABELS: ['Speech', 'Digital'],

  // Project Tabs
  DATASETS: 'Datasets',
  ALL_VERSIONS: 'All Versions',
  INTENT_GUIDE: 'Intent Guide',
  SETTINGS: 'Settings',
  OVERVIEW: 'Overview',
  NODE_ANALYTICS: 'Node Analytics',

  // PATCH OPERATION
  OP: {
    REPLACE: 'REPLACE',
    ADD: 'ADD',
    MOVE: 'MOVE',
    COPY: 'COPY',
    TEST: 'TEST',
  },

  // STATUS
  STATUS: {
    NULL: 'NULL',
    QUEUED: 'QUEUED',
    RUNNING: 'RUNNING',
    FAILED: 'FAILED',
    COMPLETED: 'COMPLETED',
    ERROR: 'ERROR',
    STARTED: 'STARTED',
    PREVIEW: 'PREVIEW',
    LIVE: 'LIVE',
  },
  STATUS_NOT_FOUND: 'Model status not available',
  MSG_BUILD_NOT_STARTED: 'Build has not started',
  MSG_ERROR_IN_MODEL_STATUS: 'There was an error obtaining the model status',
  SPINNER: 'spinner',
  LABEL: 'label',
  BADGE: 'badge',
  SELECT: 'Select...',

  // Model Action Menu
  MODEL_ACTION_MENU: {
    BUILD: 'Build',
    TEST: 'Test',
    TUNE: 'Tune',
    VIEW: 'View',
    VIEW_DIGITAL_URL: 'View Digital Url',
    MODEL_FILE: 'Download Model File',
    TRAINING_OUTPUTS: 'Download Training Outputs',
    ACCURACY_REPORT: 'Download Accuracy Report',
    CONFIGURATION: 'Download Configuration',
    ADD_SPEECH: 'Add Speech',
    REBUILD_SPEECH: 'Rebuild Speech',
    MARK_FOR_DEPLOY: 'Mark for deploy',
    UNMARK_FOR_DEPLOY: 'Unmark for deploy',
    DELETE: 'Delete',
    VIEW_LOGS: 'View Logs',
    BUILD_COMBINED_MODEL: 'Add Digital Url',
  },

  // Dialog Types
  DIALOGS: {
    SIMPLE_DIALOG: 'SimpleDialog',
    IMPORT_TAGGING_GUIDE: 'TaggingGuideImportDialog',
    CREATE_DATASET: 'CreateDatasetDialog',
    UNAUTHORIZED_USER: 'UnauthorizedUserDialog',
    DELETE_DIALOG: 'DeleteDialog',
    ADD_SPEECH: 'SpeechSelectDatasetDialog',
    PROGRESS_DIALOG: 'ProgressDialog',
    LINK_DIALOG: 'LinkDialog',
    CREATE_VERSION_DIALOG: 'CreateVersionDialog',
    PROMOTE_DIALOG: 'PromoteDialog',
    RECORD_SPEECH: 'RecordSpeechDialog',
    ADD_INTENT: 'AddIntentDialog',
    COLUMNSELECTOR_DIALOG: 'ColumnSelectorDialog',
    CREATE_MODEL_DIALOG: 'CreateModelDialog',
    DATASET_DIALOG: 'DatasetDialog',
    DATASET_COLUMN_MAPPING_DIALOG: 'DatasetColumnMapperDialog',
    ADD_COMBINED_SPEECH: 'CombinedSpeechDialog',
  },


  // Tab names
  CREATE_MODEL_TABS: {
    basicInfo: 'Basic Info*',
    transformations: 'Pre Processing',
    postProcessing: 'Post Processing',
    trainingConfigs: 'Training Configs',
    overview: 'Overview',
  },

  CREATE_INIT_MODEL_TABS: {
    basicInfo: 'Basic Info*',
    transformations: 'Pre Processing',
    postProcessing: 'Post Processing',
    trainingConfigs: 'Training Configs',
  },

  CREATE_MODEL_TABS_TRANSFORMATION_TAB: 'Pre Processing',

  CREATE_MODEL_TABS_OVERVIEW_TAB: 'Overview',

  TEST_TABS: {
    utteranceTestTab: 'Utterance Test',
    batchTestTab: 'Batch Test',
  },
  TEST_RESULTS_TABS: {
    intentsTab: 'Predicted Intents',
    transformationsTab: 'Transformations',
    entitiesTab: 'Entities',
  },
  UI_JSON_TABS: ['UI', 'JSON'],

  // Roles allowed to perform mark for deploy
  ADMIN_ROLE: 'admin',
  OPERATOR_ROLE: 'operator',

  // Client picker
  CLIENT_PICKER_ERROR: 'Error in fetching client information. Please contact admin',
  CLIENT_PICK_ERROR_TITLE: 'Client Picker Application',

  // Buttons and Action Items
  TUNE_VERSION: 'TUNE VERSION',
  BUILD_VERSION: 'CREATE VERSION',
  REVIEW_DATASETS: 'REVIEW DATASETS',
  SHOW_CONTROLS: 'SHOW CONTROLS',
  HIDE_CONTROLS: 'HIDE CONTROLS',
  RUN_TEST: 'RUN TEST',
  ADD: 'ADD',
  COLUMNS_MAPPER: 'Columns Mapping',
  CANCEL: 'CANCEL',
  CREATE: 'CREATE',
  UPDATE: 'UPDATE',
  NEXT: 'NEXT',
  CLOSE: 'CLOSE',
  YES: 'YES',
  NO: 'NO',
  INDEX: 'INDEX',
  LOG_OUT: 'LOG OUT',

  // Dialog Headers
  CREATE_MODEL: 'Create New Model',
  UPLOAD_DATASET: 'Upload Dataset',
  UPLOADING_DATASET: 'Uploading your file…',
  UPLOAD_DATASET_SUCCESS: 'Your file has been uploaded',
  CREATE_VERSION: 'Create New Version',
  DIGITAL_URL: 'Digital Model Url',

  // Default Messages
  SPEECH_TEST_MESSAGE: 'Select, record, upload, or link an audio file to run a test',
  DIGITAL_TEST_MESSAGE: 'Type an utterance to run a test',
  UNKNOWN_MODEL: 'Unknown Model. Please recheck.',

  // Dataset Transformation message
  START_TRANSFORMATION: 'Click to start transformation',
  NEED_TRANSFORMATION: 'Needs to be transformed',
  PROCESS_TRANSFORMATION: 'Processing Transformation',

  DATASET_FILE: 'file',
  SPEECH_FILE: 'file',
  AUDIO_FILE: 'audioFile',
  AUDIO_URL: 'audioURL',
  UTTERANCES: 'utterances',
  FILE_TYPE: {
    RECORDING: 'recording',
    LINK: 'link',
    UPLOADED: 'upload',
  },
  AUDIO_TYPE_WAV: 'audio/wav',
  RECORDING_MAX_TIME_LIMIT_MIN: '00',
  RECORDING_MAX_TIME_LIMIT_SEC: 40,
  TEST_IN_PROGRESS: 'Transcribing and preparing results...',
  SEARCHING_IN_PROGRESS: 'Searching...',
  DATASET_NOT_AVAILABLE_MESSAGE: 'Please upload a dataset to start.',
  UPLOAD_DATASET_MESSAGE: 'Please upload a dataset to start model building.',
  UPLOAD_DATASET_PROGRESS: 'You may close this window while we upload your file. Uploading will not cancel. We will prompt you once your file has successfully uploaded.',
  UPLOAD_DATASET_SUCCESS_MESSAGE: 'Your file has been uploaded successfully but not all data has been tagged. Do you want to build a new version of the model?',
  UPLOAD_DATASET_SUCCESS_WITH_TAG_MESSAGE: 'We are ready to build a new version of the model with the uploaded file, do you want to proceed?',
  UPLOAD_DATASET_SUCCESS_WITHOUT_TAG_MESSAGE: 'None of the utterance in the uploaded file is assigned with an intent label. Please refer to the documentation to provide intent labels.',
  BUILD_MODEL_CONFIRMATION_MESSAGE: 'Warning: Not all data has been tagged. Do you still want to proceed?',
  CREATE_FIRST_VERSION_MESSAGE: 'Please create the first version.',
  CREATE_NEW_MODEL_HEADER: 'Select Model Type',
  CREATE_NEW_MODEL_MESSAGE: 'Please select or create a new model.',
  RETRIEVE_RESULTS_MESSAGE: 'Please wait while we retrieve the results...',
  PREPARE_RESULTS_MESSAGE: 'Please wait while we prepare the results...',
  BATCH_TEST_FAILED_MESSAGE: 'The selected batchtest failed. Please select another one.',
  NO_MODELS_AVAILABLE: (itsClientId, itsAppId) => `No models available for the client ${itsClientId} ${itsAppId}`,
  NO_BATCH_TEST: 'You don\'t have any recent batchtest.',
  PROJECT_NOT_FOUND: 'Requested project is not found.',
  TAGS_NOT_AVAILABLE: 'Please upload a dataset to start.',
  LOADING_MESSAGE: 'Loading...',
  CHOOSE_TYPE: 'Choose Type...',

  // Column Selector Dialog
  SHOW_OR_HIDE: 'Show/Hide Columns',

  // Table Constants
  TABLE_CONSTANTS: {
    RESIZED_WIDTH: 'resizedWidth',
    VISBLE: 'visible',
  },

  // Used in table filtering
  ALL: 'ALL',

  ENVIRONMENTS: {
    DEV: 'dev',
    QA: 'qa',
    PSR: 'psr',
    STABLE: 'stable',
    STAGING: 'staging',
    PRODUCTION: 'production',
  },

  // localStorageKeys
  LOCALSTORAGE_KEY: {
    READ_PROJECT_DATASET: 'ReadProjectDataset',
    READ_PROJECT_MODEL: 'ReadProjectModel',
    TAG_DATASET_TABLE: 'DatasetTable',
    MODEL_VIEW: 'ModelView',
    INTENTS_TABLE: 'IntentsTable',
    COLLAPSIBLE_SIDEBAR_OPEN: 'isCollapsibleModelOpen',
  },

  BORDER: '1px solid #dadadd',
  noop: () => {},


  TAGGING_GUIDE_TABLE: {
    count: {
      header: 'Count',
      id: 'count',
    },
    granularIntent: {
      header: 'Granular Intent',
      id: 'intent',
      placeholder: 'format: noun-verb, example: agent-query',
      mandatory: true,
      tooltipText: 'Please provide a valid entry.',
    },
    rollupIntent: {
      header: 'Rollup Intent',
      id: 'rutag',
      placeholder: 'format: Noun_Verb, example: Agent_Query',
    },
    description: {
      header: 'Description',
      id: 'description',
      placeholder: 'Add your description',
    },
    keywords: {
      header: 'Keywords',
      id: 'keywords',
      placeholder: 'Add any keywords',
    },
    examples: {
      header: 'Examples',
      id: 'examples',
      placeholder: 'Add any examples',
    },
    comments: {
      header: 'Comments',
      id: 'comments',
      placeholder: 'Add any comments',
    },
    delete: {
      header: 'Delete',
      id: 'delete',
      placeholder: 'Delete Intents',
    },
  },

  STATUS_CATEGORY_MAPPING: {
    QUEUED: 'secondary',
    RUNNING: 'primary',
    STARTED: 'primary',
    FAILED: 'danger',
    COMPLETED: 'success',
    LIVE: 'success',
    PREVIEW: 'primary',
    NULL: 'secondary',
    ERROR: 'danger',
    CANCELLED: 'warning',
  },

  PROJECT_MODELS_TABLE: {
    starred: {
      header: 'For Deploy',
      id: 'starred',
    },
    datasets: {
      header: 'Datasets',
      id: 'datasetIds',
    },
    description: {
      header: 'Description',
      id: 'description',
    },
    version: {
      header: 'Versions',
      id: 'version',
    },
    type: {
      header: 'Type',
      id: 'modelType',
    },
    technology: {
      header: 'Technology',
      id: 'technology',
    },
    status: {
      header: 'Status',
      id: 'status',
    },
    modelId: {
      header: 'ID',
      id: 'modelToken',
    },
    user: {
      header: 'Created By',
      id: 'userId',
    },
    created: {
      header: 'Created Time',
      id: 'created',
    },
    action: {
      header: 'Actions',
      id: 'action',
    },
  },

  PROJECT_DATASET_TABLE: {
    name: {
      header: 'Name',
      id: 'name',
    },
    description: {
      header: 'Description',
      id: 'description',
    },
    type: {
      header: 'Type',
      id: 'type',
    },
    locale: {
      header: 'Locale',
      id: 'locale',
    },
    status: {
      header: 'Status',
      id: 'status',
    },
    user: {
      header: 'User',
      id: 'modifiedBy',
    },
    createdAt: {
      header: 'Created Date',
      id: 'createdAt',
    },
    action: {
      header: 'Actions',
      id: 'action',
    },
  },

  MODEL_TEST_ENTITIES_TABLE: {
    name: {
      header: 'Name',
      id: 'name',
    },
    value: {
      header: 'Value',
      id: 'value',
    },
  },

  MODEL_TEST_TRANSFORMS_TABLE: {
    rank: {
      header: 'Rank',
      id: 'rank',
    },
    transformation: {
      header: 'Transformation',
      id: 'id',
    },
    result: {
      header: 'Result',
      id: 'result',
    },
  },

  MODEL_TEST_INTENTS_TABLE: {
    rank: {
      header: 'Rank',
      id: 'rank',
    },
    intent: {
      header: 'Intent',
      id: 'intent',
    },
    score: {
      header: 'Score',
      id: 'score',
    },
  },

  TEXTAREA: 'Textarea',
  TEXTFIELD: 'TextField',

  // Create a New Version Contextual Bar Style
  ACTION_STYLE: { height: '35px' },
  ACTION_ITEM_STYLE: { marginTop: '10px', paddingLeft: '0px' },

  NOTIFICATION: {
    interval: 3000,
    types: {
      error: 'error',
      success: 'success',
      default: 'default',
    },
  },

  POST_PROCESSING_RULES: {
    INPUT_MATCH: 'input-match',
    INTENT_MATCH: 'intent-match',
    INTENT_REPLACEMENT: 'intent-replacement',
  },
  TRAINING_OUTPUT_PURGE_TTL_IN_DAYS: 60,
  TRAINING_OUTPUT_PURGE_MESSAGE_HEADER: 'Training output is purged',
  TRAINING_OUTPUT_PURGE_MESSAGE_BODY: 'As per the storage policy, training output for this model has been purged. However you can rebuild the model to re-generate the model training output.',

  MARK_FOR_DEPLOY_MESSAGE_HEADER: 'Mark for deploy',
  UNMARK_FOR_DEPLOY_MESSAGE_HEADER: 'Unmark for deploy',
  MARK_FOR_DEPLOY_MESSAGE_BODY: 'Thank you for your contribution in selecting an improved version for deployment consideration. Please reach out to your account contact to get it validated and deployed.',
  UNMARK_FOR_DEPLOY_MESSAGE_BODY: 'This version will be unmarked for deploy.',
  OK_CHILDREN_MARK_FOR_DEPLOY: 'MARK FOR DEPLOY',
  OK_CHILDREN_UNMARK_FOR_DEPLOY: 'UNMARK FOR DEPLOY',

  // Session timeout setting - warning in 58 min, logout in another 2 min.
  SESSION_TIMEOUT_WARNING_HEADER: 'Your session will end in 2 minutes.',
  SESSION_TIMEOUT_WARNING_MESSAGE: 'As a security precaution, you will be signed out due to inactivity. Do you want to continue working?',
  SESSION_TIMEOUT_WARNING_LOG_MESSAGE: 'User clicked on continue working, resetting server session timeout.',
  SESSION_TIMEOUT_LOGOUT_LOG_MESSAGE: 'Session Timed out!! User is being logged out!',
  SESSION_TIMEOUT_LOGOUT_DIALOG_BUTTON: 'Continue Working',
  SESSION_TIMEOUT_PING_BACKEND: 'Pinging Backend!',

  // Log Levels
  LOG_LEVEL_INFO: 'info',
  LOG_LEVEL_WARNING: 'warning',
  LOG_LEVEL_ERROR: 'error',

  // DropZone labels
  DROPZONE: {
    ACCEPCTED: 'Drop your file here',
    REJECTED: (accept) => `File is not in accepted format.
    Please upload ${accept} again.`,
  },

  VALIDATION_MSG: (value) => `Please provide a ${value} `,

  DATASET_NAME_SIZE_LIMIT: 64,
  TRANSFORMATION_ITEM_NAME_LIMIT: 64,
  INVALID_ENTERED_NAME: 'Only alphanumeric characters are allowed.',
  VALIDATION_NAME_SIZE_MSG: 'Oops, Exceeded maximum size of 64 characters.',
  ALERT_TRANSFORMATION: (name) => `Url Transformation for ${name} exists already`,
  VALIDATION_NO_DATA_MSG: 'Please provide a name',
  TYPE_MSG: 'Please select a type',
  NAME_REGEX: '[a-z0-9A-Z_][a-z0-9A-Z_-\\s]*',
  TRANSFORMATION_ITEM_NAME_REGEX: '[a-zA-Z][a-zA-Z-]*',
  VALIDATION_SPLIT_INTERNAL_MSG: 'Only range between 2 to 10 is allowed.',
  VALIDATION_SPLIT_EXTERNAL_MSG: 'Only range between 2 to 5 is allowed.',
  VALIDATION_SPLIT_NO_DATA_MSG: 'Please provide a valid entry.',
  MIN_VALIDATION_SPLIT_INTERNAL: 2,
  MAX_VALIDATION_SPLIT_INTERNAL: 10,
  MAX_VALIDATION_SPLIT_EXTERNAL: 5,
  MAX_CLASS_LENGTH: 50,
  MIN_CLASS_LENGTH: 8,


  WORD_CLASS_SYNTAX: '_class_',
  UTTERANCE_PLACEHOLDER_TEXT: (example) => `Type utterance here... example: ${example}`,

  // Speech Modal Radio button
  SPEECH_DIALOG: {
    BUNDLED: 'bundled-data',
    UNBUNDLED: 'unbundled-data',
    BUNDELED_LABEL: 'Bundle GRXML and SLM output',
    UNBUNDLED_LABEL: 'Separate GRXML from SLM output',

    MORE_OPTION: 'MORE OPTIONS >',
    INVALID_FILE_CONTENT: 'File format or content is invalid. Please consult documentation.',
  },

  // Dataset creation default value
  DATASET_TYPE_DEFAULT: 'Chat/Text',
  SUGGESTED_INTENT_DEFAULT: false,

  // Settings Page
  PREFERENCES: 'Preferences',
  NAV_MODEL_TECHNOLOGIES: 'Model Technologies',
  NAV_MODEL_MONITORING: 'Model Monitoring',
  MODEL_TECHNOLOGY_TITLE: 'Model Technology',
  MODEL_MONITORING_CONFIG_TITLE: 'Model Monitoring configuration',
  MODEL_TECHNOLOGY_DESCRIPTION: 'Select one from the list below to set as a default technology.',
  MODEL_MONITORING_DESCRIPTION: 'Set default for Model monitoring. You can set the individual model and node-level threshold under the \'Setting\' tab on model page',
  SAVE_DIALOG_TITLE: 'Are you sure',
  SAVE_DIALOG_CONTENT: 'Changing the default value will not affect the already existing models, instead the new models will use the default technology. You can always change the technology while creating or editing a model version',
  SAVE_DIALOG_CHECKBOX: 'Update model technology of all the existing models to this default',
  SETTINGS_NOTIFICATION_MESSAGE: (type) => `Model default is successfully updated to ${type}`,

  // Model Technology
  MODEL_TECHNOLOGY: {
    USE: 'use_large',
    N_GRAM: 'n-gram',
    TENSORFLOW_LABEL: (version, text) => `TensorFlow USE-Large ${version}    \n${text}`,
    N_GRAM_LABEL: (text) => `n-grams\n${text}`,
  },
  DEFAULT_TEXT: '(Default)',
  CHECKBOX_LABEL: 'Use it as default for this model',

  TENSORFLOW_VERSION: 'v1.3',

  // Settings Tab
  MODEL_LEVEL_MONITORING_THRESHOLD_TITLE: 'Model Level Monitoring Threshold',
  NODE_LEVEL_MONITORING_THRESHOLD_TITLE: 'Node Level Monitoring Threshold',
  ESCALATION_RATE_TITLE: 'Escalation Rate',
  ESCALATION_RATE_DESCRIPTION: 'Set default for all the Nodes',
  UNIVERSAL_TAB: 'Universal',
  EACH_NODE_TAB: 'Each Node',
  SET_THRESHOLD_TEXT_FOR_A_DAY: 'Set threshold and send alert if escalation rate exceeds ',
  PERCENTAGE_FOR_A_DAY: ' for a day',
  PERCENTAGE_IN_A_DAY: ' % in a day',
  SET_THRESHOLD_TEXT_FOR_CONSECUTIVE_DAYS: 'Set threshold and send alert if the difference between escalation rate exceeds ',
  PERCENTAGE_ON_CONSECUTIVE_DAYS: ' on consecutive days',
  DEFAULT_THRESHOLD: 'Default 30%',

  // Sidebar
  PERCENTAGE: (value) => `${value}%`,

  TENSORFLOW: 'Tensorflow',
  N_GRAM: 'N-Gram',
  LEVEL: {
    CLIENT: 'client',
    MODEL: 'model',
  },
  VECTORIZER_TYPE: 'Vectorizer',

  // overview
  PLACEHOLDER: 'Select Model Type',
  DESCRIPTION: 'DESCRIPTION',
  ID: 'ID',
  CREATED: 'CREATED',
  TYPE: 'TYPE',
  MODEL_TECH: 'MODEL TECH',
  DATASET: 'DATASET',
  VERSION_SELECT: 'VERSION',
  STATUSBADGE: 'STATUS',
  NODES: ' Nodes',
  NODENAME: 'NODE NAME',
  VOLUME_NODE: 'VOLUME',
  ESCLATION: 'ESCLATION',
  NODE_TYPE: 'NODE',


};
