import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import shortid from 'shortid';
import { reorder } from 'components/modelConfigs/modelConfigUtilities';

import { getPostProcessingRules, replacePostProcessingRules } from 'model/ModelConfigManager';
import PostProcessingJSONEditor from 'components/modelConfigs/PostProcessingJSONEditor';
import {
  Plus,
  ContextualActionsBar,
  ContextualActionItem,
  Tabs,
  KeyboardUtils,
  TextField,
  Button,
  CreatableSelect,
  Select,
} from '@tfs/ui-components';
import DraggableItem from 'components/modelConfigs/transformations/DraggableItem';
import DragDropWrapper from 'components/modelConfigs/transformations/DragDropWrapper';
import { actionBarStyles } from 'styles';
import Constants from 'constants/Constants';

const { POST_PROCESSING_RULES } = Constants;

const checkRegex = value => {
  if (value === Constants.ANY_REGEX) {
    return Constants.ANY_VALUE;
  }
  return value;
};

const createOption = (label) => {
  label = checkRegex(label);
  return {
    label,
    value: label,
    isDisabled: false,
  };
};

const stringConversion = (input, fromRegex, toRegex) => {
  // Remove whitespace and split by regex
  const results = input.split(fromRegex).map(item => item.trim());
  const expected = results.join(toRegex);
  return expected;
};

const createNormalizeData = value => value.label;

const createMulipleOption = (createFunction, values = []) => (values ? Array.isArray(values) ? values.map(value => createFunction(value)) : createFunction(values) : '');

const filterOptions = (customOptions, intentMatch) => customOptions.filter(option => option.label === intentMatch);

const removeDuplicates = (options = []) => options.reduce((acc, curr) => {
  const option = createOption(curr.rutag);
  return acc.some(opt => opt.label === curr.rutag) ? acc : [...acc, option];
}, []);

const isMultiEnabled = (postProcessing, customOptions, index = 0) => {
  if (postProcessing && postProcessing.length) {
    const intentMatch = postProcessing[index]['intent-match'];
    if (!Array.isArray(intentMatch) || intentMatch[0] === Constants.ANY_VALUE
      || (intentMatch.length == 1 && !filterOptions(customOptions, intentMatch[0]).length)) {
      return false;
    }
  }
  return true;
};

class PostProcessingTransformations extends Component {
  constructor(props) {
    super(props);
    this.onDragEnd = this.onDragEnd.bind(this);
    this.onAddRule = this.onAddRule.bind(this);
    this.onKeyPress = this.onKeyPress.bind(this);
    this.getItems = this.getItems.bind(this);
    this.onDelete = this.onDelete.bind(this);
    this.onSelect = this.onSelect.bind(this);
    this.updateConfigData = this.updateConfigData.bind(this);
    this.constructName = this.constructName.bind(this);
    this.onUpdateProcessingRules = this.onUpdateProcessingRules.bind(this);

    const postProcessing = getPostProcessingRules(props.config);

    const options = removeDuplicates(props.options);

    const customOptions = [
      { label: Constants.ANY_VALUE, value: Constants.ANY_VALUE, isDisabled: false },
      ...options,
    ];

    const isMulti = isMultiEnabled(postProcessing, customOptions);

    this.newRule = {
      [POST_PROCESSING_RULES.INPUT_MATCH]: '',
      [POST_PROCESSING_RULES.INTENT_MATCH]: '',
      [POST_PROCESSING_RULES.INTENT_REPLACEMENT]: '',
      minConfidenceScore: -1,
      maxConfidenceScore: -1,
    };
    this.state = {
      postProcessing,
      currentIdx: 0,
      selectedTabIndex: 0,
      selectedRule: postProcessing[0] || { ...this.newRule },
      buttonLabel: postProcessing[0] ? Constants.UPDATE : Constants.ADD,
      isMulti,
      customOptions,
      options,
    };
  }

  shouldComponentUpdate(nextProps, nextState) {
    return nextProps.isCurrentTab;
  }

  trimValue = (data) => {
    if (!_.isNil(data)) {
      return data.trim();
    }
    return '';
  }

  updateConfigData() {
    const { postProcessing } = this.state;
    const { config } = this.props;

    const updatedConfig = replacePostProcessingRules(config, postProcessing);
    this.props.saveConfigChanges(updatedConfig);
  }

