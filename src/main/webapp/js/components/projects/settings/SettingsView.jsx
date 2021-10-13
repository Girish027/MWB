import React, { Component } from 'react';
import { connect } from 'react-redux';
import {
  LegacyRow as Row,
  LegacyColumn as Column,
  TextField,
  Tabs,
  VerticalNav,
  VerticalNavItem,
} from '@tfs/ui-components';
import Constants from 'constants/Constants';

export class SettingsView extends Component {
  constructor(props) {
    super(props);

    this.getModelLevelMonitoringSection = this.getModelLevelMonitoringSection.bind(this);
    this.getNodeLevelMonitoringSection = this.getNodeLevelMonitoringSection.bind(this);
    this.getNodeLevelMonitoringTabs = this.getNodeLevelMonitoringTabs.bind(this);
    this.renderUniversalTabItems = this.renderUniversalTabItems.bind(this);
    this.renderEachNodeTabItems = this.renderEachNodeTabItems.bind(this);
    this.onNavItemSelected = this.onNavItemSelected.bind(this);
    this.state = {
      activeNodeLevelTab: Constants.UNIVERSAL_TAB,
      selectedNavItem: 'Node 1',
      percentageForADay: '30%',
      percentageOnConsecutiveDays: '0%',
    };
  }

  onTabSelected = (selectedTab, selectedTabIndex) => {
    let tab = '';
    switch (selectedTabIndex) {
    case 0:
      tab = Constants.UNIVERSAL_TAB;
      break;
    case 1:
      tab = Constants.EACH_NODE_TAB;
      break;
    default:
      tab = Constants.UNIVERSAL_TAB;
      break;
    }
    this.setState({
      activeNodeLevelTab: tab,
    });
  }

  renderUniversalTabItems() {
    return (
      <Row>
        <div className="EscalationRate" style={{ paddingTop: '15px' }}>
          {Constants.ESCALATION_RATE_TITLE}
        </div>
        <div className="EscalationRateDesc">
          {Constants.ESCALATION_RATE_DESCRIPTION}
        </div>
        <div className="Threshold">
          {Constants.SET_THRESHOLD_TEXT_FOR_A_DAY}
          <TextField
            name="thresholdValue"
            defaultValue={this.state.percentageForADay}
            onChange={() => {}}
            styleOverride={{
              width: '45px',
              input: {
                width: '100%',
                height: '23px',
              },
            }}
          />
          {Constants.PERCENTAGE_FOR_A_DAY}
        </div>
      </Row>
    );
  }

  onNavItemSelected(itemName) {
    if (itemName) {
      return (
        <div>
          <div className="EscalationRate">
            {Constants.ESCALATION_RATE_TITLE}
          </div>
          <div className="Threshold">
            {Constants.SET_THRESHOLD_TEXT_FOR_A_DAY}
            <TextField
              name="thresholdValue"
              defaultValue={this.state.percentageForADay}
              onChange={() => {}}
              styleOverride={{
                width: '45px',
                input: {
                  width: '100%',
                  height: '23px',
                },
                helpText: {
                  height: '0px',
                },
              }}
            />
            {Constants.PERCENTAGE_FOR_A_DAY}
          </div>
          <div className="DefaultThreshold">
            {Constants.DEFAULT_THRESHOLD}
          </div>
        </div>
      );
    }
  }

  onClickNavItem = (navItem) => {
    this.setState({
      selectedNavItem: navItem[0],
    });
  }

  renderEachNodeTabItems() {
    const { selectedNavItem } = this.state;
    return (
      <Row>
        <Column
          size={2}
          styleOverride={{
            height: 'auto',
            borderRight: '1px solid #CCC',
            position: 'relative',
          }}
        >
          <VerticalNav
            selectedItems={[this.state.selectedNavItem]}
            onSelection={this.onClickNavItem}
            styleOverride={{
              boxShadow: 'none',
              overflowY: 'auto',
              padding: '20px 0px',
              marginLeft: -6,
            }}
          >
            <VerticalNavItem
              name="Node 1"
            >
              Node 1
            </VerticalNavItem>
            <VerticalNavItem
              name="Node 2"
            >
              Node 2
            </VerticalNavItem>
          </VerticalNav>
        </Column>
        <Row>
          <Column
            size={9}
            styleOverride={{
              padding: '25px 0 0 25px',
              overflowY: 'auto',
            }}
          >
            {this.onNavItemSelected(selectedNavItem)}
          </Column>
        </Row>
      </Row>
    );
  }

  getNodeLevelMonitoringTabs() {
    const { activeNodeLevelTab } = this.state;
    const tabs = [Constants.UNIVERSAL_TAB, Constants.EACH_NODE_TAB];
    const tabPanels = [this.renderUniversalTabItems(), this.renderEachNodeTabItems()];

    let selectedTabIndex = 0;
    switch (activeNodeLevelTab) {
    case Constants.UNIVERSAL_TAB:
      selectedTabIndex = 0;
      break;
    case Constants.EACH_NODE_TAB:
      selectedTabIndex = 1;
      break;
    default:
      selectedTabIndex = 0;
      break;
    }

    return (
      <div className="tabs">
        <Tabs
          tabs={tabs}
          tabPanels={tabPanels}
          onTabSelected={this.onTabSelected}
          align="left"
          selectedIndex={selectedTabIndex}
          styleOverride={{
            tabItem: {
              padding: '23px 20px 21px',
            },
            tabContainer: {
              borderTop: 'none',
            },
          }}
        />
      </div>
    );
  }

  getModelLevelMonitoringSection() {
    return (
      <Row>
        <div className="ModelLevelMonitoringTitle">
          {Constants.MODEL_LEVEL_MONITORING_THRESHOLD_TITLE}
        </div>
        <hr color="#bcc1c7" />
        <div className="EscalationRate">
          {Constants.ESCALATION_RATE_TITLE}
        </div>
        <div className="Threshold">
          {Constants.SET_THRESHOLD_TEXT_FOR_A_DAY}
          <TextField
            name="thresholdValue"
            defaultValue={this.state.percentageForADay}
            onChange={() => {}}
            styleOverride={{
              width: '45px',
              input: {
                width: '100%',
                height: '23px',
              },
              helpText: {
                height: '0px',
              },
            }}
          />
          {Constants.PERCENTAGE_FOR_A_DAY}
        </div>
        <div className="DefaultThreshold">
          {Constants.DEFAULT_THRESHOLD}
        </div>
        <div className="Threshold">
          {Constants.SET_THRESHOLD_TEXT_FOR_CONSECUTIVE_DAYS}
          <TextField
            name="thresholdValue"
            defaultValue={this.state.percentageOnConsecutiveDays}
            onChange={() => {}}
            styleOverride={{
              width: '45px',
              input: {
                width: '100%',
                height: '23px',
              },
              helpText: {
                height: '0px',
              },
            }}
          />
          {Constants.PERCENTAGE_ON_CONSECUTIVE_DAYS}
        </div>
      </Row>
    );
  }

  getNodeLevelMonitoringSection() {
    return (
      <div>
        <div className="NodeLevelMonitoringTitle">
          {Constants.NODE_LEVEL_MONITORING_THRESHOLD_TITLE}
        </div>
        <hr color="#bcc1c7" style={{ marginBottom: '0px' }} />
        <div>
          {this.getNodeLevelMonitoringTabs()}
        </div>
      </div>
    );
  }

  render() {
    return (
      <div id="SettingsTab">
        <Row>
          {this.getModelLevelMonitoringSection()}
          {this.getNodeLevelMonitoringSection()}
        </Row>
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => ({});
const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(SettingsView);
