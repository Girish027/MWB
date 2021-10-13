import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import Dialog from 'components/common/Dialog/Dialog';

describe('<Dialog />', () => {
  describe('Creating an instance', () => {
    let wrapper;
    beforeAll(() => {
      wrapper = shallow(<Dialog
        visible={false}
      >
        <div> dialog message </div>
      </Dialog>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
    test('snapshot to match', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
