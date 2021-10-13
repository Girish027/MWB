
export const customDropDown = {
  label: {
    color: '#6E6E6E',
    fontSize: '14px',
    fontWeight: 'normal',
    paddingBottom: '5px',
  },
  field: {
    width: '99%',
    height: '35px',
  },
  select: {
    border: '1px solid #dce1e8',
    borderRadius: '2px',
    boxSizing: 'border-box',
    height: '35px',
    margin: '0px',
    padding: '8px',
    outline: 'none',
    width: '99%',
    opacity: 1,
    fontSize: '14px',
    marginBottom: '15px',
  },
};

export const customTextarea = {
  marginTop: '10px',
  label: {
    color: '#6E6E6E',
    fontSize: '14px',
    fontWeight: 'normal',
    paddingBottom: '5px',
  },
  field: {
    width: '99%',
  },
};

export const customTextField = {
  label: {
    fontSize: '14px',
    color: '#6E6E6E',
    letterSpacing: 0,
    textAlign: 'left',
    paddingBottom: '5px',
    optionalField: {
      float: 'right',
      textAlign: 'right',
      fontSize: '14px',
      color: '#A0A0A0',
      opacity: 1,
      fontStyle: 'italic',
      marginRight: '1%',
    },
  },
  field: {
    background: '#FFFFFF',
    border: '0px solid #DCE1E8',
    width: '99%',
    input: {
      fontSize: '14px',
      letterSpacing: 0,
      textAlign: 'left',
      height: '35px',
    },
  },
};

export const validationErrorStyle = {
  marginTop: '-10px',
  marginBottom: '10px',
  icon: {
    height: '10px',
  },
  field: {
    fontSize: '13px',
    color: 'red',
  },
};

export const logoutDialogStyle = {
  dialog: {
    container: {
      background: '#FAFAFA',
      maxWidth: '600px',
      display: 'grid',
      gridTemplateRows: '60px auto 60px',
      height: '100%',
      boxShadow: 'rgba(0, 0, 0, 0.2) 1px 3px 3px 0px, rgba(0, 0, 0, 0.2) 1px 3px 15px 2px',

    },
    childContainer: {
      marginTop: '30px',
      marginBottom: '15px',
    },
    content: {
      maxWidth: '532px',
      maxHeight: '515px',
      left: 'calc((110vw - 680px) / 2)',
      top: '243px',
    },
    header: {
      title: {
        marginTop: '20px',
        marginLeft: '20px',
        fontSize: '20px',
      },
    },
    footer: {
      display: 'flex',
      borderTop: '1px solid',
      paddingTop: '12px',
      paddingBottom: '12px',
      paddingRight: '15px',
    },
  },
  text: {
    textAlign: 'left',
    paddingTop: '10px',
    lineHeight: '18px',
    color: '#313f54',
    p: {
      marginTop: '1em',
      marginBottom: '1em',
    },
  },
  card: {
    width: '202px',
    margin: 'auto',
    padding: '20px',
    fontSize: '16px',
    borderRadius: '3px',
    boxShadow: '2px 2px 5px #0000001A',
    boxSizing: 'border-box',
    background: '#FFFFFF 0% 0% no-repeat padding-box',
  },
  cardTitle: {
    fontSize: '16px',
    fontWeight: 'bold',
    lineHeight: '18px',
    color: '#313f54',
    paddingTop: '15px',
    paddingBottom: '10px',
  },
  icon: {
    height: '80px',
    width: '122px',
    marginLeft: 'auto',
    marginRight: 'auto',
  },
};
