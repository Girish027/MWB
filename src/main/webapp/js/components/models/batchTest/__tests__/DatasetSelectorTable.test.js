import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import DatasetSelectorTable from 'components/models/batchTest/DatasetSelectorTable';

describe('<DatasetSelectorTable />', () => {
  const createDataset = (id, name, createdAt, status = 'COMPLETED') => ({
    _key: id,
    id,
    clientId: 1,
    projectId: 2,
    name,
    type: 'Audio/Voice (Data Collection)',
    description: 'test dataset',
    locale: 'en_US',
    createdAt,
    status,
    task: 'INDEX',
    isClickable: true,
  });

  let datasets;
  let props;
  let wrapper;
  let expectedData;

  beforeAll(() => {
    datasets = new Map();
    datasets.set(1, createDataset(1, 'dataset 1', 1536777405845));
    datasets.set(2, createDataset(2, 'dataset 2', 1536777805845));
    datasets.set(3, createDataset(3, 'dataset 3', 1536778005208));

    expectedData = [
      createDataset(1, 'dataset 1', 1536777405845),
      createDataset(2, 'dataset 2', 1536777805845),
      createDataset(3, 'dataset 3', 1536778005208),
    ];

    props = {
      history: {},
      client: {},
      projectId: '123',
      datasets,
      updateSelectedDatasets: () => {},
    };
  });

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<DatasetSelectorTable
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<DatasetSelectorTable
        {...props}
      />);
    });

    test('renders correctly with given datasets', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    let commonData = {};

    beforeAll(() => {
      props.updateSelectedDatasets = jest.fn();
    });

    beforeEach(() => {
      wrapper = shallow(<DatasetSelectorTable
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('getDerivedStateFromProps:', () => {
      test('should populate the list of available datasets', () => {
        const state = DatasetSelectorTable.getDerivedStateFromProps(props, { availableDatasets: [] });
        expect(state.availableDatasets).toEqual([1, 2, 3]);
      });

      test('should populate the list of available datasets which has COMPLETED as status', () => {
        const datasetsWithDiffStatus = new Map();
        datasetsWithDiffStatus.set(1, createDataset(1, 'dataset 1', 1536777405845, 'RUNNING'));
        datasetsWithDiffStatus.set(2, createDataset(2, 'dataset 2', 1536777805845, 'FAILED'));
        datasetsWithDiffStatus.set(3, createDataset(3, 'dataset 3', 1536778005208));

        const testProps = {
          datasets: datasetsWithDiffStatus,
          updateSelectedDatasets: () => {},
        };
        const state = DatasetSelectorTable.getDerivedStateFromProps(testProps, { availableDatasets: [] });
        expect(state.availableDatasets).toEqual([3]);
      });

      test('should populate the tableData', () => {
        const state = DatasetSelectorTable.getDerivedStateFromProps(props, { availableDatasets: [] });
        expect(state.tableData).toEqual(expectedData);
      });
    });

    describe('updateSelectedDatasets:', () => {
      test('should update the selected Datasets', () => {
        wrapper.instance().updateSelectedDatasets([1]);
        expect(wrapper.state().selectedDatasets).toEqual([1]);
      });

      test('should call the props updateSelectedDatasets to update the state of parent component', () => {
        wrapper.instance().updateSelectedDatasets([1]);
        expect(props.updateSelectedDatasets).toHaveBeenCalledWith([1]);
      });
    });

    describe('onToggleDatasetCheckbox:', () => {
      test('should add to the selected Datasets when the dataset is checked', () => {
        wrapper.setState({
          selectedDatasets: [],
        });
        wrapper.instance().onToggleDatasetCheckbox(1, true);
        expect(wrapper.state().selectedDatasets).toContain(1);
      });

      test('should remove from the selected Datasets when the dataset is unchecked', () => {
        wrapper.setState({
          selectedDatasets: [1, 2],
        });
        wrapper.instance().onToggleDatasetCheckbox(2, false);
        expect(wrapper.state().selectedDatasets).not.toContain(2);
      });
    });

    describe('toggleSelectAll:', () => {
      test('should add all the datasets when select all is checked', () => {
        wrapper.setState({
          selectedDatasets: [],
        });
        wrapper.instance().toggleSelectAll(true);
        expect(wrapper.state().selectedDatasets).toEqual([1, 2, 3]);
      });

      test('should remove all the datasets when selectAll is unchecked', () => {
        wrapper.setState({
          selectedDatasets: [1, 2],
        });
        wrapper.instance().toggleSelectAll(false);
        expect(wrapper.state().selectedDatasets).toEqual([]);
      });
    });
  });
});
