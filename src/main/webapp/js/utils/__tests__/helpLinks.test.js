import helpLinks from 'utils/helpLinks';

describe('helpLinks', () => {
  test('each item contains properties: href, id, displayText', () => {
    const numItems = helpLinks.length;
    const expectedNumProperties = numItems * 3;
    let numProperties = 0;
    helpLinks.forEach((item) => {
      if (item.hasOwnProperty('href')) {
        numProperties += 1;
      }
      if (item.hasOwnProperty('id')) {
        numProperties += 1;
      }
      if (item.hasOwnProperty('displayText')) {
        numProperties += 1;
      }
    });
    expect(numProperties).toEqual(expectedNumProperties);
  });
});
