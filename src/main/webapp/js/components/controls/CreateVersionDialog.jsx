import React, { Component } from 'react';
import { Dialog, Radio } from '@tfs/ui-components';
import PropTypes from 'prop-types';
import * as appActions from 'state/actions/actions_app';
import _ from 'lodash';
import Constants from 'constants/Constants';
import {
  createNewModel,
} from 'state/actions/actions_models';
import * as preferenceActions from 'state/actions/actions_preferences';
import { RouteNames } from 'utils/routeHelpers';
import * as actionsModels from 'state/actions/actions_models';
import { radioStyle } from '../../styles';

export default class CreateVersionDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.onClickCancel = this.onClickCancel.bind(this);
    this.onClickOk = this.onClickOk.bind(this);
    this.onSpeech = this.onSpeech.bind(this);
    this.onDigital = this.onDigital.bind(this);
    this.state = {
      digital: false,
      speech: false,
    };

    this.styleOverride = {
      content: {
        top: '20%',
      },
      ok: {
        marginLeft: '10px',
        paddingLeft: '25px',
        paddingRight: '25px',
      },
    };
  }

  componentDidMount() {
    const {
      clientId, projectId, dispatch,
    } = this.props;
    dispatch(preferenceActions.getTechnologyByClientModel({ clientId, projectId }));
  }

  onDigital() {
    this.setState({
      digital: true,
      speech: false,
    });
  }

  onSpeech() {
    this.setState({
      speech: true,
      digital: false,
    });
  }

  onClickCancel() {
    const { dispatch } = this.props;
    dispatch(appActions.modalDialogChange(null));
  }

  onClickOk() {
    const {
      clientId, projectId, history, dispatch, isAnyDatasetTransformed,
    } = this.props;
    const {
      speech, digital,
    } = this.state;
    if (digital) {
      if (isAnyDatasetTransformed) {
        dispatch(appActions.modalDialogChange(null));
        dispatch(createNewModel(projectId));
        dispatch(appActions.changeRoute(RouteNames.CREATEMODEL, { clientId, projectId }, history));
      }
    }
    if (speech) {
      dispatch(appActions.modalDialogChange(null));
      dispatch(actionsModels.newModel());
      dispatch(appActions.changeRoute(RouteNames.CREATESPEECHMODEL, { clientId, projectId }, history));
    }
  }

  render() {
    const { header } = this.props;
    const {
      speech, digital,
    } = this.state;
    const {
      DIGITAL, SPEECH,
    } = Constants.MODEL_TYPE;

    return (
      <div>
        <Dialog
          isOpen
          size="medium"
          okChildren="CREATE"
          headerChildren={header}
          onClickCancel={this.onClickCancel}
          onClickOk={this.onClickOk}
          onClickClose={this.onClickCancel}
          centerContent={false}
          okDisabled={!(digital || speech)}
          styleOverride={{
            childContainer: {
              paddingLeft: '20px',
              paddingRight: '30px',
              paddingTop: '30px',
              paddingBottom: '0px',
              overflow: 'auto',
              textAlign: 'left',
            },
            container: {
              width: '550px',
              height: '380px',
              display: 'grid',
              gridTemplateRows: '60px auto 60px',
            },
            header: {
              width: '100%',
              height: '100%',
              borderBottom: '1px solid',
              borderBottomColor: '#DADADA',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'center',
              title: {
                marginLeft: '23px',
                marginRight: '20px',
                fontSize: '20px',
                fontWeight: 'bold',
              },
              titleIcon: {
                marginRight: '20px',
              },
            },
          }}
        >
          <div>
            <div className="checkboxContainerVersion">
              <Radio checked={this.state.digital} label={DIGITAL} onChange={this.onDigital} styleOverride={radioStyle} />
            </div>
            <div style={{ paddingLeft: '25px', fontFamily: 'Lato', fontSize: '13px' }}>
              <p style={{ lineHeight: '1.5em' }}>
              A digital model is responsible for classification of user text (chat/recognized audio) into user intent
              </p>
            </div>
            <Radio checked={this.state.speech} label={SPEECH} onChange={this.onSpeech} styleOverride={radioStyle} />

            <div style={{ paddingLeft: '25px', fontFamily: 'Lato', fontSize: '13px' }}>
              <p style={{ lineHeight: '1.5em' }}>
              A speech model is responsible for recognizing user utterances.
              </p>
            </div>
          </div>
        </Dialog>
      </div>
    );
  }
}

CreateVersionDialog.propTypes = {
  header: PropTypes.string.isRequired,
  dispatch: PropTypes.func,
};

CreateVersionDialog.defaultProps = {
  dispatch: () => {},
};
