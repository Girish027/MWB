
import store from 'state/configureStore';
import {
  loadDatasetsStatuses,
  loadModelsStatuses,
  loadModelsBatchTestStatuses,
} from 'state/actions/actions_projectsmanager';
import Constants from 'constants/Constants';

class ProjectsManager {
  static getState() {
    return store.getState().projectsManager;
  }

  constructor() {
    if (ProjectsManager.instance) {
      throw new Error('ProjectsManager already created.');
    }

    this.trackDatasetsStatuses = this.trackDatasetsStatuses.bind(this);
    this.trackDatasetsIntervalDelay = 10000;
    this.isTrackDatasetsRequestInProgress = false;
    this.trackDatasetsIntervalId = setInterval(this.trackDatasetsStatuses, this.trackDatasetsIntervalDelay);

    this.trackModelsStatuses = this.trackModelsStatuses.bind(this);
    this.trackModelsIntervalDelay = 10000;
    this.isTrackModelsRequestInProgress = false;
    this.trackModelsIntervalId = setInterval(this.trackModelsStatuses, this.trackModelsIntervalDelay);

    this.trackModelsBatchTestStatuses = this.trackModelsBatchTestStatuses.bind(this);
    this.trackModelsBatchTestIntervalDelay = 3000;
    this.isTrackModelsBatchTestRequestInProgress = false;
    this.trackModelsBatchTestIntervalId = setInterval(this.trackModelsBatchTestStatuses, this.trackModelsBatchTestIntervalDelay);
  }

  trackDatasetsStatuses() {
    if (this.isTrackDatasetsRequestInProgress) {
      return;
    }
    const state = ProjectsManager.getState();
    const { trackDatasetsIds, projectDatasets } = state;
    if (Object.keys(projectDatasets).length === 0
       || Object.keys(trackDatasetsIds).length === 0) {
      return;
    }
    const projectId = Object.keys(trackDatasetsIds)[0];
    if (!projectDatasets.get(projectId)) {
      return;
    }

    this.isTrackDatasetsRequestInProgress = true;
    store.dispatch(loadDatasetsStatuses(trackDatasetsIds))
      .then(() => {
        this.isTrackDatasetsRequestInProgress = false;
      })
      .catch(() => {
        this.isTrackDatasetsRequestInProgress = false;
      });
  }

  trackModelsStatuses() {
    if (this.isTrackModelsRequestInProgress) {
      return;
    }
    const state = ProjectsManager.getState();
    const { trackModelsIds } = state;

    if (Object.keys(trackModelsIds).length === 0) {
      return;
    }

    this.isTrackModelsRequestInProgress = true;
    store.dispatch(loadModelsStatuses(trackModelsIds))
      .then(() => {
        this.isTrackModelsRequestInProgress = false;
      })
      .catch(() => {
        this.isTrackModelsRequestInProgress = false;
      });
  }

  trackModelsBatchTestStatuses() {
    if (this.isTrackModelsBatchTestRequestInProgress) {
      return;
    }
    const state = ProjectsManager.getState();
    const { trackModelsBatchTestIds } = state;
    if (Object.keys(trackModelsBatchTestIds).length === 0) {
      return;
    }
    this.isTrackModelsBatchTestRequestInProgress = true;
    store.dispatch(loadModelsBatchTestStatuses(trackModelsBatchTestIds))
      .then(() => {
        this.isTrackModelsBatchTestRequestInProgress = false;
      })
      .catch(() => {
        this.isTrackModelsBatchTestRequestInProgress = false;
      });
  }

  getProject(projectId) {
    const state = ProjectsManager.getState();
    return state.projects.get(projectId);
  }

  getDatasetsByProjectId(projectId, full) {
    const state = ProjectsManager.getState();
    const datasets = state.projectDatasets.get(projectId);
    if (!full || !datasets || !datasets.size) {
      return datasets;
    }
    return (typeof datasets.first().description === 'undefined') ? undefined : datasets;
  }

  getDataset(projectId, datasetId) {
    const state = ProjectsManager.getState();
    const datasets = state.projectDatasets.get(projectId);
    if (!datasets) {
      return datasets;
    }
    return datasets.get(datasetId);
  }

  getModelsByProjectId(projectId) {
    const state = ProjectsManager.getState();
    return state.projectModels.get(projectId);
  }

  getModel(projectId, modelId) {
    const state = ProjectsManager.getState();
    const models = state.projectModels.get(projectId);
    if (!models || !models.size) {
      return undefined;
    }
    return models.get(modelId);
  }

  getConfigsByProjectId(projectId) {
    const state = ProjectsManager.getState();
    return state.projectConfigs.get(projectId);
  }

  getIntentsByProjectId(projectId) {
    const state = ProjectsManager.getState();
    return state.projectIntents.get(projectId);
  }

  getConfig(projectId, configId) {
    const state = ProjectsManager.getState();
    const configs = state.projectConfigs.get(projectId);
    if (!configs || !configs.size) {
      return undefined;
    }
    return configs.get(configId);
  }

  isDatasetUsedInModel(projectId, datasetId) {
    const models = this.getModelsByProjectId(projectId);
    let usedInModel = false;
    if (models) {
      const modelsArray = models.toArray();
      usedInModel = modelsArray.some((item) => {
        if (item.datasetIds && item.datasetIds.length > 0) {
          const modelContainsId = item.datasetIds.some(id => (id === datasetId));
          return modelContainsId;
        }
      });
    }

    return usedInModel;
  }

  addDescriptionAndType(name, version, modelType = Constants.DIGITAL_MODEL, currentDescription = '') {
    let description = `This speech model has been duplicated from ${name} - ${version}.`;
    if (currentDescription) {
      description += `\n${currentDescription}`;
    }
    return {
      description,
      modelType,
    };
  }
}

const projectsManager = new ProjectsManager();

export default projectsManager;
