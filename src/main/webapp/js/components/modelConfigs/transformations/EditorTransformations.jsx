import React, { Component } from 'react';
import PropTypes from 'prop-types';

import AceEditor from 'react-ace';

import 'brace/mode/json';
import 'brace/theme/textmate';
import 'brace/theme/kuroir';

import { getTransformationContent, saveTransformationContent } from 'model/ModelConfigManager';

class EditorTransformations extends Component {
  constructor(props, context) {
    super(props, context);

    this.props = props;
    this.onBlur = this.onBlur.bind(this);
    this.onChange = this.onChange.bind(this);

    const { transformation } = this.props;

    const content = getTransformationContent(transformation);
    this.unSavedContent = false;
    this.textContent = content ? JSON.stringify(content, null, 2) : '';

    this.state = {
      content: this.textContent,
      transformation,
    };
  }

  componentWillReceiveProps(nextProps) {
    if (this.unSavedContent && this.textContent != null) {
      const updatedTransformation = saveTransformationContent(this.state.transformation, this.textContent);
      this.unSavedContent = false;
      this.props.onUpdateTransformation(updatedTransformation);
    }
    const { transformation } = nextProps;
    const content = getTransformationContent(transformation);
    this.textContent = content ? JSON.stringify(content, null, 2) : '';
    this.setState({
      transformation,
      content: this.textContent,
    });
  }

  componentWillUnmount() {
  }

  onBlur(newValue) {
    const updatedTransformation = saveTransformationContent(this.state.transformation, this.textContent);
    this.props.onUpdateTransformation(updatedTransformation);
  }

  onChange(newValue) {
    this.textContent = newValue ? newValue.trim() : newValue;
    this.unSavedContent = true;
  }

  render() {
    const { content } = this.state;
    const { modelViewReadOnly } = this.props;

    return (
      <div style={{
        margin: '0px 2px 0px 2px',
        height: '100%',
        width: '98%',
        borderRight: '1px solid #ddd',
        borderTop: '1px solid #ddd',
      }}
      >
        <AceEditor
          mode="json"
          theme={modelViewReadOnly ? 'kuroir' : 'textmate'}
          onBlur={this.onBlur}
          onChange={this.onChange}
          name="UNIQUE_ID_OF_DIV"
          editorProps={{ $blockScrolling: true }}
          showPrintMargin={false}
          defaultValue={content}
          setOptions={{
            // showLineNumbers: false
          }}
          width="100%"
          height="100%"
          fontSize={14}
          value={content}
          wrapEnabled
          readOnly={modelViewReadOnly}
        />
      </div>
    );
  }
}

EditorTransformations.propTypes = {
  transformation: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.string]),
  modelViewReadOnly: PropTypes.bool,
  onUpdateTransformation: PropTypes.func,
};

export default EditorTransformations;
