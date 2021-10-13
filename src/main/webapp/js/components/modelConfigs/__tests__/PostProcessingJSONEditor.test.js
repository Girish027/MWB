import React from 'react';
import { mount } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import PostProcessingJSONEditor from 'components/modelConfigs/PostProcessingJSONEditor';

describe('<PostProcessingJSONEditor />', () => {
  const data = {
    modelName: '',
    name: '',
  };

  describe('Creating an instance', () => {
    let wrapper;

    const props = {
      currentItem: {
        'input-match': '/new reservation/',
        'intent-match': [
          'reservation-query',
        ],
        'intent-replacement': 'reservation-make',
      },
      modelViewReadOnly: false,
      onUpdateProcessingRules: () => { },
      ruleIdx: 0,
    };

    const newProps = {
      currentItem: {
        'input-match': '/update reservation/',
        'intent-match': [
          'reservation-make',
        ],
        'intent-replacement': 'reservation-change',
      },
      modelViewReadOnly: false,
      onUpdateProcessingRules: () => { },
      ruleIdx: 1,
    };

    beforeAll(() => {
      wrapper = mount(
        <PostProcessingJSONEditor
          {...props}
        />,
      );
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('onChange should update state to the new value', () => {
      const stringData = JSON.stringify(props.currentItem);
      const wrapperInstance = wrapper.instance();
      wrapperInstance.onChange(stringData);
      expect(wrapperInstance.state.currentItem['input-match']).toEqual(props.currentItem['input-match']);
    });

    test('componentWillReceiveProps should update state to the new value', () => {
      const stringData = JSON.stringify(props.currentItem);
      const wrapperInstance = wrapper.instance();
      wrapperInstance.componentWillReceiveProps(newProps);
      expect(wrapperInstance.state.currentItem['input-match']).toEqual(newProps.currentItem['input-match']);
    });
  });
});
