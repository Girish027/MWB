
// a little function to help us with reordering the result
export const reorder = (list, startIndex, endIndex) => {
  const result = Array.from(list);
  const [removed] = result.splice(startIndex, 1);
  result.splice(endIndex, 0, removed);

  return result;
};

export const onClickTestRegex = () => {
  const testRegexUrl = 'https://regex101.com/';
  const testRegexTag = window.open(testRegexUrl, '_blank');
  testRegexTag.focus();
};

// using some little inline style helpers to make the app look okay
const getBackground = (isDragging) => (isDragging ? '#f6f7f8' : 'white');

export const getEditorListStyle = isDraggingOver => ({
  background: getBackground(isDraggingOver),
  width: '100%',
  overflow: 'auto',
  maxHeight: 'calc(100vh - 370px)',
});

export const getPostProcessingListStyle = isDraggingOver => ({
  background: getBackground(isDraggingOver),
  overflowY: 'auto',
  maxHeight: 'calc(100vh - 400px)',
});

export const getItemStyle = (isDragging, draggableStyle) => ({
  // some basic styles to make the items look a bit nicer
  userSelect: 'none',
  // change background colour if dragging
  background: getBackground(isDragging),

  // styles we need to apply on draggables
  ...draggableStyle,
});

export const getTransformationListStyle = isDraggingOver => ({
  background: getBackground(isDraggingOver),
  overflowY: 'auto',
  maxHeight: 'calc(100vh - 300px)',
});

export const getLastItemStyle = (itemIndex, noOfItems) => {
  const style = {};
  if (noOfItems - 1 === itemIndex) {
    style.borderBottom = 'none';
  }
  return style;
};
