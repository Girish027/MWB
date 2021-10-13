import React from 'react';
import { mount } from 'enzyme';
import FeatureFlag from 'components/FeatureFlag/index';
import { featureFlagDefinitions } from 'utils/FeatureFlags';

describe('<FeatureFlag />', () => {
  describe('Creating an instance', () => {
    let wrapper;

    const url = 'http://www.google.com';
    const message = 'feature flag message';

    test('should exist', () => {
      const featureFlagValue = featureFlagDefinitions.options.show;
      wrapper = mount(
        <FeatureFlag
          featureFlagValue={featureFlagValue}
          components={{
            [featureFlagDefinitions.options.show]: (
              <a href={url} target="_blank">{message}</a>
            ),
            [featureFlagDefinitions.options.hide]: (
              <span>{message}</span>

            ),
          }}
        />,
      );

      expect(wrapper.exists()).toBe(true);
    });

    test('should exist', () => {
      const featureFlagValue = '';
      wrapper = mount(
        <FeatureFlag
          featureFlagValue={featureFlagValue}
          components={{
            [featureFlagDefinitions.options.show]: (
              <a href={url} target="_blank">{message}</a>
            ),
            [featureFlagDefinitions.options.hide]: (
              <span>{message}</span>

            ),
          }}
          defaultComponent={(<div>Default Component</div>)}
        />,
      );
      expect(wrapper.exists()).toBe(true);
      const findResult = wrapper.find('div');
      expect(findResult).toHaveLength(1);
    });
  });
});
