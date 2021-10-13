import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import CreateBatchTest from 'components/models/batchTest/CreateBatchTest';
import * as actionsModels from 'state/actions/actions_models';

describe('<CreateBatchTest />', () => {
  const createDataset = (id, name, createdAt) => ({
    _key: id,
    id,
    clientId: 1,
    projectId: 2,
    name,
    type: 'Audio/Voice (Data Collection)',
    description: 'test dataset',
    locale: 'en_US',
    createdAt,
    status: 'COMPLETED',
    task: 'INDEX',
  });

  let datasets;
  let props;
  let wrapper;

  beforeAll(() => {
    datasets = {
      1: createDataset(1, 'dataset 1', 1536777405845),
      2: createDataset(2, 'dataset 2', 1536777805845),
      3: createDataset(3, 'dataset 3', 1536778005208),
    };

    props = {
      history: {},
      app: {
        userId: '247-test',
        csrfToken: 'token',
      },
      clientId: '1',
      client: {
        id: '1',
        name: 'abc',
      },
      projectId: '2',
      modelId: '3',
      model: {
        modelToken: '8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451',
        name: 'modelName',
        version: 2,
      },
      datasets,
      showBatchTestResults: () => {},
      dispatch: () => {},
    };
  });

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<CreateBatchTest
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<CreateBatchTest
        {...props}
      />);
    });

    test('renders correctly with given datasets', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when one of the datasets is selected - Run Test button is enabled', () => {
      wrapper.instance().updateSelectedDatasets([1]);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    let commonData = {};

    beforeAll(() => {
      props.showBatchTestResults = jest.fn();
      actionsModels.modelBatchTest = jest.fn();
      commonData = {
        userId: props.app.userId,
        csrfToken: props.app.csrfToken,
        projectId: props.projectId,
        modelId: props.model.modelToken,
        clientId: props.clientId,
      };
    });

    beforeEach(() => {
      wrapper = shallow(<CreateBatchTest
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('updateSelectedDatasets:', () => {
      test('should update the selected Datasets and checked state - case:none selected', () => {
        wrapper.instance().updateSelectedDatasets([]);
        expect(wrapper.state().selectedDatasets).toEqual([]);
        expect(wrapper.state().checked).toEqual(false);
      });

      test('should update the selected Datasets and checked state - case: datasets are selected', () => {
        wrapper.instance().updateSelectedDatasets([1]);
        expect(wrapper.state().selectedDatasets).toEqual([1]);
        expect(wrapper.state().checked).toEqual(true);
      });
    });

    describe('onRunBatchTest:', () => {
      test('should call the showBatchtestResults to update the state of parent component', () => {
        wrapper.instance().onRunBatchTest();
        expect(props.showBatchTestResults).toHaveBeenCalled;
      });

      test('should trigger the model batch test action with the seleted batch tests and the batch test name', () => {
        wrapper.setState({
          selectedDatasets: [1, 2],
          batchTestName: 'New Batch Test',
        });
        wrapper.instance().onRunBatchTest([1, 2]);
        expect(actionsModels.modelBatchTest).toHaveBeenCalledWith({
          ...commonData,
          datasets: [1, 2],
          batchTestName: 'New Batch Test',
        });
      });
    });
  });
});
