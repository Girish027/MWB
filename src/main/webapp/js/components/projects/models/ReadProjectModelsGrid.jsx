import React, { Component } from 'react';
import PropTypes from 'prop-types';

import _ from 'lodash';

import Model from 'model';
import ModelRowCascadeMenu from 'components/projects/models/ModelRowCascadeMenu';
import * as actionsModels from 'state/actions/actions_models';
import * as projectActions from 'state/actions/actions_projects';
import * as appActions from 'state/actions/actions_app';
import * as preferencesActions from 'state/actions/actions_preferences';
import { RouteNames } from 'utils/routeHelpers';
import Constants from 'constants/Constants';
import {
  Table, Button, RatingIcon, StatusBadge, Tooltip,
} from '@tfs/ui-components';
import dateFormat from 'dateformat';
import { connect } from 'react-redux';
import * as actionsCellEditable from 'state/actions/actions_cellEditable';
import CellEditable from 'components/controls/grid/CellEditable';
import tableUtils from 'utils/TableUtils';
import Placeholder from 'components/controls/Placeholder';
import { colors, headerIcon } from 'styles';

export class ReadProjectModelsGrid extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.state = {
      resizedData: {},
      statusDataCell: {},
    };


    this.isModified = false;
    this.cellEditableStateKey = 'ModelDescription';
    this.handleDeployableModel = this.handleDeployableModel.bind(this);
    this.handleCellValueChange = this.handleCellValueChange.bind(this);
    this.getEditorMultiline = this.getEditorMultiline.bind(this);
    this.updateStatusForCell = this.updateStatusForCell.bind(this);
    this.handleUndeployableModel = this.handleUndeployableModel.bind(this);
    this.handleDeploy = null;
    this.renderStar = this.renderStar.bind(this);
    this.onClickMarkForDeploy = this.onClickMarkForDeploy.bind(this);
    this.localStorage_key = Constants.LOCALSTORAGE_KEY.READ_PROJECT_MODEL;
  }

  componentDidMount() {
    let storedSettings = {};
    const { dispatch, clientId, projectId } = this.props;
    dispatch(preferencesActions.getTechnologyByClientModel({ clientId, projectId }));
    dispatch(actionsCellEditable.stateCreate({ stateKey: this.cellEditableStateKey }));
    try { storedSettings = JSON.parse(localStorage.getItem(this.localStorage_key)) || {}; } catch (e) { /* do nothing */ }
    storedSettings = Object.assign({}, storedSettings);
    this.isModified = true;
    this.setState({ resizedData: storedSettings });
  }

  componentWillUnmount() {
    const { dispatch } = this.props;
    dispatch(actionsCellEditable.stateRemove({ stateKey: this.cellEditableStateKey }));
  }

  handleCellValueChange(newValue, cellInfo) {
    const { dispatch, clientId, data } = this.props;

    let sortedData = _.sortBy(data, 'version');
    const { statusDataCell } = this.state;

    const updatedData = _.cloneDeep(sortedData);

    const columnId = cellInfo.column.id;
    const rowId = cellInfo.row.id;
    const { index } = cellInfo;

    const oldValue = updatedData[index][columnId];

    if (oldValue !== newValue) {
      const model = Object.assign({}, cellInfo.original, { description: newValue });
      const newData = {
        clientId,
        model,
      };
      dispatch(actionsModels.updateByModel(newData));
      this.updateStatusForCell(rowId, columnId, null);
    } else if (statusDataCell[rowId]
        && statusDataCell[rowId][columnId]
        && statusDataCell[rowId][columnId].indexOf('not saved')) {
      this.updateStatusForCell(rowId, columnId, null);
    }
  }

  updateStatusForCell(row, column, status, reset = true) {
    const updateStatusData = _.cloneDeep(this.state.statusDataCell);
    if (!updateStatusData[row]) {
      updateStatusData[row] = {};
    }
    updateStatusData[row][column] = status;
    this.setState({ statusDataCell: updateStatusData });
  }

  handleDeployableModel(model, modelId) {
    const { dispatch, clientId, projectId } = this.props;

    dispatch(projectActions.markDeployableModel({
      clientId, projectId, modelId, model,
    }));
    dispatch(appActions.modalDialogChange(null));
  }

  handleUndeployableModel(model, modelId) {
    const { dispatch, clientId, projectId } = this.props;

    dispatch(projectActions.unmarkDeployableModel({
      clientId, projectId, model,
    }));
    dispatch(appActions.modalDialogChange(null));
  }

  onClickMarkForDeploy(modelId, isDeployedEnabled) {
    const { dispatch, projectId } = this.props;
    const header = !isDeployedEnabled ? Constants.MARK_FOR_DEPLOY_MESSAGE_HEADER : Constants.UNMARK_FOR_DEPLOY_MESSAGE_HEADER;
    const message = !isDeployedEnabled ? Constants.MARK_FOR_DEPLOY_MESSAGE_BODY : Constants.UNMARK_FOR_DEPLOY_MESSAGE_BODY;
    const okChildren = !isDeployedEnabled ? Constants.OK_CHILDREN_MARK_FOR_DEPLOY : Constants.OK_CHILDREN_UNMARK_FOR_DEPLOY;
    this.handleDeploy = !isDeployedEnabled ? this.handleDeployableModel.bind(this) : this.handleUndeployableModel.bind(this);
    const model = Model.ProjectsManager.getModel(projectId, modelId) || null;

    dispatch(appActions.modalDialogChange({
      header,
      dispatch,
      message,
      showSpinner: false,
      okVisible: true,
      cancelVisible: true,
      cancelChildren: Constants.CANCEL,
      showHeader: true,
      showFooter: true,
      closeIconVisible: true,
      type: Constants.DIALOGS.PROGRESS_DIALOG,
      okChildren,
      onOk: () => this.handleDeploy(model, modelId),
      styleOverride: {
        childContainer: {
          marginTop: '10px',
          marginLeft: '-5px',
          marginRight: '30px',
        },
        container: {
          width: '460px',
          height: '280px',
          display: 'grid',
          gridTemplateRows: '60px auto 60px',
        },
        ...headerIcon,
      },
    }));
  }

  renderStar(modelInfo) {
    const { project = {}, roles } = this.props;
    const iconProps = {};
    let deployableModelId = '';
    let isDeployedEnabled = false;
    const { id, status, modelType } = modelInfo;
    let title = Constants.MARK_FOR_DEPLOY_MESSAGE_HEADER;
    const { COMPLETED } = Constants.STATUS;
    const isModelComplete = status && status === COMPLETED;
    const isUserAllowed = roles.includes(Constants.ADMIN_ROLE) || roles.includes(Constants.OPERATOR_ROLE);

    if (project && project.deployableModelId) {
      deployableModelId = project.deployableModelId.toString();
    }

    if (id === deployableModelId) {
      iconProps.fill = colors.cobalt;
      iconProps.stroke = colors.cobalt;
      isDeployedEnabled = true;
      title = Constants.UNMARK_FOR_DEPLOY_MESSAGE_HEADER;
    } else {
      iconProps.fill = colors.white;
      iconProps.stroke = colors.lightText;
    }
    return (
      <Button
        type="flat"
        title={title}
        name="starred-model"
        styleOverride={{
          height: 'auto',
          marginLeft: '10px',
          minHeight: '0px',
          ':hover': {
            boxShadow: 'none',
          },
          ':focus': {
            outline: 'none',
          },
        }}
        disabled={!isModelComplete || !isUserAllowed || modelType == Constants.DIGITAL_SPEECH_MODEL}
        onClick={() => this.onClickMarkForDeploy(id, isDeployedEnabled)}
      >
        <RatingIcon
          height={18}
          width={18}
          strokeWidth={30}
          {...iconProps}
        />
      </Button>
    );
  }

  renderModelVersion = (value) => (
    <a
      href="javascript:;"
      onClick={() => {
        this.onClickModelVersion(value);
      }}
    >
      <span>
        {value.version}
      </span>
    </a>
  );

  onClickModelVersion = (value) => {
    const {
      projectId, history, clientId, dispatch,
    } = this.props;

    dispatch(actionsModels.viewSelectedModel(value));
    if (value.modelType === 'SPEECH') {
      dispatch(appActions.changeRoute(RouteNames.VIEWSPEECHMODEL, { clientId, projectId }, history));
    } else {
      dispatch(appActions.changeRoute(RouteNames.VIEWMODEL, { clientId, projectId }, history));
    }
  };

  renderDataset = (value) => {
    const datasetNames = [];
    if (value.datasetIds && value.datasetIds.length) {
      value.datasetIds.forEach((datasetId) => {
        const dataset = Model.ProjectsManager.getDataset(value.projectId, datasetId);
        if (dataset && dataset.name) {
          datasetNames.push(dataset.name);
        }
      });
    }
    return (
      <span>{datasetNames.join(', ')}</span>
    );
  };

  onChange(newValue, cellInfo) {
    const { dispatch, clientId } = this.props;
    const model = Object.assign({}, cellInfo.original, { description: newValue });
    const data = {
      clientId,
      model,
    };
    dispatch(actionsModels.updateByModel(data));
  }

  getEditorMultiline(cellInfo) {
    const columnId = cellInfo.column.id;
    let className = 'editable-cell';
    if (columnId == Constants.PROJECT_MODELS_TABLE.description.id) {
      className += '';
    }
    return (
      <div>
        <div
          className={className}
          contentEditable
          suppressContentEditableWarning
          onBlur={(event) => {
            this.handleCellValueChange(event.target.innerText, cellInfo);
          }}
        >
          {cellInfo.value}
        </div>
      </div>
    );
  }

  renderDescription = (cellInfo) => {
    const { data } = this.props;
    return (
      <CellEditable
        stateKey="ModelDescription"
        value={cellInfo.value}
        rowIndex={cellInfo.index}
        columnIndex={0}
        maxRowIndex={data ? data.length - 1 : 0}
        maxColumnIndex={1}
        stopClickPropagation
        activateNextCellOnEnter
        onValidChange={(isValid) => {
        }}
        onChange={(newValue) => this.onChange(newValue, cellInfo)}
      />
    );
  };

  renderModelId = (value) => {
    let token;

    if (value && value.modelToken
        && typeof value.modelToken !== 'undefined') {
      token = value.modelToken;
    }
    if (typeof token === 'undefined'
        || token === 'undefined') {
      token = '';
    }

    return (
      <span>{token}</span>
    );
  };

  renderTechnology = (value) => {
    let type = Constants.MODEL_TYPE[value.modelType];
    let Technology = value.vectorizerTechnology;
    let TechnologyVersion = value.vectorizer_technology_version;
    const regex = new RegExp('^use');

    if (type == Constants.MODEL_TYPE.SPEECH) {
      Technology = Constants.TECHNOLOGY.SPEECH_SLM;
      TechnologyVersion = null;
    } else if (Technology == Constants.TECHNOLOGY.NGRAM) {
      Technology = Constants.TECHNOLOGY.DIGITAL_NGRAM;
      TechnologyVersion = null;
    } else if (regex.test(Technology)) {
      Technology = Constants.TENSORFLOW_TYPE[Technology];
      TechnologyVersion = Constants.TENSORFLOW_TYPE_VERSION(Technology, TechnologyVersion);
      Technology = Constants.TECHNOLOGY.DIGITAL_TENSORFLOW;
    }
    if (type == Constants.MODEL_TYPE.DIGITAL_SPEECH) {
      return (
        <span>
          {Constants.TECHNOLOGY.SPEECH_SLM}
          <hr />
          {Technology}
          <br />
          <small>{TechnologyVersion}</small>
        </span>
      );
    }
    return (
      <span>
        {Technology}
        <br />
        <small>{TechnologyVersion}</small>
      </span>
    );
  };

   renderType = (value) => {
     if (Constants.MODEL_TYPE[value.modelType] == Constants.MODEL_TYPE.DIGITAL_SPEECH) {
       return (
         <span>
           {Constants.MODEL_TYPE.SPEECH}
           <hr />
           {Constants.MODEL_TYPE.DIGITAL}
         </span>
       );
     }
     return (<span>{Constants.MODEL_TYPE[value.modelType]}</span>);
   };

  renderDate = (value) => {
    const dateObj = new Date(value);
    return (
      <span>
        {' '}
        {dateFormat(dateObj, 'mmmm dS, yyyy, h:MM:ss TT')}
        {' '}
      </span>
    );
  };

  renderStatus = (value) => {
    const { project = {} } = this.props;
    const { PREVIEW, LIVE } = Constants.STATUS;
    const { id } = value;

    if (project && project.previewModelId && project.previewModelId === id) {
      value.status = PREVIEW;
    }
    if (project && project.liveModelId && project.liveModelId === id) {
      value.status = LIVE;
    }
    return (
      <span>{this.getStatus(value)}</span>
    );
  }

  getStatusBadge(status) {
    const progress = status === Constants.STATUS.RUNNING
      ? {
        type: 'spinner',
      }
      : {};
    const type = status === Constants.STATUS.COMPLETED ? 'label' : 'badge';
    return (
      <StatusBadge
        category={Constants.STATUS_CATEGORY_MAPPING[status]}
        label={status}
        onHover={() => {}}
        progress={progress}
        type={type}
        styleOverride={{
          border: '1px solid',
          borderRadius: '2px',
          fontWeight: 'bold',
        }}
      />
    );
  }

  getStatus(value) {
    let message = '';
    const statusNotFound = 'Model status not available';
    let showTooltip = true;
    const status = _.isNil(value.status) ? 'NULL' : value.status;
    switch (status) {
    case 'NULL':
      message = 'Build has not started';
      break;

    case 'QUEUED':
      showTooltip = false;
      break;

    case 'RUNNING':
      message = _.isNil(value.statusMessage) ? statusNotFound : value.statusMessage;
      break;

    case 'COMPLETED':
      showTooltip = false;
      break;

    case 'PREVIEW':
      showTooltip = false;
      break;

    case 'LIVE':
      showTooltip = false;
      break;

    case 'FAILED':
      message = _.isNil(value.statusMessage) ? statusNotFound : value.statusMessage;
      break;

    case 'ERROR':
      message = _.isNil(value.statusMessage) ? statusNotFound : value.statusMessage;
      break;

    default:
      message = 'There was an error obtaining the model status';
      break;
    }

    return showTooltip ? (
      <Tooltip
        content={message}
        trigger="hover"
        direction="bottom"
        type="info"
      >
        <span>
          {this.getStatusBadge(status)}
        </span>
      </Tooltip>
    )
      : (
        <span>
          {this.getStatusBadge(status)}
        </span>
      );
  }

  get config() {
    const { resizedData } = this.state;

    const columns = [{
      Header: Constants.PROJECT_MODELS_TABLE.starred.header,
      accessor: Constants.PROJECT_MODELS_TABLE.starred.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.starred.id, 90),
      Cell: ({ original }) => (this.renderStar(original)),
    }, {
      Header: Constants.PROJECT_MODELS_TABLE.version.header,
      id: Constants.PROJECT_MODELS_TABLE.version.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.version.id, 95),
      accessor: '',
      Cell: ({ value }) => (this.renderModelVersion(value)),
    }, {
      Header: Constants.PROJECT_MODELS_TABLE.datasets.header,
      id: Constants.PROJECT_MODELS_TABLE.datasets.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.datasets.id, 100),
      accessor: '',
      Cell: ({ value }) => (this.renderDataset(value)),
    }, {
      Header: Constants.PROJECT_MODELS_TABLE.description.header,
      accessor: Constants.PROJECT_MODELS_TABLE.description.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.description.id, 120),
      Cell: this.getEditorMultiline,
    }, {
      Header: Constants.PROJECT_MODELS_TABLE.type.header,
      accessor: Constants.PROJECT_MODELS_TABLE.type.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.type.id, 90),
      Cell: ({ value }) => (this.renderType(value)),
    }, {
      Header: Constants.PROJECT_MODELS_TABLE.technology.header,
      accessor: '',
      id: Constants.PROJECT_MODELS_TABLE.type.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.type.id, 90),
      Cell: ({ value }) => (this.renderTechnology(value)),
    }, {
      Header: Constants.PROJECT_MODELS_TABLE.status.header,
      id: Constants.PROJECT_MODELS_TABLE.status.id,
      accessor: '',
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.status.id, 120),
      Cell: ({ value }) => (this.renderStatus(value)),
    }, {
      Header: Constants.PROJECT_MODELS_TABLE.modelId.header,
      accessor: '',
      id: Constants.PROJECT_MODELS_TABLE.modelId.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.modelId.id, 120),
      Cell: ({ value }) => (this.renderModelId(value)),
    }, {
      Header: Constants.PROJECT_MODELS_TABLE.user.header,
      accessor: Constants.PROJECT_MODELS_TABLE.user.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.user.id, 90),
    }, {
      Header: Constants.PROJECT_MODELS_TABLE.created.header,
      accessor: Constants.PROJECT_MODELS_TABLE.created.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.created.id, 130),
      Cell: ({ value }) => (this.renderDate(value)),
    }, {
      Header: Constants.PROJECT_MODELS_TABLE.action.header,
      id: Constants.PROJECT_MODELS_TABLE.action.id,
      accessor: '',
      sortable: false,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_MODELS_TABLE.action.id, 100),
      style: {
        paddingTop: '0px',
        paddingBottom: '0px',
      },
      Cell: ({ original }) => (
        <ModelRowCascadeMenu
          projectId={original.projectId}
          modelId={original.id}
          configId={original.configId}
        />
      ),
    }];

    return columns;
  }

  render() {
    const { data } = this.props;

    let sortedData = _.sortBy(data, 'version');

    // This check is added to prevent the table from unnecesary rendering before ComponentDidMount.
    if (this.isModified === false) return <Placeholder message={Constants.SEARCHING_IN_PROGRESS} />;

    return (
      <Table
        style={{ maxHeight: 'calc(95vh - 225px)', borderLeft: 'unset' }}
        resizable
        data={sortedData}
        filtered={this.props.filtered}
        defaultFilterMethod={(filter, value, column) => tableUtils.handleFilterMethod(filter, value, column)}
        columns={this.config}
        onResizedChange={(newResized) => {
          tableUtils.handleTableResizeChange(newResized, this.localStorage_key);
        }}
        defaultSorted={[
          {
            id: Constants.PROJECT_MODELS_TABLE.created.id,
            desc: true,
          },
        ]}
      />
    );
  }
}


const mapStateToProps = (state) => {
  const projectId = state.projectListSidebar.selectedProjectId;
  return {
    project: Model.ProjectsManager.getProject(projectId) || null,
  };
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

ReadProjectModelsGrid.propTypes = {
  data: PropTypes.array.isRequired,
  kibanaLogIndex: PropTypes.string,
  kibanaLogURL: PropTypes.string,
  userFeatureConfiguration: PropTypes.object,
  featureFlags: PropTypes.object,
  clientId: PropTypes.string,
  projectId: PropTypes.string,
  history: PropTypes.object,
  dispatch: PropTypes.func,
  roles: PropTypes.array,
};

export default connect(mapStateToProps, mapDispatchToProps)(ReadProjectModelsGrid);
