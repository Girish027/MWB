import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import {
  reorder,
} from 'components/modelConfigs/modelConfigUtilities';
import { displayGoodRequestMessage } from 'state/actions/actions_app';
import { shouldUpdateState } from 'components/modelConfigs/transformations/editorUtils';
import {
  Plus,
  ContextualActionsBar,
  ContextualActionItem,
  TextField,
} from '@tfs/ui-components';
import {
  updateTransformationValidity,
} from 'state/actions/actions_configs';
import Textarea from 'react-textarea-autosize';
import { getLanguage } from 'state/constants/getLanguage';
import { actionBarStyles } from 'styles';
import validationUtil from 'utils/ValidationUtil';
import Constants from '../../../constants/Constants';
import DraggableItem from './DraggableItem';
import DragDropWrapper from './DragDropWrapper';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

class EditorSimplePairs extends Component {
  constructor(props, context) {
    super(props, context);

    this.onDragEnd = this.onDragEnd.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.getItems = this.getItems.bind(this);
    this.onClickDelete = this.onClickDelete.bind(this);
    this.onClickAdd = this.onClickAdd.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.saveChanges = this.saveChanges.bind(this);

    this.props = props;

    this.KEY = 'Key';
    this.VALUE = 'Value';

    const listItems = this.props.transformationItems ? this.props.transformationItems : {};

    const keys = Object.keys(listItems);

    const listArray = [];

    if (keys) {
      keys.forEach((key) => {
        listArray.push({
          key,
          value: listItems[key],
        });
      });
    }

    // Do not need to make these part of the state as they
    // are only used when the component is unmounting or getting
    // updated with new data from a transformation selection
    this.selected = null;
    this.unsavedEdits = false;

    this.scrollItemIntoView = false;
    this.state = {
      listItems: listArray,
      transformation: this.props.transformation,
      unsavedChanges: false,
    };
  }

  static getDerivedStateFromProps(props, state) {
    const shouldUpdate = shouldUpdateState(props, state);

    if (shouldUpdate) {
      const listItems = props.transformationItems ? props.transformationItems : {};
      const keys = Object.keys(listItems);

      const listArray = [];

      if (keys) {
        keys.forEach((key) => {
          listArray.push({
            key,
            value: listItems[key],
          });
        });
      }
      return {
        listItems: listArray,
        transformation: props.transformation,
      };
    }

    return null;
  }

  getSnapshotBeforeUpdate(prevProps, prevState) {
    const selectedItem = this.selected;
    if (!_.isNil(selectedItem) && this.unsavedEdits) {
      let value = '';
      if (selectedItem.index !== -1) {
        const focusElement = `input${selectedItem.name}${selectedItem.index}`;
        const element = this[focusElement];
        if (!_.isNil(element)) {
          value = element.value;
        }
      }
      const listItems = prevState.listItems;
      const currentItem = Object.assign({}, listItems[selectedItem.index]);
      if (selectedItem.name === this.KEY) {
        currentItem.key = value;
      } else {
        currentItem.value = value;
      }

      return {
        currentItem,
      };
    }
    return null;
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    let clearSelected = false;

    // Case when a transformation is selected of the same type
    // and onBlur is not called.
    if (this.unsavedEdits
      && (!_.isNil(snapshot) && !_.isNil(snapshot.currentItem))
      && !_.isNil(prevState.listItems)
      && !_.isNil(this.selected)) {
      const listItems = prevState.listItems;

      const selectedItem = listItems[this.selected.index];

      if (!_.isNil(selectedItem)
        && (selectedItem.key !== snapshot.currentItem.key
          || selectedItem.value !== snapshot.currentItem.value)) {
        listItems[this.selected.index] = snapshot.currentItem;
        const mappings = {};

        listItems.forEach((item) => {
          mappings[item.key] = item.value;
        });

        this.unsavedEdits = false,
        clearSelected = true;
        this.props.onUpdateList(mappings, prevState.transformation);
      }
    }

    if ((this.scrollItemIntoView || this.setFocus) && !_.isNil(this.selected)) {
      const idx = this.selected.index;

      // Used when a new element is added at the end
      if (this.scrollItemIntoView) {
        const selector = `input[data-item = 'item-${idx}']`;
        const elements = document.querySelectorAll(selector);

        if (!_.isNil(elements) && elements.length > 0) {
          elements[0].parentNode.parentNode.scrollIntoView({
            behavior: 'smooth',
            block: 'center',
          });
        }

        this.scrollItemIntoView = false;
        clearSelected = true;
      }

      // Used when save is done
      if (this.setFocus) {
        const focusElement = `input${this.selected.name}${this.selected.index}`;
        const element = this[focusElement];
        if (!_.isNil(element)) {
          if (this.selected.name === this.KEY) {
            element._ref.focus();
          }
        }
        this.setFocus = false;
        clearSelected = true;
      }
    }

    if (clearSelected) {
      this.selected = null;
    }
  }

