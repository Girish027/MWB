import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Tabs } from '@tfs/ui-components';
import EditorTransformations from 'components/modelConfigs/transformations/EditorTransformations';
import EditorTags from 'components/modelConfigs/EditorTags';
import EditorOrderedList from 'components/modelConfigs/transformations/EditorOrderedList';
import EditorSimplePairs from 'components/modelConfigs/transformations/EditorSimplePairs';
import EditorLongPairs from 'components/modelConfigs/transformations/EditorLongPairs';
import getTransformationHelpInfo from 'components/modelConfigs/transformations/HelpInfo';
import Constants from 'constants/Constants';
import dateTransformation from 'components/modelConfigs/transformations/UrlTransformations/DateTransformations';
import addressTransformation from 'components/modelConfigs/transformations/UrlTransformations/AddressTransformations';

const urlTransformationStructure = require('./UrlTransformations/UrlTransformations.json');

class Editor extends Component {
  constructor(props, context) {
    super(props, context);
    this.props = props;

    this.components = {
      DateTransformation: dateTransformation,
      AddressTransformation: addressTransformation,
    };
    this.onUpdateTags = this.onUpdateTags.bind(this);
    this.onUpdateList = this.onUpdateList.bind(this);
    this.onUpdateMapping = this.onUpdateMapping.bind(this);
    this.specificEditor = this.specificEditor.bind(this);
    this.state = {
      selectedTabIndex: 0,
    };
  }

  onUpdateTags(items) {
    const { transformation } = this.props;
    const name = Object.keys(transformation)[0];
    // TODO: see note in 'onUpdateMapping'
    // const updateTransformationItem = Object.assign({}, transformation[name], {
    //   list: items,
    // });

    const updateTransformationItem = Object.assign(transformation[name], {
      list: items,
    });
    const updateTransformation = {
      [name]: updateTransformationItem,
    };
    this.props.onUpdateTransformation(updateTransformation);
  }

  onUpdateList(list, transformation) {
    if (transformation && typeof transformation !== 'string') {
      const name = Object.keys(transformation)[0];
      // TODO: see note in 'onUpdateMapping'
      // const updateTransformationItem = Object.assign({}, transformation[name], {
      //   list,
      // });

      const updateTransformationItem = Object.assign(transformation[name], {
        list,
      });

      const updateTransformation = {
        [name]: updateTransformationItem,
      };
      this.props.onUpdateTransformation(updateTransformation);
    }
  }

  onUpdateMapping(mappings, transformation) {
    if (transformation && typeof transformation !== 'string') {
      const name = Object.keys(transformation)[0];

      /* TODO: Investigate why cloning the data is not working when updating transformation
        TODO: Related to NT-2969
        Error Scenario:
        1. Build a model with Regex-replace transform. Try to edit both Key and Value.
        2. Only the last edited field is retained on building model.
        Not ideal: In the uncommented code, state.config.config.transformation[name] is directly mutated.
      */

      // const updateTransformationItem = Object.assign({}, transformation[name], {
      //   mappings,
      // });

      const updateTransformationItem = Object.assign(transformation[name], {
        mappings,
      });
      const updateTransformation = {
        [name]: updateTransformationItem,
      };
      this.props.onUpdateTransformation(updateTransformation);
    }
  }

