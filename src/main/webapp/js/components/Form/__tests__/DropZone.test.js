import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { Doc } from '@tfs/ui-components';
import Dropzone from 'components/Form/Dropzone';
import CsvIcon from 'components/Icons/CsvIcon';

describe('<Dropzone />', () => {
  let wrapper;

  const props = {
    accept: 'text/csv, application/vnd.ms-excel, .csv',
    icon: { CsvIcon },
    acceptedIcon: { Doc },
    maxSize: 10,
    minSize: 5,
    multiple: false,
    saveFile: jest.fn(),
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<Dropzone
    {...propsObj}
  />);

  const getMountWrapper = (propsObj) => mount(<Dropzone
    {...propsObj}
  />);

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getMountWrapper();
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly with default props', () => {
      wrapper = getShallowWrapper(props);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
