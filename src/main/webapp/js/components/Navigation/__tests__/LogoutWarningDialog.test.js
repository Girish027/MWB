import React from 'react';
import { mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import LogoutWarningDialog from 'components/Navigation/LogoutWarningDialog';

describe('<LogoutWarningDialog />', () => {
  const props = {
    isOpen: true,
    iconLink: 'http://link-to-icon.svg',
    productName: 'Engagement Cloud',
    onClose: () => {},
    onClickOk: () => {},
  };

  const callbacks = {};

  beforeEach(() => {
    props.onClose = jest.fn();
    document.addEventListener = jest.fn((eventName, callback) => {
      callbacks[eventName] = callback;
    });

    document.removeEventListener = jest.fn((eventName) => {
      delete callbacks[eventName];
    });
  });

  afterAll(() => {
    jest.clearAllMocks();
  });

  const renderComponent = (props = {}) => mount(
    <LogoutWarningDialog {...props} />,
  );

  it('should match snapshot', () => {
    const wrapper = renderComponent(props);
    expect(toJSON(wrapper)).toMatchSnapshot();
  });

  it('should unmount', () => {
    const wrapper = renderComponent(props);
    wrapper.unmount();
  });

  it('should close dialog', () => {
    // eslint-disable-next-line no-unused-vars
    const wrapper = mount(<div>
      {' '}
      <LogoutWarningDialog {...props} />
      {' '}
    </div>);
    // simulating the event by calling the registered eventhandler
    callbacks.keydown({
      stopPropagation: () => {},
      preventDefault: () => {},
      keyCode: 27,
    });
    expect(props.onClose).toHaveBeenCalled();
  });
});
