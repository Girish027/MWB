import React from 'react';
import { mount } from 'enzyme';
import AddTransformationDialog from 'components/modelConfigs/transformations/AddTransformationDialog';

describe('<AddTransformationDialog />', () => {
  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(<AddTransformationDialog />);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });
});
