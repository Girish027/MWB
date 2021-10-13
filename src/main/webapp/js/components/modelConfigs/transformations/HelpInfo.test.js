import React from 'react';
import { shallow } from 'enzyme';
import getTransformationHelpInfo from './HelpInfo';

describe('getTransformationHelpInfo', () => {
  test('stop-words should render', () => {
    const result = getTransformationHelpInfo('stems');
    const wrapper = shallow(result);
    expect(wrapper.exists()).toBe(true);
  });
  test('not a help info should still render', () => {
    const result = getTransformationHelpInfo();
    const wrapper = shallow(result);
    expect(wrapper.exists()).toBe(true);
  });
  test('invalid help info should still render', () => {
    const result = getTransformationHelpInfo('test');
    const wrapper = shallow(result);
    expect(wrapper.exists()).toBe(true);
  });
});
