
import Model from 'model';
import { RouteNames } from 'utils/routeHelpers';
import Constants from 'constants/Constants';
import {
  Table, Button, RatingIcon, StatusBadge, Tooltip, Select, DateRangePicker,
} from '@tfs/ui-components';
import dateFormat from 'dateformat';
import { connect } from 'react-redux';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { withRouter } from 'react-router-dom';
import { changeRoute, modalDialogChange } from 'state/actions/actions_app';
import Placeholder from 'components/controls/Placeholder';
import {
  createNewModel,
} from 'state/actions/actions_models';
import {
  ContextualActionsBar,
  ContextualActionItem,
  Pencil,
} from '@tfs/ui-components';
import tableUtils from 'utils/TableUtils';
import { actionBar, dropdownRightAlign } from 'styles';

const transformedModelsArray = (modelsArray, configs) => {
  const configsArray = configs ? configs.toArray() : [];

  if (modelsArray) {
    modelsArray.forEach((item) => {
      const index = _.findIndex(configsArray, configItem => configItem.id == item.configId);

      if (index > -1 && configsArray.length > index) {
        item.configName = configsArray[index].name;
      }
    });
  }
  return modelsArray;
};

class Overview extends Component {
  constructor(props, context) {
    super(props, context);
    this.props = props;

    this.onClickCreateVersionModel = this.onClickCreateVersionModel.bind(this);

    this.changeVersion = this.changeVersion.bind(this);


    let modelsArray = this.props.models ? this.props.models.toArray() : [];
    modelsArray.sort(m => m.updated || m.created);
    modelsArray = this.props.models ? transformedModelsArray(modelsArray, this.props.configs) : [];
    this.state = {
      modelsArray,
      currIndex: 0,
      sortArray: modelsArray,
      flag: 0,
      projectId: this.props.projectId,
    };
  }

  static getDerivedStateFromProps(nextProps, state) {
    let modelsArray = nextProps.models ? nextProps.models.toArray() : [];
    modelsArray.sort(m => m.updated || m.created);
    modelsArray = nextProps.models ? transformedModelsArray(modelsArray, nextProps.configs) : [];
    if (nextProps.projectId !== state.projectId) {
      state.currIndex = 0;
      state.flag = 0;
      state.sortArray = [];
    }
    return ({
      modelsArray,
    });
  }


  onClickCreateVersionModel() {
    event.stopPropagation();
    const {
      project,
      dispatch,
      clientId,
      projectId,
      history,
      isAnyDatasetTransformed,
    } = this.props;
    const header = Constants.PLACEHOLDER;
    dispatch(modalDialogChange({
      header,
      type: Constants.DIALOGS.CREATE_VERSION_DIALOG,
      projectId,
      history,
      isAnyDatasetTransformed,
      clientId,
      dispatch,
    }));
  }


  renderPlaceholder = () => {
    const {
      isAnyDatasetTransformed,
    } = this.props;

    return isAnyDatasetTransformed
      ? (
        <Placeholder>
          <div className="message-default">{Constants.CREATE_FIRST_VERSION_MESSAGE}</div>
          <Button
            type="flat"
            name="upload-2"
            onClick={this.onClickCreateVersionModel}
          >
            <ContextualActionItem>
              <Pencil
                width="10px"
                height="10px"
                fill="none"
                stroke="#004C97"
              />
              <span> CREATE NEW VERSION</span>
            </ContextualActionItem>
          </Button>
        </Placeholder>
      )
      : (
        <Placeholder message={Constants.DATASET_NOT_AVAILABLE_MESSAGE} />
      );
  }


  returnListofVersions(sortedData) {
    const value = sortedData;
    let versionsList = [];
    for (let i = sortedData.length - 1; i >= 0; i--) {
      versionsList.push(sortedData[i].version);
    }
    return versionsList;
  }

