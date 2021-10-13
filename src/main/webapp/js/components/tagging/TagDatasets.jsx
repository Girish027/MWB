import React, { Component } from 'react';
import AnimateOnChange from 'react-animate-on-change';
import { connect } from 'react-redux';
import store from 'state/configureStore';
import _ from 'lodash';
import Model from 'model';
import validationUtil from 'utils/ValidationUtil';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsStats from 'state/actions/actions_datasets_transformed_stats';
import * as actionsSearch from 'state/actions/actions_datasets_transformed_search';
import * as actionsTag from 'state/actions/actions_datasets_transformed_tag';
import * as actionsTagDatasets from 'state/actions/actions_tag_datasets';
import * as actionsCellEditable from 'state/actions/actions_cellEditable';
import * as actionscellEditableManualTagSuggest from 'state/actions/actions_cellEditableManualTagSuggest';
import { setTag, removeTag, setComment } from 'state/actions/actions_datasets_transformed_tag';
import { Actions as GridActions } from 'react-redux-grid';
import Box from 'grommet/components/Box';
import TaggerSearchInput from 'components/controls/TaggerSearchInput';
import DatasetGrid from 'components/tagging/grid/DatasetGrid';
import MultipleCheckboxFilter from 'components/controls/MultipleCheckboxFilter';
import GridPager from 'components/tagging/grid/GridPager';
import CellEditable from 'components/controls/grid/CellEditable';
import CellEditableManualTag from 'components/controls/grid/CellEditableManualTag';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import Constants from 'constants/Constants';
import { getLanguage } from 'state/constants/getLanguage';
import {
  Button,
  Checkbox,
  RadioGroup,
} from '@tfs/ui-components';


const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

class TagDatasets extends Component {
  constructor(props, context) {
    super(props, context);
    this.loadingProject = false;
    this.loadingDatasets = false;
    this.completedDatasetsCount = 0;
    this.datasetsLength = 0;
    // search
    this.handleHelpClick = this.handleHelpClick.bind(this);
    this.onSearchInputChange = this.onSearchInputChange.bind(this);
    this.onSearchInputSearch = this.onSearchInputSearch.bind(this);
    this.handleSearchSubmitClick = this.handleSearchSubmitClick.bind(this);
    this.search = this.search.bind(this);
    // radio - manages a group of radio buttons
    this.handleRadioClick = this.handleRadioClick.bind(this);
    // checkboxes
    this.handleMultipleFilterClick = this.handleMultipleFilterClick.bind(this);
    this.handleWordCountFilterClick = this.handleWordCountFilterClick.bind(this);
    this.handleHasCommentFilterClick = this.handleHasCommentFilterClick.bind(this);
    // select
    this.onGreaterThanChangeHandler = this.onGreaterThanChangeHandler.bind(this);
    // vars
    this.radios = {
      labels: ['All text strings', 'Tagged text strings', 'Untagged text strings'],
      values: ['bothTaggedUntagged', 'tagged', 'untagged'],
      defaultValue: 'tagged',
    };
    this.cellEditableStateKey = 'TagDatasets';

    this.props = props;

    this.initialized = false;
    this.projectIdCleared = false;

    this.state = {
      // radios
      bothTaggedUntagged: false,
      tagged: true,
      untagged: false,
      selectedRadioValue: 'tagged',
      // checkboxes
      hasComment: false,
      wordGreaterThan: false,
      wordCount: 15,
      onlyMultipleTags: false,
      // search
      query: '',
      fetchingStats: false,
      uniqueStatsBar: {
        containerClassName: 'percentBarContainer percentBarBlueBorderContainer',
        barClassNames: 'percentBar percentBarBlue',
      },
      allStatsBar: {
        containerClassName: 'percentBarContainer percentBarBlueBorderContainer',
        barClassNames: 'percentBar percentBarBlue',
      },
    };

    this.buttonStyle = { paddingLeft: '10px', paddingRight: '10px' };
  }

