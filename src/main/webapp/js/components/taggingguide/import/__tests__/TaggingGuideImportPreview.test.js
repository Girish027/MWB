import React from 'react';
import { shallow, mount } from 'enzyme';
import { Provider } from 'react-redux';
import toJSON from 'enzyme-to-json';
import store from 'state/configureStore';
import TaggingGuideImportPreview from 'components/taggingguide/import/TaggingGuideImportPreview';

describe('<TaggingGuideImportPreview />', () => {
  let wrapper;

  const props = {
    data: ['dsfdsf'],
    columns: [{ name: 'column1', required: true }],
    columnsBinding: { dsd: 0 },
    onColumnBindChange: jest.fn(),
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<TaggingGuideImportPreview
    {...propsObj}
  />);

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = shallow(<Provider store={store}>
        <TaggingGuideImportPreview
          match={{
            params: {
              clientId: 1,
              projectId: 2,
            },
          }}
          data={props.data}
          columns={props.columns}
        />
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getShallowWrapper(props);
      expect(wrapper.exists()).toBe(true);
    });

    test('should exist', () => {
      const defaultProps = {
        ...props,
        data: [],
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      wrapper = mount(<TaggingGuideImportPreview
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props', () => {
      const defaultProps = {
        ...props,
        data: [],
      };
      wrapper = mount(<TaggingGuideImportPreview
        {...defaultProps}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with state set as true', () => {
      wrapper = getShallowWrapper(props);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    describe('onChange', () => {
      const option = {
        value: 'column1',
      };
      test('should call onChange with 1 columns', () => {
        const boundByIndex = ['column1', 'column2'];
        const columnsBinding = { column1: { name: 'column1', id: '12' } };
        wrapper = getShallowWrapper(props);
        wrapper.setState({ columnsBinding, boundByIndex });
        wrapper.instance().columnsByName = { column1: { name: 'fdsfs', id: '12' } };
        wrapper.instance().onChange(option, 0);
      });

      test('should call onChange with 2 columns', () => {
        const defaultProps = {
          ...props,
          columns: [{ name: 'column1', required: true }, { name: 'column2', required: true }],
        };
        const boundByIndex = ['column1', 'column2'];
        const columnsBinding = { column1: { name: 'column1', id: '12' } };
        wrapper = getShallowWrapper(defaultProps);
        wrapper.setState({ columnsBinding, boundByIndex });
        wrapper.instance().columnsByName = { column1: { name: 'fdsfs', id: '12' } };
        wrapper.instance().onChange(option, 0);
      });
    });

    describe('handleScroll', () => {
      test('should call handleScroll and set state', () => {
        const event = {
          target: {
            scrollTop: 3,
          },
        };
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleScroll(event);
        expect(wrapper.state().scrollTop).toBe(3);
      });
    });
  });
});
