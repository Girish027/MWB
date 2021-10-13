import React from 'react';
import { mount, shallow } from 'enzyme';
import renderer from 'react-test-renderer';
import toJSON from 'enzyme-to-json';
import BatchTestContainer from 'components/models/batchTest/BatchTestContainer/BatchTestContainer';
import * as actionsModels from 'state/actions/actions_models';
import * as modelUtils from 'components/models/modelUtils';
import * as fileDownloadUtil from 'js-file-download';


describe('<BatchTestContainer />', () => {
  const match = {
    params: {
      clientId: '1',
      projectId: '2',
      modelId: '3',
    },
  };

  const basicProps = {
    dispatch: () => {},
    app: {
      userId: '247-test',
      csrfToken: 'token',
    },
    ...match.params,
    onTuneModelClick: () => {},
    onReviewDatasetsClick: () => {},
    onRunSingleUtterance: () => {},
    history: {},
  };

  const model = {
    modelToken: '8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451',

  };

  const datasets = {

  };

  const getListOfBatchTests = status => ({
    projectId: '1',
    modelId: '8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451',
    modelName: 'tora',
    modelVersion: '1',
    modelDescription: 'tora the cat',
    batchTestInfo: [{
      testId: '64fc4148-3ca7-4958-a40c-cd7a8f02a7cc',
      type: 'DATASETS',
      status,
      requestPayload: '1, 2',
      createdAt: '1536876619527',
      batchTestName: 'BatchTest_1536876619527',
    }, {
      testId: 'random-3ca7-4958-a40c-cd7a8f02a7cc',
      type: 'DATASETS',
      status,
      requestPayload: '1, 2',
      createdAt: '1536876893245',
      batchTestName: 'BatchTest_1536876893245',
    }],
  });

  const props = {
    ...basicProps,
    listOfBatchTests: {},
    modelBatchTestResults: {},
    model,
    datasets,
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<BatchTestContainer
        match={match}
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<BatchTestContainer
        match={match}
        {...props}
      />);
    });

    test('renders correctly for a model without any batch tests', () => {
      const tree = renderer.create(<BatchTestContainer
        match={match}
        {...props}
      />);
      expect(tree.toJSON()).toMatchSnapshot();
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
  wrapper = shallow(<BatchTestContainer
    {...testProps}
  />);
  expect(toJSON(wrapper)).toMatchSnapshot();
});

    test('renders correctly when Create New Batch Test is clicked - CreateBatchTest should be shown', () => {
      wrapper.instance().onRunBatchTestClick();
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when test has been submitted - goes back to Results page', () => {
      wrapper.instance().onRunBatchTestClick();
      expect(wrapper.find('CreateBatchTest').length).toEqual(1);
      // the below function is called from CreateBatchTest
      wrapper.instance().showBatchTestResults();
      expect(wrapper.find('CreateBatchTest').length).toEqual(0);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when sidebar is closed ', () => {
      wrapper.instance().toggleSidebar();
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test.todo('renders with results');
  });

  describe('Functionality:', () => {
    let commonData = {};

    beforeAll(() => {
      props.dispatch = jest.fn();
      props.onTuneModelClick = jest.fn();
      props.onReviewDatasetsClick = jest.fn();
      props.onRunSingleUtterance = jest.fn();

      actionsModels.modelBatchTest = jest.fn();
      actionsModels.modelCheckBatchTest = jest.fn();
      actionsModels.clearModelBatchTestResults = jest.fn(() => 'cleared');
      actionsModels.clearModelTestResults = jest.fn();
      actionsModels.modelBatchJobRequest = jest.fn();
      actionsModels.listBatchTests = jest.fn();

      modelUtils.getBatchResultsData = jest.fn(() => 'data');

      commonData = {
        userId: props.app.userId,
        csrfToken: props.app.csrfToken,
        projectId: props.projectId,
        modelId: props.model.modelToken,
        clientId: props.clientId,
      };
    });

    beforeEach(() => {
      wrapper = shallow(<BatchTestContainer
        match={match}
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('componentDidMount:', () => {
      test('should dispatch action to clear batch test results', () => {
        wrapper.instance().componentDidMount();
        expect(actionsModels.clearModelBatchTestResults).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith(actionsModels.clearModelBatchTestResults.mock.results[0].value);
      });
    });

    describe('onClickBatchTestItem:', () => {
      const selectedIndex = 1;
      const testId = '123';
      let onCheckBatchTestSpy;

      beforeEach(() => {
        wrapper = shallow(<BatchTestContainer
          {...props}
          listOfBatchTests={getListOfBatchTests('SUCCESS')}
        />);
      });

      afterEach(() => {
        if (onCheckBatchTestSpy) {
          onCheckBatchTestSpy.mockRestore();
        }
      });

      test('should set the update the value of state.selectedIndex', () => {
        wrapper.instance().onClickBatchTestItem(selectedIndex, testId);
        expect(wrapper.state().selectedIndex).toEqual(selectedIndex);
      });

      test('should set the update the value of state.inputDatasets', () => {
        wrapper.instance().onClickBatchTestItem(selectedIndex, testId);
        expect(wrapper.state().inputDatasets).toMatchSnapshot();
      });

      test('should dispatch action to clear batch test results', () => {
        wrapper.instance().onClickBatchTestItem(selectedIndex, testId);
        expect(actionsModels.clearModelBatchTestResults).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith(actionsModels.clearModelBatchTestResults.mock.results[0].value);
      });

      test('should call the onCheckBatchTest  with item index and testId', () => {
        onCheckBatchTestSpy = jest.spyOn(wrapper.instance(), 'onCheckBatchTest');
        wrapper.instance().onClickBatchTestItem(selectedIndex, testId);
        expect(onCheckBatchTestSpy).toHaveBeenCalledWith(testId);
      });
    });

    describe('toggleSidebar:', () => {
      test('should toggle the current state of isSidebarOpen', () => {
        wrapper.setState({
          isSidebarOpen: false,
        });
        wrapper.instance().toggleSidebar();
        expect(wrapper.state().isSidebarOpen).toEqual(true);
      });
    });

    describe('onRunBatchTestClick:', () => {
      test('should show batch test dialog', () => {
        wrapper.setState({
          createBatchTestMode: false,
        });
        wrapper.instance().onRunBatchTestClick();
        expect(wrapper.state().createBatchTestMode).toEqual(true);
      });

      test('should dispatch action to clear batch test results', () => {
        wrapper.instance().onRunBatchTestClick();
        expect(actionsModels.clearModelBatchTestResults).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith(actionsModels.clearModelBatchTestResults.mock.results[0].value);
      });

      test('should dispatch action to clear model test results', () => {
        wrapper.instance().onRunBatchTestClick();
        expect(actionsModels.clearModelTestResults).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith(actionsModels.clearModelTestResults.mock.results[0].value);
      });
    });

    describe('showBatchTestResults:', () => {
      test('should set createBatchTestMode to false', () => {
        wrapper.setState({
          createBatchTestMode: true,
        });
        wrapper.instance().showBatchTestResults();
        expect(wrapper.state().createBatchTestMode).toEqual(false);
      });
    });

    describe('onCheckBatchTest:', () => {
      test('should trigger the model check batch test action with the required data', () => {
        const testId = '123';
        wrapper.instance().onCheckBatchTest('123');
        expect(actionsModels.modelCheckBatchTest).toHaveBeenCalledWith({ ...commonData, modelTestJobId: '123' });
      });
    });

    // TODO enable the below test after mocking the modeule js-file-download
    describe.skip('onDownloadResults:', () => {
      let testProps;

      beforeAll(() => {
        testProps = {
          ...props,
          model: {
            modelToken: '8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451',
            name: 'modelName',
            version: 2,
          },
          modelBatchTestResults: {
            modelBatchTestResults: { // this value will be present in state.lastBatchTestResults
              dummy: 'data',
            },
            modelTestJobId: '1234',
          },
        };
      });

      beforeEach(() => {
        wrapper = shallow(<BatchTestContainer
          {...testProps}
        />);
      });

      test('should create the file data', () => {
        const now = Date.now();
        Date.now = jest.fn().mockReturnValue(now);
        const expectedHeaderData = {
          date: new Date(),
          model: testProps.model,
          jobId: '1234',
        };
        wrapper.instance().onDownloadResults();
        expect(modelUtils.getBatchResultsData).toHaveBeenCalledWith(
          wrapper.state().lastBatchTestResults.modelBatchTestResults, expectedHeaderData,
        );
      });
    });
  });
});