  componentWillReceiveProps(nextProps) {
    const { tagDatasets, isControlsCollapsed, dispatch } = this.props;
    let { projectId } = nextProps;

    if (!tagDatasets.project
      || (tagDatasets.projectId && projectId && tagDatasets.projectId !== projectId)
      || (!tagDatasets.projectId && projectId)
      || (tagDatasets.projectId && !projectId)
    ) {
      this.props = nextProps;
      this.initiate();
    } else {
      this.props = nextProps;
    }

    let forceUpdate = false;
    const datasets = Model.ProjectsManager.getDatasetsByProjectId(projectId);
    if (datasets) {
      const { filter } = this.props.tagDatasets;
      const datasetsFilter = filter.datasets;
      const completedDatasetsIds = [];

      datasets.forEach((d) => {
        if (d.id && d.status && d.status == 'COMPLETED' && d.name && d.isClickable) {
          completedDatasetsIds.push(d.id);
        }
      });

      if (completedDatasetsIds.length != this.completedDatasetsCount) {
        this.completedDatasetsCount = completedDatasetsIds.length;
        forceUpdate = true;
      }

      if (!datasetsFilter.length && completedDatasetsIds.length) {
        forceUpdate = false;
        setTimeout(() => {
          dispatch(actionsTagDatasets.setDatasetsFilter({ datasets: completedDatasetsIds }));
        }, 100);
      }

      if (isControlsCollapsed != this.props.tagDatasets.isControlsCollapsed) {
        forceUpdate = true;
      }
    }

    if (isControlsCollapsed != this.props.tagDatasets.isControlsCollapsed) {
      forceUpdate = true;
    }

    if (forceUpdate) {
      setTimeout(() => {
        this.forceUpdate();
        if (!this.initialized) {
          if (tagDatasets.projectId) {
            this.props.tagDatasets.startIndex = 0;
            this.props.tagDatasets.currentPage = 1;
            this.initialized = true;
            this.search();
          }
        }
      }, 250);
    }
  }

  componentDidMount() {
    const { dispatch } = this.props;
    this.completedDatasetsCount = 0;
    dispatch(actionsCellEditable.stateCreate({ stateKey: this.cellEditableStateKey }));
    dispatch(actionscellEditableManualTagSuggest.stateCreate({ stateKey: this.cellEditableStateKey }));
    this.initiate();

    const {
      tagged, untagged, bothTaggedUntagged, hasComment,
      wordGreaterThan, wordCount, onlyMultipleTags,
    } = this.state;
    const { limit } = this.props.tagDatasets;

    let storedSettings = {};
    try {
      storedSettings = JSON.parse(localStorage.getItem('TagDatasets')) || {};
    } catch (e) { /* do nothing */ }
    storedSettings = Object.assign({
      limit,
      untagged,
      tagged,
      bothTaggedUntagged,
      hasComment,
      wordGreaterThan,
      wordCount,
      onlyMultipleTags,
    }, storedSettings);
    this.setState({ ...storedSettings });
    if (limit !== storedSettings.limit) {
      dispatch(actionsSearch.changeLimit(storedSettings.limit, false));
    }
  }

  componentWillUnmount() {
    const { dispatch } = this.props;
    dispatch(actionsTagDatasets.cleanUp());
    dispatch(actionsCellEditable.stateRemove({ stateKey: this.cellEditableStateKey }));
    dispatch(actionscellEditableManualTagSuggest.stateRemove({ stateKey: this.cellEditableStateKey }));
    dispatch(GridActions.GridActions.setData({ data: [], stateKey: 'workbench' }));
  }

