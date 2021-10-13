import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import Constants from 'constants/Constants';
import { testModelData, testConfigData } from 'components/modelConfigs/testModelData';
import PostProcessingTransformations from 'components/modelConfigs/PostProcessingTransformations';

describe('<PostProcessingTransformations />', () => {
  const saveConfigChanges = (updatedData) => { };

  let wrapper;
  let props = {};
  const postProcessing = [{
    'input-match': '/(?:weerewr|wrewr)/',
    'intent-match': [
      'Reservation_Upgrade',
    ],
    'intent-replacement': 'Points_Buy',
    minConfidenceScore: -1,
    maxConfidenceScore: -1,
  }];
  beforeAll(() => {
    props = {
      model: testModelData,
      config: testConfigData,
      saveConfigChanges: jest.fn(),
      options: [{ value: 'any', label: 'any' }],
      isCurrentTab: true,
      dispatch: jest.fn(),
      showTransformationPredefinedDialog: false,
      modelViewReadOnly: false,
    };
  });

  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(
        <PostProcessingTransformations
          model={testModelData}
          config={testConfigData}
          isCurrentTab
          saveConfigChanges={saveConfigChanges}
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
  });

  describe('Snapshots', () => {
    test('renders correctly with string type transformation', () => {
      wrapper = shallow(<PostProcessingTransformations
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly with string type transformation', () => {
      wrapper = shallow(<PostProcessingTransformations
        {...props}
        config={{}}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly with isCurrentTab as false', () => {
      wrapper = shallow(<PostProcessingTransformations
        {...props}
        isCurrentTab={false}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should correctly render with data on trimValue call', () => {
      wrapper = shallow(<PostProcessingTransformations
        {...props}
      />);
      wrapper.instance().trimValue('fdfds');
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
    test('should correctly render with data on trimValue call', () => {
      wrapper = shallow(<PostProcessingTransformations
        {...props}
      />);
      wrapper.instance().trimValue(undefined);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when call getItems with undefined values', () => {
      wrapper = shallow(<PostProcessingTransformations
        {...props}

      />);
      wrapper.instance().getItems(undefined);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeAll(() => {
      // actionsConfigs.showTransformationAddDialog = jest.fn(() => 'showTransformationAddDialog');
      // actionsConfigs.showTransformationPredefinedDialog = jest.fn(() => 'showTransformationPredefinedDialog');
      // actionsConfigs.showTransformationDeleteDialog = jest.fn(() => 'showTransformationDeleteDialog');
    });

    beforeEach(() => {
      wrapper = shallow(<PostProcessingTransformations
        {...props}
      />);
      wrapper.setState({
        currentIdx: 0,
        selectedTabIndex: 0,
        postProcessing,
        selectedRule: postProcessing[0],
        customOptions: [{ label: 'Reservation_Upgrade' }],
      });
      wrapper.update();
    });

    afterEach(() => {
      jest.clearAllMocks();
    });


    describe('onAddRule:', () => {
      test('should set state on onAddRule call', () => {
        wrapper.instance().newRule = postProcessing[0];
        wrapper.instance().onAddRule();
        expect(wrapper.state().buttonLabel).toEqual(Constants.ADD);
        expect(wrapper.state().currentIdx).toEqual(postProcessing.length);
        expect(wrapper.state().selectedRule).toEqual({ ...postProcessing[0] });
      });
    });

    describe('onDelete:', () => {
      test('should call dispatch action on onDelete call', () => {
        const event = {
          preventDefault: jest.fn(),
          stopPropagation: jest.fn(),
        };
        wrapper.instance().updateConfigData = jest.fn();
        wrapper.instance().onDelete(event, 0);

        expect(wrapper.instance().updateConfigData).toHaveBeenCalled();
      });
    });

    describe('onAddOrSaveItem:', () => {
      test('should call updateConfigData on onAddOrSaveItem call', () => {
        wrapper.instance().updateConfigData = jest.fn();
        wrapper.instance().onAddOrSaveItem();

        expect(wrapper.instance().updateConfigData).toHaveBeenCalled();
      });
    });

    describe('onDragEnd:', () => {
      test('should call updateConfigData on onDragEnd call with result', () => {
        const result = {
          destination: {
            index: 0,
          },
          source: {
            index: 0,
          },
        };
        wrapper.instance().updateConfigData = jest.fn();
        wrapper.instance().onDragEnd(result);
        expect(wrapper.instance().updateConfigData).toHaveBeenCalled();
      });

      test('should not call updateConfigData on onDragEnd call with empty result', () => {
        const result = {
          source: {
            index: 0,
          },
        };
        wrapper.instance().updateConfigData = jest.fn();
        wrapper.instance().onDragEnd(result);

        expect(wrapper.instance().updateConfigData).not.toHaveBeenCalled();
      });
    });

    describe('onKeyPress:', () => {
      test('should call dispatch action on onKeyPress call', () => {
        const event = {
          preventDefault: jest.fn(),
          stopPropagation: jest.fn(),
        };
        wrapper.instance().onBlur = jest.fn();
        wrapper.instance().onKeyPress(event, 0);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('getPostProcessingUI:', () => {
      test('renders correctly when call getPostProcessingUI with undefined values', () => {
        wrapper.state().selectedRule = {};
        wrapper.instance().getPostProcessingUI();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('getPostProcessingJSON:', () => {
      test('renders correctly when call getPostProcessingJSON', () => {
        wrapper.instance().getPostProcessingJSON();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('renderTabs:', () => {
      test('renders correctly when call renderTabs', () => {
        wrapper.instance().renderTabs();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });


    describe('onSelect:', () => {
      test('should call dispatch action on onSelect call', () => {
        const event = {
          preventDefault: jest.fn(),
          stopPropagation: jest.fn(),
        };
        wrapper.instance().onSelect(event, 0);
        expect(wrapper.state().selectedRule).toEqual(postProcessing[0]);
        expect(wrapper.state().isMulti).toEqual(true);
      });
    });

    describe('onTabSelected:', () => {
      test('should call dispatch action on onTabSelected call', () => {
        wrapper.instance().onTabSelected('event', 0);
        expect(wrapper.state().selectedTabIndex).toEqual(0);
      });
    });

    describe('onChange:', () => {
      test('should set isMulti state as false on onChange call with actionMeta', () => {
        const newValue = {
          label: '',
        };
        const actionMeta = {
          option: {
            label: 'any',
          },
        };
        wrapper.instance().onChange(newValue, actionMeta, 'intent-match');
        expect(wrapper.state().isMulti).toEqual(false);
      });

      test('should set isMulti state as false on onChange call with newValue', () => {
        const newValue = {
          label: 'any',
        };
        const actionMeta = {
        };
        wrapper.instance().onChange(newValue, actionMeta, 'intent-match');
        expect(wrapper.state().isMulti).toEqual(false);
      });

      test('should set isMulti state as true on onChange call with actionMeta with label is not any', () => {
        const newValue = {
          label: '',
        };
        const actionMeta = {
          option: {
            label: 'something',
          },
        };
        wrapper.instance().onChange(undefined, actionMeta, 'intent-match');
        expect(wrapper.state().isMulti).toEqual(true);
      });
    });

    describe('handleCreate:', () => {
      test('should set isMulti state as false on handleCreate call with new value as any', () => {
        const newValue = 'any';

        wrapper.instance().handleCreate(newValue, 'intent-match');
        expect(wrapper.state().isMulti).toEqual(false);
      });

      test('should set isMulti state as false on handleCreate call with new value not any', () => {
        const newValue = 'something';

        wrapper.instance().handleCreate(newValue, 'intent-match');
        expect(wrapper.state().isMulti).toEqual(false);
      });
    });

    describe('onChangeComboBox:', () => {
      test('should set isMulti state as false on onChangeComboBox call with new value as any', () => {
        const newValue = {
          label: 'something',
        };
        let selectedRule = { ...postProcessing[0] };
        selectedRule['intent-match'] = newValue.label;
        wrapper.instance().onChangeComboBox(newValue, 'intent-match');
        expect(wrapper.state().selectedRule).toEqual(selectedRule);
      });

      test('should set isMulti state as false on onChangeComboBox call with new value not any', () => {
        let selectedRule = { ...postProcessing[0] };
        selectedRule['intent-match'] = '';
        wrapper.instance().onChangeComboBox(undefined, 'intent-match');
        expect(wrapper.state().selectedRule).toEqual(selectedRule);
      });
    });

    describe('onUpdateProcessingRules:', () => {
      test('should render correctly on onUpdateProcessingRules call', () => {
        wrapper.instance().updateConfigData = jest.fn();
        wrapper.instance().onUpdateProcessingRules(JSON.stringify(postProcessing[0]), 0);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('updateConfigData:', () => {
      test('should call dispatch action on updateConfigData call', () => {
        wrapper.instance().updateConfigData();
        expect(props.saveConfigChanges).toHaveBeenCalled();
      });
    });
  });
});