  componentWillUnmount() {
    // This compensates for the following defect
    // https://github.com/facebook/react/issues/12363
    if (this.unsavedEdits) {
      const listItems = this.state.listItems;
      if (!_.isNil(this.selected)) {
        let value = '';
        if (this.selected.index !== -1) {
          const focusElement = `input${this.selected.name}${this.selected.index}`;
          const element = this[focusElement];
          if (!_.isNil(element)) {
            value = element.value;
          }
        }
        const currentItem = Object.assign({}, listItems[this.selected.index]);
        if (this.selected.name === this.KEY) {
          currentItem.key = value;
        } else {
          currentItem.value = value;
        }
        listItems[this.selected.index] = currentItem;
      }
      this.saveChanges(listItems);
    }
  }

  saveChanges(listItems) {
    const mappings = {};

    listItems.forEach((item) => {
      mappings[item.key] = item.value;
    });

    this.props.onUpdateList(mappings, this.props.transformation);
  }

  onClickDelete(event, index) {
    event.stopPropagation();
    const { dispatch } = this.props;
    const listItems = this.state.listItems;
    const itemDeleted = listItems[index];
    listItems.splice(index, 1);

    this.setState({
      listItems,
      unsavedChanges: false,
    }, () => {
      dispatch(displayGoodRequestMessage(DISPLAY_MESSAGES.deleteTransformationItem(itemDeleted)));
      this.saveChanges(listItems);
      this.selected = {
        index: index > 0 ? index - 1 : index,
        name: this.KEY,
      };

      this.setFocus = true;
    });
  }

  getItems(data) {
    const items = [];
    let isTransformationValidLocal = true;
    if (!data) {
      return items;
    }

    const {
      modelViewReadOnly, onlySubstText, dispatch, isTransformationValid,
    } = this.props;
    const noOfItems = data.length;
    data.forEach((item, index) => {
      let isValid = true;
      let tooltipText = '';
      const id = `${index}-${item.key}-${item.value}`;
      if ((onlySubstText && !validationUtil.validateWordClassLabel(item.value))
        || !item.key
        || (!onlySubstText && !item.value)) {
        isValid = false;
        isTransformationValidLocal = false;
        tooltipText = Constants.VALIDATION_SPLIT_NO_DATA_MSG;
      }
      items.push({
        id,
        content: (
          <DraggableItem
            index={index}
            noOfItems={noOfItems}
            onDelete={this.onClickDelete}
            id={id}
            modelViewReadOnly={modelViewReadOnly}
          >
            <div className="list-item-textarea-top">
              <Textarea
                name={this.KEY}
                data-item={`item-${index}`}
                data-idx={index}
                data-name={this.KEY}
                data-qa={`key-${index}`}
                onBlur={this.onBlur}
                defaultValue={item.key}
                key={`inputKey-${id}`}
                className="list-item text-area-autosize"
                ref={(input) => {
                  this[`inputKey${index}`] = input;
                }}
                disabled={modelViewReadOnly}
                autoComplete="off"
              />
            </div>
            <div className="list-item-input-bottom">
              <TextField
                type="text"
                name={this.VALUE}
                data-idx={index}
                data-name={this.VALUE}
                data-qa={`value-${index}`}
                defaultValue={item.value}
                disabled={modelViewReadOnly}
                onBlur={this.onBlur}
                key={`inputKey-${id}`}
                invalid={!isValid}
                showValidCheck
                tooltipText={tooltipText}
                ref={(input) => {
                  this[`inputValue${index}`] = input;
                }}
                styleOverride={{
                  width: '100%',
                }}
                checkmarkProps={{
                  fill: '#004c97',
                }}
              />
            </div>
          </DraggableItem>
        ),
      });
    });
    if (isTransformationValidLocal !== isTransformationValid) {
      dispatch(updateTransformationValidity(isTransformationValidLocal));
    }
    return items;
  }