  renderVersions(sortedData) {
    let items = this.returnListofVersions(sortedData);
    const value = sortedData;
    let selectedIndex = 0;
    if (this.state.currIndex == 0) {
      selectedIndex = 0;
    } else {
      selectedIndex = (items.length - this.state.currIndex);
    }
    const options = [];
    for (let i = 0; i < value.length; i++) {
      options.push({ value: items[i], label: items[i] });
    }
    const placeholder = Constants.SELECT;
    const isSearchable = false;
    return (
      <div>
        <small className="greytext">{Constants.VERSION_SELECT}</small>
        <br />
        <div className="selectbox">
          <Select
            onChange={this.changeVersion}
            options={options}
            placeholder={placeholder}
            isSearchable={isSearchable}
            value={options[selectedIndex]}
            styleOverride={{
              container: {
                width: '100%',
                height: '32px',
              },
            }}
          />
        </div>
      </div>
    );
  }


  changeVersion(input) {
    let ind = 0;
    ind = this.returnIndex(input.value);
    this.setState({ currIndex: ind, flag: 1 });
  }

  returnIndex(version) {
    let index = 0;
    const value = this.state.sortArray;
    for (let i = 0; i < value.length; i++) {
      if (value[i].version == version) {
        index = i;
        break;
      }
    }
    return index + 1;
  }

  renderDescription(description) {
    return (
      <span>
        <div className="greytext">{Constants.DESCRIPTION}</div>
        <div className="limit">{description}</div>
      </span>
    );
  }

  renderId(modelToken) {
    return (
      <span>
        <div className="greytext">{Constants.ID}</div>
        <div className="fsize">{modelToken}</div>
      </span>
    );
  }

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

