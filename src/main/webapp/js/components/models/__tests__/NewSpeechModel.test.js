import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import validationUtil from 'utils/ValidationUtil';
import apiUrls from 'utils/apiUrls';

const NewSpeechModelView = require('components/models/NewSpeechModel').default;

const props = {
  setValidModel: () => { },
  dispatch: () => { },
  reportSuccess: () => { },
  reportError: () => { },
  saveModelChanges: () => { },
  modelViewReadOnly: false,
};
let wrapper;

describe('<NewSpeechModel />', () => {
  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = shallow(<NewSpeechModelView
        {...props}
      />);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<NewSpeechModelView
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });
    test('should match with basic props', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    const file = {
      name: 'config.csv',
    };
    beforeAll(() => {
      NewSpeechModelView.handleFileChoosen = jest.fn(() => 'handleFileChoosen');
      validationUtil.validateWorldClassFile = jest.fn(() => 'validateWorldClassFile');
      apiUrls.getDataUrl = jest.fn(() => 'getDataUrl');
      props.dispatch = jest.fn();
    });

    beforeEach(() => {
      wrapper = shallow(<NewSpeechModelView
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('should call saveFile', () => {
      test('should upload the file successfully', () => {
        wrapper.instance().saveFile([file]);
        expect(wrapper.handleFileChoosen).toHaveBeenCalled;
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('should call onclickDownload', () => {
      test('should call getDataURL', () => {
        wrapper.instance().onClickDownload();
        expect(apiUrls.getDataUrl).toHaveBeenCalled;
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('should call rejectFile', () => {
      test('should call onChange', () => {
        wrapper.instance().rejectFile();
        expect(wrapper.props.onChange).toHaveBeenCalled;
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('should call saveModelChanges', () => {
      test('should call saveModelChanges', () => {
        wrapper.instance().saveModelChanges();
        expect(wrapper.props.saveModelChanges).toHaveBeenCalled;
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('should call isNameValid', () => {
      test('should validate the name successfully', () => {
        expect(wrapper.instance().isNameValid('abcd')).toBe(true);
      });
      test('should validate the name as invalid', () => {
        expect(wrapper.instance().isNameValid('')).toBe(false);
      });
    });

    describe('should call isDatasetValid', () => {
      test('should upload the config successfully', () => {
        wrapper.instance().isDatasetValid(['1234']);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('should call isValidated', () => {
      test('should set the model as valid', () => {
        wrapper.instance().isValidated();
        expect(wrapper.props.setValidModel).toHaveBeenCalled;
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('should call getDatasetsLabel', () => {
      test('should display the dataset message correctly', () => {
        wrapper.instance().getDatasetsLabel();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });
  });
});
