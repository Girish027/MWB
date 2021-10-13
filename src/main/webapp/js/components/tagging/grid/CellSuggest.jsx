import React, { Component } from 'react';
import * as actionsTag from 'state/actions/actions_datasets_transformed_tag';
import { connect } from 'react-redux';
import { getLanguage } from 'state/constants/getLanguage';

const lang = getLanguage();

class CellSuggest extends Component {
  constructor(props) {
    super(props);

    this.props = props;
    this.appState = this.props.app;
    this.handleSuggestClick = this.handleSuggestClick.bind(this);
    this.handleKeyPress = this.handleKeyPress.bind(this);
    this.handleSuggestHover = this.handleSuggestHover.bind(this);

    this.handleMoveUp = this.handleMoveUp.bind(this);
    this.handleMoveDown = this.handleMoveDown.bind(this);

    this.suggest = null;
    this.keyboardSelectedIndex = -1;

    this.state = {
      currentSuggestedTag: '',
    };
  }

  componentWillReceiveProps(nextProps) {
    this.props = nextProps;
  }

  componentDidUpdate() {
    if (this.props.tagDatasets.cursor !== null) {
      if (this.props.tagDatasets.cursor.direction === 'up') {
        this.handleMoveUp();
      } else {
        this.handleMoveDown();
      }
    }
  }

  componentWillMount() {
    // document.addEventListener("keypress", this.handleKeyPress.bind(this));
  }

  componentWillUnmount() {
    // document.removeEventListener("keypress", this.handleKeyPress);
  }

  handleSuggestClick(event) {
    event.preventDefault();

    const label = event.target.getAttribute('data-label');
    this.props.selectedSuggestTag(label);
  }

  handleSuggestHover(event) {
    const label = event.target.getAttribute('data-label');
    this.props.updateCellWithSuggestTag(label);
  }

  handleKeyPress(event) {
    let newKeyboardIndex = this.keyboardSelectedIndex;

    if (event.key === 'Up') {
      newKeyboardIndex++;
      if (newKeyboardIndex >= this.props.tagDatasets.suggestTagResult.suggestedTags.length) {
        newKeyboardIndex = this.props.tagDatasets.suggestTagResult.suggestedTags.length - 1;
      }
    }

    if (event.key === 'Down') {
      newKeyboardIndex--;
      if (newKeyboardIndex <= 0) newKeyboardIndex = 0;
    }

    this.keyboardSelectedIndex = newKeyboardIndex;
  }

  handleMoveDown() {
    if (this.props.tagDatasets.suggestTagResult.suggestedTags.length === 0 && this.props.tagDatasets.suggestTagResult.status <= 3) return;

    this.keyboardSelectedIndex++;
    if (this.keyboardSelectedIndex >= this.props.tagDatasets.suggestTagResult.suggestedTags.length) this.keyboardSelectedIndex = 0;

    if (
      this.props.tagDatasets.suggestTagResult.suggestedTags
            && this.props.tagDatasets.suggestTagResult.suggestedTags.length
            && typeof this.props.tagDatasets.suggestTagResult.suggestedTags[this.keyboardSelectedIndex] !== 'undefined'
            && typeof this.props.tagDatasets.suggestTagResult.suggestedTags[this.keyboardSelectedIndex].label !== 'undefined'
    ) {
      const newSuggestedTag = this.props.tagDatasets.suggestTagResult.suggestedTags[this.keyboardSelectedIndex].label;
      this.props.updateCellWithSuggestTag(newSuggestedTag);
      this.setState({
        currentSuggestedTag: newSuggestedTag,
      });
      this.scrollToSuggestedTopic();
    }
  }

  handleMoveUp() {
    if (this.props.tagDatasets.suggestTagResult.suggestedTags.length === 0 && this.props.tagDatasets.suggestTagResult.status <= 3) return;

    this.keyboardSelectedIndex--;
    if (this.keyboardSelectedIndex < 0) this.keyboardSelectedIndex = this.props.tagDatasets.suggestTagResult.suggestedTags.length - 1;
    if (this.keyboardSelectedIndex >= this.props.tagDatasets.suggestTagResult.suggestedTags.length) this.keyboardSelectedIndex = 0;

    if (
      this.props.tagDatasets.suggestTagResult.suggestedTags
            && this.props.tagDatasets.suggestTagResult.suggestedTags.length
            && typeof this.props.tagDatasets.suggestTagResult.suggestedTags[this.keyboardSelectedIndex] !== 'undefined'
            && typeof this.props.tagDatasets.suggestTagResult.suggestedTags[this.keyboardSelectedIndex].label !== 'undefined'
    ) {
      const newSuggestedTag = this.props.tagDatasets.suggestTagResult.suggestedTags[this.keyboardSelectedIndex].label;
      this.props.updateCellWithSuggestTag(newSuggestedTag);
      this.setState({
        currentSuggestedTag: newSuggestedTag,
      });
      this.scrollToSuggestedTopic();
    }
  }

