/* react */
import React, { Component } from 'react';
import PropTypes from 'prop-types';

/* grommet */
import Box from 'grommet/components/Box';
import CaretNextIcon from 'grommet/components/icons/base/CaretNext';


export default class TaggerCascadeMenu extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.onClickNotWorking = this.onClickNotWorking.bind(this);

    this.state = {
      items: this.buildItems(this.props.items),
    };
  }

  componentWillReceiveProps(nextProps) {
    this.props = nextProps;

    this.setState({
      items: this.buildItems(this.props.items),
    });
  }

  onClickNotWorking(event) {
    event.stopPropagation();
    event.nativeEvent.stopImmediatePropagation();
  }

  buildItems(items) {
    if (!items || !Array.isArray(items) || !items.length) {
      return null;
    }
    const { hideItems, className } = this.props;
    let childItems = [],
      i,
      key = `TaggerCascadeMenu${Date.now()}`;
    for (i = 0; i < items.length; i++) {
      const item = items[i];
      if (hideItems && item.name && hideItems[item.name]) {
        continue;
      }
      childItems.push(<Box
        direction="row"
        className={`TaggerCascadeMenuItem${item.className ? ` ${item.className}` : ''}${item.disabled ? ' disabled' : ''}`}
        key={`${key}-${i}`}
        onMouseEnter={item.onMouseEnter}
        onMouseLeave={item.onMouseLeave}
      >
        {/* child items */}
        {item.disabled ? null : this.buildItems(item.items)}

        {/* icon */}
        {item.icon ? (
          <Box pad="small" className="TaggerCascadeMenuItemIcon" onClick={((item.disabled || !item.onClick) ? this.onClickNotWorking : item.onClick)}>
            {item.icon}
          </Box>
        ) : null}

        {/* label */}
        <Box pad="small" flex className="TaggerCascadeMenuItemLabel" onClick={((item.disabled || !item.onClick) ? this.onClickNotWorking : item.onClick)}>
          {item.text}
        </Box>

        {/* arrow */}
        {(item.items && Array.isArray(item.items) && items.length) ? (
          <Box pad="small" className="TaggerCascadeMenuNextIcon" onClick={((item.disabled || !item.onClick) ? this.onClickNotWorking : item.onClick)}>
            <CaretNextIcon />
          </Box>
        ) : null}
      </Box>);
    }
    return (childItems.length > 0 ? (
      <Box
        direction="column"
        separator="all"
        alignSelf="start"
        className={`TaggerControl TaggerCascadeMenu${className ? ` ${className}` : ''}`}
        key={key}
      >
        { childItems }
      </Box>
    ) : null);
  }

  render() {
    const { items } = this.state;
    return items;
  }
}


TaggerCascadeMenu.propTypes = {
  items: PropTypes.array,
  hideItems: PropTypes.object,
  className: PropTypes.string,
};
