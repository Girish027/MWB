import React from 'react';
import { mount, shallow } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import ReduxConsistencyReportGrid from 'components/consistency/ConsistencyReportGrid';
import { ConsistencyReportGrid } from 'components/consistency/ConsistencyReportGrid';
import toJSON from 'enzyme-to-json';
import * as datasetsTransformed from 'state/actions/actions_datasets_transformed_tag';
import * as actionsApp from 'state/actions/actions_app';

describe('<ConsistencyReportGrid />', () => {
  let wrapper;

  const consistencyReport = {
    searchResults: [{ transcriptionHash: 'ddsad', intentConflict: true, suggestedIntent: true }],
    updateBulkTag: [],
    projectId: '12',
  };
  const props = {
    paddingTop: 12,
    currentPage: 2,
    maxPage: 12,
    consistencyReport,
    onTagsChange: jest.fn(),
    selection: new Map(),
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<ConsistencyReportGrid
    {...propsObj}
  />);

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <ReduxConsistencyReportGrid />
        </MemoryRouter>
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
  });

  describe('Snapshots', () => {
    test('renders correctly for default props on shallow copy', () => {
      wrapper = getShallowWrapper(props);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.dispatch = jest.fn();
      datasetsTransformed.showBulkTagCell = jest.fn(() => 'called showBulkTagCell');
      datasetsTransformed.removeTag = jest.fn(() => 'called removeTag');
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
    });

    describe('handleApplySuggestedClick', () => {
      test('should call handleApplySuggestedClick', () => {
        wrapper = getShallowWrapper(props);
        wrapper.setState({ selectedConflictRowsWithSuggestedIntent: [{ suggestedIntent: 'sdf', transcriptionHash: 'sff' }] });
        wrapper.instance().handleApplySuggestedClick();
      });
    });

    describe('handleBulkTagClick', () => {
      test('should call handleBulkTagClick', () => {
        wrapper = getShallowWrapper(props);
        wrapper.setState({ selectedIndexes: 'aasd' });
        wrapper.instance().handleBulkTagClick();
        expect(datasetsTransformed.showBulkTagCell).toHaveBeenCalledWith();
        expect(props.dispatch).toHaveBeenCalledWith('called showBulkTagCell');
      });
    });

    describe('handleBulkUnTagClick', () => {
      test('should call handleBulkUnTagClick', () => {
        wrapper = getShallowWrapper(props);
        wrapper.setState({ selectedIndexes: '', selectedTranscriptionHashes: ['fdf'] });
        wrapper.instance().handleBulkUnTagClick();
        expect(datasetsTransformed.removeTag).not.toHaveBeenCalled();
      });
    });

    describe('getSelectedIndexes', () => {
      test('should call getSelectedIndexes', () => {
        wrapper = getShallowWrapper(props);
        const result = wrapper.instance().getSelectedIndexes();
        expect(result).toStrictEqual([]);
      });

      test('should call getSelectedIndexes with props', () => {
        props.selection.get = jest.fn().mockReturnValue({ toJS: jest.fn().mockReturnValue({ indexes: [1, 2, 3] }) });
        wrapper = getShallowWrapper(props);
        const result = wrapper.instance().getSelectedIndexes();
        expect(result).toEqual([1, 2, 3]);
      });
    });

    describe('handleOnClickOk', () => {
      test('should call handleOnClickOk', () => {
        const transcriptionHashesBySuggestedIntent = [{ suggestedIntent: 'dsdfdf' }];
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleOnClickOk(transcriptionHashesBySuggestedIntent);
        expect(props.dispatch).toHaveBeenCalledWith('called modalDialogChange');
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
      });
    });

    describe('componentWillReceiveProps', () => {
      test('should call componentWillReceiveProps with searchResults', () => {
        const nextProps = {
          ...props,
        };
        wrapper = getShallowWrapper(props);
        wrapper.instance().getSelectedIndexes = jest.fn().mockReturnValue([0]);
        wrapper.instance().componentWillReceiveProps(nextProps);
        expect(wrapper.state().selectedIndexes).toStrictEqual([0]);
      });
    });
  });
});