  onUpdateProcessingRules(updatedTextRule, currentIdx) {
    try {
      const { postProcessing } = this.state;

      const updatedRule = JSON.parse(updatedTextRule);

      updatedRule[POST_PROCESSING_RULES.INTENT_REPLACEMENT] = this.trimValue(updatedRule[POST_PROCESSING_RULES.INTENT_REPLACEMENT]);
      updatedRule[POST_PROCESSING_RULES.INPUT_MATCH] = this.trimValue(updatedRule[POST_PROCESSING_RULES.INPUT_MATCH]);
      updatedRule[POST_PROCESSING_RULES.INTENT_MATCH] = this.trimValue(updatedRule[POST_PROCESSING_RULES.INTENT_MATCH]);

      const newPostProcessing = Object.assign([], postProcessing);
      newPostProcessing[currentIdx] = updatedRule;

      this.setState({
        hasError: false,
        postProcessing: newPostProcessing,
        selectedRule: { ...updatedRule },
      }, () => {
        this.updateConfigData();
      });
    } catch (error) {
      this.setState({
        hasError: true,
      });
    }
  }

  validateRule = (rule) => {
    const {
      [POST_PROCESSING_RULES.INPUT_MATCH]: inputMatch = '',
      [POST_PROCESSING_RULES.INTENT_MATCH]: intentMatch = '',
      [POST_PROCESSING_RULES.INTENT_REPLACEMENT]: intentReplacement = '',
    } = rule;

    return inputMatch.length && intentMatch.length && intentReplacement.length;
  }

  constructName(item) {
    let name = '';
    if (!_.isNil(item)) {
      const {
        [POST_PROCESSING_RULES.INTENT_MATCH]: intentMatch,
        [POST_PROCESSING_RULES.INTENT_REPLACEMENT]: intentReplacement,
      } = item;
      name = `${checkRegex(intentMatch)} - ${intentReplacement}`;
    }
    return name;
  }

  onBlur = (e, index) => {
    const { selectedRule } = this.state;

    const attribute = e.target.attributes['data-name'].value;
    const value = Constants.INPUT_PREFIX + stringConversion((e.target.value).trim(), ',', '|') + Constants.INPUT_SUFFIX;

    selectedRule[attribute] = value;

    this.setState({
      selectedRule,
    });
  }

  onKeyPress(e, index) {
    if (KeyboardUtils.shouldHandleActivate(e)) {
      e.preventDefault();
      e.stopPropagation();
      this.onBlur(e, index);
    }
  }

  onDelete(e, index) {
    e.preventDefault();
    e.stopPropagation();
    const { postProcessing } = this.state;
    const newPostProcessing = Object.assign([], postProcessing);
    newPostProcessing.splice(index, 1);

    let newCurrentIdx = this.state.currentIdx;

    if (newCurrentIdx === index) {
      newCurrentIdx = 0;
    }

    this.setState({
      postProcessing: newPostProcessing,
      currentIdx: newCurrentIdx,
    }, () => {
      this.updateConfigData();
    });
  }

  onAddRule() {
    const { postProcessing } = this.state;
    this.setState({
      currentIdx: postProcessing.length,
      selectedRule: { ...this.newRule },
      buttonLabel: Constants.ADD,
    });
  }

  onAddOrSaveItem = () => {
    const { selectedRule, currentIdx, postProcessing } = this.state;
    const newPostProcessing = Object.assign([], postProcessing);
    newPostProcessing[currentIdx] = { ...selectedRule };

    this.setState({
      postProcessing: newPostProcessing,
      buttonLabel: Constants.UPDATE,
    }, () => {
      this.updateConfigData();
    });
  }

  onDragEnd(result) {
    // dropped outside the list
    if (!result.destination) {
      return;
    }

    const { postProcessing } = this.state;

    const newPostProcessing = reorder(
      postProcessing,
      result.source.index,
      result.destination.index,
    );

    this.setState({
      postProcessing: newPostProcessing,
      currentIdx: result.destination.index,
    }, () => {
      this.updateConfigData();
    });
  }


  onSelect(e, index) {
    e.preventDefault();
    e.stopPropagation();
    const { postProcessing, customOptions } = this.state;
    const isMulti = isMultiEnabled(postProcessing, customOptions, index);
    this.setState({
      currentIdx: index,
      selectedRule: postProcessing[index],
      isMulti,
    });
  }

  onTabSelected = (selectedTab, selectedTabIndex) => {
    this.setState({
      selectedTabIndex,
    });
  }

