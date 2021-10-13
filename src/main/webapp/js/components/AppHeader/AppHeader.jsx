import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Model from 'model';
import _ from 'lodash';
import breadcrumbSegments from 'components/AppHeader/breadcrumbSegments';
import { urlMap } from 'utils/routeHelpers';
import Constants from 'constants/Constants';
import {
  PageHeader,
  ToastNotification,
  Xmark,
  Button,
} from '@tfs/ui-components';
import { stopShowingServerMessage } from 'state/actions/actions_app.js';

class AppHeader extends Component {
  constructor(props) {
    super(props);

    this.props = props;
    this.subtitleMap = {
      [urlMap.TAG_DATASETS]: 'Tag Datasets',
      [urlMap.TAG_DATASETS_BETA]: 'Tag Datasets Beta',
      [urlMap.RESOLVE_INCONSISTENCY]: 'Resolve Inconsistencies',
      [urlMap.UPDATEPROJECT]: 'Update Model',
      [urlMap.CREATEMODEL]: 'Create Version',
      [urlMap.CREATESPEECHMODEL]: 'Create Version',
      [urlMap.VIEWSPEECHMODEL]: 'View Version',
      [urlMap.TUNESPEECHMODEL]: 'Tune Version',
      [urlMap.TUNEMODEL]: 'Tune Version',
      [urlMap.VIEWMODEL]: 'View Version',
      [urlMap.TESTMODEL]: 'Test Version',
      [urlMap.BATCHTESTMODEL]: 'Test Version',
    };

    this.types = Constants.NOTIFICATION.types;
  }

  closeNotification = () => {
    const { dispatch } = this.props;
    dispatch(stopShowingServerMessage());
  }

  getSubtitle = ({ url }) => this.subtitleMap[url] || Constants.MANAGE_MODELS_DATASETS

   getButtons = () => {
     const { actionItems } = this.props;
     return (
       <div>
         {actionItems.map((item, i) => (
           <Button
             key={i}
             type={item.type}
             name={item.label}
             label={item.label}
             styleOverride={{ ...item.styleOverride, marginRight: '20px' }}
             onClick={item.onClick}
             disabled={item.isDisabled}
           >
             {item.label}
           </Button>
         ))}
       </div>
     );
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
              ':focus': {
                outline: 'none',
              },
              ':hover': {
                boxShadow: 'none',
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
      selectedProject, selectedClient, modelId,
      match, history, itsURL, actionItems,
    } = this.props;

    const projectId = !_.isNil(selectedProject) ? selectedProject.id : 0;
    let model = null;

    if (modelId && projectId) {
      model = Model.ProjectsManager.getModel(projectId, modelId);
    }

    const segments = breadcrumbSegments(match, {
      selectedClient,
      selectedProject,
      model,
    }, history, itsURL);

    return (
      <PageHeader
        title={this.getSubtitle(match)}
        breadcrumb={segments}
        actionItems={actionItems}
      >
        {this.getNotification()}
        {' '}
        {this.getButtons()}
      </PageHeader>

    );
  }
}

AppHeader.defaultProps = {
  selectedClient: null,
  selectedProject: null,
  history: {},
  match: {},
  location: {},
  actionItems: [],
};

AppHeader.propTypes = {
  selectedClient: PropTypes.object,
  selectedProject: PropTypes.object,
  match: PropTypes.object,
  history: PropTypes.object,
  location: PropTypes.object,
  itsURL: PropTypes.string.isRequired,
  actionItems: PropTypes.array,
};

export default AppHeader;
