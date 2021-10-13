import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  getLastItemStyle,
} from 'components/modelConfigs/modelConfigUtilities';

import {
  Xmark,
  DragIcon,
} from '@tfs/ui-components';
import IconButton from 'components/IconButton';

class DraggableItem extends Component {
  render() {
    const {
      children,
      id,
      noOfItems,
      onDelete,
      modelViewReadOnly,
      onSelect,
      index,
      className,
      itemContentClass,
    } = this.props;

    return (
      <div
        data-name={`item-${index}`}
        className={`list-item ${className}`}
        key={`item-${id}`}
        style={getLastItemStyle(index, noOfItems)}
        onClick={(event) => onSelect(event, index)}
      >
        <div className="index">
          {index + 1}
        </div>
        <div className={`item-content  ${itemContentClass}`}>
          {children}
        </div>
        {(!modelViewReadOnly)
            && (
              <div className="list-item-delete">
                <IconButton
                  icon={Xmark}
                  onClick={(event) => onDelete(event, index)}
                  title="Delete Item"
                />
                <span
                  title="Grab and Reorder"
                  className="grabbable"
                >
                  <DragIcon />
                </span>
              </div>
            )
        }
      </div>
    );
  }
}
DraggableItem.propTypes = {
  children: PropTypes.node,
  id: PropTypes.string.isRequired,
  noOfItems: PropTypes.number.isRequired,
  onDelete: PropTypes.func,
  modelViewReadOnly: PropTypes.bool.isRequired,
  onSelect: PropTypes.func,
  index: PropTypes.number.isRequired,
  className: PropTypes.string,
  itemContentClass: PropTypes.string,
};

DraggableItem.defaultProps = {
  onDelete: () => {},
  onSelect: () => {},
  itemContentClass: '',
  className: '',
};

export default DraggableItem;