  getItems(data) {
    const { postProcessing, currentIdx } = this.state;
    const { modelViewReadOnly } = this.props;
    const postProcessingItems = [];
    if (!data) {
      return postProcessingItems;
    }
    const selectedName = this.constructName(postProcessing[currentIdx]);
    data.forEach((item, index) => {
      // ToDo:  Should create a unique id for each element
      // that does not change every time.
      const name = this.constructName(item);
      // Sometimes names can be the same, so append index to make it unique
      let id = name !== ' - ' ? `name-${index}` : shortid.generate();
      let selectedClass = 'transformation-list-item';
      if (selectedName === name && index === currentIdx) {
        selectedClass = `${selectedClass} transformation-selected-item`;
      }
      const noOfItems = postProcessingItems.length;
      postProcessingItems.push({
        id,
        content: (
          <DraggableItem
            index={index}
            noOfItems={noOfItems}
            onDelete={this.onDelete}
            onSelect={this.onSelect}
            id={id}
            modelViewReadOnly={modelViewReadOnly}
            className={selectedClass}
            itemContentClass="transformation-item-content"
          >
            <div
              className="transformation-name"
            >
              {stringConversion(item['input-match'], '|', ',').replace(Constants.INPUT_PREFIX, '').replace(Constants.INPUT_SUFFIX, '')}

            </div>
            <div
              className="transformation-type"
            >
              {name}
            </div>
          </DraggableItem>
        ),
      });
    });
    return postProcessingItems;
  }

  renderTextField = (currentIdx, name, value, disabled) => {
    value = stringConversion(value, '|', ',').replace(Constants.INPUT_PREFIX, '').replace(Constants.INPUT_SUFFIX, '');

    return (
      <TextField
        type="text"
        key={`${name}-${currentIdx}`}
        onKeyPress={(e) => this.onKeyPress(e, currentIdx)}
        onBlur={(e) => this.onBlur(e, currentIdx)}
        data-qa={name}
        data-name={name}
        data-idx={currentIdx}
        disabled={disabled}
        defaultValue={value}
        styleOverride={{
          width: '100%',
        }}
      />
    );
  }

  onChange = (newValue, actionMeta, name) => {
    const { option } = actionMeta;
    const { label = '' } = newValue || {};
    const { selectedRule } = this.state;
    let isMulti = true;
    if (label === Constants.ANY_VALUE || (option && option.label === Constants.ANY_VALUE)) {
      isMulti = false;
      newValue = option ? [option] : [newValue];
    }
    selectedRule[name] = isMulti ? createMulipleOption(createNormalizeData, newValue) : Constants.ANY_REGEX;
    this.setState({
      isMulti,
      selectedRule,
    });
  }

  handleCreate = (inputValue, name) => {
    const { selectedRule } = this.state;
    const newOption = createOption(inputValue);
    selectedRule[name] = inputValue !== Constants.ANY_VALUE ? createMulipleOption(createNormalizeData, newOption) : Constants.ANY_REGEX;
    this.setState({
      isMulti: false,
      selectedRule,
    });
  };

  renderComboBoxCreatable = (currentIdx, name, value, disabled) => {
    const { isMulti, customOptions } = this.state;
    const valuesDefault = createMulipleOption(createOption, value);
    return (
      <CreatableSelect
        isSearchable
        isClearable
        options={customOptions}
        isMulti={isMulti}
        onChange={(newValue, actionMeta) => this.onChange(newValue, actionMeta, name)}
        onCreateOption={(inputValue) => this.handleCreate(inputValue, name)}
        isDisabled={disabled}
        value={valuesDefault}
        styleOverride={{
          container: {
            width: '100%',
          },
        }}
      />
    );
  }

  onChangeComboBox = (newValue, name) => {
    const { selectedRule } = this.state;
    const { label = '' } = newValue || {};
    selectedRule[name] = label;
    this.setState({
      selectedRule,
    });
  }

  renderComboBox = (currentIdx, name, value, disabled) => {
    const { options } = this.state;
    const valuesDefault = createMulipleOption(createOption, value);
    return (
      <Select
        isSearchable
        isClearable
        options={options}
        onChange={(newValue, actionMeta) => this.onChangeComboBox(newValue, name)}
        isDisabled={disabled}
        value={valuesDefault}
        styleOverride={{
          container: {
            width: '100%',
          },
        }}
      />
    );
  }

