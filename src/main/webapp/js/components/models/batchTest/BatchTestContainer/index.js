import { connect } from 'react-redux';
import * as ramda from 'ramda';
import URLSearchParams from '@ungap/url-search-params';

import Model from 'model';
import Constants from 'constants/Constants';
import BatchTestContainer from './BatchTestContainer';

const mapStateToProps = (state, ownProps) => {
  const searchString = ramda.path(['location', 'search'], ownProps) || '';
  const query = new URLSearchParams(searchString);
  const userFeatureConfiguration = state.app.userFeatureConfiguration;
  const projectId = state.projectListSidebar.selectedProjectId;
  const modelId = query.get('modelid');

  // TODO - implement selectors
  const clientId = state.header.client.id;
  const listOfBatchTests = ramda.path(['projectsManager', 'model', 'listOfBatchTests'], state) || {};
  const model = Model.ProjectsManager.getModel(projectId, modelId) || null;
  const props = {
    app: state.app,
    projectId,
    modelId,
    clientId,
    listOfBatchTests,
    modelBatchTestResults: state.projectsManager.modelBatchTestResults,
    model,
    datasets: Model.ProjectsManager.getDatasetsByProjectId(projectId, true) || null,
    testModelType: model ? model.modelType : Constants.DIGITAL_MODEL,
    userFeatureConfiguration,
  };
  return props;
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(BatchTestContainer);
