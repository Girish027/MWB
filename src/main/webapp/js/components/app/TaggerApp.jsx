import React, { Component } from 'react';
import cookie from 'react-cookies';
import { connect } from 'react-redux';
import URLSearchParams from '@ungap/url-search-params';
import * as actions from 'state/actions/actions_header';
import * as appActions from 'state/actions/actions_app.js';
import TaggerContextMenuContainer from 'components/controls/TaggerContextMenuContainer';
import TaggerCascadeMenu from 'components/controls/TaggerCascadeMenu';
import SimpleDialog from 'components/controls/SimpleDialog';
import ProgressDialog from 'components/controls/ProgressDialog';
import LinkDialog from 'components/controls/LinkDialog';
import UnauthorizedUserDialog from 'components/controls/UnauthorizedUserDialog';
import SpeechSelectDatasetDialog from 'components/models/speech/SpeechSelectDatasetDialog';
import CombinedSpeechDialog from 'components/models/speech/CombinedSpeechDialog';
import TaggingGuideImportDialog from 'components/taggingguide/import/TaggingGuideImportDialog';
import CreateDatasetDialog from 'components/datasets/create/CreateDatasetDialog';
import { Switch } from 'react-router-dom';
import ErrorBoundary from 'components/ErrorBoundary/ErrorBoundary';
import TagDatasets from 'components/tagging/TagDatasets';
import ReadProject from 'components/projects/ReadProject';
import ProjectForm from 'components/projects/ProjectForm';
import Settings from 'components/settings/Settings';
import ConsistencyReport from 'components/consistency/ConsistencyReport';
import CreateModelTabs from 'components/models/CreateModelTabs';
import speechModelCreate from 'components/models/speechModelCreate';
import ModelTestTabs from 'components/models/ModelTestTabs';
import MainLayout from 'layouts/MainLayout';
import NoSidebarLayout from 'layouts/NoSidebarLayout';
import SettingsLayout from 'layouts/SettingsLayout';
import AppRoute from 'components/app/AppRoute';
import { fetchAppConfig } from 'state/actions/actions_appConfig';
import { routeRoot, urlMap } from 'utils/routeHelpers';
import Constants from 'constants/Constants';
import MediaRecorder from 'audio-recorder-polyfill';
import CreateVersionDialog from 'components/controls/CreateVersionDialog';
import TagDatasetBeta from '../tagging/TagDatasetsBeta';
import DeleteDialog from '../controls/DeleteDialog';
import PromoteDialog from '../controls/PromoteDialog';
import RecordSpeechDialog from '../controls/RecordSpeechDialog';
import DatasetColumnMapperDialog from '../controls/DatasetColumnMapperDialog';
import AddIntentDialog from '../controls/AddIntentDialog';
import CreateModelDialog from '../controls/CreateModelDialog';
import DatasetDialog from '../controls/DatasetDialog';
import ColumnSelectorDialog from '../controls/ColumnSelectorDialog';
// to do: create a message queue to manage multiple messages at once

class TaggerApp extends Component {
  constructor(props) {
    super(props);

    this.props = props;

    this.events = [
      'load',
      'mousemove',
      'click',
      'keypress',
    ];

    this.setActiveTime = this.setActiveTime.bind(this);
    this.pingBackend = this.pingBackend.bind(this);
    this.sessionTimeoutWarning = this.sessionTimeoutWarning.bind(this);
    this.logout = this.logout.bind(this);
    this.resetTimeout = this.resetTimeout.bind(this);
    this.resetLogoutTimeout = this.resetLogoutTimeout.bind(this);

    this.events.forEach((event) => {
      window.addEventListener(event, this.resetTimeout);
    });

    this.setTimeout();
    this.setActiveTime();

    this.handleEscapeKey = this.handleEscapeKey.bind(this);
    const userName = cookie.load('userName');
    const userId = cookie.load('userId');
    const userDetails = cookie.load('userDetails');

    const userGroup = cookie.load('ITS_GROUP');
    if (userGroup === Constants.ITS_GROUP) {
      this.props.dispatch(appActions.updateUserGroups({ [userGroup]: userGroup }));
    }
    document.title = Constants.DOCUMENT_TITLE_EC;

    this.props.dispatch(appActions.updateLoginInfo(userId, userName, userDetails));
    this.props.dispatch(appActions.getCSRFToken(userId));

    // Use the Audio Recorder Polyfill APIs to record audio and convert to wav file.
    // This supports in Chrome, Safari and Firefox
    // Without the polyfill, recorded audio in chrome is in webm format and in firefox ogg format.
    window.MediaRecorder = MediaRecorder;
  }

