import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { Provider } from 'react-redux';
import store from 'state/configureStore';
import Constants from 'constants/Constants';
import CreateDatasetForm from 'components/datasets/create/CreateDatasetForm';
import { validate } from 'components/datasets/create/CreateDatasetForm';

describe('<CreateDatasetForm />', () => {
  let wrapper;

  const props = {
    userFeatureConfiguration: null,
  };

  beforeEach(() => {
    wrapper = shallow(<Provider store={store}><CreateDatasetForm {...props} /></Provider>);
  });

  describe('Creating an instance:', () => {
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  const validValues = {
    name: 'My First Dataset Name',
  };
  const InvalidValues = {
    name: 'Dataset$#%^&*#$^',
  };
  const LongValues = {
    name: 'lllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll',
  };

  describe('Dataset naming validation:', () => {
    test('dataset name expect only Alphanumeric values and space', () => {
      const validErrors = validate(validValues, props);
      expect(validErrors).toEqual({});
    });
    test('Only alphanumeric characters are allowed for Dataset name', () => {
      const InvalidCharsErrors = validate(InvalidValues, props);
      expect(InvalidCharsErrors).toEqual({ name: Constants.INVALID_ENTERED_NAME });
    });
    test('dataset name should not exceed to 64 characters', () => {
      const LongDataErrors = validate(LongValues, props);
      expect(LongDataErrors).toEqual({ name: Constants.VALIDATION_NAME_SIZE_MSG });
    });
  });
});
