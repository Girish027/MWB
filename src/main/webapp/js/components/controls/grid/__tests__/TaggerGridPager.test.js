import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import TaggerGridPager from 'components/controls/grid/TaggerGridPager';

describe('<TaggerGridPager />', () => {
  let wrapper;

  const props = {
    onChange: jest.fn(),
    config: { cursor: 'ssdfd' },
    resultsPerPageOptions: [10, 25, 50, 100, 250, 500],
    limit: 700,
    startIndex: 0,
    showControls: true,
    total: 800,
  };

  const getShallowWrapper = (propsObj) => shallow(<TaggerGridPager
    {...propsObj}
  />);

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      const defaultProps = {
        ...props,
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with className', () => {
      const defaultProps = {
        ...props,
        showControls: true,
        controls: 2,
        limit: 4,
        startIndex: 1,
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    describe('componentWillReceiveProps', () => {
      test('should call componentWillReceiveProps with nextProps', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().componentWillReceiveProps({ stateKey: 'dfdf' });
      });
    });

    describe('componentDidMout', () => {
      test('should call componentDidMout', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleLimitChange = jest.fn();
        wrapper.instance().componentDidMout();
        expect(wrapper.instance().handleLimitChange).toHaveBeenCalledWith(10);
      });
    });

    describe('handleToggleControls', () => {
      test('should call handleToggleControls', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleToggleControls(true);
        expect(props.onChange).toHaveBeenCalledWith({ startIndex: props.startIndex, limit: props.limit, showControls: true });
      });
    });

    describe('handleLimitChange', () => {
      test('should call handleLimitChange', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleLimitChange(30);
        expect(props.onChange).toHaveBeenCalledWith({ startIndex: props.startIndex, limit: 30, showControls: props.showControls });
      });
    });

    describe('handleStartIndexChange', () => {
      test('should call handleStartIndexChange', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleStartIndexChange(10);
        expect(props.onChange).toHaveBeenCalledWith({ startIndex: 10, limit: props.limit, showControls: props.showControls });
      });
    });

    describe('onChangeExactPage', () => {
      test('should call onChangeExactPage', () => {
        const event = {
          target: {
            value: '3',
          },
        };
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleStartIndexChange = jest.fn();
        wrapper.instance().onChangeExactPage(event);
        expect(wrapper.instance().handleStartIndexChange).toHaveBeenCalledWith((Number(event.target.value) - 1) * props.limit);
      });
    });

    describe('onChangeResultPerPage', () => {
      test('should call onChangeResultPerPage', () => {
        const event = {
          target: {
            value: '3',
          },
        };
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleLimitChange = jest.fn();
        wrapper.instance().onChangeResultPerPage(event);
        expect(wrapper.instance().handleLimitChange).toHaveBeenCalledWith(Number(event.target.value));
      });
    });

    describe('onClickShowHideSearchControls', () => {
      test('should call onClickShowHideSearchControls', () => {
        const event = {
          target: {
            value: '3',
          },
        };
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleToggleControls = jest.fn();
        wrapper.instance().onClickShowHideSearchControls(event);
        expect(wrapper.instance().handleToggleControls).toHaveBeenCalledWith(!props.showControls);
      });
    });

    describe('onClickNextBtn', () => {
      test('should call onClickNextBtn', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleStartIndexChange = jest.fn();
        wrapper.instance().onClickNextBtn();
        expect(wrapper.instance().handleStartIndexChange).toHaveBeenCalledWith(700);
      });

      test('should call onClickNextBtn with total change', () => {
        const defaultProps = {
          ...props,
          limit: 100,
          total: 100,
        };
        wrapper = getShallowWrapper(defaultProps);
        wrapper.instance().handleStartIndexChange = jest.fn();
        wrapper.instance().onClickNextBtn();
        expect(wrapper.instance().handleStartIndexChange).not.toHaveBeenCalled();
      });
    });

    describe('onClickPrevBtn', () => {
      test('should call onClickPrevBtn', () => {
        const defaultProps = {
          ...props,
          limit: 100,
          startIndex: 101,
        };
        wrapper = getShallowWrapper(defaultProps);
        wrapper.instance().handleStartIndexChange = jest.fn();
        wrapper.instance().onClickPrevBtn();
        expect(wrapper.instance().handleStartIndexChange).toHaveBeenCalledWith(0);
      });
    });
  });
});
