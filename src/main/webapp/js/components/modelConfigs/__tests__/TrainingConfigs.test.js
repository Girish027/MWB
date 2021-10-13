import React from 'react';
import { mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { testModelData, testConfigData } from 'components/modelConfigs/testModelData';
import TrainingConfigs from 'components/modelConfigs/TrainingConfigs';

describe('<TrainingConfigs />', () => {
  const saveConfigChanges = (updatedData) => { };
  const onValidationSplitChange = (isValid) => { };

  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(
        <TrainingConfigs
          model={testModelData}
          config={testConfigData}
          isCurrentTab
          saveConfigChanges={saveConfigChanges}
          isTrainingConfigsValid={onValidationSplitChange}
          modelViewReadOnly={false}
        />,
      );
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('matches snapshot', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('onBlur - validationSplit should update state to the new value', () => {
      const expectedValidationSplit = '4';
      const item = wrapper.find('input[name="validationSplit"]');

      item.simulate('blur', {
        target: { value: expectedValidationSplit },
      });

      const wrapperInstance = wrapper.instance();
      const validationSplit = wrapperInstance.state.trainingConfigs.validationSplit;

      expect(validationSplit).toEqual(expectedValidationSplit);
    });

    test('onBlur - numOfEpochs should update state to the new value', () => {
      const expectedNumOfEpochs = '5';
      wrapper.setState({ isInternal: true });
      const item = wrapper.find('input[name="numEpochs"]');

      item.simulate('blur', {
        target: { value: expectedNumOfEpochs },
      });

      const wrapperInstance = wrapper.instance();
      const numOfEpochs = wrapperInstance.state.trainingConfigs.numOfEpochs;

      expect(numOfEpochs).toEqual(expectedNumOfEpochs);
    });
    test('onUpdateTags should update state to the new value', () => {
      const wrapperInstance = wrapper.instance();
      wrapperInstance.onUpdateTags(['sun', 'moon']);
      const stemmingExceptions = wrapperInstance.state.trainingConfigs.stemmingExceptions;
      expect(stemmingExceptions.length).toEqual(2);
      expect(stemmingExceptions[0]).toEqual('sun');
      expect(stemmingExceptions[1]).toEqual('moon');
    });
    test('onChange should update state to the new value', () => {
      const expectedNewText = 'new config data';
      const wrapperInstance = wrapper.instance();
      wrapperInstance.onChange(expectedNewText);
      expect(wrapperInstance.unSavedContent).toEqual(true);
      expect(wrapperInstance.textContent).toEqual(expectedNewText);
    });
    test('onBlur should update state to the new value', () => {
      const expectedNewValue = Object.assign({}, testConfigData);
      expectedNewValue.trainingConfigs.validationSplit = 7;
      expectedNewValue.trainingConfigs.out_of_domain_intent = 'Other_Other';
      const stringData = JSON.stringify(expectedNewValue);
      const wrapperInstance = wrapper.instance();
      wrapperInstance.onChange(stringData);
      wrapperInstance.onBlur('');
      expect(wrapperInstance.state.trainingConfigs.validationSplit).toEqual(expectedNewValue.validationSplit);
      expect(wrapperInstance.state.trainingConfigs.out_of_domain_intent).toEqual(expectedNewValue.out_of_domain_intent);
    });
  });
});
