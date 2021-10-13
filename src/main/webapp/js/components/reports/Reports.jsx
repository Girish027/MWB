import React, { Component } from 'react';
import { connect } from 'react-redux';
import store from 'state/configureStore';
import Model from 'model';
import {
  reset, setProjectId, setFilter, getReportTypes,
} from 'state/actions/actions_reports';

import Box from 'grommet/components/Box';
import Select from 'grommet/components/Select';
import DateTime from 'grommet/components/DateTime';
import MultipleCheckboxFilter from 'components/controls/MultipleCheckboxFilter';

class Reports extends Component {
  constructor(props, context) {
    super(props, context);
    this.props = props;

    this.state = {
      loadingProject: false,
      loadingDatasets: false,
      reportTypesRequested: false,
      loadingReportTypes: false,
      completedDatasetsCount: 0,
    };
  }

  componentWillReceiveProps(nextProps) {
    if (
      (this.props.projectId && nextProps.match.params.projectId && this.props.projectId !== nextProps.match.params.projectId)
            || (!this.props.projectId && nextProps.match.params.projectId)
            || (this.props.projectId && !nextProps.match.params.projectId)
    ) {
      this.props = nextProps;
      this.state.reportTypesRequested = false;
      this.init();
    } else {
      this.props = nextProps;
    }

    const {
      projectId, datasets, filter, reportTypes,
    } = this.props;
    if (datasets && projectId) {
      const datasetsFilter = filter.datasets;
      let forceUpdate = false;
      const completedDatasetsIds = [];

      datasets.forEach((d) => {
        if (d.id && d.status && d.status == 'COMPLETED' && d.name) {
          completedDatasetsIds.push(d.id);
        }
      });

      if (completedDatasetsIds.length != this.state.completedDatasetsCount) {
        this.state.completedDatasetsCount = completedDatasetsIds.length;
        forceUpdate = true;
      }

      if (!datasetsFilter.length && completedDatasetsIds.length) {
        forceUpdate = false;
        setTimeout(() => {
          store.dispatch(setFilter({
            datasets: completedDatasetsIds,
          }));
        }, 100);
      }

      if (completedDatasetsIds.length && !reportTypes.size && !this.state.loadingReportTypes && !this.state.reportTypesRequested) {
        this.state.reportTypesRequested = true;
        this.state.loadingReportTypes = true;
        store.dispatch(getReportTypes({ projectId, datasetId: completedDatasetsIds[0] }))
          .then(() => { this.setState({ loadingReportTypes: false }); })
          .catch(() => { this.setState({ loadingReportTypes: false }); });
      }

      if (forceUpdate) {
        setTimeout(() => {
          this.forceUpdate();
        }, 100);
      }
    }
  }

  componentDidMount() {
    this.state.completedDatasetsCount = 0;
    this.init();
  }

  componentWillUnmount() {
    this.state.reportTypesRequested = false;
    store.dispatch(reset());
  }

  init() {
    let { projectId } = this.props.match.params;
    const { project, datasets } = this.props;

    if (projectId) {
      if (projectId !== this.props.projectId) {
        store.dispatch(setProjectId(projectId));
      }
    } else {
      store.dispatch(setProjectId(null));
    }
  }

