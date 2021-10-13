import React, { Component } from 'react';
import AceEditor from 'react-ace';
import PropTypes from 'prop-types';
import 'brace/mode/json';
import 'brace/theme/textmate';
import 'brace/theme/kuroir';

class ModelReviewAce extends Component {
  constructor(props) {
    super(props);
    this.props = props;
  }

  render() {
    const {
      name, onLoad, onChange, value, height, width, readOnly, onBlur,
    } = this.props;

    let theme = 'kuroir';
    if (!readOnly) {
      theme = 'textmate';
    }

    return (
      <AceEditor
        mode="json"
        theme={theme}
        name={name}
        onLoad={onLoad}
        onChange={onChange}
        onBlur={onBlur}
        fontSize={14}
        showPrintMargin={false}
        showGutter
        highlightActiveLine
        readOnly={readOnly}
        value={value}
        height={`${height}px`}
        width={`${width}px`}
        wrapEnabled
        setOptions={{
          firstLineNumber: 1,
          enableBasicAutocompletion: false,
          enableLiveAutocompletion: false,
          enableSnippets: false,
          showLineNumbers: true,
          tabSize: 2,
        }}
      />
    );
  }
}

ModelReviewAce.propTypes = {
  name: PropTypes.string,
  onLoad: PropTypes.func,
  onChange: PropTypes.func,
  onBlur: PropTypes.func,
  value: PropTypes.string,
  height: PropTypes.number,
  width: PropTypes.number,
  readOnly: PropTypes.bool,
};

export default ModelReviewAce;
