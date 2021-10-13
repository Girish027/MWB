import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import EditorTags from 'components/modelConfigs/EditorTags';

describe('<EditorTags />', () => {
  describe('Creating an instance', () => {
    let wrapper;

    let props = {
      transformationItem: ['first', 'second', 'third'],
    };

    afterEach(() => {
      jest.clearAllMocks();
    });

    test('should exist', () => {
      wrapper = shallow(<EditorTags {...props} />);
      expect(wrapper.exists()).toBe(true);
    });

    test('matches snapshot', () => {
      wrapper = shallow(<EditorTags {...props} />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('handle addition should update parent', () => {
      props.onUpdateTags = jest.fn();
      wrapper = shallow(<EditorTags {...props} />);
      const itemToAdd = {
        id: '0',
        text: 'flower',
      };
      wrapper.instance().handleAddition(itemToAdd);
      expect(props.onUpdateTags).toHaveBeenCalledWith([...props.transformationItem, itemToAdd.text]);
    });

    test('handle deletion should update parent', () => {
      props.onUpdateTags = jest.fn();
      wrapper = shallow(<EditorTags {...props} />);
      const itemToDelete = {
        id: 2,
        text: 'third',
      };
      wrapper.instance().handleDelete(itemToDelete.id);
      expect(props.onUpdateTags).toHaveBeenCalledWith(['first', 'second']);
    });
  });
});
