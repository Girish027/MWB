
import React from 'react';
import { shallow, mount } from 'enzyme';
import configureStore from 'redux-mock-store';
import { Provider } from 'react-redux';
import ModelTestBatchResultsGrid from 'components/models/ModelTestBatchResultsGrid';
import BatchTestConstants from 'constants/BatchTestConstants';
import toJSON from 'enzyme-to-json';

describe('<ModelTestBatchResultsGrid />', () => {
  const tableData = [{
    intent: 'Agent_Query',
    transcription: 'I need help',
    predict1: 'Agent_Query',
  }, {
    intent: 'Reservation_Cancel',
    transcription: 'I want to cancel my booking',
    predict1: 'reservation_cancel',
  }, {
    intent: 'Reservation_Cancel',
    transcription: 'cancel my reservation for',
    predict1: 'Reservation_Cancel',
  }];
  const props = {
    data: tableData,
    onRunSingleUtterance: jest.fn(),
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<ModelTestBatchResultsGrid
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<ModelTestBatchResultsGrid
        {...props}
      />);
    });

    test('renders correctly with batch test results in table', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when Misclassified filter is selected', () => {
      wrapper.instance().setTranscriptionFilter('Misclassified');
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when Intent filter is enabled with an intent', () => {
      wrapper.instance().setIntentsFilter(wrapper.state().listOfIntents[1]);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when both filters are selected', () => {
      wrapper.instance().setTranscriptionFilter({
        selectedType: BatchTestConstants.TRANSCRIPTIONS_FILTER_LIST[1],
      });
      wrapper.instance().setIntentsFilter({
        selectedType: wrapper.state().listOfIntents[1],
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      wrapper = shallow(<ModelTestBatchResultsGrid
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('componentDidMount: ', () => {
      test('should populate the listOfIntents with uniqueintents from the batch test results', () => {
        wrapper.setState({
          listOfIntents: [BatchTestConstants.FILTER_SELECTED_ALL],
        });
        wrapper.instance().componentDidMount();
        expect(wrapper.state().listOfIntents).toEqual(['All', 'Agent_Query', 'Reservation_Cancel']);
      });

      test('should not repopulate listOfIntents if they are already present', () => {
        wrapper.setState({
          listOfIntents: [BatchTestConstants.FILTER_SELECTED_ALL, 'Agent_Query'],
        });
        wrapper.instance().componentDidMount();
        expect(wrapper.state().listOfIntents).toEqual(['All', 'Agent_Query']);
      });
    });

    describe('onClickTranscription:', () => {
      test('should run single utterance test on the clicked transcription', () => {
        wrapper.instance().onClickTranscription({
          target: {
            innerText: wrapper.state().filteredData[1].transcription,
          },
        });
        expect(props.onRunSingleUtterance).toHaveBeenCalledWith(wrapper.state().filteredData[1].transcription);
      });
    });

    describe('getScore:', () => {
      test('should return score with 4 decimal digits', () => {
        const score = wrapper.instance().getScore('9.01233');
        expect(score).toEqual('9.0123');
      });

      test('should return empty string when score is not defined', () => {
        const score = wrapper.instance().getScore(undefined);
        expect(score).toEqual('');
      });
    });
  });
});
