import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import SpeechSelectDatasetDialog from 'components/models/speech/SpeechSelectDatasetDialog';
import Constants from 'constants/Constants';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsModels from 'state/actions/actions_models';

describe('<SpeechSelectDatasetDialog />', () => {
  const createDataset = (id, name, createdAt, status = 'COMPLETED') => ({
    _key: id,
    id,
    clientId: 1,
    projectId: 2,
    name,
    type: 'Audio/Voice (Data Collection)',
    description: 'test dataset',
    locale: 'en_US',
    createdAt,
    status,
    task: 'INDEX',
  });

  const model = {
    _key: 1,
    id: 12,
    modelToken: '15e17bdf-f38e-42f3-a8f0-7b595dd3df40',
    projectId: 2,
    name: 'Test Model',
    description: 'Test Description',
    version: 5,
    modelType: Constants.DIGITAL_MODEL,
    created: 1536777405845,
    updated: 1536777405845,
    datasetIds: ['1', '2'],
    configId: 12,
    userId: 'mwbuser@247.ai',
  };

  let props;
  let wrapper;
  const { featureFlags } = global.uiConfig;

  beforeAll(() => {
    props = {
      model,
      userFeatureConfiguration: featureFlags.DEFAULT,
      dispatch: () => {},
    };
  });

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<SpeechSelectDatasetDialog
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<SpeechSelectDatasetDialog
        {...props}
      />);
    });

    test('renders correctly with for model with 2 datasets selected', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly with for model with all datasets selected', () => {
      const testModel = {
        ...model,
      };
      wrapper = shallow(<SpeechSelectDatasetDialog
        {...props}
        model={testModel}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for user with MWB_ROLE_EXTERNAL permission', () => {
      const testModel = {
        ...model,
      };
      wrapper = shallow(<SpeechSelectDatasetDialog
        {...props}
        model={testModel}
        userFeatureConfiguration={{
          ...featureFlags.DEFAULT,
          ...featureFlags.MWB_ROLE_EXTERNAL,
        }}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for digital model with speechModelId set', () => {
      const testModel = {
        ...model,
        speechModelId: '23',
      };
      wrapper = shallow(<SpeechSelectDatasetDialog
        {...props}
        userFeatureConfiguration={{
          ...featureFlags.DEFAULT,
          ...featureFlags.MWB_ROLE_EXTERNAL,
        }}
        model={testModel}
      />);
      wrapper.setState({
        isValidFile: false,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.dispatch = jest.fn();
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('Functionality:', () => {
      beforeEach(() => {
        wrapper = shallow(<SpeechSelectDatasetDialog
          {...props}
        />);
      });

      test('should set the valid digitalHostedUrl and isValidUrl as true in component state', () => {
        wrapper.setState({ digitalHostedUrl: '', isValidUrl: false });
        wrapper.instance().onChangeDigitalHostedUrl('https://fsffsdf.com/digital');
        expect(wrapper.state().digitalHostedUrl).toEqual('https://fsffsdf.com/digital');
        expect(wrapper.state().isValidUrl).toEqual(true);
      });

      test('should set the invalid digitalHostedUrl and isValidUrl as false in component state', () => {
        wrapper.setState({ digitalHostedUrl: '', isValidUrl: true });
        wrapper.instance().onChangeDigitalHostedUrl('fsffsdf.com/digital');
        expect(wrapper.state().digitalHostedUrl).toEqual('fsffsdf.com/digital');
        expect(wrapper.state().isValidUrl).toEqual(false);
      });


      test('should set isUnbundled in component state', () => {
        wrapper.setState({ isUnbundled: false });
        wrapper.instance().onRadioChange({ isUnbundled: true });
        expect(wrapper.state('isUnbundled')).toBe(true);
      });

      test('should set isUnbundled in component state without sending in argument type', () => {
        wrapper.setState({ isUnbundled: false });
        wrapper.instance().onRadioChange({});
        expect(wrapper.state('isUnbundled')).toBe(true);
      });
    });

    describe('onClickAddSpeech:', () => {
      beforeEach(() => {
        actionsModels.createSpeechModel = jest.fn()
          .mockImplementation((model, datasets) => ({ called: 'createSpeechModel' }));
        actionsApp.modalDialogChange = jest.fn()
          .mockImplementation(value => ({ called: 'modalDialogChange' }));
        wrapper = shallow(<SpeechSelectDatasetDialog
          {...props}
        />);
      });

      test('should dispatch createSpeechModel action', () => {
        const selectedDatasets = ['1'];
        const digitalHostedUrl = 'https://localhost.com';
        const expectedSpeechModel = {
          ...model,
          modelType: Constants.DIGITAL_SPEECH_MODEL,
          selectedDatasets,
          startBuild: true,
          isUnbundled: true,
          digitalHostedUrl,
        };
        wrapper.setState({ selectedDatasets, digitalHostedUrl });
        wrapper.instance().onClickAddSpeech();
        expect(actionsModels.createSpeechModel).toHaveBeenCalledWith(expectedSpeechModel, {});
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'createSpeechModel' });
      });

      test('should dispatch modalDialogChange action to close the Speech Modal', () => {
        wrapper.instance().onClickAddSpeech();
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'modalDialogChange' });
      });
    });

    describe('onClickCancel:', () => {
      beforeEach(() => {
        actionsApp.modalDialogChange = jest.fn()
          .mockImplementation(value => ({ called: 'modalDialogChange' }));
        wrapper = shallow(<SpeechSelectDatasetDialog
          {...props}
        />);
      });

      test('should dispatch modalDialogChange action to close the Speech Modal', () => {
        wrapper.instance().onClickCancel();
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'modalDialogChange' });
      });
    });

    describe('onFileLoaded:', () => {
      beforeEach(() => {
        wrapper = shallow(<SpeechSelectDatasetDialog
          {...props}
        />);
      });

      test('should set the valid wordclass value and isValidUrl as true in component state', () => {
        wrapper.setState({ wordClassFile: {}, isValidFile: false });
        wrapper.instance().onFileLoaded('Some valid word class file', true);
        expect(wrapper.state().wordClassFile).toEqual('Some valid word class file');
        expect(wrapper.state().isValidFile).toEqual(true);
      });

      test('should set the wordclass as empty object and isValidUrl as false in component state', () => {
        wrapper.setState({ wordClassFile: 'fsdfsd', isValidFile: true });
        wrapper.instance().onFileLoaded({}, false);
        expect(wrapper.state().wordClassFile).toEqual({});
        expect(wrapper.state().isValidFile).toEqual(false);
      });

      test('should set the wordclass as empty object and isValidUrl as false in component state when no argument passed', () => {
        wrapper.setState({ wordClassFile: 'fsdfsd', isValidFile: true });
        wrapper.instance().onFileLoaded(undefined, false);
        expect(wrapper.state().wordClassFile).toEqual({});
        expect(wrapper.state().isValidFile).toEqual(false);
      });
    });
  });
});
