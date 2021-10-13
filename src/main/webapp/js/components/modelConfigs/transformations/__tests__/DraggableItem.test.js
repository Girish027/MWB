import React from 'react';
import { mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import DraggableItem from 'components/modelConfigs/transformations/DraggableItem';

describe('<DraggableItem />', () => {
  let wrapper;

  const getWrapper = (props) => mount(<DraggableItem {...props} />);
  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getWrapper({
        noOfItems: 10,
        id: 'abc',
        modelViewReadOnly: true,
        index: 1,
      });
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('render correctly for read only view', () => {
      wrapper = getWrapper({
        noOfItems: 10,
        id: 'abc',
        modelViewReadOnly: true,
        index: 1,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('render correctly for edit view', () => {
      wrapper = getWrapper({
        noOfItems: 10,
        id: 'abc',
        modelViewReadOnly: false,
        index: 1,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });


    test('render correctly for last item in the list', () => {
      wrapper = getWrapper({
        noOfItems: 10,
        id: 'abc',
        modelViewReadOnly: false,
        index: 10,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
