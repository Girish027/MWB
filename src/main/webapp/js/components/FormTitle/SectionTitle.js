import React from 'react';
import PropTypes from 'prop-types';
import { LegacyGrid, LegacyRow, LegacyColumn } from '@tfs/ui-components';
import ObjectUtils from 'utils/ObjectUtils';

const SectionTitle = (props) => {
  const {
    title, properties, formContext,
  } = props;

  // Hide section when all the properties are set to hidden
  let hideSection = false;
  if (!ObjectUtils.isEmptyOrNull(properties)) {
    for (let index = 0; index < properties.length; index += 1) {
      const elementName = properties[index].name;
      if (!formContext[elementName]
        || (formContext[elementName] && !formContext[elementName].hidden)) {
        hideSection = false;
        break;
      }
      hideSection = true;
    }
  }
  if (hideSection) {
    return null;
  }

  return (
    <LegacyGrid>
      { !ObjectUtils.isEmptyOrNull(title)
        ? (
          <LegacyRow>
            <LegacyColumn size={10}>
              <div
                style={{
                  color: '#333333',
                  fontSize: '14px',
                  lineHeight: '24px',
                  fontWeight: 'bold',
                  marginBottom: '15px',
                  float: 'left',
                }}
              >
                {title}
              </div>
            </LegacyColumn>
          </LegacyRow>
        )
        : null
      }
      <LegacyRow>
        { properties ? properties.map((element) => (
          <LegacyColumn
            key={element.name}
            size={formContext[element.name] ? formContext[element.name].size : 12}
          >
            {element.content}
          </LegacyColumn>
        ))
          : null
        }
      </LegacyRow>
    </LegacyGrid>
  );
};

SectionTitle.propTypes = {
  title: PropTypes.string,
  properties: PropTypes.array,
  formContext: PropTypes.object,
};

SectionTitle.defaultProps = {
  title: '',
  properties: [],
  formContext: {},
};

export default SectionTitle;
