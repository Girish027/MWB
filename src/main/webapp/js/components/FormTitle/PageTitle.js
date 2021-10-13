import React from 'react';
import PropTypes from 'prop-types';
import { LegacyGrid, LegacyRow, LegacyColumn } from '@tfs/ui-components';
import ObjectUtils from 'utils/ObjectUtils';

const PageTitle = (props) => {
  const {
    title, description, properties, formContext,
  } = props;
  let hideTitle = false;
  let hideDescription = false;

  if (formContext.rootSettings) {
    hideTitle = ObjectUtils.get(['rootSettings', 'title', 'hidden'], formContext, false);
    hideDescription = ObjectUtils.get(['rootSettings', 'description', 'hidden'], formContext, false);
  }

  const showPageTitle = !ObjectUtils.isEmptyOrNull(title) && !hideTitle;
  const showPageDesc = !ObjectUtils.isEmptyOrNull(description) && !hideDescription;

  return (
    <LegacyGrid>
      { showPageTitle || showPageDesc
        ? (
          <LegacyRow>
            <LegacyColumn size={10}>
              { showPageTitle
              && (
                <div
                  style={{
                    color: '#333333',
                    fontSize: '20px',
                    lineHeight: '30px',
                    fontWeight: 'bold',
                    marginBottom: '4px',
                  }}
                >
                  {title}
                </div>
              )
              }
              { showPageDesc
                && (
                  <div
                    style={{
                      color: '#9B9B9B',
                      fontSize: '15px',
                      lineHeight: '21px',
                      fontWeight: 'normal',
                    }}
                  >
                    {description}
                  </div>
                )
              }
            </LegacyColumn>
          </LegacyRow>
        )
        : null
      }
      <LegacyRow>
        { properties.map((element) => (
          <LegacyColumn
            key={element.name}
            size={formContext[element.name] ? formContext[element.name].size : 12}
          >
            {element.content}
          </LegacyColumn>
        ))}
      </LegacyRow>
    </LegacyGrid>
  );
};

PageTitle.propTypes = {
  title: PropTypes.string,
  description: PropTypes.string,
  properties: PropTypes.array,
  formContext: PropTypes.object,
};

PageTitle.defaultProps = {
  title: '',
  description: '',
  properties: [],
  formContext: {},
};

export default PageTitle;
