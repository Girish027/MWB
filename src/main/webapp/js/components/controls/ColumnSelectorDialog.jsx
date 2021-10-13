import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Dialog, Checkbox } from '@tfs/ui-components';
import * as appActions from 'state/actions/actions_app';
import { getLanguage } from 'state/constants/getLanguage';

export default class ColumnSelectorDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.onClickClose = this.onClickClose.bind(this);
    this.onClickCheckbox = this.onClickCheckbox.bind(this);
    this.getSelectors = this.getSelectors.bind(this);

    this.styleOverride = {
      childContainer: {
        margin: '5px 30px auto',
      },
    };

    this.state = {
      modifiedData: {},
    };
  }

  componentDidMount() {
    const { localStorageKey } = this.props;

    let storedSettings = {};
    if (localStorageKey) {
      try {
        storedSettings = JSON.parse(localStorage.getItem(localStorageKey)) || {};
      } catch (e) { /* do nothing */
      }
      storedSettings = Object.assign({}, storedSettings);

      this.setState({ modifiedData: storedSettings });
    }
  }

  onClickClose() {
    const { dispatch } = this.props;

    dispatch(appActions.modalDialogChange(null));
  }

  onClickCheckbox(event) {
    const { handler, localStorageKey } = this.props;

    let column = event.value;

    let selectedColumn = {};
    selectedColumn = Object.assign({}, selectedColumn, {
      [column]: {
        visible: !event.checked,
      },
    });

    let storedSettings = {};
    try {
      storedSettings = JSON.parse(localStorage.getItem(localStorageKey)) || {};
    } catch (e) { // do nothing
    }

    selectedColumn = Object.assign({}, {
      [column]: { ...storedSettings[column], ...selectedColumn[column] },
    });

    let modifiedData = Object.assign({}, storedSettings, selectedColumn);
    localStorage.setItem(localStorageKey, JSON.stringify(modifiedData));

    this.setState({ modifiedData });
    handler();
  }

  getSelectors() {
    const { columnData } = this.props;
    const { modifiedData } = this.state;
    let selector;

    if (columnData) {
      selector = Object.keys(columnData).map(key => (
        <Checkbox
          key={columnData[key].id}
          checked={Object.entries(modifiedData).length > 0 && modifiedData.hasOwnProperty(columnData[key].id) ? !modifiedData[columnData[key].id].visible : true}
          value={columnData[key].id}
          label={columnData[key].header}
          disabled={false}
          onChange={event => this.onClickCheckbox(event.target)}
        />
      ));
    }

    return selector;
  }

  render() {
    const { header } = this.props;

    return (
      <div>
        <Dialog
          size="small"
          isOpen
          closeIconVisible
          onClickClose={this.onClickClose}
          headerChildren={header}
          showFooter={false}
          centerContent
          styleOverride={this.styleOverride}
        >
          {this.getSelectors()}
        </Dialog>
      </div>
    );
  }
}

ColumnSelectorDialog.defaultProps = {
  dispatch: () => {},
  columnData: {},
  header: '',
  localStorageKey: '',

};

ColumnSelectorDialog.propTypes = {
  dispatch: PropTypes.func.isRequired,
  columnData: PropTypes.object.isRequired,
  header: PropTypes.string,
  localStorageKey: PropTypes.string,
};
