import React from 'react';
import { mount, shallow } from 'enzyme';
import renderer from 'react-test-renderer';
import toJSON from 'enzyme-to-json';
import BatchTestSidebar from 'components/models/batchTest/BatchTestSidebar';

describe('<BatchTestSidebar />', () => {
  const basicProps = {
    isOpen: true,
    toggleSidebar: () => {},
    onClickBatchTestItem: () => {},
    onRunBatchTestClick: () => {},
  };

  const getBatchTestInfo = status => ({
    testId: '64fc4148-3ca7-4958-a40c-cd7a8f02a7cc',
    type: 'DATASETS',
    status,
    requestPayload: 'YesNo,trainingRU-2',
    createdAt: '1536876619527',
    batchTestName: 'BatchTest_1536876619527',
  });

  const getListOfBatchTests = status => ({
    projectId: '1',
    modelId: '8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451',
    modelName: 'tora',
    modelVersion: '1',
    modelDescription: 'tora the cat',
    batchTestInfo: [getBatchTestInfo(status)],
  });

  const props = {
    ...basicProps,
    listOfBatchTests: {},
  };

  let wrapper;

  beforeAll(() => {
    wrapper = shallow(<BatchTestSidebar
      {...props}
    />);
  });

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for a model without any batch tests', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test.each`
      status
      ${'FAILED'}
      ${'SUCCESS'}
      ${'QUEUED'}
      ${'IN_PROGRESS'}
    `('renders correctly for recent batch test - $status', ({ status }) => {
  const testProps = {
    ...props,
    listOfBatchTests: getListOfBatchTests(status),
  };
  wrapper = shallow(<BatchTestSidebar
    {...testProps}
  />);
  expect(toJSON(wrapper)).toMatchSnapshot();
});

    test('renders correctly for multiple batch tests', () => {
      const batchTests = [
        getBatchTestInfo('IN_PROGRESS'), getBatchTestInfo('SUCCESS'), getBatchTestInfo('SUCCESS'),
      ];
      const testProps = {
        ...props,
        listOfBatchTests: {
          ...getListOfBatchTests('IN_PROGRESS'),
          batchTestInfo: batchTests,
        },
      };
      wrapper = shallow(<BatchTestSidebar
        {...testProps}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when sidebar is closed', () => {
      wrapper = shallow(<BatchTestSidebar
        {...props}
      />);
      wrapper.setProps({
        isOpen: false,
      });
      wrapper.update();
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('onClickBatchTestItem:', () => {
    beforeEach(() => {
      props.onClickBatchTestItem = jest.fn();
      wrapper = shallow(<BatchTestSidebar
        {...props}
      />);
    });

    afterEach(() => {
      props.onClickBatchTestItem.mockClear();
    });

    test('should set the update the value of state.selectedIndex', () => {
      const selectedIndex = 1;
      const testId = '123';
      wrapper.instance().onClickBatchTestItem(selectedIndex, testId);
      expect(wrapper.state().selectedIndex).toEqual(selectedIndex);
    });

    test('should call the onClickBatchTestItem from props with item index and testId', () => {
      const selectedIndex = 1;
      const testId = '123';
      wrapper.instance().onClickBatchTestItem(selectedIndex, testId);
      expect(props.onClickBatchTestItem).toHaveBeenCalledWith(selectedIndex, testId);
    });
  });

  describe('onClickCreateNewBatchTest:', () => {
    test('should call the onRunBatchTestClick from props', () => {
      props.onRunBatchTestClick = jest.fn();
      wrapper = shallow(<BatchTestSidebar
        {...props}
      />);
      wrapper.instance().onClickCreateNewBatchTest();
      expect(props.onRunBatchTestClick).toHaveBeenCalled;
    });
  });

  describe('onFocusChange:', () => {
    test('should set the update the value of state.focusedIndex when it is in focus', () => {
      wrapper = shallow(<BatchTestSidebar
        {...props}
      />);
      const focusedIndex = 2;
      wrapper.instance().onFocusChange(focusedIndex, true);
      expect(wrapper.state().focusedIndex).toEqual(focusedIndex);
    });
  });
});
