import {
  getUpdateClient,
  areClientNamesEqual,
  getUpdateProjectId,
} from 'layouts/MainLayout/mainLayoutUtils';

describe('mainLayoutUtils:', () => {
  beforeEach(() => {
    delete global.localStorage.itsClientId;
    delete global.localStorage.itsAppId;
    delete global.localStorage.standardClientId;
    delete global.localStorage.projectId;
  });

  describe('getUpdateClientName', () => {
    let clientData = {};
    let clientList = [];
    const createClient = (id, name, itsClientId, itsAppId, standardClientName) => ({
      id,
      name,
      itsClientId,
      itsAppId,
      standardClientName,
    });

    beforeAll(() => {
      clientList = [
        createClient(0, '247ai', '247ai', 'default', 'tfsai'),
        createClient(1, 'Dish chatbot', 'dish', 'chatbot', 'dish'),
        createClient(2, 'Dish referencebot', 'dish', 'referencebot', 'dish'),
        createClient(3, '247 inc', '247inc', 'salesapp', 'tfsinc'),
      ];
    });

    test('should return undefined if clientList is not yet available', () => {
      global.localStorage.itsClientId = 'dish';
      clientData = {
        clientList: undefined,
        routeClientId: 'dish',
        routeAppId: 'chatbot',
      };
      const updateClient = getUpdateClient(clientData);
      expect(updateClient).toEqual(undefined);
    });

    test('should return client matching given client in route', () => {
      clientData = {
        clientList,
        routeClientId: 'tfsinc',
        routeAppId: undefined,
      };
      const updateClient = getUpdateClient(clientData);
      expect(updateClient).toEqual(createClient(3, '247 inc', '247inc', 'salesapp', 'tfsinc'));
    });

    test('should return client matching given client and app in route', () => {
      clientData = {
        clientList,
        routeClientId: 'dish',
        routeAppId: 'chatbot',
      };
      const updateClient = getUpdateClient(clientData);
      expect(updateClient).toEqual(createClient(1, 'Dish chatbot', 'dish', 'chatbot', 'dish'));
    });

    test('should return client matching stored data when route doesnot have client and app', () => {
      global.localStorage.itsClientId = 'dish';
      global.localStorage.standardClientId = 'dish';
      global.localStorage.itsAppId = 'referencebot';
      clientData = {
        clientList,
        routeClientId: undefined,
        routeAppId: undefined,
      };
      const updateClient = getUpdateClient(clientData);
      expect(updateClient).toEqual(createClient(2, 'Dish referencebot', 'dish', 'referencebot', 'dish'));
    });

    test('should return client matching stored client name and first and only app when route does not have info of app', () => {
      clientData = {
        clientList,
        routeClientId: 'tfsai',
        routeAppId: undefined,
      };
      const updateClient = getUpdateClient(clientData);
      expect(updateClient).toEqual(createClient(0, '247ai', '247ai', 'default', 'tfsai'));
    });

    test('should not return any client when route does not have info of app', () => {
      clientData = {
        clientList,
        routeClientId: 'dish',
        routeAppId: undefined,
      };
      const updateClient = getUpdateClient(clientData);
      expect(updateClient).toEqual({});
    });

    test('should not return any client when route does not have info of app', () => {
      clientData = {
        clientList,
        routeClientId: undefined,
        routeAppId: 'chatbot',
      };
      const updateClient = getUpdateClient(clientData);
      expect(updateClient).toEqual({});
    });

    test('should return first client in list when neither route nor storage has data', () => {
      global.localStorage.itsClientId = 'dish';
      global.localStorage.standardClientId = 'dish';
      global.localStorage.itsAppId = 'referencebot';
      clientData = {
        clientList,
        routeClientId: undefined,
        routeAppId: undefined,
      };
      const updateClient = getUpdateClient(clientData);
      expect(updateClient).toEqual(createClient(2, 'Dish referencebot', 'dish', 'referencebot', 'dish'));
    });
  });

  describe('getUpdateProjectId:', () => {
    let projectData = {};
    let projects = [];

    beforeAll(() => {
      projects = new Map();
      projects.set(0, { id: '0' });
      projects.set(1, { id: '1' });
      projects.set(2, { id: '2' });
    });

    test('should return undefined if projectList is not yet available', () => {
      global.localStorage.projectId = '2';
      projectData = {
        projects: undefined,
        routeProjectId: '3',
        selectedProjectId: '2',
      };
      const updateProjectId = getUpdateProjectId(projectData);
      expect(updateProjectId).toEqual(undefined);
    });

    test('should return undefined if selectedProjectid is same as routeProjectId', () => {
      global.localStorage.projectId = '2';
      projectData = {
        projects,
        routeProjectId: '3',
        selectedProjectId: '3',
      };
      const updateProjectId = getUpdateProjectId(projectData);
      expect(updateProjectId).toEqual(undefined);
    });

    test('should return id of route project when available and not equal to to presently selected id ', () => {
      global.localStorage.projectId = '2';
      projectData = {
        projects,
        routeProjectId: '4',
        selectedProjectId: '3',
      };
      const updateProjectId = getUpdateProjectId(projectData);
      expect(updateProjectId).toEqual('4');
    });

    test('should return id of stored projectId when available and not equal to to presently selected id, route has no project id ', () => {
      global.localStorage.projectId = '2';
      projectData = {
        projects,
        routeProjectId: undefined,
        selectedProjectId: '3',
      };
      const updateProjectId = getUpdateProjectId(projectData);
      expect(updateProjectId).toEqual('2');
    });

    test('should return id of the first project when neither route nor storage has projectId ', () => {
      projectData = {
        projects,
        routeProjectId: undefined,
        selectedProjectId: undefined,
      };
      const updateProjectId = getUpdateProjectId(projectData);
      expect(updateProjectId).toEqual('0');
    });
  });

  describe('areClientNamesEqual', () => {
    test('different names should not be equal', () => {
      const result = areClientNamesEqual('247ai', 'dish');
      expect(result).toBe(false);
    });
    test('same names with the same case should be equal', () => {
      const result = areClientNamesEqual('dish', 'dish');
      expect(result).toBe(true);
    });
    test('same names with the different case should be equal', () => {
      const result = areClientNamesEqual('dish', 'Dish');
      expect(result).toBe(true);
    });
  });
});
