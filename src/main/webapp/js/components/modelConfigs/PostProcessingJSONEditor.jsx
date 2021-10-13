import React, { Component } from 'react';
import PropTypes from 'prop-types';

import AceEditor from 'react-ace';

import 'brace/mode/json';
import 'brace/theme/textmate';
import 'brace/theme/kuroir';

import { getTransformationContent, saveTransformationContent } from 'model/ModelConfigManager';

class PostProcessingJSONEditor extends Component {
  constructor(props, context) {
    super(props, context);

    this.props = props;
    this.onChange = this.onChange.bind(this);

    const { currentItem } = this.props;

    this.unSavedContent = false;
    this.textContent = currentItem ? JSON.stringify(currentItem, null, 2) : '';

    this.state = {
      content: this.textContent,
      currentItem,
    };
  }

  // ToDo - Migrate to getDerivedStateFromProps
  componentWillReceiveProps(nextProps) {
    const { ruleIdx } = this.props;
    const { currentItem } = nextProps;
    this.textContent = currentItem ? JSON.stringify(currentItem, null, 2) : '';
    this.setState({
      currentItem,
      content: this.textContent,
    });
  }

  onChange(newValue) {
    const { ruleIdx } = this.props;
    this.textContent = newValue ? newValue.trim() : newValue;
    this.props.onUpdateProcessingRules(this.textContent, ruleIdx);
  }

  render() {
    const { content } = this.state;
    const { modelViewReadOnly } = this.props;

    return (
      <AceEditor
        mode="json"
        theme={modelViewReadOnly ? 'kuroir' : 'textmate'}
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
    );
  }
}

PostProcessingJSONEditor.propTypes = {
  currentItem: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.string]),
  onUpdateProcessingRules: PropTypes.func,
  ruleIdx: PropTypes.number,
  modelViewReadOnly: PropTypes.bool,
};

export default PostProcessingJSONEditor;
