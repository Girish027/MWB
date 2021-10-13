import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Row } from 'react-flexbox-grid';

import Multiselect from '@khanacademy/react-multi-select';
import Dialog from 'components/common/Dialog';

import { getPredefinedDisplayList, getTransforms } from 'components/modelConfigs/transformations/predefinedTransforms/predefinedTransformations';


class AddPredefinedTransformationDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.newTransformationData = [];

    this.receivedDatasets = {};

    this.validate = this.validate.bind(this);
    this.onOK = this.onOK.bind(this);
    this.getTransformationList = this.getTransformationList.bind(this);
    this.onSelectedChanged = this.onSelectedChanged.bind(this);

    this.state = {
      selected: [],
    };
  }

  validate() {
    return true;
  }

  onOK(evt) {
    const okToLeave = this.validate();
    if (okToLeave) {
      const transformation = getTransforms(this.state.selected);
      this.props.onOK(transformation);
    }
  }

  onSelectedChanged(selected) {
    this.setState({
      selected,
    });
  }

  getTransformationList() {
    const displayData = getPredefinedDisplayList();

    const selectOptions = [];

    displayData.forEach((item, index) => {
      selectOptions.push({ label: item.name, value: item.id });
    });

    return selectOptions;
  }

  render() {
    return (
      <Dialog
        title="Add Predefined Transformation"
        visible={this.props.showDialog}
        okString="Add"
        onCancel={this.props.onCancel}
        onOk={this.onOK}
      >
        <div id="add-predefined">
          <Row>
            <label>Select one or more predefined transformations</label>
          </Row>
          <Row>
            <Multiselect
              options={this.getTransformationList()}
              onSelectedChanged={this.onSelectedChanged}
              selected={this.state.selected}
            />
          </Row>
        </div>
      </Dialog>
    );
  }
}

AddPredefinedTransformationDialog.propTypes = {
  showDialog: PropTypes.bool,
  onCancel: PropTypes.func,
  onOK: PropTypes.func,
};

export default AddPredefinedTransformationDialog;
