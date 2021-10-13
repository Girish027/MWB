import apostrophe from './apostrophe';
import classNumber from './classNumber';
import classPercentage from './classPercentage';
import classTime from './classTime';
import dateRegex from './dateRegex';
import emailRegex from './emailRegex';
import htmlEncoding from './htmlEncoding';
import phoneNumber from './phoneNumber';
import punctuationNormalization from './punctuationNormalization';
import removeDupWords from './removeDupWords';
import synonyms from './synonyms';
import synonymsSet2 from './synonymsSet2';
import synonymsSet3 from './synonymsSet3';
import synonymsSet4 from './synonymsSet4';
import urlRegex from './urlRegex';
import sentenceMarker from './sentenceMarker';
import nonBreakingSpace from './nonBreakingSpace';

const preDefinedTransformations = [
  {
    name: 'apostrophe-regex',
    id: 'apostrophe',
    getMethod: apostrophe,
  },
  {
    name: 'class_number',
    id: 'classNumber',
    getMethod: classNumber,
  },
  {
    name: 'class_percentage',
    id: 'classPercentage',
    getMethod: classPercentage,
  },
  {
    name: 'class_time',
    id: 'classTime',
    getMethod: classTime,
  },
  {
    name: 'date-regex',
    id: 'dateRegex',
    getMethod: dateRegex,
  },
  {
    name: 'email-regex',
    id: 'emailRegex',
    getMethod: emailRegex,
  },
  {
    name: 'html-encoding',
    id: 'htmlEncoding',
    getMethod: htmlEncoding,
  },
  {
    name: 'non-breaking-space',
    id: 'nonBreakingSpace',
    getMethod: nonBreakingSpace,
  },
  {
    name: 'phone_number',
    id: 'phoneNumber',
    getMethod: phoneNumber,
  },
  {
    name: 'punctuation-normalization',
    id: 'punctuationNormalization',
    getMethod: punctuationNormalization,
  },
  {
    name: 'rx-remove-dup-words',
    id: 'removeDupWords',
    getMethod: removeDupWords,
  },
  {
    name: 'sentence-marker',
    id: 'sentenceMarker',
    getMethod: sentenceMarker,
  },
  {
    name: 'synonyms-set1',
    id: 'synonymsSet1',
    getMethod: synonyms,
  },
  {
    name: 'synonyms-set2',
    id: 'synonymsSet2',
    getMethod: synonymsSet2,
  },
  {
    name: 'synonyms-set3',
    id: 'synonymsSet3',
    getMethod: synonymsSet3,
  },
  {
    name: 'synonyms-set4',
    id: 'synonymsSet4',
    getMethod: synonymsSet4,
  },
  {
    name: 'url-regex',
    id: 'urlRegex',
    getMethod: urlRegex,
  },
];

export const getPredefinedDisplayList = () => {
  const names = preDefinedTransformations.map(item => ({
    name: item.name,
    id: item.id,
  }));
  return names;
};

export const getTransforms = (transformationsToAdd) => {
  const newTransforms = [];

  transformationsToAdd.forEach((item) => {
    const newTransformItem = preDefinedTransformations.find(element => element.id === item);

    if (newTransformItem) {
      newTransforms.push({
        [newTransformItem.name]: newTransformItem.getMethod(),
      });
    }
  });

  return newTransforms;
};

export default preDefinedTransformations;
