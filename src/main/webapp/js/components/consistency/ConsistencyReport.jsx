import React, { Component } from 'react';
import { connect } from 'react-redux';
import Model from 'model';
import { Button, RadioGroup } from '@tfs/ui-components';
import * as actionsConsistencyReport from 'state/actions/actions_consistency_report';
import * as actionsCellEditable from 'state/actions/actions_cellEditable';
import * as actionscellEditableManualTagSuggest from 'state/actions/actions_cellEditableManualTagSuggest';
import { Actions as GridActions } from 'react-redux-grid';
import TaggerGridPager from 'components/controls/grid/TaggerGridPager';
import MultipleCheckboxFilter from 'components/controls/MultipleCheckboxFilter';
import getIcon, { IconNames } from 'utils/iconHelpers';
import { stateKey } from 'components/consistency/ConsistencyReportGrid';
import ConsistencyReportGrid from 'components/consistency/ConsistencyReportGrid';

export class ConsistencyReport extends Component {
  constructor(props, context) {
    super(props, context);

    this.controlsEl = null;
    this.cellEditableStateKey = 'ConsistencyReport';

    this.performSearch = this.performSearch.bind(this);
    this.onChangeRadioButton = this.onChangeRadioButton.bind(this);
    this.onChangeMultipleCheckboxFilter = this.onChangeMultipleCheckboxFilter.bind(this);
    this.onClickConsistencyReportRun = this.onClickConsistencyReportRun.bind(this);
    this.onChangeTaggerGridPager = this.onChangeTaggerGridPager.bind(this);

    this.completedDatasetsCount = 0;

    this.initialized = false;
    this.radios = {
      labels: ['Show All', 'Show Transcriptions With Different Suggested Tags'],
      values: [false, true],
    };
    this.props = props;
    this.state = {
      loadingProject: false,
      loadingDatasets: false,
      runPressed: false,
      selectedRadioGroupValue: props.consistencyReport.filter.onlyConflicts || false,
    };
  }

  componentWillReceiveProps(nextProps) {
    const { consistencyReport } = this.props;
    const { selectedProjectId, datasets } = nextProps;
    if (
      (consistencyReport.projectId && selectedProjectId && consistencyReport.projectId !== selectedProjectId)
      || (!consistencyReport.projectId && selectedProjectId)
      || (consistencyReport.projectId && !selectedProjectId)
    ) {
      this.props = nextProps;
      this.initiate();
    } else {
      if (consistencyReport.showControls != nextProps.consistencyReport.showControls) {
        setTimeout(() => { this.forceUpdate(); }, 100);
      }
      this.props = nextProps;
      if (this.props.consistencyReport.doSearch) {
        this.performSearch(this.props.consistencyReport.delay);
      }
    }

    if (datasets) {
      const { consistencyReport } = this.props;
      const { filter } = consistencyReport;
      const datasetsFilter = filter.datasets;
      let forceUpdate = false;
      const completedDatasetsIds = [];

      datasets.forEach((d) => {
        if (d.id && d.status && d.status == 'COMPLETED' && d.name) {
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
          this.props.dispatch(actionsConsistencyReport.setFilter({
            datasets: completedDatasetsIds,
          }));
        }, 100);
      }

      if (forceUpdate) {
        setTimeout(() => {
          this.forceUpdate();
          if (!this.initialized) {
            // this.performSearch(this.props.consistencyReport.delay);
            this.setState({
              runPressed: true,
            });
            this.initialized = true;
          }
        }, 100);
      }
    }
  }

  componentDidMount() {
    this.completedDatasetsCount = 0;
    this.setState({
      loadingProject: false,
      loadingDatasets: false,
      runPressed: false,
    });
    const { dispatch } = this.props;
    dispatch(actionsCellEditable.stateCreate({ stateKey: this.cellEditableStateKey }));
    dispatch(actionscellEditableManualTagSuggest.stateCreate({ stateKey: this.cellEditableStateKey }));
    this.initiate();
  }

  initiate() {
    const {
      consistencyReport, datasets, selectedProjectId, dispatch,
    } = this.props;

    if (selectedProjectId) {
      if (selectedProjectId !== consistencyReport.projectId) {
        dispatch(actionsConsistencyReport.setProjectId(selectedProjectId));

        if (datasets && !consistencyReport.filter.datasets.length) {
          const datasetsIds = [];
          datasets.forEach((d) => {
            if (d.id && d.status && d.status == 'COMPLETED' && d.name) {
              datasetsIds.push(d.id);
            }
          });
          dispatch(actionsConsistencyReport.setFilter({
            datasets: datasetsIds,
          }));
        }
      }
    } else {
      dispatch(actionsConsistencyReport.setProjectId(null));
    }
  }

  componentWillUnmount() {
    const { dispatch } = this.props;
    dispatch(actionsConsistencyReport.reset());
    dispatch(GridActions.GridActions.setData({ data: [], stateKey }));
    dispatch(actionsCellEditable.stateRemove({ stateKey: this.cellEditableStateKey }));
    dispatch(actionscellEditableManualTagSuggest.stateRemove({ stateKey: this.cellEditableStateKey }));
  }

  get currentPage() {
    const { startIndex, limit } = this.props.consistencyReport;
    return Math.floor(startIndex / limit) + 1;
  }

  get maxPage() {
    const { total, limit } = this.props.consistencyReport;
    return Math.max(Math.floor(total / limit) + (total % limit ? 1 : 0), 1);
  }

