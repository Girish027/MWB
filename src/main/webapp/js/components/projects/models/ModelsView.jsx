import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { withRouter } from 'react-router-dom';
import ReadProjectModelsGrid from 'components/projects/models/ReadProjectModelsGrid';
import { changeRoute, modalDialogChange } from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import Constants from 'constants/Constants';
import Placeholder from 'components/controls/Placeholder';
import {
  createNewModel,
} from 'state/actions/actions_models';
import {
  ContextualActionsBar,
  ContextualActionItem,
  DropDown,
  Pencil,
  Button,
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

class ModelsView extends Component {
  constructor(props, context) {
    super(props, context);
    this.props = props;

    this.onClickCreateVersionModel = this.onClickCreateVersionModel.bind(this);
    this.getIndex = this.getIndex.bind(this);
    this.handleFilterChange = this.handleFilterChange.bind(this);

    this.localStorage_key = Constants.LOCALSTORAGE_KEY.MODEL_VIEW;

    let modelsArray = this.props.models ? this.props.models.toArray() : [];
    modelsArray.sort(m => m.updated || m.created);

    modelsArray = this.props.models ? transformedModelsArray(modelsArray, this.props.configs) : [];

    let statusIndex = this.getIndex(this.localStorage_key, modelsArray, Constants.PROJECT_MODELS_TABLE.status.id);
    let userIndex = this.getIndex(this.localStorage_key, modelsArray, Constants.PROJECT_MODELS_TABLE.user.id);

    this.state = {
      modelsArray,
      filtered: [],
      selectedStatusIndex: statusIndex,
      selectedUserIdIndex: userIndex,
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

  componentDidMount() {
    let storedSettings = {};
    try { storedSettings = JSON.parse(localStorage.getItem(this.localStorage_key)) || {}; } catch (e) { /* do nothing */ }
    storedSettings = Object.assign({}, storedSettings);

    let newFiltered = Object.keys(storedSettings).map(i => storedSettings[i]);

    this.setState({
      filtered: newFiltered,
    });
  }

  getIndex(localStorageKey, modelsArray, filteredValue) {
    let storedSettings = {};
    let index = 0;

    if (localStorageKey) {
      try { storedSettings = JSON.parse(localStorage.getItem(localStorageKey)) || {}; } catch (e) { /* do nothing */ }
      storedSettings = Object.assign({}, storedSettings);

      let newFiltered = Object.keys(storedSettings).map(i => storedSettings[i]);

      newFiltered.forEach((filter) => {
        if (filter.id === filteredValue) {
          let arr = tableUtils.getItemList(modelsArray, filteredValue);
          index = arr.indexOf(filter.value);
          return index;
        }
      });
      return index;
    }
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
    const header = 'Select Model Type';
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

  handleFilterChange(filtered, selectedValue, columnId) {
    const filteredState = tableUtils.onFilteredChange(filtered, selectedValue, columnId, this.localStorage_key);
    this.setState({ filtered: filteredState });
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

  render() {
    const {
      loadingModels, models, projectId,
      isAnyDatasetTransformed, history, clientId,
      kibanaLogIndex, kibanaLogURL, userFeatureConfiguration,
      featureFlags, dispatch, roles,
    } = this.props;
    const {
      modelsArray, filtered, selectedUserIdIndex, selectedStatusIndex,
    } = this.state;

    if (!loadingModels && !models) {
      /* TODO: show some error message to user? */
    }

    return (
      <div id="ReadProjectDatasetsDetails" className="models_dataset">
        <div style={actionBar}>
          <ContextualActionsBar styleOverride={actionBar.contextualBar}>
            {/* TODO: need to remove styleOverride props once tfs-ui drop down PR is merged */}
            <ContextualActionItem styleOverride={{ paddingRight: '20px' }} right>

              {modelsArray.length > 0 && (
                <DropDown
                  itemList={tableUtils.getItemList(modelsArray,
                    Constants.PROJECT_MODELS_TABLE.status.id)}
                  labelName="STATUS"
                  selectedIndex={selectedStatusIndex}
                  styleOverride={dropdownRightAlign}
                  onItemSelected={(selectedValue) => {
                    this.handleFilterChange(filtered, selectedValue,
                      Constants.PROJECT_MODELS_TABLE.status.id);
                  }}
                />
              )
              }
            </ContextualActionItem>

            <ContextualActionItem right>
              {modelsArray.length > 0 && (
                <DropDown
                  itemList={tableUtils.getItemList(modelsArray,
                    Constants.PROJECT_MODELS_TABLE.user.id)}
                  labelName="CREATED BY"
                  selectedIndex={selectedUserIdIndex}
                  styleOverride={dropdownRightAlign}
                  onItemSelected={(selectedValue) => {
                    this.handleFilterChange(filtered, selectedValue,
                      Constants.PROJECT_MODELS_TABLE.user.id);
                  }}
                />
              )
              }
            </ContextualActionItem>
          </ContextualActionsBar>
        </div>
        { modelsArray.length
          ? (
            <ReadProjectModelsGrid
              data={modelsArray}
              kibanaLogIndex={kibanaLogIndex}
              kibanaLogURL={kibanaLogURL}
              userFeatureConfiguration={userFeatureConfiguration}
              filtered={filtered}
              featureFlags={featureFlags}
              clientId={clientId}
              roles={roles}
              projectId={projectId}
              history={history}
              dispatch={dispatch}
            />
          )
          : this.renderPlaceholder()}
      </div>
    );
  }
}

ModelsView.propTypes = {
  loadingModels: PropTypes.bool,
  models: PropTypes.object,
  configs: PropTypes.object,
  projectId: PropTypes.string,
  clientId: PropTypes.string,
  isAnyDatasetTransformed: PropTypes.bool,
  kibanaLogIndex: PropTypes.string,
  kibanaLogURL: PropTypes.string,
  userFeatureConfiguration: PropTypes.object,
  featureFlags: PropTypes.object,
  dispatch: PropTypes.func,
  history: PropTypes.object,
  roles: PropTypes.array,
};

export default withRouter(ModelsView);
