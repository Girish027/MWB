import constructKibanaUrl from 'utils/kibanaUtils';

describe('constructKibanaUrl', () => {
  test('returns expected url for selection', () => {
    const props = {
      kibanaLogIndex: '123abc',
      kibanaLogURL: 'https://kibana',
      modelToken: '1234-5678-90',
    };
    const url = constructKibanaUrl(props);

    expect(url.indexOf(props.kibanaLogIndex)).not.toEqual(-1);
    expect(url.indexOf(props.kibanaLogURL)).not.toEqual(-1);
    expect(url.indexOf(props.modelToken)).not.toEqual(-1);
  });
  test('returns just the url if the index is null', () => {
    const props = {
      kibanaLogIndex: null,
      kibanaLogURL: 'https://kibana',
      modelToken: '1234-5678-90',
    };
    const url = constructKibanaUrl(props);

    expect(url.indexOf(props.kibanaLogURL)).not.toEqual(-1);
    expect(url.indexOf(props.modelToken)).toEqual(-1);
  });
  test('returns just the url if the token is null', () => {
    const props = {
      kibanaLogIndex: '123abc',
      kibanaLogURL: 'https://kibana',
      modelToken: null,
    };
    const url = constructKibanaUrl(props);

    expect(url.indexOf(props.kibanaLogURL)).not.toEqual(-1);
    expect(url.indexOf(props.kibanadLogIndex)).toEqual(-1);
  });
});
