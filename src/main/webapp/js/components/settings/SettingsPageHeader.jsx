import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import breadcrumbSegments from 'components/AppHeader/breadcrumbSegments';
import Constants from 'constants/Constants';
import {
  PageHeader,
  ToastNotification,
  Xmark,
  Button,
} from '@tfs/ui-components';
import { stopShowingServerMessage, changeRoute } from 'state/actions/actions_app.js';

export default class SettingsPageHeader extends Component {
  constructor(props) {
    super(props);

    this.props = props;
    this.types = Constants.NOTIFICATION.types;
  }

  closeNotification = () => {
    const { dispatch } = this.props;
    dispatch(stopShowingServerMessage());
  }

  getNotification = () => {
    const { app: { notificationType, serverMessage } = {} } = this.props;

    let notification = null;
    if (notificationType && serverMessage) {
      notification = (
        <ToastNotification
          type={notificationType}
          styleOverride={{
            top: '4px',
            width: '40%',
            display: 'inline-block',
            padding: '0px 8px',
            fontSize: '13px',
            fontWeight: 'normal',
            lineHeight: '20px',
          }}
        >
          {serverMessage}
          <Button
            type="flat"
            onClick={this.closeNotification}
            styleOverride={{
              marginLeft: '10px',
              ':hover': {
                boxShadow: 'none',
              },
              ':focus': {
                outline: 'none',
              },
            }}
          >
            <Xmark fill={notificationType === this.types.error ? '#c81919' : '#00ac86'} />
          </Button>
        </ToastNotification>
      );
    }
    return notification;
  }

  render() {
    const {
      selectedProject, selectedClient,
      match, history, itsURL,
    } = this.props;

    const breadcrumbs = breadcrumbSegments(match, {
      selectedClient,
      selectedProject,
    }, history, itsURL);

    return (
      <PageHeader
        title={Constants.PREFERENCES}
        breadcrumb={breadcrumbs}
      >
        {this.getNotification()}
      </PageHeader>
    );
  }
}

SettingsPageHeader.defaultProps = {
  history: {},
  selectedClient: {},
  selectedProject: {},
  match: {},
  itsURL: '',
  clientId: '',
  dispatch: () => {},
};

SettingsPageHeader.propTypes = {
  history: PropTypes.object,
  selectedClient: PropTypes.object,
  selectedProject: PropTypes.object,
  match: PropTypes.object,
  itsURL: PropTypes.string,
  dispatch: PropTypes.func,
};
