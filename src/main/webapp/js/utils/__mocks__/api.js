const api = jest.genMockFromModule('utils/api.js').default;

api.get = jest.fn(() => 'GET');
api.post = jest.fn(() => 'POST');
api.put = jest.fn(() => 'PUT');
api.patch = jest.fn(() => 'PATCH');
api.delete = jest.fn(() => 'DELETE');

export default api;