  scrollToSuggestedTopic() {
    const container = document.getElementsByClassName('cellSuggestContainer')[0];
    const currentRow = document.getElementsByClassName('cellSuggest')[this.keyboardSelectedIndex];

    container.scrollTop = currentRow.offsetTop;
  }

  render() {
    const clientRect = (this.props.tagDatasets.activeCell !== null) ? this.props.tagDatasets.activeCell.getBoundingClientRect() : { width: 300 };

    /*
         const cellStyle = {
         "top": this.props.cellPosition.top,
         "left": this.props.cellPosition.left,
         "width": clientRect.width
         } */
    const cellStyle = {
      display: 'block',
      position: 'absolute',
      zIndex: 100,
      width: clientRect.width,
    };

    const tagStateCode = this.props.tagDatasets.suggestTagResult.status;

    let tempCellList = null;
    let cellClass = 'cellSuggest normal';
    switch (tagStateCode) {
    case 1:
      tempCellList = [{ label: lang.LABEL_TAG_NO_MATCHING }, { label: lang.LABEL_TAG_NEW }];
      cellClass = 'cellSuggest warning requiresNoEvents';
      break;
    case 2:
      tempCellList = [{ label: lang.LABEL_TAG_NO_MATCHING }, { label: lang.LABEL_TAG_ERROR_CREATE }];
      cellClass = 'cellSuggest error requiresNoEvents';
      break;
    case 3:
      tempCellList = this.props.tagDatasets.suggestTagResult.suggestedTags;
      cellClass = 'cellSuggest normal active requiresEvents';
      break;
    case 4:
      tempCellList = [{ label: lang.LABEL_TAG_NO_DISPLAY }];
      cellClass = 'cellSuggest normal requiresNoEvents';
      break;
    case 5:
    default:
      tempCellList = [];
      cellClass = 'cellSuggest normal requiresNoEvents';
      break;
    }

    const cells = tempCellList;
    const cellList = [];
    cells.forEach((cell, index) => {
      if (
        this.props.suggestedIntents
                && this.props.suggestedIntents.length
                && this.props.suggestedIntents.indexOf(cell.label) != -1
      ) {
        cellList.unshift(<div
          key={String(`cell${index}`)}
          tabIndex="-1"
          className={`${(tagStateCode === 3 && this.keyboardSelectedIndex === index) ? 'cellSuggest normal activeKeyboardSelected requiresEvents' : cellClass} suggested`}
          data-label={cell.label}
          onMouseOver={this.handleSuggestHover}
          onClick={this.handleSuggestClick}
        >
          {cell.label}
        </div>);
      } else {
        cellList.push(<div
          key={String(`cell${index}`)}
          tabIndex="-1"
          className={(tagStateCode === 3 && this.keyboardSelectedIndex === index) ? 'cellSuggest normal activeKeyboardSelected requiresEvents' : cellClass}
          data-label={cell.label}
          onMouseOver={this.handleSuggestHover}
          onClick={this.handleSuggestClick}
        >
          {cell.label}
        </div>);
      }
    });

    return (
      <div
        ref={(div) => { this.suggest = div; }}
        tabIndex="0"
        className={(tagStateCode === 3) ? 'cellSuggestContainer requiresEvents' : 'cellSuggestContainer requiresNoEvents'}
        style={cellStyle}
        onKeyPress={this.handleKeyPress}
        onMouseDown={(event) => {
          event.preventDefault();
        }}
      >
        { cellList }
      </div>
    );
  }
}

function mapStateToProps(state) {
  return {
    app: state.app,
    header: state.header,
    tagDatasets: state.tagDatasets,
  };
}

const mapDispatchToProps = dispatch => ({
  selectedSuggestTag: (suggestTag) => {
    dispatch(actionsTag.selectedSuggestTag(suggestTag));
  },
  updateCellWithSuggestTag: (suggestTag) => {
    dispatch(actionsTag.updateCellWithSuggestTag(suggestTag));
  },
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(CellSuggest);
