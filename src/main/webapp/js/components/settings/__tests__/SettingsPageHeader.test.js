import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as appActions from 'state/actions/actions_app';

import SettingsPageHeader from 'components/settings/SettingsPageHeader';

describe('<SettingsPageHeader />', () => {
  let wrapper;
  let props = {
    dispatch: () => {},
    app: {
      notificationType: 'success',
      serverMessage: 'model built',
    },
  };

  const breadcrumb = [
    {
      value: 'Overview',
      label: 'Overview',
    },
  ];
  const testProps = {
    ...props,
    breadcrumb,
    title: 'Preferences',
  };

  beforeEach(() => {
    wrapper = mount(<SettingsPageHeader {...props} />);
  });

  describe('Creating an instance:', () => {
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots:', () => {
    test('should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render correctly on with breadcrumbs', () => {
      wrapper = shallow(<SettingsPageHeader {...testProps} />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.dispatch = jest.fn();
      appActions.stopShowingServerMessage = jest.fn(() => 'stopShowingServerMessage');
    });

    describe('closeNotification:', () => {
      test('should close the notification on closeNotification click', () => {
        wrapper = shallow(<SettingsPageHeader {...props} />);
        wrapper.instance().closeNotification();
        expect(appActions.stopShowingServerMessage).toHaveBeenCalledWith();
        expect(props.dispatch).toHaveBeenCalledWith('stopShowingServerMessage');
      });
    });
  });
});
