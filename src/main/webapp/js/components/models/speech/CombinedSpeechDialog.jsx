import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  TextField, Dialog,
} from '@tfs/ui-components';
import { connect } from 'react-redux';
import Constants from 'constants/Constants';
import { modalDialogChange } from 'state/actions/actions_app';
import { combineModel } from 'state/actions/actions_models';
import validationUtil from 'utils/ValidationUtil';
import { dropzoneContainer, combinedSpeechDialogStyle, headerIcon } from '../../../styles';


export default class CombinedSpeechDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.onChangeDigitalUrl = this.onChangeDigitalUrl.bind(this);
    this.onClickCancel = this.onClickCancel.bind(this);
    this.onClickOk = this.onClickOk.bind(this);
    this.state = {
      digitalHostedUrl: '',
      isValidUrl: false,
    };
    this.styleOverride = {
      ...combinedSpeechDialogStyle,
      ...headerIcon,
    };
  }

  onChangeDigitalUrl(evt) {
    let digitalUrl = evt.target.value;
    const isValidUrl = validationUtil.validateUrl(digitalUrl);
    this.setState({
      digitalHostedUrl: digitalUrl,
      isValidUrl,
    });
  }

  onClickCancel() {
    const { dispatch } = this.props;
    dispatch(modalDialogChange(null));
  }

  onClickOk() {
    const {
      dispatch, projectId, clientId, modelId,
    } = this.props;
    const { digitalHostedUrl } = this.state;
    const combineData = {
      projectId,
      clientId,
      modelId,
      digitalHostedUrl,
    };
    dispatch(combineModel(combineData));
    dispatch(modalDialogChange(null));
  }

  render() {
    const { userFeatureConfiguration, speechOnly, header } = this.props;
    const { isValidUrl, digitalHostedUrl } = this.state;
    return (
      <div>
        <Dialog
          isOpen
          okDisabled={!isValidUrl || !digitalHostedUrl}
          headerChildren={header}
          onClickOk={this.onClickOk}
          onClickClose={this.onClickCancel}
          onClickCancel={this.onClickCancel}
          cancelChildren={Constants.CANCEL}
          size="small"
          centerContent={false}
          styleOverride={this.styleOverride}
        >
          <div style={dropzoneContainer.speechRadioAndTextGroup.label}> Digital Model Url </div>
          <TextField
            type="text"
            placeholder="Enter URL"
            name="digital-url"
            title="URL of Digital Model for the Speech recognizer to point to"
            styleOverride={{
              width: '100%',
            }}
            onChange={this.onChangeDigitalUrl}
          />
        </Dialog>
      </div>
    );
  }
}

CombinedSpeechDialog.defaultProps = {
  dispatch: () => {},
};

CombinedSpeechDialog.propTypes = {
  dispatch: PropTypes.func,
  onChange: PropTypes.func.isRequired,
};
