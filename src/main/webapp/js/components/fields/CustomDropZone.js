import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { getDataUrl, pathKey } from 'utils/apiUrls';
import { Doc, Download } from '@tfs/ui-components';
import Dropzone from '../Form/Dropzone';
import CsvIcon from '../Icons/CsvIcon';
import { customDropDown } from '../../styles/customComponentsStyles';
import { uploadDataset, actionItems } from '../../styles';

export default class CustomDropZone extends Component {
  constructor(props) {
    super(props);
    this.state = {
      value: '',
    };

    this.saveFile = this.saveFile.bind(this);
    this.onClickDatasetTemplate = this.onClickDatasetTemplate.bind(this);
  }

  getField() {
    return (
      <Dropzone
        accept="text/csv, application/vnd.ms-excel, .csv"
        icon={CsvIcon}
        acceptedIcon={Doc}
        multiple={false}
        saveFile={this.saveFile}
      />
    );
  }

  onClickDatasetTemplate() {
    const locationUrl = getDataUrl(pathKey.datasetTemplate);
    document.location = locationUrl;
  }

  getLabel() {
    return (
      <div style={uploadDataset.container}>
        <span style={uploadDataset.label}>
            Upload Dataset
        </span>
        <div style={uploadDataset.link}>
          <span style={uploadDataset.link.icon}>
            <Download width="10px" height="13px" fill="#004c97" />
          </span>
          <div style={uploadDataset.link.label} onClick={this.onClickDatasetTemplate}> Download Template </div>
        </div>
      </div>
    );
  }

  saveFile(acceptedFiles) {
    const datasetFile = acceptedFiles[0];

    this.props.onChange(datasetFile);
  }

  render() {
    return (
      <div
        style={customDropDown}
      >
        {this.getLabel()}
        {this.getField()}
      </div>
    );
  }
}

CustomDropZone.propTypes = {
  onChange: PropTypes.func,
};
