import React from 'react';
import { mount } from 'enzyme';
import AddPredefinedTransformationDialog from 'components/modelConfigs/transformations/AddPredefinedTransformationDialog';

describe('<AddPredefinedTransformationDialog />', () => {
  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(<AddPredefinedTransformationDialog />);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });
});
