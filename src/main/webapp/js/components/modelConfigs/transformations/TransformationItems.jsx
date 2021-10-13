import React, { Component } from 'react';
import PropTypes from 'prop-types';
import shortid from 'shortid';
import Dialog from 'components/common/Dialog';

import {
  reorder,
  getTransformationListStyle,
} from 'components/modelConfigs/modelConfigUtilities';

import { getTransformationName } from 'model/ModelConfigManager';

import {
  showTransformationDeleteDialog,
  showTransformationAddDialog,
  showTransformationPredefinedDialog,
} from 'state/actions/actions_configs';
import AddTransformationDialog from 'components/modelConfigs/transformations/AddTransformationDialog';
import AddPredefinedTransformationDialog from 'components/modelConfigs/transformations/AddPredefinedTransformationDialog';
import {
  Plus,
  ContextualActionsBar,
  ContextualActionItem,
} from '@tfs/ui-components';
import { actionBarStyles } from 'styles';
import Constants from '../../../constants/Constants';
import DraggableItem from './DraggableItem';
import DragDropWrapper from './DragDropWrapper';

class TransformationItems extends Component {
  constructor(props, context) {
    super(props, context);

    this.onDelete = this.onDelete.bind(this);
    this.getItems = this.getItems.bind(this);
    this.onDragEnd = this.onDragEnd.bind(this);
    this.onAddPredefinedTransformations = this.onAddPredefinedTransformations.bind(this);
    this.onCreateTransformation = this.onCreateTransformation.bind(this);
    this.onAddTransformation = this.onAddTransformation.bind(this);
    this.onAddPredefined = this.onAddPredefined.bind(this);
    this.onCancelAddTransformation = this.onCancelAddTransformation.bind(this);
    this.onCancelAddPredefinedTransformation = this.onCancelAddPredefinedTransformation.bind(this);
    this.onSelect = this.onSelect.bind(this);
    this.okToDelete = this.okToDelete.bind(this);
    this.onCancelDelete = this.onCancelDelete.bind(this);
    this.props = props;

    let transformations = [];

    transformations = this.props.transformations ? this.props.transformations : [];

    // const currentItem = transformations.length > 0 ? transformations[0] : null;
    this.idxPendingDelete = null;
    this.namePendingDelete = null;
    this.scrollItemIntoView = false;

    this.state = {
      transformations,
      // currentItem,
    };
  }

  static getDerivedStateFromProps(props, state) {
    const transformations = props.transformations ? props.transformations : [];

    return {
      transformations,
    };
  }

  componentDidUpdate(prevProps, prevState) {
    if (this.scrollItemIntoView) {
      const idx = this.state.transformations.length - 1;
      const selector = `div[data-idx = '${idx}']`;
      const elements = document.querySelectorAll(selector);
      elements[0].parentNode.parentNode.scrollTop = 1000;
      this.scrollItemIntoView = false;
    }
  }


  onCancelDelete() {
    this.props.dispatch(showTransformationDeleteDialog(false));
  }

  okToDelete() {
    this.props.onDeleteTransformation(this.idxPendingDelete);
    this.props.dispatch(showTransformationDeleteDialog(false));
    this.idxPendingDelete = null;
  }

  onDelete(event, index) {
    event.preventDefault();
    const name = getTransformationName(this.state.transformations[index]);
    this.props.dispatch(showTransformationDeleteDialog(true));

    this.idxPendingDelete = index;
    this.namePendingDelete = name;

    event.stopPropagation();
  }

  getItems(data) {
    const { modelViewReadOnly, currentIndex } = this.props;
    const transformationItems = [];
    if (!data) {
      return transformationItems;
    }
    let selectedName = null;
    const currentItem = this.props.currentItem ? this.props.currentItem : null;
    if (currentItem) {
      if (typeof (currentItem) === 'string') {
        selectedName = currentItem;
      } else {
        selectedName = (Object.keys(currentItem))[0];
      }
    }
    data.forEach((item, index) => {
      let name;
      let itemType;
      if (typeof (item) === 'string') {
        name = item;
        itemType = '';
      } else {
        name = (Object.keys(item))[0];
        itemType = item[name].type;
      }
      const id = `${name}-${index}`;

      let selectedClass = 'transformation-list-item';
      if (currentIndex === index) {
        selectedClass = `${selectedClass} transformation-selected-item`;
      }
      const noOfItems = transformationItems.length;
      transformationItems.push({
        id,
        content: (
          <div
            data-idx={index}
          >
            <DraggableItem
              index={index}
              noOfItems={noOfItems}
              onDelete={this.onDelete}
              onSelect={this.onSelect}
              id={id}
              modelViewReadOnly={modelViewReadOnly}
              className={selectedClass}
              itemContentClass="transformation-item-content"
            >
              <div className="transformation-name">
                {name}
              </div>
              <div className="transformation-type">
                {itemType}
              </div>
            </DraggableItem>
          </div>
        ),
      });
    });
    return transformationItems;
  }

