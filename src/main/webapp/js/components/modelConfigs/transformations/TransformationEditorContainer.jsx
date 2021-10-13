import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Editor from 'components/modelConfigs/transformations/Editor';
import Placeholder from 'components/controls/Placeholder';

class TransformationEditorContainer extends Component {
  constructor(props, context) {
    super(props, context);
    this.props = props;
  }

  get editor() {
    const {
      currentItem, userFeatureConfiguration, onUpdateTransformation,
      dispatch, modelViewReadOnly, isTransformationValid,
    } = this.props;

    if (!currentItem) {
      return (
        <Placeholder
          style={{ paddingTop: '30vh', margin: 'auto', color: '#727272' }}
          message="Please choose a config file or create new one."
        />
      );
    }

    return (
      <Editor
        transformation={currentItem}
        onUpdateTransformation={onUpdateTransformation}
        dispatch={dispatch}
        userFeatureConfiguration={userFeatureConfiguration}
        modelViewReadOnly={modelViewReadOnly}
        isTransformationValid={isTransformationValid}
      />
    );
  }

  render() {
    return (
      <div id="ConfigEditorContent">
        {this.editor}
      </div>
    );
  }
}

TransformationEditorContainer.propTypes = {
  currentItem: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.object,
  ]),
  onUpdateTransformation: PropTypes.func,
  dispatch: PropTypes.func,
  userFeatureConfiguration: PropTypes.object,
  modelViewReadOnly: PropTypes.bool,
  isTransformationValid: PropTypes.bool,
};

export default TransformationEditorContainer;
