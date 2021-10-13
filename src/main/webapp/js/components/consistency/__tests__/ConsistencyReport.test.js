import React from 'react';
import { mount, shallow } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter, Switch, Route } from 'react-router';
import store from 'state/configureStore';
import ReduxConsistencyReport from 'components/consistency/ConsistencyReport';
import { ConsistencyReport } from 'components/consistency/ConsistencyReport';
import toJSON from 'enzyme-to-json';
import * as actionsConsistencyReport from 'state/actions/actions_consistency_report';
import * as actionsCellEditable from 'state/actions/actions_cellEditable';
import * as actionscellEditableManualTagSuggest from 'state/actions/actions_cellEditableManualTagSuggest';

describe('<ConsistencyReport />', () => {
  let wrapper;
  const datasets = [{ id: '2', status: 'COMPLETED', name: 'abc' },
    { id: '3', status: 'COMPLETED', name: 'abc1' }];
  const consistencyReport = {
    filter: { datasets: [] },
    projectId: '12',
    query: 'sd',
    sort: 'ASC',
    isSearching: false,
    isError: false,
    showControls: true,
    limit: 10,
    startIndex: 0,
    total: 20,
    isUpdatingTags: false,
  };
  const props = {
    consistencyReport,
    selectedProjectId: '13',
    datasets,
    dispatch: jest.fn(),
    project: {
      id: '12',
      name: 'project1',
    },
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<ConsistencyReport
    {...propsObj}
  />);

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/resolve-inconsistency']}
          initialIndex={0}
        >
          <Switch>
            <Route path="/resolve-inconsistency" component={ReduxConsistencyReport} />
          </Switch>
        </MemoryRouter>
      </Provider>);

      wrapper.setProps({
        selectedProjectId: '-1',
        datasets: new Map(),
        project: {},
      });
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('should contain ConsistencyReport', () => {
      expect(wrapper.find(ReduxConsistencyReport).length).toBe(1);
    });

    test('should not contain div#ConsistencyReportGridContainer', () => {
      const find = wrapper.find('div#ConsistencyReportGridContainer');
      expect(find).toHaveLength(0);
    });
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getShallowWrapper(props);
      expect(wrapper.exists()).toBe(true);
    });

    test('should exist with state set', () => {
      wrapper = getShallowWrapper(props);
      wrapper.setState({ loadingProject: false, loadingDatasets: true, runPressed: true });
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props on mount', () => {
      wrapper = mount(<ConsistencyReport
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props on shallow copy', () => {
      wrapper = getShallowWrapper(props);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.dispatch = jest.fn();
      actionsCellEditable.stateReset = jest.fn(() => 'called actionsCellEditable stateReset');
      actionscellEditableManualTagSuggest.stateReset = jest.fn(() => 'called actionscellEditableManualTagSuggest stateReset');
      actionsConsistencyReport.requestSearch = jest.fn(() => 'called requestSearch');
      actionsConsistencyReport.setFilter = jest.fn(() => 'called setFilter');
      actionsConsistencyReport.setPagerSettings = jest.fn(() => 'called setPagerSettings');
      actionsCellEditable.stateRemove = jest.fn(() => 'called actionsCellEditable stateRemove');
      actionsConsistencyReport.reset = jest.fn(() => 'called reset');
      actionscellEditableManualTagSuggest.stateRemove = jest.fn(() => 'called actionscellEditableManualTagSuggest stateRemove');
    });

    describe('onClickConsistencyReportRun', () => {
      test('should call onClickConsistencyReportRun with runPressed as true', () => {
        wrapper = getShallowWrapper(props);
        wrapper.setState({ runPressed: true });
        wrapper.instance().performSearch = jest.fn();
        wrapper.instance().onClickConsistencyReportRun();
        expect(wrapper.instance().performSearch).toHaveBeenCalledWith();
      });

      test('should call onClickConsistencyReportRun with runPressed as false', () => {
        wrapper = getShallowWrapper(props);
        wrapper.setState({ runPressed: false });
        wrapper.instance().onClickConsistencyReportRun();
        expect(wrapper.state().runPressed).toBe(true);
      });
    });

    describe('onChangeRadioButton', () => {
      test('should call onChangeRadioButton with selected', () => {
        const selected = 'false';
        wrapper = getShallowWrapper(props);
        wrapper.instance().onChangeRadioButton(selected);
        expect(actionsConsistencyReport.setFilter).toHaveBeenCalledWith({ onlyConflicts: false });
        expect(props.dispatch).toHaveBeenCalledWith('called setFilter');
      });
    });

    describe('onChangeMultipleCheckboxFilter', () => {
      test('should call onChangeMultipleCheckboxFilter with selected', () => {
        const newValue = '123';
        wrapper = getShallowWrapper(props);
        wrapper.instance().onChangeMultipleCheckboxFilter(newValue);
        expect(actionsConsistencyReport.setFilter).toHaveBeenCalledWith({ datasets: newValue });
        expect(props.dispatch).toHaveBeenCalledWith('called setFilter');
      });
    });

    describe('onChangeMultipleCheckboxFilter', () => {
      test('should call onChangeMultipleCheckboxFilter with limit, startIndex, and showControls', () => {
        const limit = 10;
        const startIndex = 0;
        const showControls = true;
        wrapper = getShallowWrapper(props);
        wrapper.instance().onChangeTaggerGridPager(startIndex, limit, showControls);
        expect(actionsConsistencyReport.setPagerSettings).toHaveBeenCalledWith({ startIndex, limit, showControls });
        expect(props.dispatch).toHaveBeenCalledWith('called setPagerSettings');
      });
    });

    describe('componentWillUnmount', () => {
      test('should call componentWillUnmount', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().cellEditableStateKey = 'stateKey';
        wrapper.instance().componentWillUnmount();
        expect(actionsConsistencyReport.reset).toHaveBeenCalledWith();
        expect(actionsCellEditable.stateRemove).toHaveBeenCalledWith({ stateKey: wrapper.instance().cellEditableStateKey });
        expect(actionscellEditableManualTagSuggest.stateRemove).toHaveBeenCalledWith({ stateKey: wrapper.instance().cellEditableStateKey });
        expect(props.dispatch).toHaveBeenCalledWith('called actionsCellEditable stateRemove');
        expect(props.dispatch).toHaveBeenCalledWith('called reset');
        expect(props.dispatch).toHaveBeenCalledWith('called actionscellEditableManualTagSuggest stateRemove');
      });
    });

    describe('componentWillReceiveProps', () => {
      test('should call componentWillReceiveProps with nextProps', () => {
        const nextProps = {
          ...props,
        };
        wrapper = getShallowWrapper(props);
        wrapper.instance().componentWillReceiveProps(nextProps);
        expect(props.dispatch).toHaveBeenCalledWith({ stateKey: 'ConsistencyReport', type: 'CELL_EDITABLE_STATE_CREATE' });
      });
    });
  });
});
