import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import store from 'state/configureStore';
import { Provider } from 'react-redux';
import ProjectsManager from 'model/ProjectsManager/ProjectsManager';
import Constants from 'constants/Constants';

describe('<ProjectsManager />', () => {
  let wrapper;

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = mount(<Provider store={store} />);
    });
  });

  describe('Snapshots', () => {
    const props = {
      dispatch: () => {},
    };
    beforeAll(() => {
      props.dispatch = jest.fn();
    });

    test('should match snapshots', () => {
      wrapper = shallow(<Provider store={store}>
        <ProjectsManager
          {...props}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('addDescriptionAndType', () => {
    test('should return description and model type', () => {
      const results = ProjectsManager.addDescriptionAndType('New Model', 2, Constants.DIGITAL_SPEECH_MODEL, 'new model');
      expect(results).toEqual({
        description: 'This speech model has been duplicated from New Model - 2.\nnew model',
        modelType: Constants.DIGITAL_SPEECH_MODEL,
      });
    });

    test('should return description and model type with default value', () => {
      const results = ProjectsManager.addDescriptionAndType('New Model', 2);
      expect(results).toEqual({
        description: 'This speech model has been duplicated from New Model - 2.',
        modelType: Constants.DIGITAL_MODEL,
      });
    });
  });

  describe('isDatasetUsedInModel', () => {
    test('should return false on isDatasetUsedInModel call', () => {
      const projectId = '123';
      ProjectsManager.getModelsByProjectId = jest.fn()
        .mockImplementation(() => (undefined));
      const results = ProjectsManager.isDatasetUsedInModel(projectId, '234');
      expect(results).toEqual(false);
    });
  });
});
