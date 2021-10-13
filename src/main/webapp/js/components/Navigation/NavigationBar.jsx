import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import Constants from 'constants/Constants';
import * as actionsApp from 'state/actions/actions_app';
import {
  NavBar, MultiLevelPicker, StringUtils, NavItem, NavLink, SettingsNavIcon,
} from '@tfs/ui-components';
import LogoutWarningDialog from 'components/Navigation/LogoutWarningDialog';
import { RouteNames } from 'utils/routeHelpers';
import { changeRoute } from 'state/actions/actions_app';

class NavigationBar extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.getClientAppConfigArray = this.getClientAppConfigArray.bind(this);
    this.clientsConfig = {
      title: Constants.CLIENT_PICK_TITLE,
      items: [],
    };

    const { clientList } = this.props;

    if ((_.isEmpty(this.clientsConfig) || !this.clientsConfig.items.length) && !_.isNil(clientList) && clientList.length > 0) {
      this.clientsConfig = this.getClientAppConfigArray(this.clientsConfig);
    }

    this.state = {
      showClientPicker: false,
      selectedClientConfig: {},
      displayClientName: [],
      showLogoutWarning: false,
    };
  }

  static getDerivedStateFromProps(props, state) {
    const { selectedClient, clientId = '' } = props;
    const displayClientName = [];
    const {
      itsClientId = '', itsAppId = '', clientDisplayName = '',
      appDisplayName = '',
    } = selectedClient;

    if (!_.isNil(itsClientId) && !_.isEmpty(itsClientId)) {
      const clientTitle = clientDisplayName || StringUtils.titleCase(itsClientId);
      const appTitle = appDisplayName || StringUtils.titleCase(itsAppId);
      displayClientName.push(clientTitle);
      displayClientName.push(appTitle);
      return ({
        displayClientName,
        selectedClientConfig: {
          clientId: itsClientId,
          appId: itsAppId,
        },
      });
    }

    if (clientId) {
      return ({
        displayClientName,
        selectedClientConfig: {
          clientId,
          appId: itsAppId,
        },
      });
    }

    return null;
  }

  getClientAppConfigArray(clientsConfig) {
    if (_.isEmpty(clientsConfig) || !clientsConfig.items.length) {
      const {
        clientList,
      } = this.props;
      if (!_.isNil(clientList) && clientList.length > 0) {
        const getAppsList = (accumulator = [], clientEntry, currentIndex, array) => {
          const { itsAppId, appDisplayName } = clientEntry;
          accumulator.push({
            title: appDisplayName || StringUtils.titleCase(itsAppId),
            field: 'appId',
            data: {
              appId: itsAppId,
            },
          });
          return accumulator;
        };

        clientList.forEach((client, index) => {
          const { itsClientId, clientDisplayName, standardClientName } = client;
          const title = clientDisplayName || StringUtils.titleCase(itsClientId);
          const existingConfig = clientsConfig.items.find(config => config.title === title);
          if (_.isNil(existingConfig)) {
            const clientEntries = clientList.slice(index).filter(cl => cl.itsClientId === itsClientId);
            const items = clientEntries.reduce(getAppsList, []);
            clientsConfig.items.push({
              title,
              field: 'clientId',
              data: {
                clientId: itsClientId,
                standardClientId: standardClientName,
              },
              items,
            });
          }
        });
      }
    }
    return clientsConfig;
  }

  componentDidUpdate(prevProps, prevState) {
    this.clientsConfig = this.getClientAppConfigArray(this.clientsConfig);
  }

  onClientPickerSelect = (selectedClientConfig, isSingleSelection) => {
    const {
      clientId, appId,
      standardClientId, title,
    } = selectedClientConfig;
    this.setState({
      showClientPicker: false,
      selectedClientConfig: {
        clientId,
        appId,
      },
      displayClientName: title,
    }, () => {
      this.props.onSelectClient(standardClientId, appId);
    });
  }

  shouldShowClientPicker = () => {
    const { showClientPicker } = this.state;
    const { selectedClient, clientList } = this.props;
    const { itsClientId = '' } = selectedClient;
    if (clientList.length > 0 && !itsClientId) {
      return true;
    }
    return showClientPicker;
  }

  onCancel = () => {
    this.setState({
      showClientPicker: false,
    });
  }

  getIconLink = () => {
    const { ufpURL } = this.props;
    const iconName = `${Constants.ICON_WORKBENCH}.svg`;
    return `${ufpURL}assets/${iconName}`;
  }

  onClickLogout = () => {
    this.setState({
      showLogoutWarning: true,
    });
  }

  hideLogoutDialog = () => {
    this.setState({
      showLogoutWarning: false,
    });
  }

  onClickClient = () => {
    this.setState({
      showClientPicker: true,
    });
  }

  onClickLogoutOkta = () => {
    const { dispatch } = this.props;
    dispatch(actionsApp.logoutFromOkta());
  }

  onClickSettings = () => {
    const { dispatch, history, clientId } = this.props;
    dispatch(changeRoute(RouteNames.SETTINGS, { clientId }, history));
  }

  render() {
    const {
      userName, email,
      userAccountLink, homeLink,
      documentationLink, supportLink,
    } = this.props;

    const {
      selectedClientConfig, displayClientName,
      showLogoutWarning,
    } = this.state;

    return (
      <div>
        <MultiLevelPicker
          show={this.shouldShowClientPicker()}
          config={this.clientsConfig}
          selection={selectedClientConfig}
          maxDepth={2}
          shouldRetainState
          shouldAutoSelect
          onCancel={this.onCancel}
          onSelect={this.onClientPickerSelect}
        />
        { showLogoutWarning && (
          <LogoutWarningDialog
            isOpen
            onClose={this.hideLogoutDialog}
            onClickOk={this.onClickLogoutOkta}
            productName={Constants.MODEL_WORKBENCH}
            iconLink={this.getIconLink()}
          />
        )}
        <NavBar
          clientValues={displayClientName}
          userInfo={{
            name: userName,
            email,
            onSignOut: this.onClickLogout,
            accountLink: userAccountLink,
          }}
          productName={Constants.MODEL_WORKBENCH}
          onClickClient={this.onClickClient}
          docLink={documentationLink}
          homeLink={homeLink}
          supportLink={supportLink}
        >
          <NavItem right type="icon">
            <NavLink
              onClick={this.onClickSettings}
              value="settings"
              href="javascript:void(0)"
              target="_self"
              icon={SettingsNavIcon}
            />
          </NavItem>
        </NavBar>
      </div>
    );
  }
}

NavigationBar.defaultProps = {
  userName: 'no username',
  email: '',
  userAccountLink: '',
  clientList: [],
  onSelectClient: () => { },
  selectedClient: null,
};


NavigationBar.propTypes = {
  userName: PropTypes.string,
  email: PropTypes.string,
  userAccountLink: PropTypes.string,
  clientList: PropTypes.array,
  onSelectClient: PropTypes.func,
  selectedClient: PropTypes.object,
};

export default NavigationBar;