  initiate() {
    const {
      tagDatasets, projectId, clientDataLoaded, dispatch,
    } = this.props;
    if (!_.isNil(projectId) && clientDataLoaded
      && !_.isNil(tagDatasets)) {
      if (projectId !== tagDatasets.projectId) {
        this.fetchingStats = true;
        dispatch(actionsTagDatasets.setProjectId(projectId));
        if (tagDatasets.filter.datasets && tagDatasets.filter.datasets.length) {
          this.props.fetchDatasetStats({ projectId, filter: { datasets: tagDatasets.filter.datasets } })
            .finally(() => {
              this.fetchingStats = false;
            });
          this.highliteStats();
        }
      }

      const project = Model.ProjectsManager.getProject(projectId);
      if (typeof project !== 'undefined') {
        if (!tagDatasets.project || tagDatasets.project.id != project.id) {
          dispatch(actionsTagDatasets.setProject({ projectId, project }));
        }
      }

      const datasets = Model.ProjectsManager.getDatasetsByProjectId(projectId);
      if (typeof datasets !== 'undefined') {
        dispatch(actionsTagDatasets.setDatasets({ projectId, datasets }));
      }
    } else if (!this.projectIdCleared) {
      dispatch(actionsTagDatasets.setProjectId(null));
      this.projectIdCleared = true;
    }
  }

  componentDidUpdate(/* prevProps, prevState */) {
    const { tagDatasets } = this.props;
    const { projectId } = tagDatasets;

    if (_.isNil(tagDatasets) || _.isNil(projectId)) {
      return;
    }

    if (tagDatasets.refreshStats && !this.fetchingStats) {
      this.fetchingStats = true;
      setTimeout(() => {
        this.props.fetchDatasetStats({ projectId, filter: { datasets: tagDatasets.filter.datasets } })
          .finally(() => {
            this.fetchingStats = false;
          });
        this.highliteStats();
      }, 500);
    }

    if (tagDatasets.pageSearch && tagDatasets.projectId) {
      tagDatasets.pageSearch = false;
      this.search();
    }
  }

  onSearchInputChange(searchValue) {
    this.setState({ query: searchValue });
  }

  onSearchInputSearch(searchValue) {
    this.setState({ query: searchValue });
    setTimeout(() => {
      this.handleSearchSubmitClick();
    }, 100);
  }

  handleHelpClick(event) {
    event.preventDefault();
    const helpPath = Constants.HELP.SEARCH;
    window.open(helpPath, '_blank');
  }

  handleRadioClick(selectedRadio) {
    const newState = {
      bothTaggedUntagged: false,
      tagged: false,
      untagged: false,
      [selectedRadio]: true,
      selectedRadioValue: selectedRadio,
    };
    this.setState(newState, () => {
      let storedSettings = {};
      try {
        storedSettings = JSON.parse(localStorage.getItem('TagDatasets')) || {};
      } catch (e) { /* do nothing */ }
      localStorage.setItem('TagDatasets', JSON.stringify(Object.assign(storedSettings, newState)));
    });
    if (this.props.tagDatasets.bulkTagMode) {
      this.props.hideBulkTagCell();
    }
  }

  handleMultipleFilterClick() {
    const newState = !this.state.onlyMultipleTags;
    this.setState({ onlyMultipleTags: newState });

    let storedSettings = {};
    try { storedSettings = JSON.parse(localStorage.getItem('TagDatasets')) || {}; } catch (e) { /* do nothing */ }
    localStorage.setItem('TagDatasets', JSON.stringify(Object.assign(storedSettings, { onlyMultipleTags: newState })));

    if (this.props.tagDatasets.bulkTagMode) this.props.hideBulkTagCell();
  }

  handleWordCountFilterClick() {
    const newState = !this.state.wordGreaterThan;
    this.setState({ wordGreaterThan: newState });

    let storedSettings = {};
    try { storedSettings = JSON.parse(localStorage.getItem('TagDatasets')) || {}; } catch (e) { /* do nothing */ }
    localStorage.setItem('TagDatasets', JSON.stringify(Object.assign(storedSettings, { wordGreaterThan: newState })));

    if (this.props.tagDatasets.bulkTagMode) this.props.hideBulkTagCell();
  }

  handleHasCommentFilterClick() {
    const newState = !this.state.hasComment;
    this.setState({ hasComment: newState });

    let storedSettings = {};
    try { storedSettings = JSON.parse(localStorage.getItem('TagDatasets')) || {}; } catch (e) { /* do nothing */ }
    localStorage.setItem('TagDatasets', JSON.stringify(Object.assign(storedSettings, { hasComment: newState })));

    if (this.props.tagDatasets.bulkTagMode) this.props.hideBulkTagCell();
  }

