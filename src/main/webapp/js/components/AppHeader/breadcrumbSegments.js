import _ from 'lodash';
import {
  capitalizeFirstLetter,
} from 'utils/StringUtils';
import {
  RouteNames, uniqueURLStringToRouteMap,
} from 'utils/routeHelpers';
import { changeRoute } from 'state/actions/actions_app';
import store from 'state/configureStore';
import { getItsURL } from 'utils/apiUrls';
import Model from 'model';
import Constants from 'constants/Constants';

const getModelLabel = (model) => `Version ${model.version}`;

const breadcrumbSegments = (match, info, history, itsURLPath) => {
  const dataRoute = 'data-route';
  const {
    selectedClient,
    selectedProject,
    model,
  } = info;

  if (_.isNil(match)) {
    return '';
  }
  const urlItems = match.url ? match.url.split('/') : [];

  let modelLabel = '';
  if (model) {
    modelLabel = getModelLabel(model);
  }

  const projectId = !_.isNil(selectedProject) ? selectedProject.id : '0';
  const projectName = !_.isNil(selectedProject) && selectedProject.name ? selectedProject.name : 'Loading Model';

  const { itsAppId, standardClientId } = localStorage;
  const itsURL = getItsURL({ itsURLPath, itsClientId: standardClientId, itsAppId });

  const state = store.getState();
  const userGroup = state.app.userGroups;

  const onClick = (attributes = {}) => {
    const { itsurl } = attributes;

    if (itsurl) {
      window.location.href = itsurl;
    } else if (attributes[dataRoute]) {
      const route = attributes[dataRoute];
      if (route) {
        const data = {
          client: selectedClient,
          projectId,
        };
        store.dispatch(changeRoute(route, data, history));
      }
    }
  };

  const getItemProps = routeName => ({
    [dataRoute]: routeName,
    onClick: onClick.bind(this),
  });

  const getRouteToAdd = (item, routeName) => {
    routeName = _.isNil(routeName) ? uniqueURLStringToRouteMap[item] : routeName;
    return {
      onClick: onClick.bind(this),
      value: item,
      ...getItemProps(routeName),
      label: capitalizeFirstLetter(item),
    };
  };

  const getProjectRoute = () => {
    // Add project name to breadcrumb for all pages other than Create Project
    if (match.url.indexOf(`${RouteNames.PROJECTS}/create`) === -1) {
      return getRouteToAdd(projectName, RouteNames.PROJECTS);
    }
  };

  let routeItems = [];
  const itsRoute = {
    ...getItemProps(),
    itsurl: itsURL,
    label: Constants.BOT_OVERVIEW,
    value: Constants.BOT_OVERVIEW,
  };

  routeItems = [
    itsRoute,
  ];

  urlItems.forEach((item) => {
    const routesToAdd = [];
    switch (item) {
    case 'projects':
    case 'datasets':
    case 'models':
    case 'manage-intents':
    case 'manage-settings':
      break;
    case 'test':
    case 'batchtest':
      routesToAdd.push(getRouteToAdd(Constants.MANAGE_MODELS_DATASETS));
      routesToAdd.push(getProjectRoute());
      // Note: Add model breadcrumb only if model info is available by this time
      // TODO: the breadcrumbs logic should be revisited along with refreshing state
      // TODO: make Appheader a connected component.
      if (modelLabel) {
        routesToAdd.push({
          ...getRouteToAdd(modelLabel, RouteNames.MODELS),
          label: modelLabel,
        });
      }
      break;
    case 'view':
    case 'tune': {
      routesToAdd.push(getRouteToAdd(Constants.MANAGE_MODELS_DATASETS));
      routesToAdd.push(getProjectRoute());
      const state = store.getState();
      let modelId = state.projectsManager.viewModelId || state.projectsManager.tuneModelId;
      const modelItem = Model.ProjectsManager.getModel(projectId, modelId);
      if (modelItem) {
        routesToAdd.push({
          label: getModelLabel(modelItem),
          value: modelId,
        });
      }
      break;
    }
    case 'settings': {
      routeItems = [];
      const routeItem = getRouteToAdd(Constants.MANAGE_MODELS_DATASETS);
      routeItem.label = Constants.BOT_OVERVIEW;
      routeItem.value = Constants.BOT_OVERVIEW;
      routesToAdd.push(routeItem);
      break;
    }
    default:
      if (item.length > 0) {
        routesToAdd.push(getRouteToAdd(Constants.MANAGE_MODELS_DATASETS));
        const projectRoute = getProjectRoute();
        if (projectRoute) {
          routesToAdd.push(getProjectRoute());
        }
      }
      break;
    }
    if (routesToAdd.length > 0) {
      routeItems = [...routeItems, ...routesToAdd];
    }
  });
  return routeItems;
};

export default breadcrumbSegments;
