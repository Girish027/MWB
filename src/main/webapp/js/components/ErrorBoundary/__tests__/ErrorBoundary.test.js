import React from 'react';
import { mount, shallow } from 'enzyme';
import Constants from 'constants/Constants';
import toJSON from 'enzyme-to-json';
import ErrorBoundary from 'components/ErrorBoundary/ErrorBoundary';

describe('<ErrorBoundary />', () => {
  let wrapper;
  let props = {};
  describe('Snapshots:', () => {
    beforeEach(() => {
      wrapper = shallow(
        <ErrorBoundary {...props} />,
      );
    });
    test('should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
