import { getCategory } from 'components/modelConfigs/transformations/transformationTypes';
import _ from 'lodash';
import Constants from 'constants/Constants';

export const getPostProcessingRules = (config) => {
  let retVal = [];

  if (config && config.postProcessingRules) {
    retVal = config.postProcessingRules;
  }

  return retVal;
};

export const replacePostProcessingRules = (config, postProcessingRules) => {
  const retVal = config || {};
  const postProcessing = [];

  (postProcessingRules).forEach((item) => {
    let intentMatch = item['intent-match'];
    let inputMatch = item['input-match'];
    let intentReplacement = item['intent-replacement'];

    if ((_.isNil(intentMatch) || intentMatch.length === 0)
       || (_.isNil(inputMatch) || inputMatch.length === 0)
       || (_.isNil(intentReplacement) || intentReplacement.length === 0)) {
      return;
    }

    if (typeof (intentMatch) === 'string') {
      if (intentMatch.indexOf('/') === -1) {
        intentMatch = intentMatch.split(',');
        intentMatch = intentMatch.map(item => item.trim());
      }
    }

    const newItem = {
      'input-match': item['input-match'],
      'intent-match': intentMatch,
      'intent-replacement': item['intent-replacement'],
    };
    if (item.minConfidenceScore > -1) {
      newItem.minConfidenceScore = item.minConfidenceScore;
    }

    if (item.maxConfidenceScore > -1) {
      newItem.maxConfidenceScore = item.maxConfidenceScore;
    }

    postProcessing.push(newItem);
  });

  retVal.postProcessingRules = postProcessing;
  return retVal;
};

export const getTransformationContent = (transformationItem) => {
  if (!transformationItem) {
    return null;
  }

  if (typeof transformationItem === 'string') {
    return transformationItem;
  }

  const key = getTransformationName(transformationItem);
  const transformationItemObject = transformationItem[key];

  if (transformationItemObject.hasOwnProperty('mappings')) {
    return transformationItemObject.mappings;
  }
  if (transformationItemObject.hasOwnProperty('url')) {
    return transformationItemObject.url;
  }
  if (transformationItemObject.hasOwnProperty('list')) {
    return transformationItemObject.list;
  }

  return null;
};

export const saveTransformationContent = (transformationItem, content) => {
  const updatedTransformation = transformationItem;
  const key = getTransformationName(transformationItem);
  const transformationItemObject = transformationItem[key];
  const category = getCategory(transformationItemObject.type);

  if (typeof content === 'string' && content.trim().length > 0) {
    content = JSON.parse(content);
  }

  if (transformationItemObject.hasOwnProperty('mappings')) {
    transformationItemObject.mappings = content;
  }
  if (transformationItemObject.hasOwnProperty('url') || category === 'url') {
    content = content.trim();
    if (content.length > 0) {
      transformationItemObject.url = content;
    } else if (transformationItemObject.hasOwnProperty('url')) {
      delete transformationItemObject.url;
    }
  }
  if (transformationItemObject.hasOwnProperty('list')) {
    transformationItemObject.list = content;
  }

  return updatedTransformation;
};

export const getTransformationName = (transformation) => {
  if (typeof transformation === 'string') {
    return transformation;
  }
  return Object.keys(transformation)[0];
};

export const findMatchingTransformationIndexes = (transformationList, transformation) => {
  const name = getTransformationName(transformation);
  const indexList = [];
  transformationList.forEach((item, idx) => {
    const itemName = getTransformationName(item);
    if (itemName === name) {
      indexList.push(idx);
    }
  });
  return indexList;
};

export const saveTransformationToList = (transformationList, transformation) => {
  const matchingIndexes = findMatchingTransformationIndexes(transformationList, transformation);
  matchingIndexes.forEach((idxMatch) => {
    transformationList[idxMatch] = transformation;
  });

  return transformationList;
};

export const getDefaultTransformation = (transformationDefinition) => {
  let transformation;
  const category = getCategory(transformationDefinition.typeId);

  if (category === 'boolean') {
    return transformationDefinition.typeId;
  }

  transformation = {
    [transformationDefinition.name]: {
      comments: transformationDefinition.comment,
      type: transformationDefinition.typeId,
    },
  };

  switch (category) {
  case 'url': {
    transformation[transformationDefinition.name].url = ' ';
    break;
  }
  case 'pairs': {
    transformation[transformationDefinition.name].mappings = {};
    break;
  }
  case 'list': {
    transformation[transformationDefinition.name].list = [];
    break;
  }
  }

  return transformation;
};

export const getDefaultConfig = (configs) => {
  const configsArray = !_.isNil(configs) ? configs.toArray() : [];

  let defaultConfig = null;
  defaultConfig = _.find(configsArray, { name: Constants.DEFAULT_CONFIG_NAME });
  if (!defaultConfig) {
    defaultConfig = _.find(configsArray, { name: Constants.ALTERNATE_DEFAULT_CONFIG_NAME });
  }
  return defaultConfig || configsArray[0];
};
