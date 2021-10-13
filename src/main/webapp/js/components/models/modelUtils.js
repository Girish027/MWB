import _ from 'lodash';
import Papa from 'papaparse';
// import base64 from 'base-64';

export const getDatasetOptions = (datasets = []) => {
  const datasetsOptions = [];
  if (datasets && datasets.size > 0) {
    datasets.forEach((item) => {
      if (item.id && item.status
        && item.status == 'COMPLETED' && item.name) {
        datasetsOptions.push({ value: item.id, label: item.name });
      }
    });
  }
  return datasetsOptions;
};

export const getSelectedDatasets = (datasetsOptions, model, stateSelectedDatasets) => {
  let selectedDatasets = [];

  if (stateSelectedDatasets) {
    return stateSelectedDatasets;
  }

  // Retreive selected datsets from incoming model or
  if (datasetsOptions.length > 0) {
    if (model && model.datasetIds
      && model.datasetIds.length > 0) {
      selectedDatasets = model.datasetIds;
    }
  }

  return selectedDatasets;
};

export const getBatchResultsData = (results, headerData) => {
  let bytes = '';
  if (!_.isEmpty(results) && !_.isEmpty(results.message)) {
    // bytes = base64.decode(results.message);
    // bytes = decodeURIComponent(escape(atob(results.message)));
    bytes = decodeURIComponent(atob(results.message).split('').map((c) => `%${(`00${c.charCodeAt(0).toString(16)}`).slice(-2)}`).join(''));
  }
  let data = bytes;
  if (!_.isNil(headerData)) {
    const { date, model, jobId } = headerData;
    const headers = [`date: ${date} | model name: ${model.name} | version: ${model.version} | job request id: ${jobId}`];
    data = `${headers}\n${bytes}`;
  }
  return data;
};

export const batchResultsDataToJson = (results) => {
  let bytes = '';
  if (!_.isEmpty(results) && !_.isEmpty(results.message)) {
    // bytes = base64.decode(results.message);
    // bytes = decodeURIComponent(escape(atob(results.message)));
    bytes = decodeURIComponent(atob(results.message).split('').map((c) => `%${(`00${c.charCodeAt(0).toString(16)}`).slice(-2)}`).join(''));
  }
  let data = Papa.parse(bytes, { header: true });

  return data;
};

export const base64ToBlob = (base64FileData, contentType) => {
  const byteCharacters = atob(base64FileData);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);
  const blob = new Blob([byteArray], {
    type: contentType,
  });

  return blob;
};
