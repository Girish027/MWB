import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { WithContext as ReactTags } from 'react-tag-input';

class EditorTags extends Component {
  constructor(props, context) {
    super(props, context);

    this.handleDelete = this.handleDelete.bind(this);
    this.handleAddition = this.handleAddition.bind(this);
    this.convertArrayToTags = this.convertArrayToTags.bind(this);

    this.props = props;

    const listItems = this.props.transformationItem ? this.props.transformationItem : [];

    this.state = {
      listItems,
    };
  }

  static getDerivedStateFromProps(nextProps) {
    return ({
      listItems: nextProps.transformationItem ? nextProps.transformationItem : [],
    });
  }

  convertArrayToTags(listItems) {
    // listItems.sort();
    const tags = [];

    if (listItems) {
      listItems.forEach((item, index) => {
        const tagItem = {
          text: item,
          id: `${index}`,
          key: `${index}`,
        };
        tags.push(tagItem);
      });
    }
    return tags;
  }

  handleDelete(index) {
    const listItems = this.state.listItems.slice();
    listItems.splice(index, 1);
    this.props.onUpdateTags(listItems);
  }

  handleAddition(tag) {
    const listItems = this.state.listItems.slice();
    listItems.push(tag.text);
    this.props.onUpdateTags(listItems);
  }

  render() {
    const tags = this.convertArrayToTags(this.state.listItems);
    const { modelViewReadOnly } = this.props;

    return (
      <div id="EditorTags">
        <ReactTags
          tags={tags}
          suggestions={[]}
          handleDelete={this.handleDelete}
          handleAddition={this.handleAddition}
          inline={false}
          readOnly={modelViewReadOnly}
        />

        {(modelViewReadOnly && tags.length === 0)
          ? <span>(No stemming exceptions defined)</span>
          : (null)
        }
      </div>
    );
  }
}

EditorTags.defaultProps = {
  onUpdateTags: () => {},
};

EditorTags.propTypes = {
  transformationItem: PropTypes.array,
  onUpdateTags: PropTypes.func,
  modelViewReadOnly: PropTypes.bool,
};

export default EditorTags;
