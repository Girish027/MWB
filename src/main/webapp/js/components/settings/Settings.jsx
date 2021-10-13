import React, { Component } from 'react';
import { connect } from 'react-redux';
import {
  LegacyRow as Row,
  LegacyColumn as Column,
  VerticalNav,
  VerticalNavItem,
  Button,
  Dialog,
  Checkbox,
  TextField,
} from '@tfs/ui-components';
import * as preferencesActions from 'state/actions/actions_preferences';
import IconButton from 'components/IconButton';
import Constants from 'constants/Constants';
import TensorFlowIcon from '../Icons/TensorFlowIcon';
import NGramIcon from '../Icons/NGramIcon';

export class Settings extends Component {
  constructor(props) {
    super(props);

    this.getStoredTechnology = this.getStoredTechnology.bind(this);
    this.getConfirmationDialog = this.getConfirmationDialog.bind(this);
    this.onClickConfirm = this.onClickConfirm.bind(this);
    this.onClickCancel = this.onClickCancel.bind(this);
    this.onClickCheckbox = this.onClickCheckbox.bind(this);
    this.onChange = this.onChange.bind(this);
    this.getTensorFlowTile = this.getTensorFlowTile.bind(this);
    this.onClickTensorFlowTile = this.onClickTensorFlowTile.bind(this);
    this.getNGramTile = this.getNGramTile.bind(this);
    this.onClickNGramTile = this.onClickNGramTile.bind(this);
    this.onClickNavItem = this.onClickNavItem.bind(this);

    this.props = props;
    this.state = {
      selectedNavItem: Constants.NAV_MODEL_TECHNOLOGIES,
      previousTechnology: Constants.MODEL_TECHNOLOGY.N_GRAM,
      currentTechnology: '',
      showDialog: false,
      isChecked: false,
      componentMounted: false,
      localUpdate: false,
      percentageInADay: 32,
    };
  }

  componentDidMount() {
    this.setState({
      componentMounted: true,
    });
  }

  componentDidUpdate(prevProps, prevState) {
    const prevClientId = prevProps.preferences.clientId;
    const clientId = this.props.preferences.clientId;
    const prevCompMounted = prevState.componentMounted;
    const compMounted = this.state.componentMounted;
    if (prevClientId !== clientId || prevCompMounted !== compMounted) {
      const { clientId } = this.props.preferences;
      this.props.getVectorizer();
      this.props.getTechnology({
        clientId: clientId || this.props.header.client.id,
      });
    }
  }

  onClickNavItem(navItem) {
    this.setState({
      selectedNavItem: navItem[0],
    });
  }

  getStoredTechnology() {
    const { currentTechnology, localUpdate } = this.state;
    const { technology } = this.props.preferences;
    if (technology && technology !== '' && technology !== currentTechnology && !localUpdate) {
      this.setState({
        previousTechnology: currentTechnology,
        currentTechnology: technology,
        localUpdate: false,
      });
    }
  }

  onClickConfirm() {
    const clientId = this.props.header.client.id;
    const { currentTechnology, previousTechnology, isChecked } = this.state;
    this.props.addOrUpdateTechnology({
      clientId,
      technologyValue: currentTechnology,
      updateExistingModels: isChecked,
    });
    this.setState({
      showDialog: false,
      isChecked: false,
      previousTechnology: previousTechnology !== currentTechnology ? currentTechnology : previousTechnology,
    });
  }

  onClickCancel() {
    const { previousTechnology } = this.state;
    this.setState({
      showDialog: false,
      isChecked: false,
      currentTechnology: previousTechnology,
      localUpdate: false,
    });
  }

  onClickCheckbox(event) {
    this.setState({
      isChecked: event.checked,
    });
  }

  onChange(event) {
    const { value } = event.target;
    this.setState({
      percentageInADay: value,
    });
  }

  getConfirmationDialog() {
    const { showDialog, isChecked } = this.state;
    let result;
    if (showDialog) {
      result = (
        <Dialog
          isOpen
          headerChildren={Constants.SAVE_DIALOG_TITLE}
          size="small"
          closeIconVisible={false}
          okChildren="CONFIRM"
          cancelChildren="CANCEL"
          onClickOk={this.onClickConfirm}
          onClickCancel={this.onClickCancel}
        >
          <div
            style={{
              textAlign: 'left',
              lineHeight: '25px',
              fontSize: '14px',
              paddingBottom: '25px',
            }}
          >
            {Constants.SAVE_DIALOG_CONTENT}
          </div>
          <Checkbox
            checked={isChecked}
            value
            label={Constants.SAVE_DIALOG_CHECKBOX}
            onChange={(event) => this.onClickCheckbox(event.target)}
          />
        </Dialog>
      );
    }
    return result;
  }

  onClickTensorFlowTile() {
    const { currentTechnology } = this.state;
    this.setState({
      previousTechnology: currentTechnology,
      currentTechnology: Constants.MODEL_TECHNOLOGY.USE,
      localUpdate: true,
    });
  }

  onClickNGramTile() {
    const { currentTechnology } = this.state;
    this.setState({
      previousTechnology: currentTechnology,
      currentTechnology: Constants.MODEL_TECHNOLOGY.N_GRAM,
      localUpdate: true,
    });
  }

  getTensorFlowIcon = () => {
    const { latestTechnology } = this.props.preferences;
    const version = latestTechnology && latestTechnology.version || '';
    return (
      <TensorFlowIcon
        version={version}
        width={180}
        height={200}
      />
    );
  }

