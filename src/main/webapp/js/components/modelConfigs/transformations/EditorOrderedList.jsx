import React, { Component } from 'react';
import PropTypes from 'prop-types';

import shortid from 'shortid';
import _ from 'lodash';
import {
  Plus,
  ContextualActionsBar,
  ContextualActionItem,
  ExternalLink,
} from '@tfs/ui-components';
import { displayGoodRequestMessage } from 'state/actions/actions_app';
import { shouldUpdateState } from 'components/modelConfigs/transformations/editorUtils';

import { getLanguage } from 'state/constants/getLanguage';
import {
  reorder,
  onClickTestRegex,
} from 'components/modelConfigs/modelConfigUtilities';
import { actionBarStyles } from 'styles';
import Constants from '../../../constants/Constants';
import DraggableItem from './DraggableItem';
import DragDropWrapper from './DragDropWrapper';


const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

class EditorOrderedList extends Component {
  constructor(props, context) {
    super(props, context);

    this.onDragEnd = this.onDragEnd.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyPress = this.onKeyPress.bind(this);
    this.getItems = this.getItems.bind(this);
    this.onDelete = this.onDelete.bind(this);
    this.onAddItem = this.onAddItem.bind(this);
    this.saveChanges = this.saveChanges.bind(this);
    this.onFocusAndInput = this.onFocusAndInput.bind(this);

    this.props = props;
    this.scrollItemIntoView = false;

    this.unsavedEdits = false;
    this.lastEdited = null;

    const listItems = this.props.transformationItems ? this.props.transformationItems : [];

    this.state = {
      listItems,
      unsavedChanges: false,
      transformation: this.props.transformation,
    };
  }

  static getDerivedStateFromProps(props, state) {
    const shouldUpdate = shouldUpdateState(props, state);

    if (shouldUpdate) {
      const transformationName = Object.keys(props.transformation)[0];

      return ({
        unsavedChanges: false,
        listItems: props.transformationItems ? props.transformationItems : [],
        transformation: props.transformation,
        transformationName,
      });
    }

    return null;
  }

  getSnapshotBeforeUpdate(prevProps, prevState) {
    if (!_.isNil(this.lastEdited) && this.unsavedEdits) {
      let { value, index } = this.lastEdited;
      const { currentTransformationName } = this.props;
      const listItems = prevState.listItems;

      let currentItem = listItems[index];
      if (this.unsavedEdits || currentTransformationName === prevProps.currentTransformationName) {
        currentItem = value;
      }
      return {
        currentItem,
      };
    }
    return null;
  }

  componentWillUnmount() {
    if (this.state.unsavedChanges || this.unsavedEdits) {
      const listItems = this.state.listItems.slice();
      if (!_.isNil(this.lastEdited)) {
        const { index, value } = this.lastEdited;
        listItems[index] = value;
      }
      this.unsavedEdits = false;
      this.lastEdited = null;
      this.props.onUpdateList(listItems, this.props.transformation);
    }
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.scrollItemIntoView && !_.isNil(this.lastEdited)) {
      const idx = this.state.listItems.length > 0 ? this.state.listItems.length - 1 : 0;
      const selector = `div[data-idx = '${idx}']`;
      const elements = document.querySelectorAll(selector);
      elements[0].parentNode.parentNode.scrollIntoView({
        behavior: 'smooth',
        block: 'center',
      });

      this.scrollItemIntoView = false;
    }