  onGreaterThanChangeHandler(event) {
    event.preventDefault();
    const wordCount = event.target.value;
    this.setState({ wordCount });

    let storedSettings = {};
    try { storedSettings = JSON.parse(localStorage.getItem('TagDatasets')) || {}; } catch (e) { /* do nothing */ }
    localStorage.setItem('TagDatasets', JSON.stringify(Object.assign(storedSettings, { wordCount })));

    if (this.props.tagDatasets.bulkTagMode) this.props.hideBulkTagCell();
  }

  handleSearchSubmitClick() {
    this.props.tagDatasets.startIndex = 0;
    this.props.tagDatasets.currentPage = 1;
    this.search();
  }

  highliteStats() {
    setTimeout(() => {
      this.setState({
        uniqueStatsBar: {
          containerClassName: 'percentBarContainer percentBarGreyBorderContainer',
          barClassNames: 'percentBar percentBarGrey',
        },
        allStatsBar: {
          containerClassName: 'percentBarContainer percentBarGreyBorderContainer',
          barClassNames: 'percentBar percentBarGrey',
        },
      });
    }, 3000);

    this.setState({
      uniqueStatsBar: {
        containerClassName: 'percentBarContainer percentBarBlueBorderContainer',
        barClassNames: 'percentBar percentBarBlue',
      },
      allStatsBar: {
        containerClassName: 'percentBarContainer percentBarBlueBorderContainer',
        barClassNames: 'percentBar percentBarBlue',
      },
    });
  }

  search() {
    const { tagDatasets } = this.props;
    const { projectId } = tagDatasets;

    if (_.isNil(tagDatasets) || _.isNil(projectId)) {
      return;
    }

    const sort = tagDatasets.sort;
    const query = this.state.query;
    // if onlyMultipleTags is unchecked, don"t send anything about it in the filter
    // https://247inc.atlassian.net/wiki/display/APT/APIs
    const filter = (this.state.onlyMultipleTags) ? {
      tagged: this.state.tagged,
      untagged: this.state.untagged,
      autoTagCountRange: {
        min: 2,
      },
    } : {
      tagged: this.state.tagged,
      untagged: this.state.untagged,
    };
    filter.datasets = tagDatasets.filter.datasets;

    if (this.state.wordGreaterThan) {
      filter.wordCountRange = {
        min: this.state.wordCount,
      };
    }
    if (this.state.hasComment) {
      filter.hasComment = true;
    }

    const startIndex = tagDatasets.startIndex;
    const limit = tagDatasets.limit;

    tagDatasets.query = query;
    tagDatasets.filter = filter;

    this.props.fetchDatasetSearch({
      query, filter, projectId, startIndex, limit, sort,
    });
    this.props.fetchDatasetStats({ projectId, filter: { datasets: filter.datasets } })
      .finally(() => {
        this.fetchingStats = false;
      });
  }

