import React, { Component } from 'react';
import { connect } from 'react-redux';
import store from 'state/configureStore';
import * as actionsSearch from 'state/actions/actions_datasets_transformed_search';
import * as actionsTagDatasets from 'state/actions/actions_tag_datasets';
import * as _ from 'lodash';

class GridPager extends Component {
  constructor(props) {
    super(props);

    this.handleSetPageChange = this.handleSetPageChange.bind(this);
    this.handleSetLimitChange = this.handleSetLimitChange.bind(this);
    this.handleKeyPress = this.handleKeyPress.bind(this);
    this.handleToggleExpandCollapse = this.handleToggleExpandCollapse.bind(this);
    this.state = { newPage: 1 };
  }

  componentWillReceiveProps(nextProps/* , nextState */) {
    this.props = nextProps;
  }

  componentDidMout() {

  }

  componentDidUpdate() {
  }

  shouldComponentUpdate() {
    return true;
  }

  handleSetPageChange(event) {
    event.preventDefault();
    this.setState({
      newPage: event.target.value,
    });
  }

  handleSetLimitChange(event) {
    event.preventDefault();

    if (this.state.newLimit !== event.target.value) {
      this.setState({
        newLimit: event.target.value,
      }, () => {
        this.props.changeLimit(this.state.newLimit);

        let storedSettings = {};
        try {
          storedSettings = JSON.parse(localStorage.getItem('TagDatasets')) || {};
        } catch (e) { /* do nothing */ }
        storedSettings.limit = this.state.newLimit;
        localStorage.setItem('TagDatasets', JSON.stringify(storedSettings));
      });
    }
  }

  handleKeyPress(event) {
    if (event.key === 'Enter') {
      const newStartIndex = (this.state.newPage * this.props.tagDatasets.limit === 0) ? 0 : this.state.newPage * this.props.tagDatasets.limit + 1;

      if (this.state.newPage !== this.props.tagDatasets.currentPage) {
        this.props.changePage(this.state.newPage, newStartIndex, this.state.newPage);
      }
    }
  }

  handleToggleExpandCollapse() {
    store.dispatch(actionsTagDatasets.setIsControlsCollapsed({ isCollapsed: !this.props.filtersCollapsed }));
  }

