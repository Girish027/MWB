import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { Settings } from 'components/settings/Settings';

describe('<Settings />', () => {
  const initialState = {
    selectedNavItem: 'Model Technologies',
    showDialog: true,
    isChecked: false,
    componentMounted: false,
    localUpdate: false,
    currentTechnology: 'n-gram',
    previousTechnology: 'use_large',
  };

  const testProps = {
    preferences: {
      technology: 'use_large',
    },
    getTechnology: jest.fn(() => 'called getTechnology'),
    addOrUpdateTechnology: jest.fn(() => 'called addOrUpdateTechnology'),
    getVectorizer: jest.fn(() => 'called getVectorizer'),
    header: {
      client: {
        id: '123',
      },
    },
    dispatch: jest.fn(),
  };

  let wrapper;
  beforeEach(() => {
    wrapper = shallow(<Settings
      {...testProps}
    />);
    wrapper.setState({
      ...initialState,
    });
  });

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (props) => shallow(<Settings
    {...props}
  />);

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getShallowWrapper(testProps);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('should match with basic props', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      testProps.dispatch = jest.fn();
    });

    test('should call onClickNavItem - Model Technologies', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickNavItem(['Model Technologies']);
      expect(wrapper.state().selectedNavItem).toBe('Model Technologies');
    });

    test('should call onClickNavItem - Model Monitoring', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickNavItem(['Model Monitoring']);
      expect(wrapper.state().selectedNavItem).toBe('Model Monitoring');
    });

    test('should call getStoredTechnology', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().getStoredTechnology();
      expect(wrapper.state().currentTechnology).toBe('use_large');
      expect(wrapper.state().previousTechnology).toBe('');
    });

    test('should call onClickTensorFlowTile', () => {
      wrapper.setState({ ...initialState, currentTechnology: 'use_large', previousTechnology: 'n-gram' });
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickTensorFlowTile();
      expect(wrapper.state().previousTechnology).toBe('use_large');
    });

    test('should call onClickNGramTile', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.setState({ ...initialState, currentTechnology: 'n-gram', previousTechnology: 'use_large' });
      wrapper.instance().onClickTensorFlowTile();
      expect(wrapper.state().previousTechnology).toBe('n-gram');
    });

    test('should call onClickCancel', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.setState({ ...initialState, currentTechnology: 'n-gram', previousTechnology: 'use_large' });
      wrapper.instance().onClickCancel();
      expect(wrapper.state().currentTechnology).toBe('use_large');
    });

    test('should call onClickCheckbox', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickCheckbox({ checked: true });
      expect(wrapper.state().isChecked).toBe(true);
    });
  });
});