  componentDidMount() {
    const {
      dispatch, app, routeClientId, routeAppId,
    } = this.props;
    const { userId, csrfToken } = app;
    dispatch(actions.fetchClientList(userId, csrfToken, routeClientId, routeAppId));
    dispatch(actions.fetchVersionInfo());
    dispatch(fetchAppConfig());
    document.addEventListener('keydown', this.handleEscapeKey, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleEscapeKey, false);
  }

  clearTimeout() {
    if (this.warnTimeout) clearTimeout(this.warnTimeout);
  }

  setTimeout() {
    const { logoutWarningTimeout } = this.props.app;
    this.warnTimeout = setTimeout(this.sessionTimeoutWarning, logoutWarningTimeout);
  }

  setActiveTime() {
    const { logoutWarningTimeout, logoutTimeout } = this.props.app;
    const pingBackendTime = logoutWarningTimeout - logoutTimeout;
    this.activeTime = setTimeout(this.pingBackend, pingBackendTime);
  }

  pingBackend() {
    appActions.callLogIngestAPI(Constants.SESSION_TIMEOUT_PING_BACKEND, Constants.LOG_LEVEL_INFO);
    if (this.activeTime) clearTimeout(this.activeTime);
    this.setActiveTime();
  }

  resetTimeout() {
    this.clearTimeout();
    this.setTimeout();
  }

  resetLogoutTimeout() {
    this.clearTimeout();
    this.setTimeout();
    appActions.callLogIngestAPI(Constants.SESSION_TIMEOUT_WARNING_LOG_MESSAGE, Constants.LOG_LEVEL_INFO);
    if (this.logoutTimeout) clearTimeout(this.logoutTimeout);
  }

  sessionTimeoutWarning() {
    const { logoutTimeout } = this.props.app;
    this.logoutTimeout = setTimeout(this.logout, logoutTimeout);

    const { dispatch } = this.props;
    const header = Constants.SESSION_TIMEOUT_WARNING_HEADER;
    const message = Constants.SESSION_TIMEOUT_WARNING_MESSAGE;

    dispatch(appActions.modalDialogChange({
      header,
      dispatch,
      message,
      showSpinner: false,
      okVisible: true,
      cancelVisible: false,
      closeIconVisible: false,
      showHeader: true,
      showFooter: true,
      type: Constants.DIALOGS.PROGRESS_DIALOG,
      okChildren: Constants.SESSION_TIMEOUT_LOGOUT_DIALOG_BUTTON,
      onOk: () => {
        this.resetLogoutTimeout();
        dispatch(appActions.modalDialogChange(null));
      },
      styleOverride: {
        childContainer: {
          marginLeft: '30px',
          marginRight: '30px',
        },
        content: {
          top: '160px',
        },
      },
    }));
  }

  logout() {
    const { dispatch } = this.props;
    this.destroy();
    appActions.callLogIngestAPI(Constants.SESSION_TIMEOUT_LOGOUT_LOG_MESSAGE, Constants.LOG_LEVEL_INFO);
    dispatch(appActions.logoutFromOkta());
  }

  destroy() {
    this.clearTimeout();
    this.events.forEach((event) => {
      window.addEventListener(event, this.resetTimeout);
    });
  }

  handleEscapeKey(event) {
    if (event.keyCode === 27) {
      event.preventDefault();
      const { dispatch } = this.props;
      dispatch(appActions.modalDialogChange(null));
    }
  }

  getContextMenu() {
    const { contextMenuState } = this.props.app;
    if (!contextMenuState) {
      return null;
    }
    switch (contextMenuState.type) {
    case 'TaggerCascadeMenu':
    default:
      return (
        <TaggerContextMenuContainer
          top={contextMenuState.top}
          left={contextMenuState.left}
        >
          <TaggerCascadeMenu
            items={contextMenuState.items}
            className={contextMenuState.className || undefined}
          />
        </TaggerContextMenuContainer>
      );
    }
  }