  render() {
    const {
      tagDatasets, userFeatureConfiguration, dispatch,
    } = this.props;
    const {
      projectId, datasetsLoaded, project, filter, isControlsCollapsed,
    } = tagDatasets;
    const { searchEl, datasetsFilterEl, statsEl } = this.refs;
    const datasets = Model.ProjectsManager.getDatasetsByProjectId(projectId);

    if (!datasetsLoaded) {
      /* loading data */
      return (
        <div className="openDatasetsContainer">
          <div className="openDatasetProgress">
            <div className="progressContainer" />
          </div>
        </div>
      );
    }

    if (!project) {
      // TODO: styles
      return (
        <div className="openDatasetsContainer">
          <div className="openDatasetProgress">
            <div className="progressContainer">
              <p>Requested project is not found.</p>
            </div>
          </div>
        </div>
      );
    }

    if (!datasets) {
      // TODO: styles
      return (
        <div className="openDatasetsContainer">
          <div className="openDatasetProgress">
            <div className="progressContainer">
              <p>Requested project does not have transformed datasets.</p>
            </div>
          </div>
        </div>
      );
    }

    const uniqueIntents = tagDatasets.statsResults.intents;
    const all = tagDatasets.statsResults.all;
    const unique = tagDatasets.statsResults.unique;

    // checkboxes
    const onlyMultipleTagsCheckbox = {
      checked: this.state.onlyMultipleTags,
      label: 'Multiple suggested categories',
      value: false,
      onChange: this.handleMultipleFilterClick,
    };
    const hasCommentCheckbox = {
      checked: this.state.hasComment,
      label: 'Has comment',
      value: false,
      onChange: this.handleHasCommentFilterClick,
    };
    const wordGreaterThanCheckbox = {
      checked: this.state.wordGreaterThan,
      label: 'Word count greater or equal',
      value: false,
      onChange: this.handleWordCountFilterClick,
    };

    // the bar container is 200 px wide
    const getBarWidth = percent => String(`${Number(Math.floor(percent) * 2)}px`);

    const uniqueBarStyle = { width: getBarWidth(unique.percent) };
    const allBarStyle = { width: getBarWidth(all.percent) };

    // let gridPaddingTop = 50; /* 50px - fixed header height */
    let gridPaddingTop = 1;
    if (!isControlsCollapsed) {
      if (searchEl) {
        gridPaddingTop += searchEl.offsetHeight;
      }
      if (datasetsFilterEl) {
        gridPaddingTop += datasetsFilterEl.offsetHeight;
      }
      if (statsEl) {
        gridPaddingTop += statsEl.offsetHeight;
      }
      gridPaddingTop -= 2;
    }

    const datasetsOptions = [];
    datasets.forEach((d) => {
      if (d.id && d.status && d.status == 'COMPLETED' && d.name && d.isClickable) {
        datasetsOptions.push({ value: d.id, label: d.name });
      }
    });

    return (
      <div className="openDatasetsContainer" id="TagDataset">
        {isControlsCollapsed ? ''
          : (
            <div className="openDatasetProgress" style={{ display: isControlsCollapsed ? 'none' : 'block' }}>
              <div className="progressContainer">
                <div className="ribbon searchFilterContainer oddRow" ref="searchEl">
                  <Box className="searchContainer" direction="row" flex>

                    <Box className="searchFieldContainer">
                      <div className="exampleSearch">
                        <div className="searchInputContainer">
                          <TaggerSearchInput
                            placeholder="Search"
                            onChange={this.onSearchInputChange}
                            onSearch={this.onSearchInputSearch}
                          />
                        </div>
                        { isFeatureEnabled(featureFlagDefinitions.names.tagSearchHelp, userFeatureConfiguration)
                          ? (<div className="searchHelpContainer" onClick={this.handleHelpClick} />)
                          : (<div />) }
                        <span className="booleanOperators">
                          <span className="boolean">Boolean operators:</span>
                          {' '}
                        AND,  OR,  NOT,  AND NOT
                        </span>
                      </div>
                    </Box>

                    <Box className="filtersContainer" flex>
                      <div className="radiosContainer">
                        {/* RADIO BUTTONS */}
                        <div className="radioColContainer col-md-4">
                          <RadioGroup
                            displayType="stacked"
                            onChange={this.handleRadioClick}
                            values={this.radios.values}
                            labels={this.radios.labels}
                            value={this.state.selectedRadioValue}
                          />
                        </div>
                        {/* CHECKBOXES */}
                        <div className="checkboxColContainer col-md-4">
                          <div className="checkboxContainer">
                            <Checkbox {...onlyMultipleTagsCheckbox} />
                          </div>
                          <div className="checkboxContainer">
                            <Checkbox {...wordGreaterThanCheckbox} />
                          &nbsp;&nbsp;
                            <div className="selectContainer">
                              <select onChange={this.onGreaterThanChangeHandler} value={this.state.wordCount}>
                                <option value="1">1</option>
                                <option value="2">2</option>
                                <option value="3">3</option>
                                <option value="4">4</option>
                                <option value="5">5</option>
                                <option value="6">6</option>
                                <option value="7">7</option>
                                <option value="8">8</option>
                                <option value="9">9</option>
                                <option value="10">10</option>
                                <option value="11">11</option>
                                <option value="12">12</option>
                                <option value="13">13</option>
                                <option value="14">14</option>
                                <option value="15">15</option>
                              </select>
                            </div>
                          </div>
                          <div className="checkboxContainer">
                            <Checkbox {...hasCommentCheckbox} />
                          </div>
                        </div>
                        {/* APPLY FILTERS */}
                        <div className="checkboxColContainer col-md-4">
                          <div className="applyFiltersColContainer">
                            <Button
                              type="primary"
                              name="submit"
                              styleOverride={this.buttonStyle}
                              onClick={this.handleSearchSubmitClick}
                            >
                            APPLY FILTERS
                            </Button>
                          </div>
                        </div>
                      </div>
                    </Box>
                  </Box>
                </div>

                <div
                  className="ribbon datasetsFilterContainer oddRow"
                  ref="datasetsFilterEl"
                  style={{
                    overflowY: 'scroll', alignItems: 'flex-start', maxHeight: '100px', marginRight: '5px',
                  }}
                >
                  <MultipleCheckboxFilter
                    label="Datasets Filter:"
                    className="DatasetsFilter"
                    options={datasetsOptions}
                    value={filter.datasets}
                    onChange={(newValue) => {
                      dispatch(actionsTagDatasets.setDatasetsFilter({ datasets: newValue }));
                    }}
                    allowEmpty={false}
                  />
                </div>

                <div className="ribbon statsContainer oddRow" ref="statsEl">
                  <div className="stat">
                    <div className="statLabel">
                      <p>
                      Total:&nbsp;
                        <span className="nums">
                          {uniqueIntents}
                          {' '}
                        tags
                        </span>
                      </p>
                    </div>
                  </div>
                  <div className="stat bar">
                    <div className="statLabel">
                      <p>
                      Unique Text Strings:&nbsp;
                        <AnimateOnChange
                          baseClassName="nums"
                          animationClassName="animateColor"
                          animate={tagDatasets.refreshStats === true}
                        >
                          {unique.tagged.toLocaleString()}
                          {' '}
                        /
                          {unique.total.toLocaleString()}
                        </AnimateOnChange>
                        <span className="nums">
                        &nbsp;(
                          {Number(unique.percent).toFixed(2)}
                        % tagged)
                        </span>
                      </p>
                    </div>
                    <div className={this.state.uniqueStatsBar.containerClassName}>
                      <div className={this.state.uniqueStatsBar.barClassNames} style={uniqueBarStyle} />
                    </div>
                  </div>
                  <div className="stat bar">
                    <div className="statLabel">
                      <p>
                      All Text Strings:&nbsp;
                        <AnimateOnChange
                          baseClassName="nums"
                          animationClassName="animateColor"
                          animate={tagDatasets.refreshStats === true}
                        >
                          {all.tagged.toLocaleString()}
                          {' '}
                        /
                          {all.total.toLocaleString()}
                        </AnimateOnChange>
                        <span className="nums">
                        &nbsp;(
                          {Number(all.percent).toFixed(2)}
                        % tagged)
                        </span>
                      </p>
                    </div>
                    <div className={this.state.allStatsBar.containerClassName}>
                      <div className={this.state.allStatsBar.barClassNames} style={allBarStyle} />
                    </div>
                  </div>
                </div>

              </div>
            </div>
          )}
        <DatasetGrid paddingTop={gridPaddingTop} config={this.gridConfig} />
      </div>
    );
  }

