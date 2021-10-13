import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import store from 'state/configureStore';
import validationUtil from 'utils/ValidationUtil';
import * as actionsCellEditableManualTagSuggest from 'state/actions/actions_cellEditableManualTagSuggest';
import * as actionsCellEditable from 'state/actions/actions_cellEditable';

export class CellEditableManualTagSuggest extends Component {
  constructor(props, context) {
    super(props, context);

    this.handleKeyPress = this.handleKeyPress.bind(this);
    this.handleSuggestHover = this.handleSuggestHover.bind(this);

    this.isValid = true;

    this.state = {
      keyboardSelectedIndex: -1,
      cursor: this.props.suggestions.cursor,
    };
  }

  componentWillReceiveProps(nextProps) {
    const prevIntent = this.props.intent;
    this.props = nextProps;

    const { intent, stateKey, projectId } = this.props;
    this.isValid = validationUtil.validatePartialTaggingGuideTag(intent);

    if (this.isValid && intent.length > 0 && prevIntent != intent) {
      store.dispatch(actionsCellEditableManualTagSuggest.getSuggestedTags({
        stateKey, projectId, intent,
      }));
    }

    if (this.props.suggestions.cursor != this.state.cursor) {
      this.moveSelection();
    }
  }

  moveSelection = () => {
    const { stateKey } = this.props;
    const { cursor: updatedCursor, suggestedTags } = this.props.suggestions;
    const { keyboardSelectedIndex, cursor: previousCursor } = this.state;

    if (!suggestedTags.length) {
      return;
    }

    let newIndex = keyboardSelectedIndex;
    if (updatedCursor > previousCursor) {
      newIndex += 1;
      if (newIndex >= suggestedTags.length) {
        newIndex = 0; // first of the list
      }
    } else {
      newIndex -= 1;
      if (newIndex === -1) {
        newIndex = suggestedTags.length - 1; // last of the list
      }
    }


    const container = document.getElementsByClassName('SuggestContainer')[0];
    const currentRow = document.getElementsByClassName('Suggest')[newIndex];
    if (currentRow) {
      const tag = currentRow.innerText;

      store.dispatch(actionsCellEditable.updateValue({
        stateKey,
        value: tag,
      }));

      container.scrollTop = currentRow.offsetTop;
    }

    this.setState({
      cursor: updatedCursor,
      keyboardSelectedIndex: newIndex,
    });
  }

  handleSuggestHover(event) {
    const { stateKey } = this.props;
    const tag = event.target.getAttribute('data-label');

    store.dispatch(actionsCellEditable.updateValue({
      stateKey,
      value: tag,
    }));
  }

  handleKeyPress(event) {
    const { suggestedTags } = this.props.suggestions;
    if (!suggestedTags.length) {
      return;
    }
    let { keyboardSelectedIndex } = this.state;

    if (event.key === 'Up') {
      keyboardSelectedIndex--;
      if (keyboardSelectedIndex < 0) {
        keyboardSelectedIndex = suggestedTags.length - 1;
      }
    } else if (event.key === 'Down') {
      keyboardSelectedIndex++;
      if (keyboardSelectedIndex > suggestedTags.length - 1) {
        keyboardSelectedIndex = 0;
      }
    }

    this.setState({
      keyboardSelectedIndex,
    });
  }

  render() {
    const { id, className, highlightTags } = this.props;
    const { intent, suggestedTags } = this.props.suggestions;
    const { keyboardSelectedIndex } = this.state;

    if (this.isValid && intent != this.props.intent) {
      return null;
    }

    let listItems = [];
    if (!this.isValid) {
      listItems = [
        <div
          key="cell-0"
          className="Suggest error"
        >
                    No matching tags
        </div>,
        <div
          key="cell-1"
          className="Suggest error"
        >
                    To create a new tag, please follow the topic-goal format
        </div>,
      ];
    } else if (intent.length && !suggestedTags.length) {
      listItems = [
        <div
          key="cell-0"
          className="Suggest warning"
        >
                    No matching tags
        </div>,
        <div
          key="cell-1"
          className="Suggest warning"
        >
                    This will create a new tag
        </div>,
      ];
    } else if (suggestedTags.length) {
      const tagsOrderred = [];
      suggestedTags.forEach((tag) => {
        if (highlightTags.indexOf(tag) != -1) {
          tagsOrderred.unshift({ tag, isSuggested: true });
        } else {
          tagsOrderred.push({ tag, isSuggested: false });
        }
      });

      tagsOrderred.forEach((data, index) => {
        const itemClassName = `Suggest${
          keyboardSelectedIndex == index ? ' keyboardSelected' : ''
        }${data.isSuggested ? ' suggested' : ''}`;
        listItems.push(<div
          key={`cell-${index}`}
          tabIndex="-1"
          className={itemClassName}
          data-label={data.tag}
          onMouseOver={this.handleSuggestHover}
        >
          {data.tag}
        </div>);
      });
    }

    const classNameAttr = `SuggestContainer${className ? ` ${className}` : ''}`;
    return (
      <div
        id={id}
        className={classNameAttr}
        onKeyPress={this.handleKeyPress}
      >
        { listItems }
      </div>
    );
  }
}

CellEditableManualTagSuggest.propTypes = {
  id: PropTypes.string,
  className: PropTypes.string,
  stateKey: PropTypes.string.isRequired,
  intent: PropTypes.string.isRequired,
  projectId: PropTypes.string.isRequired,
  highlightTags: PropTypes.array,
  rowIndex: PropTypes.number,
  columnIndex: PropTypes.number,
};

CellEditableManualTagSuggest.defaultProps = {
  highlightTags: [],
  rowIndex: -1,
  columnIndex: -1,
};

const mapStateToProps = (state, ownProps) => ({
  suggestions: state.cellEditableManualTagSuggest.get(ownProps.stateKey),
});

export default connect(mapStateToProps)(CellEditableManualTagSuggest);
