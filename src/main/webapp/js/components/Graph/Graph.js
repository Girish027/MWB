import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Plot from 'react-plotly.js';
import moment from 'moment';

export default class Graph extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.parseData = this.parseData.bind(this);
    this.timeToDate = this.timeToDate.bind(this);
  }

  parseData(data) {
    let startdatesVersions = {};
    let startdatesVolumes = {};
    let datesVolumes = {};
    let latest = {};
    data.forEach((element) => {
      element.Duration.forEach((duration) => {
        let start = new Date(duration.start).getTime();
        // let end = duration['end'] === 'NA' ? selectedTo : Moment(duration['end'])
        startdatesVersions[start] = `V${element.Version}`;
        startdatesVolumes[start] = duration.volume[duration.start];
        // for each date-volume entry
        Object.entries(duration.volume).forEach(([date, volume]) => {
          datesVolumes[new Date(date).getTime()] = volume;
        });
        // updating the latest with in the selected range
        let existing = Object.keys(latest);
        if (existing.length === 0 || start > existing[0]) {
          latest = { [start]: element.Version };
        }
      });
    });
    // get the volume-dates for the latest version deployed in the selected range
    let latestDate = Object.keys(latest)[0];
    Object.keys(datesVolumes).forEach((each) => {
      if (each >= latestDate) {
        latest[each] = datesVolumes[each];
      }
    });

    // sorting based on date
    startdatesVersions = Object.keys(startdatesVersions)
      .sort()
      .reduce((obj, key) => {
        obj[key] = startdatesVersions[key];
        return obj;
      }, {});
    datesVolumes = Object.keys(datesVolumes)
      .sort()
      .reduce((obj, key) => {
        obj[key] = datesVolumes[key];
        return obj;
      }, {});
    startdatesVolumes = Object.keys(startdatesVolumes)
      .sort()
      .reduce((obj, key) => {
        obj[key] = startdatesVolumes[key];
        return obj;
      }, {});
    startdatesVersions = this.timeToDate(startdatesVersions);
    datesVolumes = this.timeToDate(datesVolumes);
    startdatesVolumes = this.timeToDate(startdatesVolumes);
    latest = this.timeToDate(latest);
    return {
      startdatesVersions,
      datesVolumes,
      startdatesVolumes,
      latest,
    };
  }

  timeToDate(timeObject) {
    let dateObject = {};
    Object.keys(timeObject).forEach((key) => {
      dateObject[moment(new Date(Number(key))).format('DD/MM')] = timeObject[key];
    });
    return dateObject;
  }

  render() {
    const { data } = this.props;
    if (!(data && data.length)) {
      return <div>NO DATA TO PLOT</div>;
    }
    const {
      startdatesVersions,
      datesVolumes,
      startdatesVolumes,
      latest,
    } = this.parseData(data);
    return (
      <Plot
        data={[
          {
            x: Object.keys(datesVolumes),
            y: Object.values(datesVolumes),
            type: 'scatter',
            mode: 'lines',
            opacity: 0.2,
            marker: { color: '#4A90E2' },
          },
          {
            x: Object.keys(startdatesVolumes),
            y: Object.values(startdatesVolumes),
            type: 'scatter',
            mode: 'markers+text',
            text: Object.values(startdatesVersions),
            textposition: 'left',
            marker: { color: '#4A4A4A' },
          },
          {
            x: Object.keys(latest),
            y: Object.values(latest),
            type: 'scatter',
            mode: 'lines',
            marker: { color: 'rgb(48,111,190)' },
          },
        ]}
        layout={{
          responsive: true,
          width: '429px',
          height: '129px',
          title: { text: 'Volumes' },
          showlegend: false,
          font: { family: 'Lato', color: '#93ADD3' },
        }}
      />
    );
  }
}

Graph.propTypes = {
  data: PropTypes.array,
};

Graph.defaultProps = {
  data: [],
  // data: [
  //   {
  //     Version: '1',
  //     Duration: [
  //       {
  //         start: '1-Jan-2021',
  //         end: '5-Jan-2021',
  //         volume: {
  //           '1-Jan-2021': 10,
  //           '2-Jan-2021': 15,
  //           '3-Jan-2021': 18,
  //           '4-Jan-2021': 23,
  //           '5-Jan-2021': 26,
  //         },
  //       },
  //       {
  //         start: '15-Jan-2021',
  //         end: '30-Jan-2021',
  //         volume: {
  //           '15-Jan-2021': 31,
  //           '16-Jan-2021': 33,
  //           '17-Jan-2021': 35,
  //           '18-Jan-2021': 37,
  //           '19-Jan-2021': 40,
  //           '20-Jan-2021': 42,
  //           '21-Jan-2021': 49,
  //           '22-Jan-2021': 52,
  //           '23-Jan-2021': 55,
  //           '24-Jan-2021': 60,
  //           '25-Jan-2021': 62,
  //           '26-Jan-2021': 67,
  //           '27-Jan-2021': 71,
  //           '28-Jan-2021': 72,
  //           '29-Jan-2021': 75,
  //           '30-Jan-2021': 80,
  //         },
  //       },
  //     ],
  //   },
  //   {
  //     Version: '2',
  //     Duration: [
  //       {
  //         start: '6-Jan-2021',
  //         end: '10-Jan-2021',
  //         volume: {
  //           '6-Jan-2021': 3,
  //           '7-Jan-2021': 8,
  //           '8-Jan-2021': 10,
  //           '9-Jan-2021': 14,
  //           '10-Jan-2021': 19,
  //         },
  //       },
  //     ],
  //   },
  //   {
  //     Version: '3',
  //     Duration: [
  //       {
  //         start: '11-Jan-2021',
  //         end: '14-Jan-2021',
  //         volume: {
  //           '11-Jan-2021': 2,
  //           '12-Jan-2021': 10,
  //           '13-Jan-2021': 13,
  //           '14-Jan-2021': 18,
  //         },
  //       },
  //     ],
  //   },
  //   {
  //     Version: '4',
  //     Duration: [
  //       {
  //         start: '31-Jan-2021',
  //         end: 'NA',
  //         volume: {
  //           '31-Jan-2021': 7,
  //           '1-Feb-2021': 13,
  //           '2-Feb-2021': 24,
  //           '3-Feb-2021': 28,
  //           '4-Feb-2021': 31,
  //         },
  //       },
  //     ],
  //   },
  // ],
};