  render() {
    let buttons = [];

    const onClick = (event) => {
      // get the Page number from a button with a number
      const btnNum = parseInt(event.target.innerHTML);

      const currentPage = this.props.tagDatasets.currentPage;
      const label = event.target.getAttribute('data-label');
      let newPage = (label === 'next') ? currentPage + 1 : currentPage - 1;
      if (newPage <= 1) newPage = 1;

      const totalPages = Math.ceil(this.props.tagDatasets.total / this.props.tagDatasets.limit);
      if (newPage >= totalPages) newPage = totalPages;

      if (label !== 'prev' && label !== 'next') newPage = btnNum;

      const newStartIndex = (newPage - 1) * this.props.tagDatasets.limit;

      if (newPage !== this.props.tagDatasets.currentPage) {
        this.props.changePage(newPage, newStartIndex, label);
      }
    };

    const nextClass = (this.props.tagDatasets.pageActiveButtonLabel === 'next') ? 'pager-btn-active' : 'pager-btn-inactive';
    const prevClass = (this.props.tagDatasets.pageActiveButtonLabel !== 'next') ? 'pager-btn-active' : 'pager-btn-inactive';

    buttons.push(<button className={prevClass} data-label="prev" key="buttonPrev" onClick={onClick}>Prev</button>);
    buttons.push(<button className={nextClass} data-label="next" key="buttonNext" onClick={onClick}>Next</button>);

    // If the pager needs more buttons to show page numbers...
    const totalPages = Math.ceil(this.props.tagDatasets.total / this.props.tagDatasets.limit);
    const curPageIndex = this.props.tagDatasets.currentPage - 1;

    const drawButton = (i) => {
      buttons.push(<button
        data-label={i + 1}
        key={`button${i}`}
        children={i + 1}
        onClick={onClick}
        className={
          i === curPageIndex
            ? 'pager-btn-active'
            : 'pager-btn-inactive'
        }
      />);
    };

    const buttonLimit = 10;

    if (totalPages <= buttonLimit) { // show all pages
      _.times(totalPages, drawButton);
    } else if (curPageIndex < buttonLimit / 2) { // show first 9 pages + last page
      _.times(buttonLimit - 1, drawButton);
      buttons.push(<div key="buttonEllipsis">...</div>);
      drawButton(totalPages - 1);
    } else if (totalPages - 1 - curPageIndex < buttonLimit / 2) { // show first page + last 9 pages
      drawButton(0);
      buttons.push(<div key="buttonEllipsis">...</div>);
      for (let i = totalPages + 1 - buttonLimit; i < totalPages; i++) {
        drawButton(i);
      }
    } else { // show first page + 7 pages centered on current page + last page
      drawButton(0);
      buttons.push(<div key="buttonEllipsis1">...</div>);
      for (let i = curPageIndex - buttonLimit / 2 + 2; i < curPageIndex + buttonLimit / 2 - 1; i++) {
        drawButton(i);
      }
      buttons.push(<div key="buttonEllipsis2">...</div>);
      drawButton(totalPages - 1);
    }

    if (totalPages === 1) buttons = [];

    const getLimitOptions = () => {
      const curPageLimit = this.props.tagDatasets.limit;

      return (
        <div className="ResultsPerPage">
                    Results per Page:
          <select onChange={this.handleSetLimitChange} value={curPageLimit}>
            <option key="pageLimit25" value="25">25</option>
            <option key="pageLimit50" value="50">50</option>
            <option key="pageLimit100" value="100">100</option>
            <option key="pageLimit500" value="500">500</option>
            <option key="pageLimit1000" value="1000">1000</option>
          </select>
        </div>
      );
    };


    const getToggleExpandCollapseButton = () => (
      <div
        className={`toggleBtn ${this.props.filtersCollapsed ? 'contractBtn' : 'expandBtn'}`}
        title="Show/Hide Search Controls"
        onClick={this.handleToggleExpandCollapse}
      />
    );

    const getExactPageControl = () => {
      if (totalPages < 2) {
        return null;
      }

      const options = [];
      for (let i = 1; i <= totalPages; i++) {
        options.push(<option key={i} value={i}>{i}</option>);
      }

      return (
        <div className="ChooseExactPage">
                    Choose Exact Page:
          <select
            onChange={(event) => {
              event.preventDefault();
              const newPage = Number(event.target.value);
              const newStartIndex = (newPage - 1) * this.props.tagDatasets.limit;
              if (newPage !== this.props.tagDatasets.currentPage) {
                this.props.changePage(newPage, newStartIndex, event.target.value);
              }
            }}
            value={Math.min(this.props.tagDatasets.currentPage, totalPages)}
          >
            {options}
          </select>
        </div>
      );
    };

    return (
      <div className="footerContainer">
        { getToggleExpandCollapseButton() }
        <div className="FooterSegment">
          { getLimitOptions() }
        </div>
        <div className="FooterSegment Last">
          { getExactPageControl() }
        </div>
        { buttons }
      </div>
    );
  }
}

const mapStateToProps = state => ({
  app: state.app,
  tagDatasets: state.tagDatasets,
  filtersCollapsed: state.tagDatasets.isControlsCollapsed,
  header: state.header,
  grid: state.grid,
  current: state.pager.get('custom-pager'),
});

const mapDispatchToProps = dispatch => ({
  changePage: (newPage, newStartIndex, label) => {
    dispatch(actionsSearch.changePage(newPage, newStartIndex, label));
  },
  changeLimit: (newLimit) => {
    dispatch(actionsSearch.changeLimit(newLimit));
  },
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(GridPager);
