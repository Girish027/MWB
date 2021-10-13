import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsDatasetCreate from 'state/actions/actions_dataset_create';
import DatasetColumnMapperDialog from 'components/controls/DatasetColumnMapperDialog';

describe('<DatasetColumnMapperDialog />', () => {
  let wrapper;
  let createDatasetDialog = {
    columns: [
      {
        id: '1',
        name: 'transcription',
        required: true,
        displayName: 'Transcription',
      },
      {
        id: '7',
        name: 'rutag',
        required: false,
        displayName: 'Rollup Intent',
      },
    ],
    previewData: [
      [
        'Transcription',
        'Granular Intent',
        'Rollup Intent',
      ],
      [
        'cancel reservation',
        'reservation-cancel',
        'Reservation_Update',
      ],
    ],
    bindingArray: [
      {
        id: '1',
        columnName: 'transcription',
        columnIndex: '0',
        displayName: 'Transcription',
      },
    ],
    columnsBinding: {
      transcription: 0,
    },
    isPreSelected: true,
    isBindingValid: true,
    skipFirstRow: true,
    isMappingRequestLoading: false,
    isCommitRequestLoading: false,
    token: '0f2e70b7-9ca9-4464-97c3-73bf64b55341',
    fileId: null,
  };
  const props = {
    dispatch: () => {},
    createDatasetDialog,
    onOk: () => {},
  };

  const getShallowWrapper = (propsObj) => shallow(<DatasetColumnMapperDialog
    {...props}
  />);

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = getShallowWrapper(props);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Function Call Handler:', () => {
    beforeAll(() => {
      props.dispatch = jest.fn();
      props.onOk = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
      actionsApp.stopShowingServerMessage = jest.fn(() => 'called stopShowingServerMessage');
      actionsDatasetCreate.reset = jest.fn(() => 'called reset');
      actionsDatasetCreate.changeFirstRowSkip = jest.fn(() => 'called changeFirstRowSkip');
      actionsDatasetCreate.columnsBind = jest.fn(() => 'called columnsBind');
    });

    beforeEach(() => {
      wrapper = shallow(<DatasetColumnMapperDialog
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('onClickCancel:', () => {
      test('should onClickCancel call click handler', () => {
        wrapper.instance().onClickCancel();
        expect(props.dispatch).toHaveBeenCalledWith('called modalDialogChange');
        expect(props.dispatch).toHaveBeenCalledWith('called stopShowingServerMessage');
        expect(props.dispatch).toHaveBeenCalledWith('called reset');
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
        expect(actionsApp.stopShowingServerMessage).toHaveBeenCalledWith();
        expect(actionsDatasetCreate.reset).toHaveBeenCalledWith();
      });
    });

    describe('onChange:', () => {
      test('should change statet and call onChange', () => {
        wrapper.setState({ skipFirstRow: true });
        wrapper.instance().onChange();
        expect(actionsDatasetCreate.changeFirstRowSkip).toHaveBeenCalledWith({
          skipFirstRow: false,
        });
        expect(props.dispatch).toHaveBeenCalledWith('called changeFirstRowSkip');
        expect(wrapper.state().skipFirstRow).toBe(false);
      });
    });

    describe('onClickOk:', () => {
      test('should call onOk prop function', () => {
        wrapper.instance().onClickOk();
        expect(props.onOk).toHaveBeenCalledWith();
      });
    });

    describe('onColumnBindChange:', () => {
      const {
        bindingArray, isBindingValid,
        columnsBinding, isPreSelected,
      } = createDatasetDialog;
      test('should call columnsBind', () => {
        wrapper.instance().onColumnBindChange({
          bindingArray,
          isBindingValid,
          columnsBinding,
          isPreSelected,
        });
        expect(props.dispatch).toHaveBeenCalledWith('called columnsBind');
        expect(actionsDatasetCreate.columnsBind).toHaveBeenCalledWith({
          bindingArray,
          isBindingValid,
          columnsBinding,
          isPreSelected,
        });
      });
    });
  });
});