  getStatus(value) {
    let message = '';
    const statusNotFound = Constants.STATUS_NOT_FOUND;
    let showTooltip = true;
    const status = _.isNil(value.status) ? Constants.STATUS.NULL : value.status;
    switch (status) {
    case Constants.STATUS.NULL:
      message = Constants.MSG_BUILD_NOT_STARTED;
      break;

    case Constants.STATUS.QUEUED:
      showTooltip = false;
      break;

    case Constants.STATUS.RUNNING:
      message = _.isNil(value.statusMessage) ? statusNotFound : value.statusMessage;
      break;

    case Constants.STATUS.COMPLETED:
      showTooltip = false;
      break;

    case Constants.STATUS.PREVIEW:
      showTooltip = false;
      break;

    case Constants.STATUS.LIVE:
      showTooltip = false;
      break;

    case Constants.STATUS.FAILED:
      message = _.isNil(value.statusMessage) ? statusNotFound : value.statusMessage;
      break;

    case Constants.STATUS.ERROR:
      message = _.isNil(value.statusMessage) ? statusNotFound : value.statusMessage;
      break;

    default:
      message = Constants.MSG_ERROR_IN_MODEL_STATUS;
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

  getStatusBadge(status) {
    const progress = status === Constants.STATUS.RUNNING
      ? {
        type: Constants.SPINNER,
      }
      : {};
    const type = status === Constants.STATUS.COMPLETED ? Constants.LABEL : Constants.BADGE;
    return (
      <StatusBadge
        category={Constants.STATUS_CATEGORY_MAPPING[status]}
        label={status}
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

  renderStatusElement(value) {
    return (
      <span>
        <div className="greytext">{Constants.STATUSBADGE}</div>
        <div className="fsize">{this.getStatus(value)}</div>
      </span>
    );
  }

  renderDate = (value) => {
    const dateObj = new Date(value);
    return (
      <span>
        {' '}
        {dateFormat(dateObj, 'mmmm dS, yyyy | h:MM ')}
        {' '}
      </span>
    );
  };

  renderCreated(value) {
    return (
      <span>
        <div className="greytext">{Constants.CREATED}</div>
        <div className="fsize">
          {value.userId}
          <div className="greytext">{this.renderDate(value.created)}</div>
        </div>
      </span>
    );
  }

  renderType(modelType) {
    return (
      <span>
        <div className="greytext">{Constants.TYPE}</div>
        <div className="fsize">{Constants.MODEL_TYPE[modelType]}</div>
      </span>
    );
  }

  renderTechnology(modelType, vectorizerTechnology) {
    const regex = new RegExp('^use');
    let type = modelType;
    let technology = vectorizerTechnology;
    if (type == Constants.DIGITAL_SPEECH_MODEL && technology == Constants.TECHNOLOGY.NGRAM) {
      technology = Constants.TECHNOLOGY.SPEECH_DIGITAL_NGRAM;
    } else if (type == Constants.DIGITAL_SPEECH_MODEL && regex.test(technology)) {
      technology = Constants.TECHNOLOGY.SPEECH_DIGITAL_TENSORFLOW;
    } else if (type == Constants.SPEECH_MODEL) {
      technology = Constants.TECHNOLOGY.SPEECH_SLM;
    } else if (technology == Constants.TECHNOLOGY.NGRAM) {
      technology = Constants.TECHNOLOGY.DIGITAL_NGRAM;
    } else if (regex.test(technology)) {
      technology = Constants.TECHNOLOGY.DIGITAL_TENSORFLOW;
    }
    return (
      <span>
        <div className="greytext">{Constants.MODEL_TECH}</div>
        <div className="fsize">{technology}</div>
      </span>
    );
  }


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
      <span>
        {datasetNames.join(', ')}
      </span>
    );
  };

  renderDatasetslist(value) {
    return (
      <span>
        <div className="greytext">{Constants.DATASET}</div>
        <span className="dsize">{this.renderDataset(value)}</span>
      </span>
    );
  }


  render() {
    const {
      loadingModels, models, projectId,
      isAnyDatasetTransformed, history, clientId,
      roles,
    } = this.props;
    const { modelsArray, flag, sortArray } = this.state;
    if (!loadingModels && !models) {
      this.renderPlaceholder();
    }
    let ind = modelsArray.length;

    let sortedData = _.sortBy(modelsArray, 'version');


    let value = [];
    if (modelsArray.length > 0 && this.state.currIndex != ind && flag == 0) {
      this.setState({ currIndex: ind, sortArray: sortedData, projectId: this.props.projectId });
      value = sortedData[ind - 1];
    }

    if (flag == 1) {
      value = sortedData[this.state.currIndex - 1];
    } else { value = sortedData[ind - 1]; }


    return (
      <div>
        { modelsArray.length
          ? (
            <div>
              <div>
                <div>
                  <div className="left">
                    <li className="list">
                      {this.renderVersions(sortedData)}
                      {' '}
                    </li>
                    <li className="list">{this.renderVersions(sortedData)}</li>
                    <div className="left">
                      <li className="leftdes">
                        {' '}
                        {this.renderDescription(value.description)}
                      </li>
                    </div>
                    <div className="div-component">
                      <hr />
                    </div>
                    <div className="graph-component" />
                    <div className="nodes-component">
                      <div className="nodes">
                        {Constants.NODES}
                        <hr className="underline" />
                        {' '}
                      </div>
                      <small className="greytext">
                        {' '}
                       &emsp;&emsp;
                        {Constants.NODENAME}
                        {' '}
                      </small>
                      <small className="greytext volpadding">
                        {Constants.VOLUME_NODE}
                        {' '}
                      </small>
                      <small className="greytext">
                        {Constants.ESCLATION}
                        {' '}
                      </small>
                    </div>

                  </div>
                  <div className="middle">
                    {this.renderId(value.modelToken)}

                    {this.renderStatusElement(value)}

                    {this.renderCreated(value)}
                  </div>
                  <div className="right">
                    {this.renderType(value.modelType)}
                    <br />
                    {this.renderTechnology(value.modelType, value.vectorizerTechnology)}

                    {this.renderDatasetslist(value)}

                  </div>
                </div>
              </div>

            </div>
          )
          : this.renderPlaceholder()}
      </div>
    );
  }
}

Overview.propTypes = {
  loadingModels: PropTypes.bool,
  models: PropTypes.object,
  configs: PropTypes.object,
  projectId: PropTypes.string,
  clientId: PropTypes.string,
  isAnyDatasetTransformed: PropTypes.bool,
  history: PropTypes.object,
  roles: PropTypes.array,
  dispatch: PropTypes.func,
};

export default withRouter(Overview);
