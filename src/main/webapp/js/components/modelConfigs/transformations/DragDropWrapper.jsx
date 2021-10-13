import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import {
  getEditorListStyle,
  getItemStyle,
} from 'components/modelConfigs/modelConfigUtilities';

class DragDropWrapper extends Component {
  render() {
    const {
      isDisabled,
      onDragEnd,
      items,
      droppableId,
      getListStyle,
    } = this.props;

    return (
      <DragDropContext onDragEnd={onDragEnd}>
        <Droppable
          droppableId="droppable"
          isDropDisabled={isDisabled}
        >
          {(provided, snapshot) => (
            <div
              ref={provided.innerRef}
              style={getListStyle(snapshot.isDraggingOver)}
            >
              {items.map((item, index) => (
                <Draggable
                  key={item.id}
                  draggableId={item.id}
                  index={index}
                  isDragDisabled={isDisabled}
                >
                  {(provided, snapshot) => (
                    <div
                      ref={provided.innerRef}
                      style={getItemStyle(
                        snapshot.isDragging,
                        provided.draggableProps.style,
                      )}
                      {...provided.dragHandleProps}
                      {...provided.draggableProps.style}
                    >
                      {item.content}
                    </div>
                  )}
                </Draggable>
              ))}
              {provided.placeholder}
            </div>
          )}
        </Droppable>
      </DragDropContext>
    );
  }
}
DragDropWrapper.propTypes = {
  onDragEnd: PropTypes.func,
  items: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    content: PropTypes.node.isRequired,
  })),
  droppableId: PropTypes.string,
  isDisabled: PropTypes.bool.isRequired,
};

DragDropWrapper.defaultProps = {
  onDragEnd: () => {},
  items: [],
  droppableId: 'droppable',
  getListStyle: getEditorListStyle,
};

export default DragDropWrapper;
