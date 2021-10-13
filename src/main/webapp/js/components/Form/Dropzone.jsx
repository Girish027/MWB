import React, { useMemo, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import PropTypes from 'prop-types';
import Constants from 'constants/Constants';

export default function Dropzone(props) {
  const {
    accept,
    saveFile,
    rejectFile,
    multiple,
    disabled,
    shouldUpload,
    uploadHandler,
    icon: FileIcon,
    acceptedIcon: AcceptIcon,
    acceptStyle,
    activeStyle,
    rejectStyle,
    baseStyle,
    removeFile,
  } = props;

  const sizeRestrictions = {};
  if (props.maxSize) {
    sizeRestrictions.maxSize = props.maxSize;
  }
  if (props.minSize) {
    sizeRestrictions.minSize = props.minSize;
  }

  const status = {
    ready: 'ready',
    accepted: 'accepted',
    rejected: 'rejected',
    acceptedFiles: [],
  };

  const state = {
    status: status.ready,
  };

  const eventHandlers = {
    onDrop: useCallback((acceptedFiles) => {
      if (acceptedFiles.length) {
        state.status = status.accepted;
        state.acceptedFiles = removeFile ? [] : acceptedFiles;
        saveFile(removeFile ? [] : acceptedFiles);

        if (shouldUpload) {
          uploadHandler(removeFile ? [] : acceptedFiles);
        }
      }
    }, [saveFile, shouldUpload, state.acceptedFiles, state.status, status.accepted, uploadHandler]),

    onDropRejected: useCallback((rejectedFiles) => {
      state.status = status.rejected;
      state.rejectedFiles = rejectedFiles;
      rejectFile(rejectedFiles);
    }, [rejectFile]),
  };

  const {
    getRootProps,
    getInputProps,
    isDragActive,
    isDragAccept,
    isDragReject,
    acceptedFiles,
  } = useDropzone({
    ...eventHandlers,
    accept,
    multiple,
    disabled,
    preventDropOnDocument: true,
    ...sizeRestrictions,
  });

  const style = useMemo(() => ({
    ...baseStyle,
    ...(isDragActive ? activeStyle : {}),
    ...(isDragAccept ? acceptStyle : {}),
    ...(isDragReject ? rejectStyle : {}),
  }), [baseStyle, isDragActive, activeStyle, isDragAccept, acceptStyle, isDragReject, rejectStyle]);

  const renderDetails = (view) => {
    switch (view) {
    case status.ready:
      return (
        <div>
Drag and drop your file
          <div> or </div>
          <div style={{ cursor: 'pointer', color: '#004c97' }}> BROWSE</div>
        </div>
      );
    case status.accepted: {
      return Constants.DROPZONE.ACCEPCTED;
    }
    case status.rejected: {
      return Constants.DROPZONE.REJECTED(accept);
    }
    default: return null;
    }
  };


  let newAcceptedFiles = removeFile ? [] : acceptedFiles;
  return (
    <div {...getRootProps({ style })}>
      <input {...getInputProps()} />
      <div style={{ margin: 'auto' }} className="dropzone-content">
        {newAcceptedFiles.length > 0
          ? newAcceptedFiles.map((acceptedFile) => (
            <div style={{ display: 'inline-block' }} key={acceptedFile.name}>
              {AcceptIcon
                ? (
                  <div style={{ textAlign: 'center' }}>
                    <AcceptIcon height={48} width={48} fill="#004c97" />
                  </div>
                )
                : null
              }
              <div style={{ paddingTop: '10px', whiteSpace: 'pre-wrap', textAlign: 'center' }}>
                {acceptedFile.name}
              </div>
            </div>
          )) : (
            <div style={{ display: 'inline-block' }}>
              {FileIcon
                ? (
                  <div style={{ textAlign: 'center' }}>
                    <FileIcon height={48} width={48} />
                  </div>
                )
                : null
              }
              <div style={{ paddingTop: '10px', whiteSpace: 'pre-wrap', textAlign: 'center' }}>
                {isDragAccept && renderDetails(status.accepted)}
                {isDragReject && renderDetails(status.rejected)}
                {!isDragActive && renderDetails(status.ready)}
              </div>
            </div>
          )
        }
      </div>
    </div>
  );
}

const dropzoneStyles = {
  baseStyle: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    height: '170px',
    alignItems: 'center',
    padding: '20px',
    borderWidth: 2,
    borderRadius: 2,
    borderColor: '#eeeeee',
    borderStyle: 'dashed',
    backgroundColor: '#fafafa',
    color: '#bdbdbd',
    outline: 'none',
    transition: 'border .24s ease-in-out',
  },
  activeStyle: {
    borderColor: '#2196f3',
  },

  acceptStyle: {
    borderColor: '#00e676',
  },

  rejectStyle: {
    borderColor: '#ff1744',
  },
};


Dropzone.propTypes = {
  accept: PropTypes.string.isRequired,
  saveFile: PropTypes.func,
  rejectFile: PropTypes.func,
  uploadHandler: PropTypes.func,
  icon: PropTypes.func,
  acceptedIcon: PropTypes.func,
  multiple: PropTypes.bool,
  disabled: PropTypes.bool,
  shouldUpload: PropTypes.bool,
  acceptStyle: PropTypes.object,
  activeStyle: PropTypes.object,
  rejectStyle: PropTypes.object,
  baseStyle: PropTypes.object,
  maxSize: PropTypes.number,
  minSize: PropTypes.number,
};

Dropzone.defaultProps = {
  saveFile: () => {},
  rejectFile: () => {},
  uploadHandler: () => {},
  icon: null,
  acceptedIcon: null,
  multiple: false,
  disabled: false,
  shouldUpload: false,
  acceptStyle: dropzoneStyles.acceptStyle,
  activeStyle: dropzoneStyles.activeStyle,
  rejectStyle: dropzoneStyles.rejectStyle,
  baseStyle: dropzoneStyles.baseStyle,
  maxSize: undefined,
  minSize: undefined,
  removeFile: false,
};