  render() {
    const {
      project, datasets, filter, reportTypes, projectId,
    } = this.props;
    const { loadingProject, loadingDatasets, loadingReportTypes } = this.state;

    if (loadingProject || loadingDatasets || loadingReportTypes) {
      return (<div id="TaggerReports" />);
    }

    if (!project) {
      return (
        <div id="TaggerReports">
          <p>Requested project is not found.</p>
        </div>
      );
    }

    if (!datasets || !this.state.completedDatasetsCount) {
      return (
        <div id="TaggerReports">
          <p>Requested project does not have transformed datasets.</p>
        </div>
      );
    }

    const reportTypesOptions = Object.keys(reportTypes.toObject());
    const datasetsOptions = [];
    datasets.forEach((d) => {
      if (d.id && d.status && d.status == 'COMPLETED' && d.name) {
        datasetsOptions.push({ value: d.id, label: d.name });
      }
    });

    /* ToDo: move url to config */
    const reportSettings = reportTypes.get(filter.reportType);
    const { urlTemplate } = reportSettings;

    let hasEntriesFilter = false,
      hasIntervalFilter = false,
      hasTimeRangeFilter = false;
    let url = urlTemplate,
      nextUrl;

    url = url.replace('{DATASET_ID}', encodeURI(`(${filter.datasets.join(' OR ')})`));
    url = url.replace('{PROJECT_ID}', projectId);

    nextUrl = url.replace('{DISPLAY_COUNT}', `${filter.entries}`);
    hasEntriesFilter = nextUrl != url;
    url = nextUrl;

    nextUrl = url.replace('{DISPLAY_INTERVAL}', filter.interval);
    hasIntervalFilter = nextUrl != url;
    url = nextUrl;

    const timeFormat = 'M/D/YYYY HH:mm';
    const timeTo = filter.timeTo || Date.now();
    const timeFrom = filter.timeFrom || timeTo - 1000 * 60 * 60 * 24;
    const timeToDate = new Date(timeTo);
    const timeFromDate = new Date(timeFrom);

    nextUrl = url.replace(/\s*\{TIMERANGE\}/g, encodeURI(`[${timeFrom} TO ${timeTo}]`));
    nextUrl = nextUrl.replace(/\s*\{DATERANGE\}/g, encodeURI(`[${timeFrom} TO ${timeTo}]`));
    hasTimeRangeFilter = nextUrl != url;
    url = nextUrl;

    return (
      <Box
        id="TaggerReports"
        direction="column"
        flex
      >
        <Box
          id="TaggerReportsControls"
          className="TaggerGridControls"
          ref="controls"
          direction="column"
        >

          <Box
            direction="row"
            id="TaggerReportsMainBar"
            className="ControlsRow"
          >
            <Box
              className="RowSegment"
              direction="row"
            >
              <span className="labelLarge">Report Type:</span>
              <Select
                className="TaggerControl ReportType"
                options={reportTypesOptions}
                value={filter.reportType}
                onChange={(newValueObj) => {
                  store.dispatch(setFilter({
                    reportType: newValueObj.value,
                  }));
                }}
              />
            </Box>

            {hasEntriesFilter
              ? (
                <Box
                  className="RowSegment"
                  direction="row"
                >
                  <span className="labelLarge">Entries:</span>
                  <Select
                    className="TaggerControl Entries"
                    options={[10, 25, 50, 100]}
                    value={filter.entries}
                    onChange={(newValueObj) => {
                      store.dispatch(setFilter({
                        entries: newValueObj.value,
                      }));
                    }}
                  />
                </Box>
              )
              : null}

            {hasIntervalFilter
              ? (
                <Box
                  className="RowSegment"
                  direction="row"
                >
                  <span className="labelLarge">Interval:</span>
                  <Select
                    className="TaggerControl Interval"
                    options={['h', '3h', 'd', 'w']}
                    value={filter.interval}
                    onChange={(newValueObj) => {
                      store.dispatch(setFilter({
                        interval: newValueObj.value,
                      }));
                    }}
                  />
                </Box>
              )
              : null}

            {hasTimeRangeFilter
              ? (
                <Box
                  className="RowSegment"
                  direction="row"
                >
                  <span className="labelLarge">Time Range:</span>
                  <DateTime
                    onChange={(val) => {
                      const date = new Date(val);
                      store.dispatch(setFilter({
                        timeFrom: date.getTime(),
                      }));
                    }}
                    value={timeFromDate}
                    format={timeFormat}
                  />
                                to
                  <DateTime
                    onChange={(val) => {
                      const date = new Date(val);
                      store.dispatch(setFilter({
                        timeTo: date.getTime(),
                      }));
                    }}
                    value={timeToDate}
                    format={timeFormat}
                  />
                </Box>
              )
              : null}
          </Box>

          <Box
            direction="row"
            id="TaggerReportsDatasetsFilter"
            className="ControlsRow"
          >
            <MultipleCheckboxFilter
              label="Datasets Filter:"
              className="DatasetsFilter"
              options={datasetsOptions}
              value={filter.datasets}
              onChange={(newValue) => {
                store.dispatch(setFilter({
                  datasets: newValue,
                }));
              }}
              allowEmpty={false}
            />

          </Box>

        </Box>
        <Box
          id="TaggerReportsContainer"
          flex
        >
          <iframe
            src={url}
            frameBorder="0"
            height="700"
            width="1000"
          />
        </Box>
      </Box>
    );
  }
}

const mapStateToProps = (state, ownProps) => ({
  ...state.reports,
  project: Model.ProjectsManager.getProject(ownProps.match.params.projectId) || null,
  datasets: Model.ProjectsManager.getDatasetsByProjectId(ownProps.match.params.projectId, true) || null,
  clientName: state.header.client.name,
});

export default connect(mapStateToProps)(Reports);