  getTensorFlowTile() {
    const { currentTechnology } = this.state;
    const styles = currentTechnology === Constants.MODEL_TECHNOLOGY.USE ? {
      border: '2px solid #000000',
    } : {
      border: '2px solid #dedede',
    };
    return (
      <IconButton
        onClick={this.onClickTensorFlowTile}
        icon={this.getTensorFlowIcon}
        height={200}
        width={180}
        styleOverride={{
          marginRight: '25px',
          borderRadius: '5px',
          ':hover': {
            border: '2px solid #004c97',
          },
          ...styles,
        }}
      />
    );
  }

  getNGramTile() {
    const { currentTechnology } = this.state;
    const styles = currentTechnology === Constants.MODEL_TECHNOLOGY.N_GRAM ? {
      border: '2px solid #000000',
    } : {
      border: '2px solid #dedede',
    };
    return (
      <IconButton
        onClick={this.onClickNGramTile}
        icon={NGramIcon}
        height={200}
        width={180}
        styleOverride={{
          borderRadius: '5px',
          ':hover': {
            border: '2px solid #004c97',
          },
          ...styles,
        }}
      />
    );
  }

  onNavItemSelected(itemName) {
    let result;
    switch (itemName) {
    case Constants.NAV_MODEL_TECHNOLOGIES:
      this.getStoredTechnology();
      result = (
        <div>
          <div
            style={{
              color: '#333333',
              fontSize: '15px',
              lineHeight: '30px',
              fontWeight: 'bold',
              marginBottom: '4px',
            }}
          >
            {Constants.MODEL_TECHNOLOGY_TITLE}
          </div>
          <div
            style={{
              color: '#9B9B9B',
              fontSize: '14px',
              lineHeight: '13px',
              fontWeight: 'normal',
            }}
          >
            {Constants.MODEL_TECHNOLOGY_DESCRIPTION}
          </div>
          <hr />
          {this.getTensorFlowTile()}
          {this.getNGramTile()}
          <div
            style={{
              paddingLeft: '800px',
              paddingTop: '40px',
            }}
          >
            <Button
              type="primary"
              name="save-as-default-button"
              onClick={() => {
                this.setState({
                  showDialog: true,
                });
              }}
              styleOverride={{
                paddingLeft: '25px',
                paddingRight: '25px',
              }}
            >
              SAVE AS DEFAULT
            </Button>
          </div>
          <hr />
        </div>
      );
      break;
    case Constants.NAV_MODEL_MONITORING:
      result = (
        <div>
          <div
            style={{
              color: '#333333',
              fontSize: '15px',
              lineHeight: '30px',
              fontWeight: 'bold',
              marginBottom: '4px',
            }}
          >
            {Constants.MODEL_MONITORING_CONFIG_TITLE}
          </div>
          <div
            style={{
              color: '#9B9B9B',
              fontSize: '14px',
              lineHeight: '13px',
              fontWeight: 'normal',
            }}
          >
            {Constants.MODEL_MONITORING_DESCRIPTION}
          </div>
          <hr />
          <div
            style={{
              color: '#333333',
              fontSize: '15px',
              lineHeight: '30px',
              fontWeight: 'bold',
              marginBottom: '4px',
            }}
          >
            {Constants.ESCALATION_RATE_TITLE}
          </div>
          <div
            className="Threshold"
            style={{
              color: '#4A4A4A',
              fontSize: '15px',
              lineHeight: '13px',
              fontWeight: 'normal',
            }}
          >
            {Constants.SET_THRESHOLD_TEXT_FOR_A_DAY}
            <input
              name="thresholdValue"
              type="number"
              min="1"
              max="100"
              value={this.state.percentageInADay}
              onChange={this.onChange}
              ref="input"
            />
            {Constants.PERCENTAGE_IN_A_DAY}
          </div>
          <br />
          <hr />
          <div
            style={{
              paddingLeft: '800px',
              paddingTop: '20px',
            }}
          >
            <Button
              type="primary"
              name="save-item"
              styleOverride={{
                paddingLeft: '25px',
                paddingRight: '25px',
              }}
            >
                  SAVE
            </Button>
          </div>
        </div>
      );
      break;
    default:
      // Do nothing
    }
    return result;
  }

  render() {
    const { selectedNavItem } = this.state;
    return (
      <div id="Settings">
        <Row>
          <Column
            size={2}
            styleOverride={{
              height: 'calc(100vh - 101px)',
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
                marginTop: '-20px',
              }}
            >
              <VerticalNavItem
                styleOverride={{ lineHeight: '60px', fontSize: '15px' }}
                name={Constants.NAV_MODEL_TECHNOLOGIES}
              >
                Model Technologies
              </VerticalNavItem>
              <VerticalNavItem
                styleOverride={{ lineHeight: '60px', fontSize: '15px' }}
                name={Constants.NAV_MODEL_MONITORING}
              >
                Model Monitoring
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
            {this.getConfirmationDialog()}
          </Row>
        </Row>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  preferences: state.preferences,
  header: state.header,
});
const mapDispatchToProps = dispatch => ({
  getTechnology: ({ clientId }) => {
    dispatch(preferencesActions.getTechnology({ clientId }));
  },
  addOrUpdateTechnology: ({ clientId, technologyValue, updateExistingModels }) => {
    dispatch(preferencesActions.addOrUpdateTechnology({ clientId, technologyValue, updateExistingModels }));
  },
  getVectorizer: () => {
    dispatch(preferencesActions.getVectorizer());
  },
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(Settings);