  specificEditor(displayJson) {
    const {
      transformation, userFeatureConfiguration,
      modelViewReadOnly, dispatch, isTransformationValid,
    } = this.props;

    if (typeof transformation === 'string') {
      return (
        <EditorTransformations
          transformation={transformation}
          modelViewReadOnly={modelViewReadOnly}
        />
      );
    }

    let key = Object.keys(transformation);
    const transformationItem = transformation[key[0]];
    const { type: transformationType, comments = '' } = transformationItem;
    const { TRANSFORMATION_TYPES } = Constants;

    if (!displayJson) {
      switch (transformationType) {
      case TRANSFORMATION_TYPES.STOP_WORDS: {
        return (
          <EditorTags
            transformationItem={transformationItem.list}
            onUpdateTags={this.onUpdateTags}
            transformation={transformation}
            dispatch={dispatch}
            modelViewReadOnly={modelViewReadOnly}
          />
        );
      }
      case TRANSFORMATION_TYPES.WORDCLASS_SUBST_TEXT: {
        return (
          <EditorSimplePairs
            transformationItems={transformationItem.mappings}
            onUpdateList={this.onUpdateMapping}
            transformation={transformation}
            helpComponent={getTransformationHelpInfo(transformationType, userFeatureConfiguration, comments)}
            dispatch={dispatch}
            onlySubstText
            isTransformationValid={isTransformationValid}
            modelViewReadOnly={modelViewReadOnly}
          />
        );
      }
      case TRANSFORMATION_TYPES.STEMS:
      case TRANSFORMATION_TYPES.STEMS_NOCASE: {
        return (
          <EditorSimplePairs
            transformationItems={transformationItem.mappings}
            onUpdateList={this.onUpdateMapping}
            transformation={transformation}
            helpComponent={getTransformationHelpInfo(transformationType, userFeatureConfiguration, comments)}
            dispatch={dispatch}
            isTransformationValid={isTransformationValid}
            modelViewReadOnly={modelViewReadOnly}
          />
        );
      }
      case TRANSFORMATION_TYPES.REGEX_REPLACE: {
        return (
          <EditorLongPairs
            currentTransformationName={key[0]}
            transformationItems={transformationItem.mappings}
            onUpdateList={this.onUpdateMapping}
            transformation={transformation}
            helpComponent={getTransformationHelpInfo(transformationType, userFeatureConfiguration, comments)}
            dispatch={dispatch}
            isTransformationValid={isTransformationValid}
            modelViewReadOnly={modelViewReadOnly}
          />
        );
      }
      case TRANSFORMATION_TYPES.INPUT_MATCH:
        return (
          <EditorLongPairs
            currentTransformationName={key[0]}
            transformationItems={transformationItem.mappings}
            onUpdateList={this.onUpdateMapping}
            transformation={transformation}
            helpComponent={getTransformationHelpInfo(transformationType, userFeatureConfiguration, comments)}
            trimSpaces
            dispatch={dispatch}
            isTransformationValid={isTransformationValid}
            modelViewReadOnly={modelViewReadOnly}
          />
        );
      case TRANSFORMATION_TYPES.WORDCLASS_SUBST_REGEX: {
        return (
          <EditorLongPairs
            currentTransformationName={key[0]}
            transformationItems={transformationItem.mappings}
            onUpdateList={this.onUpdateMapping}
            transformation={transformation}
            helpComponent={getTransformationHelpInfo(transformationType, userFeatureConfiguration, comments)}
            trimSpaces
            onlyRegex
            isTransformationValid={isTransformationValid}
            dispatch={dispatch}
            modelViewReadOnly={modelViewReadOnly}
          />
        );
      }
      case TRANSFORMATION_TYPES.SPELL_CHECKING:
      case TRANSFORMATION_TYPES.STEMS_URL:
      case TRANSFORMATION_TYPES.STEMS_NOCASE_URL: {
        return (
          <EditorTransformations
            transformation={transformation}
            readOnly={false}
            onUpdateTransformation={this.props.onUpdateTransformation}
            dispatch={dispatch}
            modelViewReadOnly={modelViewReadOnly}
          />
        );
      }
      case TRANSFORMATION_TYPES.REGEX_REMOVAL: {
        return (
          <EditorOrderedList
            currentTransformationName={key[0]}
            transformationItems={transformationItem.list}
            onUpdateList={this.onUpdateList}
            transformation={transformation}
            dispatch={dispatch}
            modelViewReadOnly={modelViewReadOnly}
          />
        );
      }
      }
    }

    return (
      <EditorTransformations
        currentTransformationName={key[0]}
        transformation={transformation}
        onUpdateTransformation={this.props.onUpdateTransformation}
        modelViewReadOnly={modelViewReadOnly}
      />
    );
  }

  onTabSelected = (selectedTab, selectedTabIndex) => {
    this.setState({
      selectedTabIndex,
    });
  }

  render() {
    const { selectedTabIndex } = this.state;
    const { transformation } = this.props;
    const { TRANSFORMATION_TYPES } = Constants;
    const urlTrans = urlTransformationStructure.urlTransforms;
    let transformItem;
    let contentImport;
    if ((typeof transformation) === 'object') {
      const keys = Object.keys(transformation);
      if ((transformation[keys[0]].type === TRANSFORMATION_TYPES.TRANSFORMATION_URL)) {
        transformItem = urlTrans.find(item => item.name === keys[0]);
        contentImport = transformItem.content;
        const TransformationComponent = this.components[contentImport];
        return TransformationComponent;
      }
    }

    return (
      <Tabs
        align="left"
        tabs={Constants.UI_JSON_TABS}
        onTabSelected={this.onTabSelected}
        selectedIndex={selectedTabIndex}
        forceRenderTabPanel={false}
        tabPanels={[this.specificEditor(false), this.specificEditor(true)]}
        styleOverride={{
          tabContainer: {
            marginLeft: '1px',
            borderTop: 'none',
          },
          tabPanel: {
            height: 'calc(100vh - 245px)',
          },
        }}
      />
    );
  }
}

Editor.defaultProps = {
  transformation: '',
};

Editor.propTypes = {
  transformation: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.object,
    PropTypes.array,
  ]),
  onUpdateTransformation: PropTypes.func,
  dispatch: PropTypes.func,
  userFeatureConfiguration: PropTypes.object,
  modelViewReadOnly: PropTypes.bool,
  isTransformationValid: PropTypes.bool,
};

export default Editor;
