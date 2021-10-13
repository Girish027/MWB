import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Box from 'grommet/components/Box';
import SearchIcon from 'grommet/components/icons/base/Search';
import FormCloseIcon from 'grommet/components/icons/base/FormClose';

export default class TaggerSearchInput extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.state = {
      focus: false,
      value: '',
    };
    this.input = null;

    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onChange = this.onChange.bind(this);
    this.onClickClear = this.onClickClear.bind(this);
    this.onClickSearch = this.onClickSearch.bind(this);
    this.onKeyPress = this.onKeyPress.bind(this);
  }

  onFocus() {
    this.setState({
      focus: true,
    });
  }

  onBlur() {
    this.setState({
      focus: false,
    });
  }

  onChange() {
    this.setState({
      value: this.input.value,
    });
    const { onChange } = this.props;
    if (typeof onChange === 'function') {
      onChange(this.input.value);
    }
  }

  onClickClear() {
    this.setState({
      value: '',
    });
    setTimeout(() => {
      this.input.focus();
    }, 100);
    const { onChange } = this.props;
    if (typeof onChange === 'function') {
      onChange('');
    }
  }

  onClickSearch() {
    const { onSearch } = this.props;
    if (typeof onSearch === 'function') {
      onSearch(this.input.value);
    }
  }

  onKeyPress(event) {
    const { onSearch } = this.props;
    if (typeof onSearch === 'function' && event.key == 'Enter') {
      onSearch(this.input.value);
    }
  }

  render() {
    const {
      id,
      className,
      placeholder,
      disabled,
    } = this.props;

    const {
      focus,
      value,
    } = this.state;

    return (
      <Box
        id={id}
        className={
          `TaggerControl TaggerSearchInput${
            focus ? ' focus' : ''
          }${disabled ? ' disabled' : ''
          }${className ? ` ${className}` : ''}`
        }
        direction="row"
      >
        <Box
          className="InputContainer"
          flex
        >
          <input
            ref={(input) => { this.input = input; }}
            type="text"
            value={value}
            disabled={disabled}
            placeholder={placeholder}
            onChange={this.onChange}
            onFocus={this.onFocus}
            onBlur={this.onBlur}
            onKeyPress={this.onKeyPress}
          />
        </Box>
        <Box
          className="FormCloseIconContainer"
          onClick={this.onClickClear}
        >
          <FormCloseIcon />
        </Box>
        <Box
          className="SearchIconContainer"
          onClick={this.onClickSearch}
        >
          <SearchIcon />
        </Box>
      </Box>
    );
  }
}

TaggerSearchInput.propTypes = {
  id: PropTypes.string,
  className: PropTypes.string,
  placeholder: PropTypes.string,
  disabled: PropTypes.bool,
  onChange: PropTypes.func,
  onSearch: PropTypes.func,
};
