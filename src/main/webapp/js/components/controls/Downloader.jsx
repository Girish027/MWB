import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Button } from '@tfs/ui-components';
import Constants from 'constants/Constants';
import * as actionsApp from 'state/actions/actions_app';
import { getLanguage } from 'state/constants/getLanguage';

class Downloader extends Component {
  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);
    this.downloadAvailableResource = this.downloadAvailableResource.bind(this);
  }

  downloadAvailableResource(file, fileName) {
    // create <a> for downloading the file and then delete it
    const element = document.createElement('a');
    element.style.display = 'none';
    element.href = file;
    element.download = fileName;
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  }

  onClick(event) {
    event.preventDefault();
    const {
      file,
      fileName,
      fileType,
    } = this.props;
    if (file) {
      this.downloadAvailableResource(file, fileName);
    }
  }

  render() {
    const {
      icon, iconProps = {}, children,
    } = this.props;

    let IconComponent = icon;

    return (
      <span>
        <Button
          type="flat"
          name="download-button"
          key="download-file"
          onClick={this.onClick}
          styleOverride={{
            paddingLeft: '10px',
            fontWeight: 'normal',
            ':focus': {
              outline: 'none',
            },
          }}
        >
          {icon
            ? (<IconComponent {...iconProps} />)
            : null
          }
          {children}
        </Button>
      </span>
    );
  }
}

Downloader.propTypes = {
  children: PropTypes.node,
  file: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.instanceOf(Blob),
    PropTypes.instanceOf(File),
  ]),
  fileName: PropTypes.string.isRequired,
  icon: PropTypes.func,
  iconProps: PropTypes.object,
};

Downloader.defaultProps = {
  children: '',
  file: '',
  iconProps: {},
};

export default Downloader;
