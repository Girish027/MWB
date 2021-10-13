
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

class NodeAnalytics extends Component {
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
    };
  }

  static getDerivedStateFromProps(nextProps) {
    let modelsArray = nextProps.models ? nextProps.models.toArray() : [];
    modelsArray.sort(m => m.updated || m.created);

    modelsArray = nextProps.models ? transformedModelsArray(modelsArray, nextProps.configs) : [];

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
            defaultValue={options[selectedIndex]}
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

  renderNode() {
    const options = [
      { value: 'Pre-Selected Node', label: 'Pre-Selected Node' },
      { value: 'None', label: 'None' },
    ];
    const placeholder = Constants.SELECT;
    const isSearchable = false;
    return (
      <div>
        <small className="greytext">{Constants.NODE_TYPE}</small>
        <br />
        <div className="selectbox">
          <Select
            options={options}
            placeholder={placeholder}
            isSearchable={isSearchable}
            defaultValue={options[0]}
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


  render() {
    const {
      loadingModels, models, projectId,
      isAnyDatasetTransformed, history, clientId,
      roles,
    } = this.props;
    const { modelsArray, flag, sortArray } = this.state;
    if (!loadingModels && !models) {
      /* TODO: show some error message to user? */
    }
    let ind = modelsArray.length;
    let sortedData = _.sortBy(modelsArray, 'version');
    let value = [];
    if (modelsArray.length > 0 && this.state.currIndex != ind && flag == 0) {
      this.setState({ currIndex: ind, sortArray: sortedData });
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
              <li className="list">
                {' '}
                {this.renderVersions(sortedData)}
                {' '}
              </li>
              <li className="list">{this.renderVersions(sortedData)}</li>
              <li className="list">{this.renderNode()}</li>
            </div>
          )
          : this.renderPlaceholder()}
      </div>
    );
  }
}

NodeAnalytics.propTypes = {
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

export default withRouter(NodeAnalytics);
