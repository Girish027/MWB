import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsConfigs from 'state/actions/actions_configs';
import * as newModel from 'components/models/NewModel';

const NewModelView = require('components/models/NewModel').default;

const props = {
  setValidModel: () => {},
  dispatch: () => {},
  reportSuccess: () => {},
  reportError: () => {},
  saveModelChanges: () => {},
  modelViewReadOnly: false,
};
let wrapper;

describe('<NewModel />', () => {
  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = shallow(<NewModelView
        {...props}
      />);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<NewModelView
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
      actionsConfigs.configUploadSuccess = jest.fn(() => 'configUploadSuccess');
      actionsConfigs.configUploadFail = jest.fn(() => 'configUploadFail');
      props.dispatch = jest.fn();
    });

    beforeEach(() => {
      wrapper = shallow(<NewModelView
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('configUploadSuccess', () => {
      test('should upload the config successfully', () => {
        wrapper.instance().configUploadSuccess('', file, '');
        expect(actionsConfigs.configUploadSuccess).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith('configUploadSuccess');
      });
    });

    describe('configUploadError', () => {
      test('should upload config error out', () => {
        wrapper.instance().configUploadError({}, {}, file);
        expect(actionsConfigs.configUploadFail).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith('configUploadFail');
      });
    });

    describe('should call isDatasetValid', () => {
      test('should upload the config successfully', () => {
        wrapper.instance().isDatasetValid(['1234']);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('getConfigSelect call', () => {
      test('should show related model configs correctly', () => {
        wrapper.instance().getConfigSelect(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('getConfigSelect call', () => {
      test('should show related model configs correctly with parameters', () => {
        const configsArray = [{
          key: '-1',
          id: '101',
          name: 'Version 101-cfg',
          description: '',
          modelType: 'DIGITAL_SPEECH',
        }, {
          key: '-2',
          id: '102',
          name: 'Version 102-cfg',
          description: '',
          modelType: 'DIGITAL_SPEECH',
        }, {
          key: '-3',
          id: '103',
          name: 'Version 103-cfg',
          description: '',
          modelType: 'DIGITAL',
        }];
        const selectedConfig = { name: 'Version 103-cfg' };
        wrapper.setState({ configsArray, selectedConfig });
        wrapper.instance().getConfigSelect(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('onClickRadioButton', () => {
      test('should render onClickRadioButton while clicking on Radio Button', () => {
        wrapper.instance().onClickRadioButton();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('onClickCheckbox', () => {
      test('should render onClickCheckbox while clicking on checkbox', () => {
        const event = { checked: true };
        wrapper.instance().onClickCheckbox(event);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('getCheckbox for Radio Buttons', () => {
      test('should render getCheckbox with correct styles on click of Tensorflow Radio Button', () => {
        wrapper.setState({ showCheckbox: true, model: { modelTechnology: 'USE' } });
        wrapper.instance().getCheckbox();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
      test('should render getCheckbox with correct styles on click of N-Gram Radio Button', () => {
        wrapper.setState({ showCheckbox: true, model: { modelTechnology: 'N-GRAM' } });
        wrapper.instance().getCheckbox();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });
  });
});
