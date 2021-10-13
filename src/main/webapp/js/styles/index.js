export const colors = {
  darkText: '#313F54',
  disabledText: '#9B9B9B',
  lightText: '#a7a9af',
  border: '#DADADD',
  headerBackground: '#F6F7F8',
  orange: '#EF8822',
  darkOrange: '#bf6c1b',
  navy: '#313f54',
  focusItem: '#f3f4f5',
  selectedItem: '#eee',
  white: '#fff',
  prussianBlue: '#003467',
  cobalt: '#004c97',
};

export const styleColors = {
  headerBackground: '#f6f7f8',
};

export const actionItems = {
  icon: {
    width: '10px',
    height: '13px',
  },
};

export const actionBar = {
  marginLeft: '-10px',

  contextualBar: {
    borderBottom: 'none',
    borderTop: 'none',
    paddingTop: '5px',
  },
};

export const dropdownRightAlign = {
  containerStyleOverride: {
    position: 'relative',
  },
  menuListStyleOverride: {
    right: 0,
  },
};

// for Create Version Screens.
export const actionBarStyles = {
  bar: {
    borderTop: 'none', borderBottom: '1px solid #ddd',
  },
  item: {
    margin: '24px 0px',
  },
};

export const progressDialogLabel = {
  marginBottom: '25px',
  textAlign: 'left',
  color: '#313F54',
};

export const uploadDataset = {

  container: {
    display: 'flow-root',
    paddingBottom: '5px',
    marginTop: '10px',
  },
  label: {
    color: '#6E6E6E',
    fontSize: '14px',
    fontWeight: 'normal',
    paddingBottom: '5px',
  },

  link: {
    float: 'right',
    display: 'inline-flex',
    icon: {
      paddingRight: '5px',
    },
    label: {
      color: '#6E6E6E',
      fontSize: '12px',
      fontWeight: 'normal',
      textDecoration: 'underline',
      cursor: 'pointer',
    },
  },
};

// Speech enchancement styles
export const multipleCheckboxFilter = {
  paddingLeft: '10px',
  border: '1px solid #DDDDDD',
  marginTop: '10px',
  boxShadow: '5px 3px 5px #d3d3d3',
  height: '220px',
  overflow: 'auto',
};

// Speech enchancement styles
export const speechDialog = {
  childContainer: {
    paddingLeft: '30px',
    paddingRight: '30px',
    paddingTop: '0px',
    paddingBottom: '0px',
    overflow: 'auto',
    textAlign: 'left',
  },
  container: {
    width: '520px',
    height: '480px',
    display: 'grid',
    gridTemplateRows: '60px auto 60px',
  },
  ok: {
    marginLeft: '10px',
    paddingLeft: '25px',
    paddingRight: '25px',
  },
  cancel: {
    marginRight: '10px',
    paddingLeft: '25px',
    paddingRight: '15px',
  },
  header: {
    width: '100%',
    height: '100%',
    borderBottom: '1px solid',
    borderBottomColor: '#DADADA',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    title: {
      marginLeft: '23px',
      marginRight: '20px',
      fontSize: '20px',
      fontWeight: 'bold',
    },
    titleIcon: {
      marginRight: '20px',
    },
  },
};

// Speech enchancement styles
export const seperator = {
  paddingLeft: '30px',
  paddingRight: '30px',
};

// Speech enchancement styles
export const moreOptionBtn = {
  fontWeight: 'normal',
  marginTop: '15px',
};

export const seperators = {
  width: '400px',
  marginBottom: '30px',
  marginRight: 'auto',
  marginLeft: 'auto',
  marginTop: '30px',
};

// Speech enchancement styles
export const speechModal = {
  container: {
    display: 'flex', height: '100%',
  },

  datasetContainer: {
    paddingLeft: '0px',
    borderRight: '1px solid #DADADA',
    width: '50%',
    label: {
      paddingBottom: '10px',
      marginTop: '30px',
      color: '#6E6E6E',
      fontSize: '14.5px',
      fontWeight: 'normal',
    },
  },
};

// Speech enchancement styles
export const dropzoneContainer = {
  label: {
    color: '#6E6E6E',
    fontSize: '14.5px',
    fontWeight: 'normal',
    paddingBottom: '5px',
  },

  link: {
    float: 'right',
    display: 'inline-flex',
    icon: {
      paddingRight: '5px',
      marginTop: '-3px',
    },
    label: {
      color: '#6E6E6E',
      fontSize: '12.5px',
      fontWeight: 'normal',
      textDecoration: 'underline',
      cursor: 'pointer',
    },
  },
  // Speech enchancement styles
  speechRadioAndTextGroup: {
    marginTop: '20px',
    label: {
      marginBottom: '10px',
      color: '#6E6E6E',
      fontSize: '14.5px',
      fontWeight: 'normal',
      paddingBottom: '5px',
    },
  },
};

export const combinedSpeechDialogStyle = {
  container: {
    width: '400px',
    height: '300px',
    display: 'grid',
    gridTemplateRows: '60px auto 60px',
  },
  childContainer: {
    marginTop: '30px',
    marginBottom: '20px',
  },
  top: '20%',
  cancel: {
    backgroundColor: 'unset',
    paddingLeft: '10px',
    paddingRight: '10px',
  },
  ok: {
    marginLeft: '10px',
    paddingLeft: '25px',
    paddingRight: '25px',
  },
};

export const radioStyle = {
  container: {
    disabled: {
      cursor: 'not-allowed',
      lineHeight: '20px',
      display: 'flex',
      alignItems: 'center',
    },
    active: {
      cursor: 'pointer',
      lineHeight: '20px',
      display: 'flex',
      alignItems: 'center',
    },
  },
  input: {
    disabled: {
      opacity: 0,
      position: 'absolute',
      cursor: 'not-allowed',
      visibility: 'hidden',
    },
    active: {
      opacity: 0,
      position: 'absolute',
      cursor: 'pointer',
      visibility: 'hidden',
    },
  },
  label: {
    color: '#111111',
    fontSize: '17px',
    lineHeight: '20px',
    fontWeight: 'bold',
    padding: '0px 0px 0px 10px',
  },
  imageContainer: {
    checked: {
      border: '1px solid #FF8822',
      backgroundColor: '#F2E2D7',
    },
    hover: {
      border: '1px solid #FF8822',
    },
    unchecked: {
      border: '1px solid #E6E6E6',
    },
  },
  imageContainerChecked: {
    border: '1px solid #FF8822',
    backgroundColor: '#F2E2D7',
  },
  image: {
    maxHeight: '150px',
    maxWidth: '200px',
    display: 'block',
    margin: '20px auto',
    textAlign: 'center',
    boxShadow: '0px 0px 10px rgba(0, 0, 0, 0.35)',
  },
  icon: {
    disabled: {
      borderColor: '#DCE1E8',
    },
    activeChecked: {
      borderColor: '#FF8822',
    },
    activeUnchecked: {
      borderColor: '#999999',
    },
  },
};

export const headerIcon = {
  header: {
    width: '100%',
    height: '100%',
    borderBottom: '1px solid',
    borderBottomColor: '#DADADA',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    title: {
      marginLeft: '23px',
      marginRight: '20px',
      fontSize: '20px',
      fontWeight: 'bold',
    },
    titleIcon: {
      marginRight: '20px',
    },
  },
};
