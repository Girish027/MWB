import _ from 'lodash';

export const areClientNamesEqual = (name1, name2) => {
  const name1LowerCase = !_.isNil(name1) ? name1.toLowerCase() : '';
  const name2LowerCase = !_.isNil(name2) ? name2.toLowerCase() : '';
  return name1LowerCase === name2LowerCase;
};

export const filterClientsList = (clientList, standardClientId, itsAppId) => {
  const matchedClient = clientList.filter(client => client.standardClientName.toLowerCase() === standardClientId.toLowerCase()
        && client.itsAppId.toLowerCase() === itsAppId.toLowerCase())[0];
  return matchedClient;
};

export const getClientsByStandardClientId = (clientList, standardClientId) => {
  const matchedClient = clientList.filter(client => client.standardClientName.toLowerCase() === standardClientId.toLowerCase());
  return matchedClient;
};

export const getUpdateClient = (clientData) => {
  /**
   * returns the client in this priority when available.
   * client in Route > client in localstorage >
   */
  const {
    clientList,
    routeClientId,
    routeAppId,
  } = clientData;
  let updatedClient;

  if (_.isNil(clientList) || clientList.length === 0) {
    return updatedClient;
  }

  if (routeClientId && !routeAppId) {
    const clients = getClientsByStandardClientId(clientList, routeClientId);
    if (clients && clients.length == 1) {
      updatedClient = clients[0];
    }
  } else if (routeClientId && routeAppId) {
    // check against route
    updatedClient = filterClientsList(clientList, routeClientId, routeAppId);
  } else if (!routeClientId && !routeAppId) {
    //  check against localstorage
    const {
      standardClientId: storedStandardClientId,
      itsAppId: storedITSAppId,
    } = localStorage;

    if (storedStandardClientId && storedITSAppId) {
      updatedClient = filterClientsList(clientList, storedStandardClientId, storedITSAppId);
    }

    // if not, select the first client in the list
    if (!updatedClient) {
      updatedClient = clientList[0];
    }
  }
  return updatedClient || {};
};

export const getUpdateProjectId = (projectData) => {
  /**
   * returns the project id in this priority when available.
   * projectId in Route > projectId in localstorage > first project
   */
  const {
    projects,
    routeProjectId,
    selectedProjectId,
  } = projectData;
  let updateProjectId;

  // If the clientList is null or empty, just return null
  if (_.isNil(projects) || projects.length === 0) {
    return updateProjectId;
  }

  // Check if there is a project id in the route
  if (!_.isNil(routeProjectId)) {
    updateProjectId = routeProjectId;
  } else {
    // no project id in the route - check if there is a project id in local storage
    const storageProjectId = localStorage.projectId;
    if (!_.isNil(storageProjectId)) {
      updateProjectId = storageProjectId;
    } else if (_.isNil(selectedProjectId) || selectedProjectId < 0) {
      // Select the first project
      updateProjectId = projects.values().next().value.id;
    }
  }
  // do not update Project if same is already selected
  if (!_.isNil(selectedProjectId) && selectedProjectId === updateProjectId) {
    updateProjectId = undefined;
  }
  return updateProjectId;
};
