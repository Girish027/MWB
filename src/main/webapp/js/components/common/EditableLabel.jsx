import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import getIcon, { IconNames } from 'utils/iconHelpers';
import ReactSVG from 'react-svg';


class EditableLabel extends Component {
  constructor(props) {
    super(props);

    this.state = {
      value: props.defaultValue,
      editMode: false,
      labelStyle: {},
    };

    this.myRef = React.createRef();

    this.handleChange = this.handleChange.bind(this);
    this.handleClickOutside = this.handleClickOutside.bind(this);
    this.enableEditMode = this.enableEditMode.bind(this);

    this.contenteditableStyle = {
      caretColor: '#ef8822',
      display: 'inline-block',
      borderBottom: '1px dotted black',
      paddingBottom: '2px',
    };
  }

  getLabelDOMNode() {
    return this.refs.batchtestname.nodeType === 1
      ? this.refs.batchtestname
      // eslint-disable-next-line react/no-find-dom-node
      : ReactDOM.findDOMNode(this.refs.batchtestname);
  }

  handleChange(event) {
    this.setState({
      // eslint-disable-next-line react/no-find-dom-node
      value: ReactDOM.findDOMNode(this.refs.batchtestname).innerText,
    }, () => {
      this.props.validateAndUpdateValue(this.state.value);
    });
  }

  enableEditMode() {
    this.setState({
      editMode: 'true',
      labelStyle: this.contenteditableStyle,
    });
    document.addEventListener('mousedown', this.handleClickOutside);
  }

  handleClickOutside() {
    if (this.myRef && !this.myRef.current.contains(event.target)) {
      this.setState({
        editMode: 'false',
        labelStyle: {},
      });
      document.removeEventListener('mousedown', this.handleClickOutside);
    }
  }

  render() {
    return (
      <div
        className={this.props.customClassName}
        ref={this.myRef}
        style={{
          display: 'inline-block',
        }}
      >
        <div className="float-left">
          <label
            ref="batchtestname"
            contentEditable={this.state.editMode}
            style={this.state.labelStyle}
            onInput={this.handleChange}
          >
            {this.state.value}
          </label>
        </div>
        <div
          className="float-left"
          onClick={this.enableEditMode}
          style={{
            paddingLeft: '8px',
          }}
        >
          <ReactSVG src={getIcon(IconNames.CREATE)} />
        </div>
        <div className="float-clear" />
      </div>
    );
  }
}

EditableLabel.defaultProps = {
  customClassName: '',
  validateAndUpdateValue: () => {},
  defaultValue: 'Default Value',
};

EditableLabel.propTypes = {
  defaultValue: PropTypes.string,
  customClassName: PropTypes.string,
  validateAndUpdateValue: PropTypes.func,
};

export default EditableLabel;
