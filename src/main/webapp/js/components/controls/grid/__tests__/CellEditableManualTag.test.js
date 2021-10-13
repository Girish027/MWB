import React from 'react';
import { mount, shallow } from 'enzyme';
import thunkMiddleware from 'redux-thunk';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import { appReducer } from 'state/reducers/app_reducers';
import { headerReducer, defaultState as defaultHeaderState } from 'state/reducers/header_reducers';
import { cellEditableReducer } from 'state/reducers/cellEditable_reducers';
import { cellEditableManualTagSuggestReducer } from 'state/reducers/cellEditableManualTagSuggest_reducers';
import * as actionsCellEditable from 'state/actions/actions_cellEditable';
import * as actionsCellEditableManualTagSuggest from 'state/actions/actions_cellEditableManualTagSuggest';
import ReduxCellEditableManualTag from 'components/controls/grid/CellEditableManualTag';
import { CellEditableManualTag } from 'components/controls/grid/CellEditableManualTag';
import toJSON from 'enzyme-to-json';

const mockStore = {
  header: {
    ...defaultHeaderState,
    username: 'TestUser@247-inc.com',
  },
};

const rootReducer = combineReducers({
  app: appReducer,
  header: headerReducer,
  cellEditable: cellEditableReducer,
  cellEditableManualTagSuggest: cellEditableManualTagSuggestReducer,
});

const store = createStore(rootReducer, mockStore, applyMiddleware(thunkMiddleware));

const getShallowWrapper = (propsObj) => shallow(<CellEditableManualTag
  {...propsObj}
/>);

const defaultProps = {
  stateKey: 'TestEditable',
  value: 'abc',
  projectId: '-1',
  rowIndex: 1,
  columnIndex: 1,
  onChange: jest.fn(),
  editable: {
    activeRowIndex: 1,
    activeColumnIndex: 1,
  },
};

describe('<CellEditableManualTag />', () => {
  let wrapper;
  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getShallowWrapper(defaultProps);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props with state set as true', () => {
      wrapper = getShallowWrapper(defaultProps);
      wrapper.setState({ editedValue: 'dff' });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Creating an instance', () => {
    let wrapper;

    test('should create state', () => {
      expect(store.getState().cellEditable.size).toBe(0);
      store.dispatch(actionsCellEditable.stateCreate({ stateKey: defaultProps.stateKey }));
      expect(store.getState().cellEditable.size).toBe(1);
      expect(store.getState().cellEditableManualTagSuggest.size).toBe(0);
      store.dispatch(actionsCellEditableManualTagSuggest.stateCreate({ stateKey: defaultProps.stateKey }));
      expect(store.getState().cellEditableManualTagSuggest.size).toBe(1);
    });

    test('should create instance', () => {
      wrapper = mount(<Provider store={store}>
        <ReduxCellEditableManualTag
          {...defaultProps}
        />
      </Provider>);
      expect(wrapper.exists()).toBe(true);
      expect(wrapper.find(ReduxCellEditableManualTag).length).toBe(1);
    });

    test('should create instance with default props', () => {
      const props = {
        ...defaultProps,
        className: 'sad',
        rowIndex: null,
        columnIndex: null,
      };
      wrapper = mount(<Provider store={store}>
        <ReduxCellEditableManualTag
          {...props}
        />
      </Provider>);
      wrapper.setState({ editedValue: 'new Option' });
      expect(wrapper.exists()).toBe(true);
      expect(wrapper.find(ReduxCellEditableManualTag).length).toBe(1);
    });
  });

  describe('Functionality:', () => {
    describe('componentWillReceiveProps', () => {
      test('should call componentWillReceiveProps with nextProps', () => {
        const nextProps = {
          value: 'abcd',
        };
        wrapper = getShallowWrapper(defaultProps);
        wrapper.instance().componentWillReceiveProps(nextProps);
        expect(wrapper.state().editedValue).toEqual(defaultProps.value);
        expect(wrapper.state().isEditedValueValid).toEqual(true);
      });
    });

    describe('onEdit', () => {
      test('should call onEdit with newValue and isValid as true', () => {
        const newValue = 'abcd';
        wrapper = getShallowWrapper(defaultProps);
        wrapper.instance().onEdit(newValue, true);
        expect(wrapper.state().editedValue).toEqual(newValue);
      });
    });

    describe('onChange', () => {
      test('should call onChange with ', () => {
        const newValue = 'abcd';
        wrapper = getShallowWrapper(defaultProps);
        wrapper.instance().onChange(newValue);
        expect(defaultProps.onChange).toHaveBeenCalledWith(newValue);
      });
    });

    describe('onValidChange', () => {
      test('should call onValidChange with ', () => {
        const newValue = 'abcd';
        wrapper = getShallowWrapper(defaultProps);
        wrapper.instance().onValidChange(newValue);
      });
    });
  });
});