  onFocus(e) {
    e.preventDefault();
    const index = parseInt(e.target.attributes['data-idx'].value);
    const name = e.target.attributes['data-name'].value;

    this.unsavedEdits = true;
    this.selected = {
      index,
      name,
      value: e.target.value,
    };
  }

  onClickAdd(e) {
    // e.preventDefault();
    e.persist();
    this.scrollItemIntoView = true;
    const newItems = [];
    const { onlySubstText } = this.props;
    let value = '';
    if (onlySubstText) {
      value = Constants.WORD_CLASS_SYNTAX;
    }

    this.state.listItems.forEach((item) => {
      newItems.push(item);
    });
    newItems.push({
      key: '',
      value,
    });

    this.setFocus = true;
    this.selected = {
      index: newItems.length - 1,
      name: this.KEY,
    };

    this.setState({
      listItems: newItems,
      unsavedChanges: true,
    });

    // e.stopPropagation();
  }

  onDragEnd(result) {
    // dropped outside the list
    if (!result.destination) {
      return;
    }

    const listItems = reorder(
      this.state.listItems,
      result.source.index,
      result.destination.index,
    );

    this.setState({
      listItems,
      unsavedChanges: false,
    }, () => {
      this.saveChanges(listItems);
    });
  }

  manageEdits(index, name, value, save = true) {
    const listItems = this.state.listItems;
    const currentValue = listItems[index];
    let dataChanged = false;
    if (name === this.KEY) {
      const newKey = value ? value.trim() : value;
      if (newKey !== currentValue.key) {
        currentValue.key = newKey;
        dataChanged = true;
      }
    } else {
      const newValue = value ? value.trim() : value;
      if (newValue !== currentValue.value) {
        currentValue.value = newValue;
        dataChanged = true;
      }
    }

    this.unsavedEdits = false;

    if (dataChanged) {
      // Setup so that the next element can get set focus
      // after a save
      let nextIndex = index;
      let nextName = this.VALUE;
      if (name === this.VALUE) {
        if (index < (listItems.length - 1)) {
          nextIndex = index + 1;
          nextName = this.KEY;
        } else {
          nextName = this.KEY;
        }
      }

      listItems[index] = currentValue;

      this.setState({
        listItems,
        unsavedChanges: false,
      }, () => {
        if (save) {
          this.setFocus = true;
          this.selected = {
            index: nextIndex,
            name: nextName,
          };
          this.saveChanges(listItems);
        }
      });
    }
  }

  onBlur(e) {
    const index = parseInt(e.target.attributes['data-idx'].value);
    const name = e.target.attributes['data-name'].value;

    this.manageEdits(index, name, e.target.value, true);
  }

  renderAddNewItem = () => (
    <ContextualActionsBar styleOverride={actionBarStyles.bar}>
      <ContextualActionItem
        onClickAction={this.onClickAdd}
        icon={Plus}
        styleOverride={actionBarStyles.item}
      >
                    ADD ITEM
      </ContextualActionItem>
    </ContextualActionsBar>
  );

  render() {
    const items = this.getItems(this.state.listItems);

    const { modelViewReadOnly } = this.props;

    return (
      <div id="EditorSimplePairs" className="model-transformation-editor-container">
        {!modelViewReadOnly && this.renderAddNewItem()}
        {this.props.helpComponent}
        <div className="transformation-editor-area">
          <DragDropWrapper
            onDragEnd={this.onDragEnd}
            items={items}
            droppableId="editor-simple-pairs"
            isDisabled={modelViewReadOnly}
          />
        </div>
      </div>
    );
  }
}

EditorSimplePairs.defaultProps = {
  onlySubstText: false,
  isTransformationValid: true,
  dispatch: () => {},
};

EditorSimplePairs.propTypes = {
  transformationItems: PropTypes.object,
  onUpdateList: PropTypes.func,
  transformation: PropTypes.object,
  helpComponent: PropTypes.node,
  dispatch: PropTypes.func,
  onlySubstText: PropTypes.bool,
  modelViewReadOnly: PropTypes.bool,
};

export default EditorSimplePairs;