  getPostProcessingUI = () => {
    const { modelViewReadOnly } = this.props;
    const { currentIdx, selectedRule } = this.state;

    const {
      [POST_PROCESSING_RULES.INPUT_MATCH]: inputMatch = '',
      [POST_PROCESSING_RULES.INTENT_MATCH]: intentMatch = '',
      [POST_PROCESSING_RULES.INTENT_REPLACEMENT]: intentReplacement = '',
    } = selectedRule;

    return (
      <div id="Editor" className="editor-panel">
        <div className="post-processing-container" style={{ width: '430px', position: 'relative' }}>
          <ul className="form-fields rules-form">
            <li>
              <label>Input Match*</label>
              {this.renderTextField(currentIdx, POST_PROCESSING_RULES.INPUT_MATCH, inputMatch, modelViewReadOnly)}
              <div style={{ color: '#727272' }} className="help-text overflow-wrap">
                 Enter comma separated input match values
              </div>
            </li>
            <li>
              <label>Intent Match*</label>
              {this.renderComboBoxCreatable(currentIdx, POST_PROCESSING_RULES.INTENT_MATCH, intentMatch, modelViewReadOnly)}
            </li>
            <li>
              <label>Intent Replacement*</label>
              {this.renderComboBox(currentIdx, POST_PROCESSING_RULES.INTENT_REPLACEMENT, intentReplacement, modelViewReadOnly)}
            </li>
          </ul>
          {!modelViewReadOnly
              && (
                <Button
                  name="save-item"
                  type="primary"
                  onClick={this.onAddOrSaveItem}
                  styleOverride={{
                    position: 'absolute',
                    right: 0,
                  }}
                  disabled={!this.validateRule(selectedRule)}
                >
                  {this.state.buttonLabel}
                </Button>
              )
          }
        </div>
      </div>
    );
  }

  getPostProcessingJSON = () => {
    const { modelViewReadOnly } = this.props;
    const { currentIdx, selectedRule } = this.state;

    return (
      <div id="Editor" className="editor-panel">
        <PostProcessingJSONEditor
          currentItem={selectedRule}
          onUpdateProcessingRules={this.onUpdateProcessingRules}
          ruleIdx={currentIdx}
          modelViewReadOnly={modelViewReadOnly}
        />
      </div>
    );
  }

  renderTabs = () => {
    const { selectedTabIndex } = this.state;

    const tabPanels = [this.getPostProcessingUI(), this.getPostProcessingJSON()];
    return (
      <Tabs
        align="left"
        tabs={Constants.UI_JSON_TABS}
        onTabSelected={this.onTabSelected}
        selectedIndex={selectedTabIndex}
        forceRenderTabPanel={false}
        tabPanels={tabPanels}
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

  renderAddNewItem = () => (
    <ContextualActionsBar styleOverride={actionBarStyles.bar}>
      <ContextualActionItem
        onClickAction={this.onAddRule}
        icon={Plus}
        styleOverride={actionBarStyles.item}
      >
        ADD
      </ContextualActionItem>
    </ContextualActionsBar>
  );

  render() {
    if (!this.props.isCurrentTab) {
      return null;
    }

    const { modelViewReadOnly } = this.props;
    const { postProcessing } = this.state;
    const items = this.getItems(postProcessing);

    return (
      <div id="ModelTransformationContainer">
        <div id="modelTransformationItems">
          <div id="PostProcessingTransformations">
            <div style={{
              lineHeight: '60px',
              paddingLeft: 30,
              borderBottom: '1px solid #ddd',
              paddingBottom: 2,
            }}
            >
                Rules
            </div>
            {!modelViewReadOnly && this.renderAddNewItem()}
            <DragDropWrapper
              onDragEnd={this.onDragEnd}
              items={items}
              droppableId="postprocessing-droppable"
              isDisabled={modelViewReadOnly}
            />
          </div>
        </div>
        <div id="modelTransformationEditor">
          {this.renderTabs()}
        </div>
        <div id="nextButtonArea" />
      </div>
    );
  }
}

PostProcessingTransformations.propTypes = {
  model: PropTypes.object,
  options: PropTypes.array,
  config: PropTypes.object,
  saveConfigChanges: PropTypes.func,
  isCurrentTab: PropTypes.bool,
  modelViewReadOnly: PropTypes.bool,
};

export default PostProcessingTransformations;
