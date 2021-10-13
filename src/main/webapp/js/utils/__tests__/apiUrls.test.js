import getUrl, { getDataUrl, pathKey, versionKey } from 'utils/apiUrls';

describe('apiUrls', () => {
  test('returns expected url for selection', () => {
    const url = getUrl(pathKey.config, versionKey.v1);
    const expectedUrl = '/nltools/private/v1/configs';
    expect(url).toEqual(expectedUrl);
  });
  test('returns null if the pathKey does not exist', () => {
    const url = getUrl(pathKey.nokey, {}, versionKey.v1);
    const expectedUrl = null;
    expect(url).toEqual(expectedUrl);
  });
  test('returns default version if the versionKey does not exist', () => {
    const url = getUrl(pathKey.config, {}, versionKey.unknown);
    const expectedUrl = '/nltools/private/v1/configs';
    expect(url).toEqual(expectedUrl);
  });
  test('returns null if the pathkey and the versionKey do not exist', () => {
    const url = getUrl(pathKey.nokey, {}, versionKey.unknown);
    const expectedUrl = null;
    expect(url).toEqual(expectedUrl);
  });
  test('returns expected url if the versionKey is not passed in', () => {
    const url = getUrl(pathKey.config);
    const expectedUrl = '/nltools/private/v1/configs';
    expect(url).toEqual(expectedUrl);
  });
  test('returns expected url for a data url request', () => {
    const url = getDataUrl(pathKey.modelConfigTemplate);
    const expectedUrl = '/dist/data/ModelConfigTemplate.zip';
    expect(url).toEqual(expectedUrl);
  });
});
