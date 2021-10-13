import * as modelUtils from 'components/models/modelUtils';

describe('modelUtils', () => {
  describe('base64ToBlob', () => {
    let base64encodedData;
    beforeAll(() => {
      base64encodedData = btoa('hello');
    });

    test('convert the base 64 data into blob with the given contentType', () => {
      const result = modelUtils.base64ToBlob(base64encodedData, 'text/plain');
      expect(result instanceof Blob).toBe(true);
      expect(result.type).toEqual('text/plain');
    });
  });
});
