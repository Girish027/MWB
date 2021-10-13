import React from 'react';
import { mount, shallow } from 'enzyme';
import renderer from 'react-test-renderer';
import toJSON from 'enzyme-to-json';
import ReactDOM from 'react-dom';
import LocaleSelect from 'components/common/LocaleSelect';


describe('<LocaleSelect />', () => {
  let wrapper;

  const getShallowWrapper = () => shallow(<LocaleSelect />);

  describe('Snapshots', () => {
    test('renders correctly', () => {
      wrapper = getShallowWrapper();
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should call processResults', () => {
      wrapper = getShallowWrapper();
      wrapper.instance().processResults({ body: ['2332'] });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should call processResults without body', () => {
      wrapper = getShallowWrapper();
      wrapper.instance().processResults({ bgcolor: ['232'] });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
