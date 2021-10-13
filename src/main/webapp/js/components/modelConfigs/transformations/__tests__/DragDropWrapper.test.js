import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import {
  getTransformationListStyle,
} from 'components/modelConfigs/modelConfigUtilities';
import DragDropWrapper from 'components/modelConfigs/transformations/DragDropWrapper';
import DraggableItem from 'components/modelConfigs/transformations/DraggableItem';


describe('<DragDropWrapper />', () => {
  let wrapper;

  const items = [{
    id: 'abc',
    content: (<div> some  content </div>),
  }, {
    id: 'qwe',
    content: (<div> other  content </div>),
  }];

  const getWrapper = (props = {}) => shallow(<DragDropWrapper
    items={items}
    isDisabled={false}
    {...props}
  />);
  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getWrapper();
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('render correctly for disabled view', () => {
      wrapper = getWrapper({ isDisabled: true });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('render correctly for enabled view', () => {
      wrapper = getWrapper({ isDisabled: false });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('render correctly for zero items', () => {
      wrapper = getWrapper({ items: [] });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('render correctly for given style', () => {
      wrapper = getWrapper({ getListStyle: getTransformationListStyle });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
