import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { CellEditable } from 'components/controls/grid/CellEditable';

describe('<CellEditable />', () => {
  let wrapper;

  const props = {
    stateKey: 'TestEditable',
    rowIndex: 1,
    columnIndex: 1,
    maxRowIndex: 4,
    id: '12',
    tableState: {
      activeRowIndex: 1,
      activeColumnIndex: 1,
    },
  };

  const getShallowWrapper = (propsObj) => shallow(<CellEditable
    {...propsObj}
  />);

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getShallowWrapper(props);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      wrapper = mount(<CellEditable
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with state set as true', () => {
      wrapper = shallow(<CellEditable
        {...props}
        className="dsf"
      />);
      wrapper.setState({ value: 'dff', isValid: true, isTouched: true });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with state set as false', () => {
      wrapper = getShallowWrapper(props);
      wrapper.setState({ value: 'dff', isValid: false, isTouched: false });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    describe('validate', () => {
      test('should call validate without validation props', () => {
        const value = 'sds';
        wrapper = getShallowWrapper(props);
        const validated = wrapper.instance().validate(value);
        expect(validated).toEqual(true);
      });

      test('should call validate with validation props', () => {
        const value = 'sds';
        props.validation = jest.fn();
        wrapper = getShallowWrapper(props);
        wrapper.instance().validate(value);
        expect(props.validation).toHaveBeenCalledWith(value);
      });
    });

    describe('onChange', () => {
      const event = {
        target: {
          value: 'fsdfdf',
        },
      };
      test('should call onChange without validation props', () => {
        props.validation = jest.fn();
        props.onValidChange = jest.fn();
        wrapper = getShallowWrapper(props);
        wrapper.setState({ value: 'dff', isValid: true });
        wrapper.instance().onChange(event);
        expect(props.onValidChange).toHaveBeenCalledWith(undefined);
      });

      test('should call onChange with validation props', () => {
        props.onValidChange = jest.fn();
        props.onEdit = jest.fn();
        wrapper = getShallowWrapper(props);
        wrapper.instance().onChange(event);
        expect(props.onEdit).toHaveBeenCalledWith(event.target.value, undefined);
      });
    });

    describe('onClick', () => {
      test('should call onClick with props', () => {
        const event = {
          stopPropagation: jest.fn(),
        };
        props.stopClickPropagation = true;
        wrapper = getShallowWrapper(props);
        wrapper.instance().onClick(event);
        expect(event.stopPropagation).toHaveBeenCalledWith();
      });
    });

    describe('componentWillReceiveProps', () => {
      test('should call componentWillReceiveProps with nextProps as true', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          tableState: {
            activeRowIndex: 1,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(props);
        wrapper.instance().setState = jest.fn();
        wrapper.instance().checkFocus = jest.fn();
        wrapper.instance().componentWillReceiveProps(nextProps);
        expect(wrapper.instance().checkFocus).toHaveBeenCalledWith();
        expect(wrapper.instance().setState).toHaveBeenCalledWith({ value: 'sdsd' });
      });
    });

    describe('onKeyDown', () => {
      test('should call onKeyDown with activateNextCellOnEnter as true', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          suggested: 'sad',
          activateNextCellOnEnter: true,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          key: 'Enter',
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });

      test('should call onKeyDown with activateNextCellOnEnter as false', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          suggested: 'sad',
          activateNextCellOnEnter: false,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          key: 'Enter',
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });

      test('should call onKeyDown with keyCode as 38', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          suggested: { suggestedTags: 'dfdf', intent: 'ffdf' },
          activateNextCellOnEnter: false,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          keyCode: 38,
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });

      test('should call onKeyDown with keyCode as 40', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          suggested: { suggestedTags: 'dfdf', intent: 'ffdf' },
          activateNextCellOnEnter: false,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          keyCode: 40,
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });

      test('should call onKeyDown with keyCode as 39', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          suggested: { suggestedTags: 'dfdf', intent: 'ffdf' },
          activateNextCellOnEnter: false,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          keyCode: 39,
          preventDefault: jest.fn(),
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });

      test('should call onKeyDown with keyCode as 38 without suggested Intent', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          activateNextCellOnEnter: false,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          keyCode: 38,
          preventDefault: jest.fn(),
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });

      test('should call onKeyDown with keyCode as 40 without suggested Intent', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          activateNextCellOnEnter: false,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          keyCode: 40,
          preventDefault: jest.fn(),
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });

      test('should call onKeyDown with keyCode as 9 with shiftKey', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          suggested: { suggestedTags: 'dfdf', intent: 'ffdf' },
          activateNextCellOnEnter: false,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          keyCode: 9,
          shiftKey: 'sdfdsf',
          preventDefault: jest.fn(),
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });

      test('should call onKeyDown with keyCode as 9', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          suggested: { suggestedTags: 'dfdf', intent: 'ffdf' },
          activateNextCellOnEnter: false,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          keyCode: 9,
          preventDefault: jest.fn(),
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });

      test('should call onKeyDown with keyCode as 37', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          suggested: { suggestedTags: 'dfdf', intent: 'ffdf' },
          activateNextCellOnEnter: false,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          keyCode: 37,
          preventDefault: jest.fn(),
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });

      test('should call onKeyDown with keyCode as 27', () => {
        const nextProps = {
          stateKey: 'ddfs',
          rowIndex: 1,
          columnIndex: 1,
          value: 'dss',
          maxRowIndex: 1,
          maxColumnIndex: 1,
          suggested: { suggestedTags: 'dfdf', intent: 'ffdf' },
          activateNextCellOnEnter: false,
          tableState: {
            activeRowIndex: 2,
            activeColumnIndex: 1,
            value: 'sdsd',
          },
        };
        const defaultProps = {
          ...nextProps,
          ...props,
        };
        const event = {
          keyCode: 27,
          preventDefault: jest.fn(),
        };
        props.value = 'sdf';
        wrapper = getShallowWrapper(defaultProps);
        let input = {};
        input.blur = jest.fn();
        wrapper.instance().refs = { input };
        wrapper.instance().onKeyDown(event);
      });
    });

    describe('onBlur', () => {
      test('should call onBlur with validate as true', () => {
        const value = 'sds';
        props.onChange = jest.fn();
        props.value = 'sdf';
        wrapper = getShallowWrapper(props);
        wrapper.setState({ value, isValid: true });
        wrapper.instance().isEscaped = false;
        wrapper.instance().validate = jest.fn().mockImplementation(() => (true));
        wrapper.instance().onBlur();
        expect(props.onChange).toHaveBeenCalledWith(value);
      });

      test('should call onBlur with validate as false', () => {
        const value = 'sds';
        props.onChange = jest.fn();
        props.value = 'sdf';
        wrapper = getShallowWrapper(props);
        wrapper.setState({ value, isValid: true });
        wrapper.instance().isEscaped = false;
        wrapper.instance().validate = jest.fn().mockImplementation(() => (false));
        wrapper.instance().dispatchSetActiveCell = jest.fn();
        wrapper.instance().onBlur();
        expect(wrapper.instance().dispatchSetActiveCell).toHaveBeenCalledWith(false);
      });
    });
  });
});
