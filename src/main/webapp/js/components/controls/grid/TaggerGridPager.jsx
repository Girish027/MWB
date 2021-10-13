import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Box from 'grommet/components/Box';

export default class TaggerGridPager extends Component {
  constructor(props) {
    super(props);

    this.handleToggleControls = this.handleToggleControls.bind(this);
    this.handleStartIndexChange = this.handleStartIndexChange.bind(this);
    this.handleLimitChange = this.handleLimitChange.bind(this);
    this.onChangeExactPage = this.onChangeExactPage.bind(this);
    this.onChangeResultPerPage = this.onChangeResultPerPage.bind(this);
    this.onClickShowHideSearchControls = this.onClickShowHideSearchControls.bind(this);
    this.onClickNextBtn = this.onClickNextBtn.bind(this);
    this.onClickPrevBtn = this.onClickPrevBtn.bind(this);
  }

  componentWillReceiveProps(nextProps/* , nextState */) {
    this.props = nextProps;
  }

  componentDidMout() {
    const { resultsPerPageOptions } = this.props;
    if (resultsPerPageOptions.indexOf(this.props.limit) == -1) {
      this.handleLimitChange(resultsPerPageOptions[0]);
    }
  }

  handleToggleControls(showControls) {
    const { startIndex, limit, onChange } = this.props;
    if (onChange) {
      onChange({ startIndex, limit, showControls });
    }
  }

  handleLimitChange(limit) {
    const { startIndex, showControls, onChange } = this.props;
    if (onChange) {
      onChange({ startIndex, limit, showControls });
    }
  }

  handleStartIndexChange(startIndex) {
    const { limit, showControls, onChange } = this.props;
    if (onChange) {
      onChange({ startIndex, limit, showControls });
    }
  }

  onChangeExactPage(event) {
    const { limit } = this.props;
    const selectedPage = Number(event.target.value);
    const newStartIndex = (selectedPage - 1) * limit;
    this.handleStartIndexChange(newStartIndex);
  }

  onChangeResultPerPage(event) {
    const limit = Number(event.target.value);
    this.handleLimitChange(limit);
  }

  onClickShowHideSearchControls() {
    const { showControls } = this.props;
    this.handleToggleControls(!showControls);
  }

  onClickNextBtn() {
    const { total, limit, startIndex } = this.props;
    const maxPage = Math.max(Math.floor(total / limit) + (total % limit ? 1 : 0), 1);
    const currentPage = Math.floor(startIndex / limit) + 1;
    if (currentPage < maxPage) {
      this.handleStartIndexChange((currentPage) * limit);
    }
  }

  onClickPrevBtn() {
    const { limit, startIndex } = this.props;
    const currentPage = Math.floor(startIndex / limit) + 1;
    if (currentPage > 1) {
      this.handleStartIndexChange((currentPage - 2) * limit);
    }
  }

  get resultsPerPageOptions() {
    const { resultsPerPageOptions } = this.props;
    const options = [];

    resultsPerPageOptions.forEach((number) => {
      options.push(<option key={`option-${number}`} value={number}>
        {number}
      </option>);
    });

    return options;
  }

  get exactPageOptions() {
    const { total, limit } = this.props;
    let options = [],
      i;
    const maxPage = Math.max(Math.floor(total / limit) + (total % limit ? 1 : 0), 1);

    for (i = 1; i <= maxPage; i++) {
      options.push(<option key={`option-${i}`} value={i}>{i}</option>);
    }

    return options;
  }

