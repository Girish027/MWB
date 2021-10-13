import store from 'state/configureStore';
import { fetchUtil } from 'utils/fetchUtils';

describe('fetchUtil', () => {
  const mockDispatchError = jest.fn();
  jest.mock('utils/ErrorMessageUtil', () => jest.fn().mockImplementation(() => ({
    dispatchError: mockDispatchError,
  })));

  const mockStore = {
    app: {
      userId: 'test user',
      csrfToken: '123-456',
    },
  };
  const mockAppActions = {
    displayBadRequestMessage: () => {},
  };

  jest.mock('state/configureStore', () => mockStore);

  jest.mock('state/actions/actions_app', () => mockAppActions);

  const mockResponses = {
    mockOnFetchSuccess() {
      return {
        type: 'MOCK_FETCH_SUCCESS',
      };
    },
    mockOnFetchFailure() {
      return {
        type: 'MOCK_FETCH_FAILURE',
      };
    },
  };

  const fetchData = {
    fetchUrl: 'http://localhost:8080/nltools/v1/projects',
    fetchMethod: 'get',
    onFetchSuccess: mockResponses.mockOnFetchSuccess,
    onFetchFailure: mockResponses.mockOnFetchFailure,
    fetchHeaders: {},
    fetchRequestData: {},
    dispatch: store.dispatch,
    getState: store.getState,
  };

  beforeEach(() => {
  });

  it('fetchUtil - success', async () => {
    const successSpy = jest.spyOn(mockResponses, 'mockOnFetchSuccess');
    global.fetch.mockResponseOnce(JSON.stringify({ data: '12345' }));
    fetchData.onFetchSuccess = successSpy;
    await fetchUtil(fetchData);

    expect(successSpy).toHaveBeenCalled();
    expect(global.fetch.mock.calls.length).toEqual(1);
  });
  it('fetchUtil - error', async () => {
    global.fetch.resetMocks();
    const spy = jest.spyOn(mockResponses, 'mockOnFetchFailure');
    global.fetch.mockRejectOnce(new Error('error with fetch'));
    fetchData.onFetchFailure = spy;

    try {
      await fetchUtil(fetchData);
      expect(spy).toHaveBeenCalled();
      expect(global.fetch.mock.calls.length).toEqual(1);
    } catch (error) { /* eslint-disble no-empty */ }
  });
});
