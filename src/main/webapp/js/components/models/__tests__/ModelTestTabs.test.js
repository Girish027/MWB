import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import * as actionsTagDatasets from 'state/actions/actions_tag_datasets';
import * as actionsModels from 'state/actions/actions_models';
import * as actionsHeader from 'state/actions/actions_header';
import * as actionsApp from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import Constants from 'constants/Constants';
import {
  Pencil,
} from '@tfs/ui-components';
import { ModelTestTabs } from 'components/models/ModelTestTabs';
import ConnectedModelTestTabs from 'components/models/ModelTestTabs';

describe('<ModelTestTabs />', () => {
  const match = {
    params: {
      projectId: '7',
      modelId: '3',
    },
  };

  const modelTestResults = {
    projectId: '7',
    modelId: 'b61693f6-fa65-4308-af09-b32872dfb5f6',
    type: 'UTTERANCES',
    status: 'SUCCESS',
    evaluations: [{
      utterance: 'i want to cancel for june 13',
      intents: [{
        intent: 'reservation-query',
        score: 0.49544276870123316,
      }, {
        intent: 'agent-query',
        score: 0.42004871402243316,
      }, {
        intent: 'None_None',
        score: 0.14285217997731586,
      }],
      transformations: [{
        id: 'rx-mapper.non-breaking-space-regex',
        result: 'i want to cancel for june 13',
      }, {
        id: 'wsp',
        result: 'i want to cancel for june 13',
      }],
      entities: [{
        name: 'date',
        value: 'june 13',
      }],
    }],
  };

  const props = {
    userId: 'abc',
    csrfToken: '123',
    modelTestResults,
    projectId: '7',
    modelId: '3',
    client: {
      id: '2',
    },
    match: {
      path: '/models/test',
    },
    model: {
      modelToken: 'b61693f6-fa65-4308-af09-b32872dfb5f6',
    },
    dispatch: () => {},
    history: {},
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });


  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <ConnectedModelTestTabs
            match={match}
            location={{
              search: '?clientid=247ai&appid=aisha&projectid=7&modelid=3',
            }}
          />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });


  describe('Snapshots', () => {
    test('renders correctly with two tabs', () => {
      wrapper = shallow(<ModelTestTabs
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when a utterance tab currently active', () => {
      wrapper = shallow(<ModelTestTabs
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
    test('renders correctly when a batch test tab currently active', () => {
      const batchtestprops = Object.assign({}, props, { match: { path: '/models/batchtest' } });
      wrapper = shallow(<ModelTestTabs
        {...batchtestprops}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeAll(() => {
      props.dispatch = jest.fn();

      actionsModels.tuneSelectedModel = jest.fn(() => 'called tuneSelectedModel');
      actionsModels.clearModelTestResults = jest.fn(() => 'called clearModelTestResults');
      actionsModels.testModel = jest.fn(() => 'called testModel');
      actionsModels.listBatchTests = jest.fn(() => 'called listBatchTests');

      actionsHeader.setActionItems = jest.fn(() => 'called setActionItems');

      actionsApp.changeRoute = jest.fn(() => 'called changeRoute');
      actionsApp.displayWarningRequestMessage = jest.fn(() => 'called displayWarningRequestMessage');

      actionsTagDatasets.setIncomingFilter = jest.fn(() => 'called setIncomingFilter');
    });

    beforeEach(() => {
      wrapper = shallow(<ModelTestTabs
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('componentDidMount:', () => {
      test('should dispatch action to clear test results', () => {
        wrapper.instance().componentDidMount();
        expect(actionsModels.clearModelTestResults).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith('called clearModelTestResults');
      });

      test('should dispatch action toset action items in page header', () => {
        wrapper.instance().componentDidMount();
        expect(actionsHeader.setActionItems).toHaveBeenCalledWith([{
          type: 'flat',
          label: Constants.TUNE_VERSION,
          icon: Pencil,
          onClick: wrapper.instance().onTuneModelClick,
          styleOverride: expect.any(Object),
        }]);
        expect(props.dispatch).toHaveBeenCalledWith('called setActionItems');
      });
    });

    describe('componentWillUnmount:', () => {
      test('should dispatch action to remove action items in page header', () => {
        wrapper.instance().componentWillUnmount();
        expect(actionsHeader.setActionItems).toHaveBeenCalledWith([]);
        expect(props.dispatch).toHaveBeenCalledWith('called setActionItems');
      });
    });

    describe('onTabSelected:', () => {
      test('should dispatch action to change the route to testmodel', () => {
        const {
          client, history, projectId, modelId,
        } = props;
        wrapper.instance().onTabSelected(Constants.TEST_TABS.utteranceTestTab, 0);
        expect(actionsApp.changeRoute)
          .toHaveBeenCalledWith(RouteNames.TESTMODEL, { client, projectId, modelId }, history);
        expect(props.dispatch).toHaveBeenCalledWith('called changeRoute');
      });

      test('should dispatch action to change the route to batchtest', () => {
        const {
          client, history, projectId, modelId,
        } = props;
        wrapper.instance().onTabSelected(Constants.TEST_TABS.batchTestTab, 1);
        expect(actionsApp.changeRoute)
          .toHaveBeenCalledWith(RouteNames.BATCHTESTMODEL, { client, projectId, modelId }, history);
        expect(props.dispatch).toHaveBeenCalledWith('called changeRoute');
      });

      test('should dispatch action to change the route to testmodel', () => {
        const {
          client, history, projectId, modelId,
        } = props;
        wrapper.instance().onTabSelected(Constants.TEST_TABS.batchTestTab, 2);
        expect(actionsApp.changeRoute)
          .toHaveBeenCalledWith(RouteNames.TESTMODEL, { client, projectId, modelId }, history);
        expect(props.dispatch).toHaveBeenCalledWith('called changeRoute');
      });
    });

    describe('onTuneModelClick:', () => {
      test('should dispatch action to tune model', () => {
        wrapper.instance().onTuneModelClick();
        expect(actionsModels.tuneSelectedModel).toHaveBeenCalledWith(props.model);
        expect(props.dispatch).toHaveBeenCalledWith('called tuneSelectedModel');
      });

      test('should dispatch action to change route to tune model view', () => {
        const {
          modelId, client, history, projectId,
        } = props;
        wrapper.instance().onTuneModelClick();
        expect(actionsApp.changeRoute)
          .toHaveBeenCalledWith(RouteNames.TUNEMODEL, { client, projectId, modelId }, history);
        expect(props.dispatch).toHaveBeenCalledWith('called changeRoute');
      });
    });

    describe('onReviewDatasetsClick:', () => {
      test('should dispatch action to set filter on Tag Datasets view', () => {
        const { projectId } = props;
        wrapper.instance().onReviewDatasetsClick();
        expect(actionsTagDatasets.setIncomingFilter).toHaveBeenCalledWith({ projectId, datasets: [] });
        expect(props.dispatch).toHaveBeenCalledWith('called setIncomingFilter');
      });

      test('should dispatch action to change route to tag datasets view', () => {
        const {
          client, history, projectId,
        } = props;
        wrapper.instance().onReviewDatasetsClick();
        expect(actionsApp.changeRoute)
          .toHaveBeenCalledWith(RouteNames.TAG_DATASETS, { client, projectId }, history);
        expect(props.dispatch).toHaveBeenCalledWith('called changeRoute');
      });
    });

    describe('onClickBatchTestTab:', () => {
      test('should dispatch action to display warning message if model is not present', () => {
        wrapper = shallow(<ModelTestTabs
          {...props}
          model={{}}
        />);
        wrapper.instance().onClickBatchTestTab();
        expect(actionsApp.displayWarningRequestMessage).toHaveBeenCalledWith(Constants.UNKNOWN_MODEL);
        expect(props.dispatch).toHaveBeenCalledWith('called displayWarningRequestMessage');
      });

      test('should not dispatch action to list batch tests, if model is not available', () => {
        wrapper = shallow(<ModelTestTabs
          {...props}
          model={{}}
        />);
        wrapper.instance().onClickBatchTestTab();
        expect(actionsModels.listBatchTests).not.toHaveBeenCalled;
        expect(props.dispatch).not.toHaveBeenCalledWith('called listBatchTests');
      });

      test('should dispatch action to list existing batch tests', () => {
        const {
          model, client, projectId, userId, csrfToken,
        } = props;
        const expectedData = {
          userId,
          csrfToken,
          projectId,
          modelId: model.modelToken,
          clientId: client.id,
        };
        wrapper.instance().onClickBatchTestTab();
        expect(actionsModels.listBatchTests).toHaveBeenCalledWith(expectedData);
        expect(props.dispatch).toHaveBeenCalledWith('called listBatchTests');
      });
    });

    describe('onRunSingleUtterance:', () => {
      test('should set the active tab to utterance and update incoming utterance', () => {
        wrapper.setState({
          runSingleUtterance: '',
        });
        wrapper.instance().onRunSingleUtterance('how are you');
        expect(wrapper.state().runSingleUtterance).toEqual('how are you');
      });
    });

    describe('clearRunSingleUtterance:', () => {
      test('should clear the state for incoming utterance', () => {
        wrapper.setState({
          runSingleUtterance: 'hi...',
        });
        wrapper.instance().clearRunSingleUtterance();
        expect(wrapper.state().runSingleUtterance).toEqual('');
      });
    });
  });
});