  get pagesButtons() {
    const { total, limit, startIndex } = this.props;
    const maxPage = Math.max(Math.floor(total / limit) + (total % limit ? 1 : 0), 1);
    const currentPage = Math.floor(startIndex / limit) + 1;
    let buttons = [],
      i,
      buttonNumbers = [];

    buttons.push(<button
      className="pagerBtn"
      key="btn-prev"
      disabled={currentPage <= 1}
      onClick={this.onClickPrevBtn}
    >
                Prev
    </button>);


    for (i = 1; i <= maxPage; i++) {
      if (
        (i <= 3)
                || (i >= maxPage - 2)
                || (i >= currentPage - 2 && i <= currentPage + 2)
      ) {
        buttonNumbers.push(i);
      }
    }

    let prevPageNumber = 0;
    buttonNumbers.forEach((pageNumber) => {
      if (pageNumber - 1 > prevPageNumber) {
        buttons.push(<span key={`dots-${prevPageNumber}`}>...</span>);
      }
      buttons.push(<button
        className={`pagerBtn${pageNumber == currentPage ? ' active' : ''}`}
        key={`btn-${pageNumber}`}
        onClick={() => {
          this.handleStartIndexChange((pageNumber - 1) * limit);
        }}
      >
        {pageNumber}
      </button>);
      prevPageNumber = pageNumber;
    });

    buttons.push(<button
      className="pagerBtn"
      key="btn-next"
      disabled={currentPage >= maxPage}
      onClick={this.onClickNextBtn}
    >
                Next
    </button>);

    return buttons;
  }


  render() {
    const {
      controls, showControls, limit, startIndex,
    } = this.props;
    const currentPage = Math.floor(startIndex / limit) + 1;

    return (
      <Box
        className="TaggerGridPager"
        flex
        direction="row"
      >
        {controls & TaggerGridPager.CONTROLS.TOGGLE_CONTROLS ? (
          <Box className="TaggerGridPagerSegment">
            <div
              className={`toggleBtn ${!showControls ? 'contractBtn' : 'expandBtn'}`}
              title="Show/Hide Search Controls"
              onClick={this.onClickShowHideSearchControls}
            />
          </Box>
        ) : null}

        {controls & TaggerGridPager.CONTROLS.RESULTS_PER_PAGE ? (
          <Box className="TaggerGridPagerSegment">
            <div className="ResultsPerPage">
                        Results per Page:
              <select
                onChange={this.onChangeResultPerPage}
                value={limit}
              >
                {this.resultsPerPageOptions}
              </select>
            </div>
          </Box>
        ) : null}

        {controls & TaggerGridPager.CONTROLS.CHOOSE_EXACT_PAGE ? (
          <Box className="TaggerGridPagerSegment">
            <div className="ChooseExactPage">
                        Choose Exact Page:
              <select
                onChange={this.onChangeExactPage}
                value={currentPage}
              >
                {this.exactPageOptions}
              </select>
            </div>
          </Box>
        ) : null}

        {controls & TaggerGridPager.CONTROLS.PAGE_BUTTONS ? (
          <Box className="TaggerGridPagerSegment">
            <div className="PagesButtons">
              {this.pagesButtons}
            </div>
          </Box>
        ) : null}
      </Box>
    );
  }
}

TaggerGridPager.CONTROLS = {
  TOGGLE_CONTROLS: 1,
  RESULTS_PER_PAGE: 2,
  CHOOSE_EXACT_PAGE: 4,
  PAGE_BUTTONS: 8,
};

TaggerGridPager.propTypes = {
  total: PropTypes.number,
  startIndex: PropTypes.number,
  limit: PropTypes.number,
  showControls: PropTypes.bool,
  resultsPerPageOptions: PropTypes.array,
  onChange: PropTypes.func,
  controls: PropTypes.number,
};

TaggerGridPager.defaultProps = {
  startIndex: 0,
  limit: 50,
  showControls: true,
  resultsPerPageOptions: [10, 25, 50, 100, 250, 500],
  controls: TaggerGridPager.CONTROLS.TOGGLE_CONTROLS
        + TaggerGridPager.CONTROLS.RESULTS_PER_PAGE
        + TaggerGridPager.CONTROLS.CHOOSE_EXACT_PAGE
        + TaggerGridPager.CONTROLS.PAGE_BUTTONS,
};
