import { normalizeIds, normalizeModel } from 'utils/apiUtils';

describe('apiUtils', () => {
  describe('normalizeIds', () => {
    test('returns expected normalized result', () => {
      const inputData = {
        configId: 5,
        id: 2,
        projectId: 4,
        modelId: 19,
        cid: 5,
        clientId: 123,
      };
      const result = normalizeIds(inputData);
      const expectedResult = {
        configId: '5',
        id: '2',
        projectId: '4',
        modelId: '19',
        otherData: 'otherData',
        cid: '5',
        clientId: '123',
      };
      expect(typeof result.configId).toEqual(typeof expectedResult.configId);
      expect(result.configId).toEqual(expectedResult.configId);
    });
    test('returns expected result when nothing to normalize', () => {
      const inputData = {
        count: 123,
        modelToke: 'abc-123',
      };
      const result = normalizeIds(inputData);
      const expectedResult = {
        count: 123,
        modelToke: 'abc-123',
      };
      expect(typeof result.count).toEqual(typeof expectedResult.count);
      expect(result.count).toEqual(expectedResult.count);
    });
  });
  describe('normalizeModels', () => {
    test('returns expected result for input model', () => {
      const inputData = {
        id: 5,
        configId: 23,
        datasetIds: [
          '123   ',
          '456',
        ],
      };
      const result = normalizeModel(inputData);
      const expectedResult = {
        id: '5',
        configId: '23',
        datasetIds: [
          '123',
          '456',
        ],
      };
      expect(result).toEqual(expectedResult);
    });
  });
});