  get gridConfig() {
    const { dispatch } = this.props;
    const stateKey = 'workbench';

    const pageSize = 100;

    const height = '';

    const useStyles = false;

    const events = {
    };

    const dataSource = function getData(
      arg1,
      arg2,
      arg3,
    ) {
      const state = store.getState();
      const tagDatasets = state.tagDatasets;
      const sort = {
        ...tagDatasets.sort,
        direction: arg3.sort.direction,
        sortDirection: arg3.sort.direction,
        sortKey: arg3.sort.property,
      };

      return new Promise(() => dispatch(actionsSearch.fetchDatasetSearch({
        query: tagDatasets.query,
        filter: tagDatasets.filter,
        projectId: tagDatasets.projectId,
        startIndex: tagDatasets.startIndex,
        limit: tagDatasets.limit,
        sort,
      })));
    };

    const columns = [
      {
        name: 'Count',
        width: '6%',
        className: 'additional-class',
        dataIndex: 'count',
        sortable: true,
      },
      {
        name: 'Datasets',
        width: '20%',
        className: 'additional-class',
        dataIndex: 'datasetIds',
        sortable: false,
        hideable: true,
        renderer: (cellData) => {
          // {column, value, row, key, index};
          const state = store.getState();
          const { tagDatasets } = state;
          const { datasets } = tagDatasets;
          const { row } = cellData;

          const datasetNames = [];
          if (row.datasetIds && row.datasetIds.length) {
            row.datasetIds.forEach((id) => {
              const d = datasets.get(id);
              if (d && d.name) {
                datasetNames.push(d.name);
              }
            });
          }

          return (
            <span>{datasetNames.join(', ')}</span>
          );
        },
      },
      {
        name: 'Unique Text String',
        width: '20%',
        dataIndex: 'uniqueTextString',
        className: 'additional-class',
        sortable: true,
        hideable: false,
      },
      {
        name: 'Suggested Category',
        width: '15%',
        dataIndex: 'suggestedTag',
        className: 'additional-class',
        sortable: true,
      },
      {
        name: 'Granular Intent',
        dataIndex: 'manualTag',
        width: '15%',
        className: 'additional-class',
        sortable: true,
        renderer: ({ column, value, row }) => {
          const state = store.getState();
          const { tagDatasets } = state;
          const { projectId } = tagDatasets;

          return (
            <CellEditableManualTag
              stateKey="TagDatasets"
              value={row.manualTag || ''}
              rowIndex={row.index}
              columnIndex={0}
              maxRowIndex={tagDatasets.searchResults ? tagDatasets.searchResults.length - 1 : 0}
              maxColumnIndex={1}
              stopClickPropagation
              projectId={tagDatasets.projectId}
              activateNextCellOnEnter
              onChange={(newValue) => {
                const oldValue = row.manualTag || '';
                dispatch(GridActions.EditorActions.updateRow({
                  stateKey: 'workbench', rowIndex: row.index, values: { manualTag: newValue },
                }));

                if (newValue.length) {
                  dispatch(setTag({
                    projectId,
                    hashList: [row.transcriptionHash],
                    intent: newValue,
                    isUpdate: oldValue.length > 0,
                  }))
                    .then(() => {
                      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.tagUpdated));
                    })
                    .catch(() => {
                      dispatch(GridActions.EditorActions.updateRow({
                        stateKey: 'workbench', rowIndex: row.index, values: { manualTag: oldValue },
                      }));
                    });
                } else {
                  dispatch(removeTag({
                    projectId,
                    hashList: [row.transcriptionHash],
                  }))
                    .then(() => {
                      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.tagRemoved));
                    })
                    .catch(() => {
                      dispatch(GridActions.EditorActions.updateRow({
                        stateKey: 'workbench', rowIndex: row.index, values: { manualTag: oldValue },
                      }));
                    });
                }
              }}
            />
          );
        },
      }, {
        name: 'Rollup Intent',
        width: '10%',
        dataIndex: 'rutag',
        className: 'additional-class',
        sortable: true,
      },
      {
        name: 'Comments',
        dataIndex: 'comment',
        width: '14%',
        className: 'additional-class',
        sortable: true,
        renderer: ({ column, value, row }) => {
          const state = store.getState();
          const { tagDatasets } = state;
          const { projectId } = tagDatasets;

          return (
            <CellEditable
              stateKey="TagDatasets"
              value={row.comment || ''}
              rowIndex={row.index}
              columnIndex={1}
              maxRowIndex={tagDatasets.searchResults ? tagDatasets.searchResults.length - 1 : 0}
              maxColumnIndex={1}
              stopClickPropagation
              activateNextCellOnEnter
              validation={validationUtil.validateTranscriptionHashComment}
              onChange={(newValue) => {
                const oldValue = row.comment || '';
                dispatch(GridActions.EditorActions.updateRow({
                  stateKey: 'workbench', rowIndex: row.index, values: { comment: newValue },
                }));

                if (newValue.length) {
                  dispatch(setComment({
                    projectId,
                    hashList: [row.transcriptionHash],
                    comment: newValue,
                  }))
                    .then(() => {
                      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.commentUpdated));
                    })
                    .catch(() => {
                      dispatch(GridActions.EditorActions.updateRow({
                        stateKey: 'workbench', rowIndex: row.index, values: { comment: oldValue },
                      }));
                    });
                } else {
                  dispatch(setComment({
                    comment: '',
                    projectId,
                    hashList: [row.transcriptionHash],
                  }))
                    .then(() => {
                      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.commentRemoved));
                    })
                    .catch(() => {
                      dispatch(GridActions.EditorActions.updateRow({
                        stateKey: 'workbench', rowIndex: row.index, values: { comment: oldValue },
                      }));
                    });
                }
              }}
            />
          );
        },
      },
    ];

    const plugins = {
      SELECTION_MODEL: {
        mode: 'checkbox-multi',
        enabled: true,
        allowDeselect: true,
        activeCls: 'false',
        selectionEvent: 'doubleclick',
      },
      COLUMN_MANAGER: {
        resizable: true,
        moveable: true,
        sortable: {
          enabled: true,
          method: 'remote',
          sortingSource: dataSource,
        },
      },
      STICKY_HEADER: {
        enabled: true,
        scrollTarget: '.react-grid-table-container',
      },
      STICKY_FOOTER: {
        enabled: false,
        scrollTarget: '.react-grid-table-container',
      },
      PAGER: {
        enabled: true,
        pagingType: 'remote',
        pagerComponent: <GridPager />,
      },
      BULK_ACTIONS: {
        enabled: true,
      },
      GRID_ACTIONS: {
        iconCls: 'action-icon',
        menu: [],
      },
      ROW: {
        enabled: true,
        renderer: ({ rowProps, cells, row }) => {
          if (row.get('_active_')) {
            rowProps.className += ' react-grid-active';
          }
          return (
            <tr {...rowProps}>
              {cells}
            </tr>
          );
        },
      },
    };

    return {
      stateKey,
      height,
      useStyles,
      columns,
      pageSize,
      events,
      plugins,
    };
  }
}


