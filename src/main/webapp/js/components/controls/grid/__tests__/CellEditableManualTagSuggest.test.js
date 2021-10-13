import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { CellEditableManualTagSuggest } from 'components/controls/grid/CellEditableManualTagSuggest';

describe('<CellEditableManualTagSuggest />', () => {
  let wrapper;

  const props = {
    dispatch: jest.fn(),
    stateKey: 'sdffds',
    projectId: '23',
    intent: 'sdff',
    suggestions: {
      cursor: 2,
      intent: 'sdff',
      suggestedTags: [],
    },
  };

  const getShallowWrapper = (propsObj) => shallow(<CellEditableManualTagSuggest
    {...propsObj}
  />);

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = mount(<CellEditableManualTagSuggest
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      const defaultProps = {
        ...props,
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with suggestedTags not empty', () => {
      const defaultProps = {
        ...props,
        suggestions: {
          intent: 'sdff',
          suggestedTags: ['ddf'],
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with highlightTags', () => {
      const defaultProps = {
        ...props,
        highlightTags: ['ddf'],
        suggestions: {
          intent: 'sdff',
          suggestedTags: ['ddf'],
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    describe('handleSuggestHover', () => {
      test('should call handleSuggestHover with event', () => {
        const event = {
          target: {
            getAttribute: jest.fn().mockImplementation(() => ('tags')),
          },
        };
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleSuggestHover(event);
      });
    });

    describe('handleKeyPress', () => {
      test('should call handleKeyPress with no event', () => {
        const event = {
        };
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleKeyPress(event);
      });

      test('should call handleKeyPress with event key as Up', () => {
        const defaultProps = {
          ...props,
          highlightTags: ['ddf'],
          suggestions: {
            cursor: 1,
            intent: 'sdff',
            suggestedTags: ['ddf'],
          },
        };
        const event = {
          key: 'Up',
        };
        wrapper = getShallowWrapper(defaultProps);
        wrapper.setState({ keyboardSelectedIndex: 0 });
        wrapper.instance().handleKeyPress(event);
        expect(wrapper.state().keyboardSelectedIndex).toEqual(0);
      });

      test('should call handleKeyPress with event key as Down case 1', () => {
        const defaultProps = {
          ...props,
          highlightTags: ['ddf'],
          suggestions: {
            cursor: 1,
            intent: 'sdff',
            suggestedTags: ['ddf'],
          },
        };
        const event = {
          key: 'Down',
        };
        wrapper = getShallowWrapper(defaultProps);
        wrapper.setState({ keyboardSelectedIndex: 1 });
        wrapper.instance().handleKeyPress(event);
        expect(wrapper.state().keyboardSelectedIndex).toEqual(0);
      });

      test('should call handleKeyPress with event key as Down case 2', () => {
        const defaultProps = {
          ...props,
          highlightTags: ['ddf'],
          suggestions: {
            cursor: 1,
            intent: 'sdff',
            suggestedTags: ['ddf', 'sfdsfdf'],
          },
        };
        const event = {
          key: 'Down',
        };
        wrapper = getShallowWrapper(defaultProps);
        wrapper.setState({ keyboardSelectedIndex: 0 });
        wrapper.instance().handleKeyPress(event);
        expect(wrapper.state().keyboardSelectedIndex).toEqual(1);
      });
    });

    describe('moveSelection', () => {
      test('should call moveSelection', () => {
        wrapper = getShallowWrapper(props);
        wrapper.setState({ keyboardSelectedIndex: 1, cursor: 1 });
        wrapper.instance().moveSelection();
      });

      test('should call moveSelection with suggestions cursor is same as state cursor', () => {
        const defaultProps = {
          ...props,
          highlightTags: ['ddf'],
          suggestions: {
            cursor: 1,
            intent: 'sdff',
            suggestedTags: ['ddf'],
          },
        };
        wrapper = getShallowWrapper(defaultProps);
        wrapper.setState({ keyboardSelectedIndex: 0, cursor: 1 });
        wrapper.instance().moveSelection();
        expect(wrapper.state().cursor).toEqual(1);
      });

      test('should call moveSelection with suggestedTags', () => {
        const defaultProps = {
          ...props,
          highlightTags: ['ddf'],
          suggestions: {
            cursor: 2,
            intent: 'sdff',
            suggestedTags: ['ddf'],
          },
        };
        wrapper = getShallowWrapper(defaultProps);
        wrapper.setState({ keyboardSelectedIndex: 1, cursor: 1 });
        wrapper.instance().moveSelection();
        expect(wrapper.state().cursor).toEqual(2);
      });
    });

    describe('componentWillReceiveProps', () => {
      test('should call componentWillReceiveProps', () => {
        const nextProps = {
          intent: 'ffds',
          stateKey: 'dsf',
          projectId: '23',
          suggestions: {
            cursor: 2,
          },
        };
        wrapper = getShallowWrapper(props);
        wrapper.setState({ keyboardSelectedIndex: 1, cursor: 1 });
        wrapper.instance().moveSelection = jest.fn();
        wrapper.instance().componentWillReceiveProps(nextProps);
        expect(wrapper.instance().moveSelection).toHaveBeenCalledWith();
      });
    });
  });
});
