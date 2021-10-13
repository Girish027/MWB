import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import * as actionsDatasetCreate from 'state/actions/actions_dataset_create';
import DatasetDialog from 'components/controls/DatasetDialog';

describe('<DatasetDialog />', () => {
  let wrapper;
  const props = {
    header: Constants.UPLOAD_DATASET,
    onCancel: () => {},
    dispatch: () => {},
  };

  const getShallowWrapper = (propsObj) => shallow(<DatasetDialog
    {...props}
  />);

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = getShallowWrapper(props);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Function Call Handler:', () => {
    beforeAll(() => {
      props.header = Constants.CREATE_MODEL,
      props.dispatch = jest.fn();
      props.onCancel = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
      actionsDatasetCreate.importFile = jest.fn(() => 'called importFile');
    });

    beforeEach(() => {
      wrapper = shallow(<DatasetDialog
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('onClickCancel:', () => {
      test('should onClickCancel call click handler', () => {
        wrapper.instance().onClickCancel();
        expect(props.dispatch).toHaveBeenCalledWith('called modalDialogChange');
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
      });
    });

    describe('onSubmit:', () => {
      test('should call importFile action', () => {
        wrapper.instance().onSubmit();
        expect(props.dispatch).toHaveBeenCalledWith('called importFile');
      });
    });
  });

  describe('onFormChange:', () => {
    test('should change the state when there are no error', () => {
      const data = {
        name: 'abc',
        description: 'form',
        dropZone: [{ acceptedFile: 1 }],
      };
      wrapper.instance().onFormChange(data, []);
      expect(wrapper.state().formData).toBe(data);
      expect(wrapper.state().isValid).toBe(true);
    });

    test('should change the state when there are no error', () => {
      const data = {
        name: 'abc',
        description: 'form',
      };
      wrapper.instance().onFormChange(data, [{ name: 'is required' }]);
      expect(wrapper.state().isValid).toBe(false);
    });
  });
});
