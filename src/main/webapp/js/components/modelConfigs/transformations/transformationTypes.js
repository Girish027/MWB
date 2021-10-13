
import _ from 'lodash';
import Constants from 'constants/Constants';

const transformCategories = {
  pairs: 'pairs',
  trueFalse: 'boolean',
  listCategory: 'list',
  url: 'url',
};

const transformationTypes = [
  {
    displayName: 'Case Normalization',
    id: Constants.TRANSFORMATION_TYPES.CASE_NORMALIZATION,
    category: transformCategories.trueFalse,
    supportMultiple: true,
    ordered: false,
  },
  {
    displayName: 'Input Match',
    id: Constants.TRANSFORMATION_TYPES.INPUT_MATCH,
    category: transformCategories.pairs,
    supportMultiple: true,
    ordered: true,
  },
  {
    displayName: 'Regex Removal',
    id: Constants.TRANSFORMATION_TYPES.REGEX_REMOVAL,
    category: transformCategories.listCategory,
    supportMultiple: true,
    ordered: true,
  },
  {
    displayName: 'Regex Replace',
    id: Constants.TRANSFORMATION_TYPES.REGEX_REPLACE,
    category: transformCategories.pairs,
    supportMultiple: true,
    ordered: true,
  },
  {
    displayName: 'Spell Checking',
    id: Constants.TRANSFORMATION_TYPES.SPELL_CHECKING,
    category: transformCategories.url,
    supportMultiple: false,
    ordered: false,
  },
  {
    displayName: 'Stems (no case) - pairs',
    id: Constants.TRANSFORMATION_TYPES.STEMS_NOCASE,
    category: transformCategories.pairs,
    supportMultiple: true,
    ordered: false,
  },
  {
    displayName: 'Stems (no case) - url',
    id: Constants.TRANSFORMATION_TYPES.STEMS_NOCASE_URL,
    category: transformCategories.url,
    supportMultiple: false,
    ordered: false,
  },
  {
    displayName: 'Stems - pairs',
    id: Constants.TRANSFORMATION_TYPES.STEMS,
    category: transformCategories.pairs,
    supportMultiple: true,
    ordered: false,
  },
  {
    displayName: 'Stems - url',
    id: Constants.TRANSFORMATION_TYPES.STEMS_URL,
    category: transformCategories.url,
    supportMultiple: false,
    ordered: false,
  },
  {
    displayName: 'Stop Words',
    id: Constants.TRANSFORMATION_TYPES.STOP_WORDS,
    category: transformCategories.listCategory,
    supportMultiple: false,
    ordered: false,
  },
  {
    displayName: 'Training Data Stems',
    id: Constants.TRANSFORMATION_TYPES.TRAINING_DATA_STEMS,
    category: transformCategories.trueFalse,
    supportMultiple: false,
    ordered: false,
  },
  {
    displayName: 'White Space Normalization',
    id: Constants.TRANSFORMATION_TYPES.WHITESPACE_NORMALIZATION,
    category: transformCategories.trueFalse,
    supportMultiple: true,
    ordered: false,
  },
  {
    displayName: 'Wordclass Substitution Regex',
    id: Constants.TRANSFORMATION_TYPES.WORDCLASS_SUBST_REGEX,
    category: transformCategories.pairs,
    supportMultiple: true,
    ordered: true,
  },
  {
    displayName: 'Wordclass Substitution Text',
    id: Constants.TRANSFORMATION_TYPES.WORDCLASS_SUBST_TEXT,
    category: transformCategories.pairs,
    supportMultiple: true,
    ordered: true,
  },
//  {
//    displayName: 'Url Transformation',
//    id: Constants.TRANSFORMATION_TYPES.TRANSFORMATION_URL,
//    supportMultiple: true,
//    ordered: false,
//  },
];

export const getTransformationTypesDisplayArray = () => {
  const names = transformationTypes.map(item => ({
    name: item.displayName,
    id: item.id,
  }));
  return names;
};

export const getFilteredTransformationTypesDisplayArray = (transformations) => {
  // Assemble a list of the names of the transformation types
  // If any of them do not support multiple and are already being used
  // do not include them in the list
  const names = transformationTypes.map((item) => {
    if (!item.supportMultiple) {
      if (!_.isNil(transformations)) {
        const isAlreadyUsed = transformations.find((element) => {
          // If the type is just a string - just compare to the id
          if (typeof (element) === 'string') {
            return item.id === element;
          }
          // If not, compare the type to the id
          const keys = Object.keys(element);
          const transformationItem = element[keys[0]];
          if (transformationItem.hasOwnProperty('type')) {
            return transformationItem.type === item.id;
          }
        });
        // If it is already used, return undefined which will be filtered out
        if (isAlreadyUsed) {
          return undefined;
        }
      }
    }
    return {
      name: item.displayName,
      id: item.id,
    };
  })
    // Filter out the undefined items
    .filter(item => item);
  return names;
};

export const getCategory = (transformationId) => {
  const transformItem = transformationTypes.find(element => (element.id === transformationId));

  return transformItem.category;
};

export const transformationTypeHasName = (transformationType) => {
  const category = getCategory(transformationType);
  return !(category === transformCategories.trueFalse);
};

export const convertTransformationTypeIfNeeded = (transformationType) => {
  const { TRANSFORMATION_TYPES } = Constants;

  switch (transformationType) {
  case TRANSFORMATION_TYPES.STEMS_URL: return TRANSFORMATION_TYPES.STEMS;
  case TRANSFORMATION_TYPES.STEMS_NOCASE_URL: return TRANSFORMATION_TYPES.STEMS_NOCASE;
  default: return transformationType;
  }
};

export default transformationTypes;
