
const constructKibanaUrl = (props) => {
  const { kibanaLogIndex, kibanaLogURL, modelToken } = props;

  let retUrl = kibanaLogURL;

  if (modelToken && kibanaLogIndex) {
    const segment1 = '#/discover?_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-24h,mode:quick,to:now))';
    const segment2 = '&_a=(columns:!(log_message),filters:!(),';
    const segment3 = `index:${kibanaLogIndex},`;
    const segment4 = 'interval:auto,';
    const segment5 = `query:(query_string:(query:'uuid:%20"${modelToken}"')),`;
    const segment6 = 'sort:!(\'@timestamp\',desc))';

    retUrl = `${kibanaLogURL}${segment1}${segment2}${segment3}${segment4}${segment5}${segment6}`;
  }

  return retUrl;
};

export default constructKibanaUrl;
