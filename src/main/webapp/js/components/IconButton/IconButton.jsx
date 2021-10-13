import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Button } from '@tfs/ui-components';
import { colors } from '../../styles';

export default class IconButton extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.onFocus = this.onFocus.bind(this);
    this.clearFocus = this.clearFocus.bind(this);

    this.state = {
      focus: false,
    };
  }

  onFocus() {
    this.setState({
      focus: true,
    });
  }

  getFillColor = () => {
    const {
      focusedColor,
      defaultColor,
      disabledColor,
      disabled,
    } = this.props;
    const { focus } = this.state;

    if (disabled) {
      return disabledColor;
    }
    return focus ? focusedColor : defaultColor;
  }

  getStrokeColor = () => {
    const {
      strokeColor,
    } = this.props;
    return strokeColor;
  }

  clearFocus() {
    this.setState({
      focus: false,
    });
  }

  render() {
    const {
      onClick,
      icon: Icon,
      height,
      width,
      padding,
      title,
      disabled,
      className,
      styleOverride,
    } = this.props;

    return (
      <Button
        onClick={onClick}
        type="flat"
        styleOverride={{
          height: 'auto',
          minHeight: '0px',
          border: 'none',
          padding,
          backgroundColor: 'initial',
          color: '#004C97',
          ':hover': {
            color: '#003467',
            boxShadow: 'none',
          },
          ':focus': {
            outline: 'none',
          },
          ...styleOverride,
        }}
        disabled={disabled}
        onBlur={this.clearFocus}
        onFocus={this.onFocus}
        onMouseEnter={this.onFocus}
        onMouseOver={this.onFocus}
        onMouseLeave={this.clearFocus}
        onMouseOut={this.clearFocus}
        name="action-button"
        title={title}
        tabIndex={0}
      >
        <Icon
          fill={this.getFillColor()}
          stroke={this.getStrokeColor()}
          width={width}
          height={height}
        />
      </Button>
    );
  }
}

IconButton.propTypes = {
  icon: PropTypes.func.isRequired,
  onClick: PropTypes.func,
  width: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  height: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  padding: PropTypes.string,
  focusedColor: PropTypes.string,
  defaultColor: PropTypes.string,
  disabledColor: PropTypes.string,
  title: PropTypes.string,
  disabled: PropTypes.bool,
  styleOverride: PropTypes.object,
};

IconButton.defaultProps = {
  onClick: () => {},
  width: 10,
  height: 10,
  padding: '1px',
  focusedColor: colors.prussianBlue,
  defaultColor: colors.cobalt,
  strokeColor: 'none',
  disabledColor: colors.disabledText,
  title: '',
  disabled: false,
  styleOverride: {},
};
