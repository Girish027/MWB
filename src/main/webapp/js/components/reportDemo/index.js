import React, { Component } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import store from 'state/configureStore';
import Model from 'model';
import {
  reset, setProjectId, setFilter, getReportTypes,
} from 'state/actions/actions_reports';

import { Row, Grid, Col } from 'react-flexbox-grid';
import Select from 'grommet/components/Select';
import DateTime from 'grommet/components/DateTime';
import MultipleCheckboxFilter from 'components/controls/MultipleCheckboxFilter';

import {
  XYPlot,
  XAxis,
  YAxis,
  VerticalGridLines,
  HorizontalGridLines,
  VerticalBarSeries,
  Treemap,
  RadialChart,
  Hint,
} from 'react-vis';


import { requestLastImportInfo, requestSearch } from 'state/actions/actions_taggingguide';
import { requestProject } from 'state/actions/actions_projects';
import ShowcaseButton from './showcaseButton';

import './report-demo.scss';

class ReportDemo extends Component {
  constructor(props, context) {
    super(props, context);
    this.props = props;

    this.getIntentData = this.getIntentData.bind(this);
    this.getIntentTreemapData = this.getIntentTreemapData.bind(this);
    this.getPieChartData = this.getPieChartData.bind(this);
    this.init = this.init.bind(this);

    this.state = {
      hoveredNode: false,
      treemapData: this.getIntentTreemapData(),
      useCirclePacking: false,
      loadingProject: false,
      loadingDatasets: false,
      reportTypesRequested: false,
      loadingReportTypes: false,
      completedDatasetsCount: 0,
      value: false,
      plotValue: false,
      treemapValue: false,
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
    const { projectId } = this.props.match.params;
    const sort = {
      direction: 'desc',
      property: 'count',
    };
    store.dispatch(requestProject(projectId));
    store.dispatch(requestLastImportInfo(projectId));
    store.dispatch(requestSearch(projectId, sort));
    this.loadingData = true;
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

  getIntentData() {
    const { intents } = this.props;

    if (_.isNil(intents)) {
      return [];
    }

    const plotData = [];

    intents.forEach((item) => {
      plotData.push({
        x: item.intent,
        y: item.count,
      });
    });

    return plotData;
  }

  getIntentTreemapData() {
    const { intents } = this.props;

    if (_.isNil(intents)) {
      return [];
    }

    const leaves = [];

    intents.forEach((item, index) => {
      leaves.push({
        name: `${item.intent} (${item.count})`,
        size: item.count * 10,
        color: index,
        style: {
          border: 'thin solid orange',
        },
      });
    });

    return {
      title: '',
      color: 1,
      children: leaves,
    };
  }

  getPieChartData() {
    const { intents } = this.props;

    if (_.isNil(intents)) {
      return [];
    }

    const slices = [];
    let total = 0;

    intents.forEach((item, index) => {
      total += item.count;
    });

    intents.forEach((item, index) => {
      slices.push({
        angle: item.count,
        color: index,
        label: item.intent,
        subLabel: item.count,
      });
    });

    return slices;
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

    // Code added for plot demos

    const timeFormat = 'M/D/YYYY HH:mm';
    const timeTo = filter.timeTo || Date.now();
    const timeFrom = filter.timeFrom || timeTo - 1000 * 60 * 60 * 24;
    const timeToDate = new Date(timeTo);
    const timeFromDate = new Date(timeFrom);

    nextUrl = url.replace(/\s*\{TIMERANGE\}/g, encodeURI(`[${timeFrom} TO ${timeTo}]`));
    nextUrl = nextUrl.replace(/\s*\{DATERANGE\}/g, encodeURI(`[${timeFrom} TO ${timeTo}]`));
    hasTimeRangeFilter = nextUrl != url;
    url = nextUrl;

    const {
      hoveredNode, useCirclePacking, value, plotValue, treemapValue,
    } = this.state;
    const treeProps = {
      animation: {
        damping: 9,
        stiffness: 500,
      },
      data: this.state.treemapData,
      // onLeafMouseOver: x => {
      //   console.log(x)
      //   x.x = x.x1,
      //   x.y = x.y1
      //   this.setState({ treemapValue:  x });
      // },
      // onLeafMouseOut: () => {
      //   this.setState({ treemapValue: false })
      // },
      onLeafClick: () => this.setState({ treemapData: this.getIntentTreemapData() }),
      height: 300,
      mode: this.state.useCirclePacking ? 'circlePack' : 'squarify',
      getLabel: x => x.name,
      width: 550,
    };

    return (
      <Grid
        id="TaggerReports"
        fluid
      >
        <Row
          id="TaggerReportsControls"
          className="TaggerGridControls"
        >

          <Row
            id="TaggerReportsMainBar"
            className="ControlsRow"
          >
            <Row
              className="RowSegment"
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
            </Row>

            {hasEntriesFilter
              ? (
                <Row
                  className="RowSegment"
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
                </Row>
              )
              : null}

            {hasIntervalFilter
              ? (
                <Row
                  className="RowSegment"
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
                </Row>
              )
              : null}

            {hasTimeRangeFilter
              ? (
                <Row
                  className="RowSegment"
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
                </Row>
              )
              : null}
          </Row>

          <Row
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

          </Row>

        </Row>
        <Row
          id="TaggerReportsContainer"
          style={{
            marginLeft: 'auto',
            marginRight: 'auto',
            marginBottom: '100px',
            marginTop: '30px',
          }}
        >
          <Col xs={12}>
            <div>
              <XYPlot
                xType="ordinal"
                height={500}
                width={800}
              >
                <XAxis tickLabelAngle={-45} />
                <VerticalGridLines />
                <HorizontalGridLines />
                <YAxis />
                <VerticalBarSeries
                  className="vertical-bar-series-example"
                  data={this.getIntentData()}
                // onValueMouseOver={v => {
                //   this.setState({
                //     plotValue: {
                //       intent: v.x,
                //       count: v.y,
                //       y: v.y,
                //       x: 20
                //     }
                //   });
                // }}
                // onSeriesMouseOut={v => this.setState({ plotValue: false })}
                />
              </XYPlot>
              {plotValue && (
                <Hint value={
                  { ...plotValue }
                }
                />
              )}
            </div>
          </Col>
        </Row>
        <Row
          style={{
            marginLeft: 'auto',
            marginRight: 'auto',
            marginBottom: '20px',
          }}
        >
          <Col xs={12}>
            <div className="dynamic-treemap-example">
              <ShowcaseButton
                onClick={() => this.setState({ useCirclePacking: !useCirclePacking })}
                buttonContent="TOGGLE CIRCLE PACK"
              />
              <Treemap
                {...treeProps}
              />
              {treemapValue && <Hint value={treemapValue} />}
            </div>
          </Col>
        </Row>

        <Row>
          <Col xs={12}>
            <RadialChart
              showLabels
              className="donut-chart-example"
              innerRadius={0}
              radius={200}
              getAngle={d => d.angle}
              data={this.getPieChartData()}
              onValueMouseOver={(v) => {
                this.setState({
                  value: {
                    intent: v.label,
                    count: v.subLabel,
                    x: v.x,
                    y: v.y,
                  },
                });
              }}
              onSeriesMouseOut={v => this.setState({ value: false })}
              width={400}
              height={400}
              padAngle={0.04}
            >
              {value && <Hint value={value} />}
            </RadialChart>
          </Col>
        </Row>
      </Grid>
    );
  }
}

const mapStateToProps = (state, ownProps) => ({
  ...state.reports,
  project: Model.ProjectsManager.getProject(ownProps.match.params.projectId) || null,
  datasets: Model.ProjectsManager.getDatasetsByProjectId(ownProps.match.params.projectId, true) || null,
  clientName: state.header.client.name,
  intents: Model.ProjectsManager.getIntentsByProjectId(ownProps.match.params.projectId, true) || null,
  csrfToken: state.app.csrfToken,
});

export default connect(mapStateToProps)(ReportDemo);
