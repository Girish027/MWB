import React from 'react';
import { mount, shallow } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import EditorSimplePairs from 'components/modelConfigs/transformations//EditorSimplePairs';
import toJSON from 'enzyme-to-json';

describe('<EditorSimplePairs />', () => {
  let wrapper;
  let props;
  const listItems = [{
    key: '"vfff"',
    value: '_class_email',
  }, {
    key: 'dsdssa',
    value: 'sdfdsf',
  }];

  const initialState = { listItems };

  const transformation = {
    'non-breaking-space-regex': {
      type: 'regex-replace',
      mappings: {
        '/\\xao/i': ' ',
      },
    },
  };
  beforeAll(() => {
    props = {
      onlySubstText: true,
      modelViewReadOnly: false,
      dispatch: jest.fn(),
      isTransformationValid: true,
      onUpdateList: jest.fn(),
      displayGoodRequestMessage: jest.fn(),
      transformation,
      transformationItems: {},
      currentTransformationName: 'chat-shortcuts',
      helpComponent: '<div/>',
    };
  });

  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <EditorSimplePairs />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly with props', () => {
      wrapper = shallow(<EditorSimplePairs
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders getItems correctly with props', () => {
      wrapper = shallow(<EditorSimplePairs
        {...props}
      />);
      wrapper.instance().getItems(listItems);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders getItems correctly with listItems as undefined', () => {
      wrapper = shallow(<EditorSimplePairs
        {...props}
      />);
      wrapper.instance().getItems(undefined);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders getItems correctly with listItems and onlySubstText as false', () => {
      wrapper = shallow(<EditorSimplePairs
        {...props}
        onlySubstText={false}
      />);
      wrapper.instance().getItems(listItems);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      wrapper = shallow(<EditorSimplePairs
        {...props}
        onlySubstText={false}
      />);
      wrapper.setState({
        initialState,
      });
      wrapper.update();
    });

    describe('saveChanges:', () => {
      test('should set listItems state onClickDelete call', () => {
        wrapper.instance().saveChanges(listItems);
        expect(props.onUpdateList).toHaveBeenCalled();
      });
    });

    describe('onClickDelete:', () => {
      const event = {
        stopPropagation: jest.fn(),
      };
      test('should set listItems state onClickDelete call', () => {
        wrapper.instance().onClickDelete(event, 1);
        expect(wrapper.state().listItems).toEqual([]);
      });
    });

    describe('onClickAdd:', () => {
      const event = {
        persist: jest.fn(),
      };
      test('should update state with empty value on onClickAdd call on onlyRegex as false', () => {
        wrapper.instance().onClickAdd(event);
        expect(wrapper.state().listItems).toEqual([{ key: '', value: '' }]);
        expect(wrapper.state().unsavedChanges).toEqual(true);
      });

      test('should update state with _class_ as value on onClickAdd call on onlyRegex as true', () => {
        wrapper = shallow(<EditorSimplePairs
          {...props}
          onlyRegex
        />);
        wrapper.setState({
          listItems,
        });
        wrapper.update();
        wrapper.instance().onClickAdd(event);
        expect(wrapper.state().listItems).toEqual([{ key: '', value: '_class_' }]);
        expect(wrapper.state().unsavedChanges).toEqual(true);
      });
    });

    describe('onDragEnd:', () => {
      test('should update state on onDragEnd call', () => {
        const result = {
          destination: false,
        };
        wrapper.instance().onDragEnd(result);
        expect(wrapper.state().listItems).toEqual([]);
        expect(wrapper.state().unsavedChanges).toEqual(false);
      });
    });
  });
});
