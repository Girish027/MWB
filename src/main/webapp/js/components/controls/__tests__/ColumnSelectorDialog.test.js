import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import { getLanguage } from 'state/constants/getLanguage';
import ColumnSelectorDialog from 'components/controls/ColumnSelectorDialog';

describe('<ColumnSelectorDialog />', () => {
  let wrapper;

  const TABLEDATA = {
    Count: {
      header: 'Count',
      id: 'count',
    },
    Datasets: {
      header: 'Datasets',
      id: 'datasetIds',
    },
    UniqueTextString: {
      header: 'Unique Text String',
      id: 'uniqueTextString',
    },
    SuggestedCategory: {
      header: 'Suggested Category',
      id: 'suggestedTag',
    },
    GranularIntents: {
      header: 'Granular Intent',
      id: 'manualTag',
    },
    RuTag: {
      header: 'Rollup Intent',
      id: 'rutag',
    },
    Comments: {
      header: 'Comments',
      id: 'comment',
    },
  };

  const props = {
    columnData: TABLEDATA,
  };

  const initialState = { modifiedData: {} };

  describe('Creating an instance with no props', () => {
    beforeEach(() => {
      wrapper = shallow(<ColumnSelectorDialog />);
    });
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = shallow(<ColumnSelectorDialog
        {...props}
      />);
      wrapper.setState({
        initialState,
      });
      wrapper.update();
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality', () => {
    beforeAll(() => {
      props.dispatch = jest.fn();
      props.handler = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
    });

    beforeEach(() => {
      wrapper = shallow(<ColumnSelectorDialog
        {...props}
      />);
    });

    describe('Close Dialog', () => {
      test('should close the dialog', () => {
        const wrapperInstance = wrapper.instance();
        wrapperInstance.onClickClose();
        expect(wrapperInstance.state).toEqual(initialState);
      });
    });

    describe('State change', () => {
      const event = {
        value: 'count',
        checked: true,
      };
      test('should change the state when checkbox is clicked', () => {
        const wrapperInstance = wrapper.instance();
        wrapperInstance.onClickCheckbox(event);
        expect(wrapperInstance.state.modifiedData.count.visible).toEqual(false);
      });
    });
  });
});