  getModalDialog() {
    let { modalDialogState } = this.props.app;
    const { history, userFeatureConfiguration, dispatch } = this.props;
    if (!modalDialogState) {
      return null;
    }
    const { DIALOGS } = Constants;
    modalDialogState = { history, ...modalDialogState };
    switch (modalDialogState.type) {
    case DIALOGS.SIMPLE_DIALOG:
      return (
        <SimpleDialog {...modalDialogState} />
      );

    case DIALOGS.DELETE_DIALOG:
      return (
        <DeleteDialog {...modalDialogState} />
      );

    case DIALOGS.PROGRESS_DIALOG:
      return (
        <ProgressDialog {...modalDialogState} />
      );

    case DIALOGS.IMPORT_TAGGING_GUIDE:
      return (
        <TaggingGuideImportDialog {...modalDialogState} />
      );
    case DIALOGS.CREATE_DATASET:
      return (
        <CreateDatasetDialog {...modalDialogState} />
      );
    case DIALOGS.UNAUTHORIZED_USER:
      return (
        <UnauthorizedUserDialog {...modalDialogState} />
      );
    case DIALOGS.ADD_SPEECH:
      return (
        <SpeechSelectDatasetDialog {...modalDialogState} />
      );
    case DIALOGS.ADD_COMBINED_SPEECH:
      return (
        <CombinedSpeechDialog {...modalDialogState} />
      );
    case DIALOGS.LINK_DIALOG:
      return (
        <LinkDialog {...modalDialogState} />
      );
    case DIALOGS.PROMOTE_DIALOG:
      return (
        <PromoteDialog {...modalDialogState} />
      );
    case DIALOGS.CREATE_VERSION_DIALOG:
      return (
        <CreateVersionDialog {...modalDialogState} />
      );
    case DIALOGS.COLUMNSELECTOR_DIALOG:
      return (
        <ColumnSelectorDialog {...modalDialogState} />
      );
    case DIALOGS.RECORD_SPEECH:
      return (
        <RecordSpeechDialog {...modalDialogState} />
      );
    case DIALOGS.ADD_INTENT:
      return (
        <AddIntentDialog {...modalDialogState} />
      );
    case DIALOGS.CREATE_MODEL_DIALOG:
      return (
        <CreateModelDialog {...modalDialogState} />
      );
    case DIALOGS.DATASET_DIALOG:
      return (
        <DatasetDialog {...modalDialogState} userFeatureConfiguration={userFeatureConfiguration} />
      );
    case DIALOGS.DATASET_COLUMN_MAPPING_DIALOG:
      return (
        <DatasetColumnMapperDialog {...modalDialogState} />
      );
    }

    return null;
  }

  render() {
    const { environment } = this.props;
    const rootRoute = `${routeRoot}`;
    return (
      <ErrorBoundary>
        <div>
          <Switch>
            <AppRoute
              path={`${rootRoute}${urlMap.TAG_DATASETS}`}
              layout={NoSidebarLayout}
              component={TagDatasets}
            />
            { (environment !== Constants.ENVIRONMENTS.PRODUCTION)
              ? (
                <AppRoute
                  path={`${rootRoute}${urlMap.TAG_DATASETS_BETA}`}
                  layout={NoSidebarLayout}
                  component={TagDatasetBeta}
                />
              ) : null}
            <AppRoute
              path={`${rootRoute}${urlMap.RESOLVE_INCONSISTENCY}`}
              layout={NoSidebarLayout}
              component={ConsistencyReport}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.CREATEMODEL}`}
              layout={NoSidebarLayout}
              component={CreateModelTabs}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.TUNEMODEL}`}
              layout={NoSidebarLayout}
              component={CreateModelTabs}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.VIEWMODEL}`}
              layout={NoSidebarLayout}
              component={CreateModelTabs}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.CREATESPEECHMODEL}`}
              layout={NoSidebarLayout}
              component={speechModelCreate}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.TUNESPEECHMODEL}`}
              layout={NoSidebarLayout}
              component={speechModelCreate}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.VIEWSPEECHMODEL}`}
              layout={NoSidebarLayout}
              component={speechModelCreate}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.TESTMODEL}`}
              layout={NoSidebarLayout}
              component={ModelTestTabs}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.BATCHTESTMODEL}`}
              layout={NoSidebarLayout}
              component={ModelTestTabs}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.UPDATEPROJECT}`}
              layout={NoSidebarLayout}
              component={ProjectForm}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.DATASETS}`}
              layout={MainLayout}
              component={ReadProject}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.MODELS}`}
              layout={MainLayout}
              component={ReadProject}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.MANAGE_INTENTS}`}
              layout={MainLayout}
              component={ReadProject}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.OVERVIEW}`}
              layout={MainLayout}
              component={ReadProject}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.NODE_ANALYTICS}`}
              layout={MainLayout}
              component={ReadProject}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.MANAGE_SETTINGS}`}
              layout={MainLayout}
              component={ReadProject}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.PROJECTS}`}
              layout={MainLayout}
              component={ReadProject}
            />
            <AppRoute
              exact
              path={`${rootRoute}${urlMap.SETTINGS}`}
              layout={SettingsLayout}
              component={Settings}
            />
            <AppRoute
              exact
              path={`${rootRoute}`}
              layout={MainLayout}
              component={ReadProject}
            />
          </Switch>

          {this.getContextMenu()}
          {this.getModalDialog()}
        </div>
      </ErrorBoundary>
    );
  }
}


const mapStateToProps = (state, ownProps) => {
  const query = new URLSearchParams(ownProps.location.search);

  const routeClientId = query.get('clientid');
  const routeAppId = query.get('appid');
  return {
    app: state.app,
    header: state.header,
    environment: state.app.environment,
    userFeatureConfiguration: state.app.userFeatureConfiguration,
    routeClientId,
    routeAppId,
  };
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});


export default connect(mapStateToProps, mapDispatchToProps)(TaggerApp);