    // Case when a transformation is selected of the same type
    // and onBlur is not called
    // - direclty update the transformation list for previous transformation
    if ((prevState.unsavedChanges || this.unsavedEdits)
      && (!_.isNil(snapshot) && !_.isNil(snapshot.currentItem))
      && !_.isNil(prevState.listItems)
      && !_.isNil(this.lastEdited)) {
      const listItems = prevState.listItems.slice();
      const { index, value = '' } = this.lastEdited;

      if (prevProps.currentTransformationName !== this.props.currentTransformationName) {
        listItems[index] = snapshot.currentItem;
        this.props.onUpdateList(listItems, prevState.transformation);
        this.unsavedEdits = false;
      }
    }
    this.lastEdited = null;
  }

  saveChanges(listItems) {
    this.unsavedEdits = false;
    this.setState({
      listItems,
      unsavedChanges: false,
    }, () => {
      this.props.onUpdateList(listItems, this.props.transformation);
    });
  }

  onDelete(e, index) {
    e.preventDefault();
    const { dispatch } = this.props;
    const listItems = this.state.listItems.slice();
    const itemDeleted = listItems.splice(index, 1)[0];

    this.setState({
      listItems,
    }, () => {
      dispatch(displayGoodRequestMessage(DISPLAY_MESSAGES.deleteTransformationItem(itemDeleted)));
      this.saveChanges(listItems);
    });
    e.stopPropagation();
  }

  getItems(data) {
    const items = [];
    if (!data) {
      return items;
    }
    const { modelViewReadOnly } = this.props;
    const noOfItems = data.length;

    data.forEach((item, index) => {
      const id = shortid.generate();
      // const isOpen = (this.state.listofOpenItems.indexOf(name) > -1);
      // setting open is not working
      items.push({
        id,
        content: (
          <div
            data-idx={index}
          >
            <DraggableItem
              index={index}
              noOfItems={noOfItems}
              onDelete={this.onDelete}
              id={id}
              modelViewReadOnly={modelViewReadOnly}
            >
              <div className="list-item-input-one">
                <input
                  data-item={`item-${index}`}
                  data-idx={index}
                  onBlur={this.onBlur}
                  onKeyPress={this.onKeyPress}
                  onInput={this.onFocusAndInput}
                  onFocus={this.onFocusAndInput}
                  defaultValue={item}
                  disabled={modelViewReadOnly}
                />
              </div>
            </DraggableItem>
          </div>
        ),
      });
    });
    return items;
  }

  onAddItem() {
    this.scrollItemIntoView = true;
    const newItems = this.state.listItems.slice();
    newItems.push(' ');
    this.setState({
      listItems: newItems,
      unsavedChanges: true,
    });
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

    this.saveChanges(listItems);
  }

  onKeyPress(event) {
    if (event.key === 'Enter'
      || event.key === 'Tab') {
      event.preventDefault();
      this.onBlur(event);
    }
  }

  onBlur(e) {
    let { attributes = {}, value } = e.target;
    const index = parseInt(attributes['data-idx'].value);
    value = value.trim();

    const listItems = this.state.listItems.slice();
    listItems[index] = value;

    this.saveChanges(listItems);
  }

  onFocusAndInput(e) {
    e.preventDefault();
    let { attributes = {}, value } = e.target;
    const index = parseInt(attributes['data-idx'].value);
    value = value.trim();

    this.unsavedEdits = true;
    this.lastEdited = {
      index,
      value,
    };
  }

 renderAddNewItem = () => (
   <ContextualActionsBar styleOverride={actionBarStyles.bar}>
     <ContextualActionItem
       onClickAction={this.onAddItem}
       icon={Plus}
       styleOverride={actionBarStyles.item}
     >
                    ADD ITEM
     </ContextualActionItem>
     <ContextualActionItem
       onClickAction={onClickTestRegex}
       icon={ExternalLink}
       styleOverride={actionBarStyles.item}
     >
                    TEST REGEX
     </ContextualActionItem>
   </ContextualActionsBar>
 );

 render() {
   const items = this.getItems(this.state.listItems);
   const { modelViewReadOnly } = this.props;

   return (
     <div id="EditorOrderedList" className="model-transformation-editor-container">
       {!modelViewReadOnly && this.renderAddNewItem()}
       {this.props.helpComponent}
       <div className="transformation-editor-area">
         <DragDropWrapper
           onDragEnd={this.onDragEnd}
           items={items}
           droppableId="editor-long-pairs"
           isDisabled={modelViewReadOnly}
         />
       </div>
     </div>
   );
 }
}

EditorOrderedList.propTypes = {
  currentTransformationName: PropTypes.string.isRequired,
  transformationItems: PropTypes.array,
  onUpdateList: PropTypes.func,
  transformation: PropTypes.object,
  modelViewReadOnly: PropTypes.bool,
};

export default EditorOrderedList;
