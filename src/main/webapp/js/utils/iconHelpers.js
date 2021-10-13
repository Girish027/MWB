
const actionsImagesPathRoot = '/images/actions';
const iconsPathRoot = '/images/icons';

const iconListNames = {
  ARROW_LEFT: 'ARROW_LEFT',
  ARROW_RIGHT: 'ARROW_RIGHT',
  BARCHART: 'BARCHART',
  CHECKMARK: 'CHECKMARK',
  DOC: 'DOC',
  FILTER: 'FILTER',
  PENCIL: 'PENCIL',
  PLUS: 'PLUS',
  TAG: 'TAG',
  XMARK: 'XMARK',
  NEXT: 'NEXT',
  DELETE_TRASH: 'DELETE_TRASH',
  FAILED: 'FAILED',
  QUEUED: 'QUEUED',
  COMPLETED: 'COMPLETED',
  PROCESSING: 'PROCESSING',
};

const iconList = {
  [iconListNames.ARROW_LEFT]: `${actionsImagesPathRoot}/arrowLeft.svg`,
  [iconListNames.ARROW_RIGHT]: `${actionsImagesPathRoot}/arrowRight.svg`,
  [iconListNames.BARCHART]: `${actionsImagesPathRoot}/barChart.svg`,
  [iconListNames.CHECKMARK]: `${actionsImagesPathRoot}/checkmark.svg`,
  [iconListNames.DOC]: `${actionsImagesPathRoot}/doc.svg`,
  [iconListNames.FILTER]: `${actionsImagesPathRoot}/filter.svg`,
  [iconListNames.PENCIL]: `${actionsImagesPathRoot}/pencil.svg`,
  [iconListNames.PLUS]: `${actionsImagesPathRoot}/plus.svg`,
  [iconListNames.TAG]: `${actionsImagesPathRoot}/tag.svg`,
  [iconListNames.XMARK]: `${actionsImagesPathRoot}/xmark.svg`,
  [iconListNames.NEXT]: `${iconsPathRoot}/next.svg`,
  [iconListNames.DELETE_TRASH]: `${actionsImagesPathRoot}/deleteTrash.svg`,
  [iconListNames.FAILED]: `${iconsPathRoot}/failed.svg`,
  [iconListNames.QUEUED]: `${iconsPathRoot}/queued.svg`,
  [iconListNames.COMPLETED]: `${iconsPathRoot}/completed.svg`,
  [iconListNames.PROCESSING]: `${iconsPathRoot}/processing.svg`,
};

export const IconNames = {
  CREATE: 'CREATE',
  CONSISTENCY: 'CONSISTENCY',
  DELETE: 'DELETE',
  ADD: 'ADD',
  IMPORT: 'IMPORT',
  REPORTS: 'REPORTS',
  TAG: 'TAG',
  FILTER: 'FILTER',
  EXPORT: 'EXPORT',
  INTENTS: 'INTENTS',
  APPLY_SUGGESTED: 'APPLY_SUGGESTED',
  BULK_TAG: 'BULK_TAG',
  BULK_UNTAG: 'BULK_UNTAG',
  MODEL_CREATION_NEXT: 'MODEL_CREATION_NEXT',
  // Batch Test Icons
  DELETE_BATCH_TEST: 'DELETE_BATCH_TEST',
  FAILED_BATCH_TEST: 'FAILED_BATCH_TEST',
  QUEUED_BATCH_TEST: 'QUEUED_BATCH_TEST',
  COMPLETED_BATCH_TEST: 'COMPLETED_BATCH_TEST',
  INPROGRESS_BATCH_TEST: 'INPROGRESS_BATCH_TEST',
};

const iconMap = {
  [IconNames.CREATE]: iconList[iconListNames.PENCIL],
  [IconNames.CONSISTENCY]: iconList[iconListNames.DOC],
  [IconNames.DELETE]: iconList[iconListNames.XMARK],
  [IconNames.ADD]: iconList[iconListNames.PLUS],
  [IconNames.IMPORT]: iconList[iconListNames.PLUS],
  [IconNames.REPORTS]: iconList[iconListNames.BARCHART],
  [IconNames.TAG]: iconList[iconListNames.TAG],
  [IconNames.FILTER]: iconList[iconListNames.filter],
  [IconNames.EXPORT]: iconList[iconListNames.ARROW_RIGHT],
  [IconNames.INTENTS]: iconList[iconListNames.DOC],
  [IconNames.APPLY_SUGGESTED]: iconList[iconListNames.TAG],
  [IconNames.BULK_TAG]: iconList[iconListNames.TAG],
  [IconNames.BULK_UNTAG]: iconList[iconListNames.XMARK],
  [IconNames.MODEL_CREATION_NEXT]: iconList[iconListNames.NEXT],
  [IconNames.FAILED_BATCH_TEST]: iconList[iconListNames.FAILED],
  [IconNames.DELETE_BATCH_TEST]: iconList[iconListNames.DELETE_TRASH],
  [IconNames.QUEUED_BATCH_TEST]: iconList[iconListNames.QUEUED],
  [IconNames.COMPLETED_BATCH_TEST]: iconList[iconListNames.COMPLETED],
  [IconNames.INPROGRESS_BATCH_TEST]: iconList[iconListNames.PROCESSING],
};

const getIcon = iconName => iconMap[iconName];

export default getIcon;
