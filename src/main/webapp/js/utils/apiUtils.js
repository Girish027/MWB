
// Make all ids strings

export const normalizeIds = (item) => {
  const newItem = Object.assign({}, item);
  if (newItem.hasOwnProperty('id')) {
    newItem.id = `${newItem.id}`;
  }
  if (newItem.hasOwnProperty('projectId')) {
    newItem.projectId = `${newItem.projectId}`;
  }
  if (newItem.hasOwnProperty('cid')) {
    newItem.cid = `${newItem.cid}`;
  }
  if (newItem.hasOwnProperty('clientId')) {
    newItem.clientId = `${newItem.clientId}`;
  }
  if (newItem.hasOwnProperty('configId')) {
    newItem.configId = `${newItem.configId}`;
  }

  return newItem;
};

export const normalizeModel = (model) => {
  const newModel = normalizeIds(model);
  if (newModel.datasetIds.length > 1) {
    const newDatasets = [];
    newModel.datasetIds.forEach((element) => {
      newDatasets.push(element.trim());
    });
    newModel.datasetIds = newDatasets;
  }
  return newModel;
};

export const normalizeIdArray = (idArray) => {
  const newIds = [];
  idArray.forEach((id) => {
    newIds.push(`${id}`);
  });

  return newIds;
};