const mapStateToProps = state => ({
  projectsManager: state.projectsManager, // TODO: improvements
  tagDatasets: state.tagDatasets,
  clientName: state.header.client.name,
  clientId: state.header.client.id,
  projectId: state.projectListSidebar.selectedProjectId,
  userFeatureConfiguration: state.app.userFeatureConfiguration,
  clientDataLoaded: state.projectsManager.clientDataLoaded,
});

const mapDispatchToProps = dispatch => ({
  hideBulkTagCell: () => {
    dispatch(actionsTag.hideBulkTagCell());
  },
  updateQuery: (query) => {
    dispatch(actionsSearch.updateQuery(query));
  },
  updateFilters: (filter) => {
    dispatch(actionsSearch.updateFilters(filter));
  },
  updateColumnSort: (sort) => {
    dispatch(actionsTag.updateColumnSort(sort));
  },
  fetchDatasetSearch: ({
    query, filter, projectId, startIndex, limit, sort,
  }) => dispatch(actionsSearch.fetchDatasetSearch({
    query, filter, projectId, startIndex, limit, sort,
  })),
  fetchSimulateDatasetSearch: (query, filter) => {
    dispatch(actionsSearch.fetchSimulateDatasetSearch(query, filter));
  },
  fetchDatasetStats: ({ projectId, filter }) => dispatch(actionsStats.fetchDatasetStats({ projectId, filter })),
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(TagDatasets);
