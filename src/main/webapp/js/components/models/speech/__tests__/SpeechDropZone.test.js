import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as apiUrls from 'utils/apiUrls';
import { SpeechDropzone } from 'components/models/speech/SpeechDropzone';

describe('<SpeechDropzone />', () => {
  let wrapper;

  const { featureFlags } = global.uiConfig;

  const props = {
    showDropbox: true,
    userFeatureConfiguration: featureFlags.DEFAULT,
    dispatch: jest.fn(),
    onChange: jest.fn(),
    onRadioChange: jest.fn(),
    onChangeDigitalHostedUrl: jest.fn(),
  };

  const file = {
    lastModified: '1575018060587',
    lastModifiedDate: 'Fri Nov 29 2019 14:31:00 GMT+0530 (India Standard Time) {}',
    name: 'word_class.txt',
    path: 'word_class.txt',
    size: 3876,
    type: 'text/plain',
    webkitRelativePath: '',
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<SpeechDropzone
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      wrapper = mount(<SpeechDropzone
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.onChange = jest.fn();
      props.onRadioChange = jest.fn();
      apiUrls.getDataUrl = jest.fn();
      props.onChangeDigitalHostedUrl = jest.fn();
    });

    describe('onChangeRadioButton', () => {
      test('should call onChange props when bundled data is selected in radio', () => {
        wrapper = shallow(<SpeechDropzone
          {...props}
        />);

        wrapper.instance().onChangeRadioButton('bundled-data');
        expect(props.onRadioChange).toHaveBeenCalledWith({ isUnbundled: false });
      });

      test('should call onChange props when unbundled data is selected in radio', () => {
        wrapper = shallow(<SpeechDropzone
          {...props}
        />);

        wrapper.instance().onChangeRadioButton('unbundled-data');
        expect(props.onRadioChange).toHaveBeenCalledWith({ isUnbundled: true });
      });
    });

    describe('onClickDownload', () => {
      test('should call onChange props when bundled data is selected in radio', () => {
        wrapper = shallow(<SpeechDropzone
          {...props}
        />);

        wrapper.instance().onClickDownload();
        expect(apiUrls.getDataUrl).toHaveBeenCalledWith(apiUrls.pathKey.wordClassDefault);
      });
    });

    describe('saveFile', () => {
      test('should save the text file', () => {
        wrapper = shallow(<SpeechDropzone
          {...props}
        />);

        wrapper.instance().handleFileChoosen = jest.fn();
        wrapper.instance().saveFile([file]);

        let blob = new Blob([file], { type: 'text/plain;charset=utf-8' });
        expect(wrapper.instance().handleFileChoosen).toHaveBeenCalledWith(blob);
      });
    });

    describe('handleFileChoosen', () => {
      test('should read the text file', () => {
        wrapper = shallow(<SpeechDropzone
          {...props}
        />);

        let blob = new Blob([file], { type: 'text/plain;charset=utf-8' });
        wrapper.instance().handleFileChoosen(blob);

        expect(wrapper.instance().fileReader).toEqual(expect.anything());
      });
    });

    describe('onChangeDigitalUrl', () => {
      test('should call onChangeDigitalUrl', () => {
        const evt = {
          target: {
            value: 'http://digitalUrl.com/digital',
          },
        };
        const digitalUrl = evt.target.value;
        wrapper = shallow(<SpeechDropzone
          {...props}
        />);

        wrapper.instance().onChangeDigitalUrl(evt);
        expect(props.onChangeDigitalHostedUrl).toHaveBeenCalledWith(digitalUrl);
      });
    });

    describe('rejectFile', () => {
      test('should call rejectFile', () => {
        wrapper = shallow(<SpeechDropzone
          {...props}
        />);
        wrapper.setState({
          isValid: true,
        });

        wrapper.instance().rejectFile();
        expect(props.onChange).toHaveBeenCalledWith({}, false);
        expect(wrapper.state().isValid).toBe(false);
      });
    });

    describe('handleFileRead', () => {
      test('should properly validate the file when content uploaded is in correct format', () => {
        wrapper = shallow(<SpeechDropzone
          {...props}
        />);

        wrapper.instance().wordClassFile = file;
        wrapper.instance().fileReader = {
          result: '_class_family\n'
          + 'son',
        };
        wrapper.update();
        wrapper.instance().handleFileRead({});

        expect(props.onChange).toHaveBeenCalledWith(file, true);
        expect(wrapper.state().isValid).toBe(true);
      });

      test('should properly validate the file when content uploaded is not in correct format', () => {
        wrapper = shallow(<SpeechDropzone
          {...props}
        />);

        wrapper.instance().wordClassFile = file;
        wrapper.instance().fileReader = {
          result: '_class_family test'
          + 'son',
        };
        wrapper.update();
        wrapper.instance().handleFileRead({});

        expect(props.onChange).toHaveBeenCalledWith({}, false);
        expect(wrapper.state().isValid).toBe(false);
      });

      test('should properly validate the file when content uploaded is not in correct format 2', () => {
        wrapper = shallow(<SpeechDropzone
          {...props}
        />);

        wrapper.instance().wordClassFile = file;
        wrapper.instance().fileReader = {
          result: '_class_family test'
          + 'son'
          + '_class_ newtest',
        };
        wrapper.update();
        wrapper.instance().handleFileRead({});

        expect(props.onChange).toHaveBeenCalledWith({}, false);
        expect(wrapper.state().isValid).toBe(false);
      });
    });
  });
});