  onDragEnd(result) {
    // dropped outside the list
    if (!result.destination) {
      return;
    }

    const transformations = reorder(
      this.state.transformations,
      result.source.index,
      result.destination.index,
    );

    this.props.onUpdateTransformationItems(transformations);
  }

  onCreateTransformation(item) {
    this.scrollItemIntoView = true;
    this.props.dispatch(showTransformationAddDialog(false));
    this.props.onCreateTransformation(item);
  }

  onCancelAddTransformation() {
    this.props.dispatch(showTransformationAddDialog(false));
  }

  onAddTransformation() {
    this.props.dispatch(showTransformationAddDialog(true));
  }

  onAddPredefined() {
    this.props.dispatch(showTransformationPredefinedDialog(true));
  }

  onCancelAddPredefinedTransformation() {
    this.props.dispatch(showTransformationPredefinedDialog(false));
  }

  onAddPredefinedTransformations(itemList) {
    this.scrollItemIntoView = true;
    this.props.dispatch(showTransformationPredefinedDialog(false));
    this.props.onAddPredefined(itemList);
  }

  onSelect(event, index) {
    event.preventDefault();
    const item = this.state.transformations[index];
    this.props.onSelectItem(item, index);
  }

  renderAddNewItem = () => (
    <ContextualActionsBar styleOverride={actionBarStyles.bar}>
      <ContextualActionItem
        onClickAction={this.onAddTransformation}
        icon={Plus}
        styleOverride={actionBarStyles.item}
      >
                NEW
      </ContextualActionItem>
      <ContextualActionItem
        onClickAction={this.onAddPredefined}
        icon={Plus}
        styleOverride={actionBarStyles.item}
      >
                PREDEFINED
      </ContextualActionItem>
    </ContextualActionsBar>
  );

  render() {
    const { modelViewReadOnly, dispatch } = this.props;
    const items = this.getItems(this.state.transformations);

    return (
      <div id="TransformationItems">

        <div style={{
          lineHeight: '60px',
          paddingLeft: 30,
          borderBottom: '1px solid #ddd',
          paddingBottom: 2,
        }}
        >
        Transformations
        </div>
        {!modelViewReadOnly && this.renderAddNewItem()}
        <div
          className="dragdrop-container"
        >
          <DragDropWrapper
            onDragEnd={this.onDragEnd}
            items={items}
            droppableId="transformation-items"
            isDisabled={modelViewReadOnly}
            getListStyle={getTransformationListStyle}
          />
        </div>
        {this.props.showTransformationAddDialog
          ? (
            <AddTransformationDialog
              dispatch={dispatch}
              onOK={this.onCreateTransformation}
              onCancel={this.onCancelAddTransformation}
              showDialog={this.props.showTransformationAddDialog}
              transformations={this.props.transformations}
            />
          )
          : ''
        }
        {this.props.showTransformationPredefinedDialog
          ? (
            <AddPredefinedTransformationDialog
              onOK={(itemList) => {
                this.onAddPredefinedTransformations(itemList);
              }}
              onCancel={this.onCancelAddPredefinedTransformation}
              showDialog={this.props.showTransformationPredefinedDialog}
            />
          )
          : ''
        }
        <Dialog
          title="Delete Transformation Item Confirmation"
          visible={this.props.showTransformationDeleteDialog}
          okString="Delete"
          onCancel={this.onCancelDelete}
          onOk={this.okToDelete}
        >
          <div>
            <label>Select Delete to remove transformation: </label>
            <br />
            {this.namePendingDelete}
          </div>
        </Dialog>
      </div>

    );
  }
}

TransformationItems.propTypes = {
  transformations: PropTypes.array,
  onCreateTransformation: PropTypes.func,
  onAddPredefined: PropTypes.func,
  onDeleteTransformation: PropTypes.func,
  currentItem: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.object,
  ]),
  onSelectItem: PropTypes.func,
  onUpdateTransformationItems: PropTypes.func,
  showTransformationDeleteDialog: PropTypes.bool,
  showTransformationAddDialog: PropTypes.bool,
  showTransformationPredefinedDialog: PropTypes.bool,
  dispatch: PropTypes.func,
  modelViewReadOnly: PropTypes.bool,
};

export default TransformationItems;
