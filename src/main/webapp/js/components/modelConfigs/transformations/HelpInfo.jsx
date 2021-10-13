/* eslint-disable react/no-unescaped-entities */
/* eslint-disable react/jsx-no-target-blank */
import React from 'react';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import Constants from 'constants/Constants';

const getInfo = ({ example, link, value = 'MORE INFO' }, comments) => (
  <div className="transformation-item-help">
    { comments && (
      <div>
        <span className="label">
Comment:
        </span>
        <span className="content">
          {comments}
        </span>
      </div>
    )}
    <div>
      <span className="label">
Example:
      </span>
      <span className="content">
        {example}
      </span>
      <a href={link} target="_blank">{value}</a>
    </div>
  </div>
);

const { HELP, TRANSFORMATION_TYPES } = Constants;

const HelpInfo = {
  [TRANSFORMATION_TYPES.REGEX_REPLACE]: {
    // eslint-disable-next-line
    example: '/\\b(\\w+)(\\s\\1\\b)+/i: $1',
    link: HELP.TRANSFORMATION_LINK,
  },
  [TRANSFORMATION_TYPES.WORDCLASS_SUBST_REGEX]: {
    // eslint-disable-next-line
    example: '/\d+\.?\d*/: _class_number',
    link: HELP.TRANSFORMATION_LINK,
  },
  [TRANSFORMATION_TYPES.WORDCLASS_SUBST_TEXT]: {
    example: 'boston: _class_city',
    link: HELP.TRANSFORMATION_LINK,
  },
  [TRANSFORMATION_TYPES.INPUT_MATCH]: {
    example: '/gold|copper|aluminum/i: MetalRU',
    link: HELP.TRANSFORMATION_LINK,
  },
  [TRANSFORMATION_TYPES.REGEX_REMOVAL]: {
    example: '/%[0-9]+/',
    link: HELP.TRANSFORMATION_LINK,
  },
  [TRANSFORMATION_TYPES.STEMS]: {
    example: 'account\'s: account',
    link: HELP.TRANSFORMATION_LINK,
  },
  [TRANSFORMATION_TYPES.STEMS_NOCASE]: {
    example: 'account\'s: account',
    link: HELP.TRANSFORMATION_LINK,
  },
};

const getTransformationHelpInfo = (transformationType, userFeatureConfiguration, comments) => {
  let helpInfo = <div />;

  if (isFeatureEnabled(featureFlagDefinitions.names.transformationHelp,
    userFeatureConfiguration, featureFlagDefinitions.options.show)) {
    if (HelpInfo.hasOwnProperty(transformationType)) {
      helpInfo = HelpInfo[transformationType];
    }
  }
  return getInfo(helpInfo, comments);
};
export default getTransformationHelpInfo;