  performSearch(delay) {
    const {
      projectId, query, filter, sort, startIndex, limit,
    } = this.props.consistencyReport;
    const { dispatch } = this.props;
    dispatch(actionsCellEditable.stateReset({ stateKey: this.cellEditableStateKey }));
    dispatch(actionscellEditableManualTagSuggest.stateReset({ stateKey: this.cellEditableStateKey }));
    dispatch(actionsConsistencyReport.requestSearch({
      projectId,
      query,
      filter,
      sort,
      startIndex,
      limit,
      delay,
    })).then(() => {
      dispatch(GridActions.GridActions.setData({ data: this.props.consistencyReport.searchResults, stateKey }));
    });
  }

  onClickConsistencyReportRun() {
    const { runPressed } = this.state;
    if (!runPressed) {
      this.setState({
        runPressed: true,
      });
    } else {
      this.performSearch();
    }
  }

  onChangeRadioButton(selected) {
    let selectedValueInBoolean = false;
    const { dispatch } = this.props;
    if (selected === 'false') {
      selectedValueInBoolean = false;
    } else if (selected === 'true') {
      selectedValueInBoolean = true;
    }
    this.setState({
      selectedRadioGroupValue: selectedValueInBoolean,
    });
    dispatch(actionsConsistencyReport.setFilter({ onlyConflicts: selectedValueInBoolean }));
  }

  onChangeMultipleCheckboxFilter(newValue) {
    const { dispatch } = this.props;
    dispatch(actionsConsistencyReport.setFilter({ datasets: newValue }));
  }

  onChangeTaggerGridPager(startIndex, limit, showControls) {
    const { dispatch } = this.props;
    dispatch(actionsConsistencyReport.setPagerSettings({ startIndex, limit, showControls }));
  }

  render() {
    const {
      consistencyReport, project, datasets,
    } = this.props;
    const {
      filter, isSearching, isError,
      showControls, limit, startIndex,
      total, isUpdatingTags,
    } = consistencyReport;
    const { loadingProject, loadingDatasets, runPressed } = this.state;

    if (loadingProject || loadingDatasets) {
      return (<div id="ConsistencyReport" />);
    }

    if (!project) {
      return (
        <div id="ConsistencyReport">
          <p>Requested project is not found.</p>
        </div>
      );
    }

    if (!datasets || !this.completedDatasetsCount) {
      return (
        <div id="ConsistencyReport">
          <p>Requested project does not have transformed datasets.</p>
        </div>
      );
    }

    let gridPaddingTop = 0;
    if (showControls && this.controlsEl && this.controlsEl.boxContainerRef && this.controlsEl.boxContainerRef.offsetHeight) {
      gridPaddingTop += this.controlsEl.boxContainerRef.offsetHeight;
    }

    const gridMessage = isSearching || isUpdatingTags ? 'Loading...' : (isError ? 'There was error processing your request.' : '');

    const datasetsOptions = [];
    datasets.forEach((d) => {
      if (d.id && d.status && d.status == 'COMPLETED' && d.name) {
        datasetsOptions.push({ value: d.id, label: d.name });
      }
    });

    return (
      <div id="ConsistencyReport">
        {showControls ? (
          <div
            id="ConsistencyReportControls"
            className="TaggerGridControls"
            direction="column"
            ref={(div) => { this.controlsEl = div; }}
          >

            <MultipleCheckboxFilter
              label="Datasets Filter:"
              className="DatasetsFilter"
              options={datasetsOptions}
              value={filter.datasets}
              onChange={this.onChangeMultipleCheckboxFilter}
              allowEmpty={false}
            />
            <div className="searchContainer" direction="row">
              <div
                className="filters"
                direction="column"
              >
                <div className="filtersBlock">
                  <RadioGroup
                    displayType="stacked"
                    onChange={this.onChangeRadioButton}
                    values={this.radios.values}
                    labels={this.radios.labels}
                    value={this.state.selectedRadioGroupValue}
                  />
                </div>
              </div>
              <div className="actions" direction="column">
                <Button
                  name="consistency-run"
                  id="ConsistencyReportRun"
                  onClick={isSearching || isUpdatingTags ? null : this.onClickConsistencyReportRun}
                  icon={getIcon(IconNames.CONSISTENCY)}
                  data-qa="consistency-run"
                >
                  RUN
                </Button>
              </div>
            </div>
          </div>
        ) : null}

        {isSearching || isUpdatingTags || isError ? (
          <div className="GridMessage" style={{ paddingTop: gridPaddingTop }}>
            {gridMessage}
          </div>
        ) : null}

        {runPressed && !isUpdatingTags ? (
          <ConsistencyReportGrid
            paddingTop={gridPaddingTop}
            maxPage={this.maxPage}
            currentPage={this.currentPage}
            total={total}
            onTagsChange={() => { this.performSearch(3000); }}
          />
        ) : null}

        {!isSearching && !isUpdatingTags ? (
          <TaggerGridPager
            onChange={({ startIndex, limit, showControls }) => this.onChangeTaggerGridPager(startIndex, limit, showControls)}
            total={total}
            limit={limit}
            startIndex={startIndex}
            showControls={showControls}
            controls={
              TaggerGridPager.CONTROLS.TOGGLE_CONTROLS
              + (runPressed ? TaggerGridPager.CONTROLS.RESULTS_PER_PAGE : 0)
              + (runPressed ? TaggerGridPager.CONTROLS.CHOOSE_EXACT_PAGE : 0)
              + (runPressed && this.maxPage > 2 ? TaggerGridPager.CONTROLS.PAGE_BUTTONS : 0)
            }
          />
        ) : null}

      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => ({
  consistencyReport: state.consistencyReport,
  projectsManager: state.projectsManager, // TODO: improvements
  selectedProjectId: state.projectListSidebar.selectedProjectId,
  project: Model.ProjectsManager.getProject(state.projectListSidebar.selectedProjectId) || null,
  datasets: Model.ProjectsManager.getDatasetsByProjectId(state.projectListSidebar.selectedProjectId, true) || null,
});

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(ConsistencyReport);
