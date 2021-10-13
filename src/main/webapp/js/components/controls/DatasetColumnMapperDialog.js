import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Dialog, Checkbox } from '@tfs/ui-components';
import Box from 'grommet/components/Box';
import {
  modalDialogChange, stopShowingServerMessage,
} from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import {
  changeFirstRowSkip,
  columnsBind, reset,
} from 'state/actions/actions_dataset_create';
import BindColumnsPreviewGrid from 'components/controls/BindColumnsPreviewGrid';

export default class DatasetColumnMapperDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.onClickCancel = this.onClickCancel.bind(this);
    this.onClickOk = this.onClickOk.bind(this);
    this.onChange = this.onChange.bind(this);
    this.onColumnBindChange = this.onColumnBindChange.bind(this);
    const { createDatasetDialog } = this.props;
    const { isBindingValid, columnsBinding, skipFirstRow } = createDatasetDialog;
    this.state = {
      isBindingValid,
      columnsBinding,
      skipFirstRow,
    };
  }

  onClickCancel() {
    const { dispatch } = this.props;
    dispatch(stopShowingServerMessage());
    dispatch(reset());
    dispatch(modalDialogChange(null));
  }

  onChange() {
    const { dispatch } = this.props;
    const { skipFirstRow } = this.state;
    this.setState({
      skipFirstRow: !skipFirstRow,
    }, () => {
      dispatch(changeFirstRowSkip({ skipFirstRow: !skipFirstRow }));
    });
  }

  onClickOk() {
    const { onOk } = this.props;
    onOk();
  }

  onColumnBindChange({
    bindingArray, isBindingValid, columnsBinding, isPreSelected,
  }) {
    const { dispatch } = this.props;
    this.setState({
      columnsBinding,
      isBindingValid,
    }, () => {
      dispatch(columnsBind({
        bindingArray, isBindingValid, columnsBinding, isPreSelected,
      }));
    });
  }

  render() {
    const { columnsBinding, isBindingValid, skipFirstRow } = this.state;
    const { createDatasetDialog } = this.props;
    const {
      previewData, columns,
      isPreSelected, isMappingRequestLoading,
    } = createDatasetDialog;

    let columnsRequired = [],
      columnsSelected = [],
      columnsSkipped = [];
    if (columns && columns.length) {
      columns.forEach((c, i) => {
        let isRequired = false;
        if (c.required) {
          isRequired = true;
          columnsRequired.push(<span className="Required" key={i}>{c.displayName}</span>);
        }
        if (typeof columnsBinding[c.name] !== 'undefined') {
          columnsSelected.push(<span className={`Selected${isRequired ? ' Required' : ''}`} key={i}>{c.displayName}</span>);
        } else {
          columnsSkipped.push(<span className={`Skipped${isRequired ? ' Required' : ''}`} key={i}>{c.displayName}</span>);
        }
      });
    }

    return (
      <div>
        <Dialog
          isOpen
          okVisible
          okChildren={Constants.NEXT}
          onClickOk={isBindingValid && !isMappingRequestLoading ? this.onClickOk : null}
          closeIconVisible
          onClickClose={this.onClickCancel}
          cancelVisible
          onClickCancel={isMappingRequestLoading ? null : this.onClickCancel}
          cancelChildren={Constants.CANCEL}
          headerChildren={Constants.COLUMNS_MAPPER}
          centerContent={false}
          styleOverride={{
            content: {
              maxWidth: '800px',
              minHeight: '500px',
              left: 'calc((100vw - 800px) / 2)',
            },
            ok: {
              marginLeft: '10px',
              paddingLeft: '25px',
              paddingRight: '25px',
            },
            overlay: {
              zIndex: 9000,
            },
          }}
        >
          <Box
            id="CreateDatasetMapping"
            className="TaggerMapping"
            flex
            direction="column"
          >
            <Box>
                                      Please choose corresponding columns for the file uploaded.
              {isPreSelected ? <div className="notice">Notice: some columns were selected automatically based on first row data.</div> : null}
            </Box>
            <Box
              className="ContentRow BindingInfo"
              direction="column"
            >
              <Box direction="row">
                <Box className="InfoLabel">Required:</Box>
                <Box className={`InfoText${columnsRequired.length ? '' : ' Empty'}`} flex direction="row">
                  {columnsRequired.length ? columnsRequired.reduce((prev, curr) => [prev, ', ', curr]) : <span>none</span>}
                </Box>
              </Box>
              <Box direction="row">
                <Box className="InfoLabel">Selected:</Box>
                <Box className={`InfoText${columnsSelected.length ? '' : ' Empty'}`} flex direction="row">
                  {columnsSelected.length ? columnsSelected.reduce((prev, curr) => [prev, ', ', curr]) : <span>none</span>}
                </Box>
              </Box>
              <Box direction="row">
                <Box className="InfoLabel">Skipped:</Box>
                <Box className={`InfoText${columnsSkipped.length ? '' : ' Empty'}`} flex direction="row">
                  {columnsSkipped.length ? columnsSkipped.reduce((prev, curr) => [prev, ', ', curr]) : <span>none</span>}
                </Box>
              </Box>
            </Box>
            <BindColumnsPreviewGrid
              data={previewData}
              columns={columns}
              columnsBinding={columnsBinding}
              onColumnBindChange={this.onColumnBindChange}
            />
            <Box
              className="ContentRow SkipFirstRow"
            >
              <Checkbox
                checked={skipFirstRow}
                value="skip-first-row"
                label="Skip First Row"
                onChange={this.onChange}
              />
            </Box>
          </Box>
        </Dialog>
      </div>
    );
  }
}

DatasetColumnMapperDialog.propTypes = {
  dispatch: PropTypes.func,
  createDatasetDialog: PropTypes.object,
  onOk: PropTypes.func,
};

DatasetColumnMapperDialog.defaultProps = {
  dispatch: () => {},
  createDatasetDialog: {},
  onOk: () => {},
};
