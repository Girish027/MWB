import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Row } from 'react-flexbox-grid';
import { Label, Input } from 'reactstrap';
import { TextField } from '@tfs/ui-components';
import Dialog from 'components/common/Dialog';
import validationUtil from 'utils/ValidationUtil';
import Constants from 'constants/Constants';
import { getFilteredTransformationTypesDisplayArray, transformationTypeHasName } from 'components/modelConfigs/transformations/transformationTypes';
import { getDefaultTransformation } from 'model/ModelConfigManager';
import ErrorLayout from 'components/modelConfigs/transformations/ErrorLayout';

const urlTransformationStructure = require('./UrlTransformations/UrlTransformations.json');

class AddTransformationDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.newTransformationData = {
      name: '',
      comment: '',
      typeId: null,
    };

    this.receivedDatasets = {};

    this.validate = this.validate.bind(this);
    this.onOK = this.onOK.bind(this);
    this.onNameBlur = this.onNameBlur.bind(this);
    this.onNameChange = this.onNameChange.bind(this);
    this.onNameKeyPress = this.onNameKeyPress.bind(this);
    this.onCommentBlur = this.onCommentBlur.bind(this);
    this.onCommentKeyPress = this.onCommentKeyPress.bind(this);
    this.onSelect = this.onSelect.bind(this);
    this.getPopupContainer = this.getPopupContainer.bind(this);
    this.getTransformationList = this.getTransformationList.bind(this);
    this.getUrlList = this.getUrlList.bind(this);
    this.onSelectTransform = this.onSelectTransform.bind(this);

    const displayData = props.transformations ? getFilteredTransformationTypesDisplayArray(props.transformations) : [];
    let okDisabled = true;
    if (displayData.length) {
      okDisabled = false;
      this.newTransformationData.typeId = displayData[0].id;
    }

    this.state = {
      showNameComment: false,
      okDisabled,
      displayData,
      urlTransformation: false,
      urlTransformationSelect: null,
      errorMessage: null,
    };
  }

  validate() {
    return true;
  }

  getPopupContainer(node) {
    return node.parentNode;
  }

  onOK(evt) {
    const { transformations, dispatch } = this.props;
    const okToLeave = this.validate();
    const urlTrans = urlTransformationStructure.urlTransforms;
    let transformItem;
    let disallow = false;
    let errorMessage;
    if (okToLeave) {
      if (this.state.urlTransformation) {
        transformItem = urlTrans.find(item => item.name === this.state.urlTransformationSelect) || urlTrans[0];
        this.newTransformationData.name = transformItem.name;
        transformations.forEach((item) => {
          if ((typeof item) === 'object') {
            if (item[transformItem.name]) {
              disallow = true;
              errorMessage = Constants.ALERT_TRANSFORMATION(transformItem.name);
            }
          }
        });
      }
      if (!disallow) {
        const transformation = getDefaultTransformation(this.newTransformationData);
        this.props.onOK(transformation);
      } else {
        this.setState({
          errorMessage,
        });
      }
    }
  }

  onNameKeyPress(event) {
    if (event.key === 'Enter'
      || event.key === 'Tab') {
      event.preventDefault();
      this.onNameBlur(event);
    }
  }

  validateName() {

  }

  onNameBlur(evt) {
    this.onNameChange(evt);
  }

  onNameChange(evt) {
    let okDisabled = true;
    let name = evt.target.value;
    if (validationUtil.checkField(name, Constants.TRANSFORMATION_ITEM_NAME_REGEX)
       && name.length < Constants.TRANSFORMATION_ITEM_NAME_LIMIT) {
      okDisabled = false;
    }
    this.newTransformationData.name = name;
    this.setState({
      okDisabled,
    });
  }

  onCommentKeyPress(event) {
    if (event.key === 'Enter'
      || event.key === 'Tab') {
      event.preventDefault();
      this.onCommentBlur(event);
    }
  }

  onCommentBlur(evt) {
    this.newTransformationData.comment = evt.target.value;
  }

  getUrlList() {
    const urlTrans = urlTransformationStructure.urlTransforms;
    const selectOptions = [];

    urlTrans.forEach((item) => {
      selectOptions.push(<option
        value={item.name}
        key={item.name}
      >
        {item.name}
      </option>);
    });

    return selectOptions;
  }

  onSelect(selectedValue) {
    const { TRANSFORMATION_TYPES } = Constants;
    this.newTransformationData.typeId = selectedValue.target.value;
    let showNameComment = transformationTypeHasName(selectedValue.target.value);
    let urlTransformation = false;
    let urlTransformationSelect;
    if (selectedValue.target.value === TRANSFORMATION_TYPES.TRANSFORMATION_URL) {
      showNameComment = false;
      urlTransformation = true;
    }
    const okDisabled = showNameComment;

    this.setState({
      showNameComment,
      okDisabled,
      urlTransformation,
      urlTransformationSelect,
    });
  }

  onSelectTransform(selectedValue) {
    let urlTransformationSelect;
    if (selectedValue.target.value !== null) {
      urlTransformationSelect = selectedValue.target.value;
    }
    this.setState({
      urlTransformationSelect,
    });
  }

  getTransformationList() {
    const { displayData } = this.state;
    const selectOptions = [];

    displayData.forEach((item) => {
      selectOptions.push(<option
        value={item.id}
        key={item.id}
      >
        {item.name}
      </option>);
    });

    return selectOptions;
  }

  getNameCommentIfNeeded() {
    if (this.state.showNameComment) {
      return (
        <div className="add-transformation-name-comment-container">
          <Row>
            <label>Name*</label>
          </Row>
          <Row>
            <TextField onBlur={this.onNameBlur} onChange={this.onNameChange} onKeyPress={this.onNameKeyPress} tabIndex="2" />
          </Row>
          <Row>
            <label>Comment</label>
          </Row>
          <Row>
            <TextField onBlur={this.onCommentBlur} onKeyPress={this.onCommentKeyPress} tabIndex="3" />
          </Row>
        </div>
      );
    }
    if (this.state.urlTransformation) {
      return (
        <div id="AddTransformationDialog">
          <Row>
            <Label>
              Select url transformation
            </Label>
            <Input
              type="select"
              onChange={this.onSelectTransform}
            >
              {this.getUrlList()}
            </Input>
          </Row>
        </div>
      );
    }

    return '';
  }

  render() {
    const { errorMessage } = this.state;
    return (
      <Dialog
        title="Add New Transformation"
        visible={this.props.showDialog}
        okString="Add"
        onCancel={this.props.onCancel}
        onOk={this.onOK}
        okDisabled={this.state.okDisabled}
      >
        <div id="AddTransformationDialog">
          {errorMessage && (
            <ErrorLayout errorMsg={errorMessage} styleOverride={{ marginBottom: '10px' }} />
          )}
          <Row>
            <Label>
              Select a transformation type
            </Label>
            <Input
              type="select"
              onChange={this.onSelect}
            >
              {this.getTransformationList()}
            </Input>
          </Row>
          {this.getNameCommentIfNeeded()}
        </div>
      </Dialog>
    );
  }
}

AddTransformationDialog.propTypes = {
  showDialog: PropTypes.bool,
  onCancel: PropTypes.func,
  onOK: PropTypes.func,
  transformations: PropTypes.array,
  dispatch: PropTypes.func,
};

export default AddTransformationDialog;
