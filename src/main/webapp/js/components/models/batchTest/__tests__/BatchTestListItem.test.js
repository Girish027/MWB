import React from 'react';
import { mount, shallow } from 'enzyme';
import renderer from 'react-test-renderer';
import toJSON from 'enzyme-to-json';
import BatchTestListItem from 'components/models/batchTest/BatchTestListItem';
import ReactSVG from 'react-svg';

describe('<BatchTestListItem />', () => {
  const basicProps = {
    focused: false,
    onFocusChange: () => {},
    onClickBatchTestItem: () => {},
    selected: false,
    index: 0,
  };

  const getBatchTestInfo = status => ({
    testId: '64fc4148-3ca7-4958-a40c-cd7a8f02a7cc',
    type: 'DATASETS',
    status,
    requestPayload: 'YesNo,trainingRU-2',
    createdAt: '1536876619527',
    batchTestName: 'BatchTest_1536876619527',
  });

  const props = {
    ...basicProps,
    batchtest: getBatchTestInfo('SUCCESS'),
  };

  let wrapper;

  beforeAll(() => {
    wrapper = shallow(<BatchTestListItem
      {...props}
    />);
  });

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test.each`
      status
      ${'FAILED'}
      ${'SUCCESS'}
      ${'QUEUED'}
      ${'IN_PROGRESS'}
    `('renders correctly for batch test - $status', ({ status }) => {
  const testProps = {
    ...props,
    batchtest: getBatchTestInfo(status),
  };
  wrapper = shallow(<BatchTestListItem
    {...testProps}
  />);
  expect(toJSON(wrapper)).toMatchSnapshot();
});

    test('renders correctly when item is focused', () => {
      wrapper = shallow(<BatchTestListItem
        {...props}
        focused
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when item is selected', () => {
      wrapper = shallow(<BatchTestListItem
        {...props}
        selected
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when item is selected and focused', () => {
      wrapper = shallow(<BatchTestListItem
        {...props}
        focused
        selected
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('toggleFocus:', () => {
    beforeEach(() => {
      props.onFocusChange = jest.fn();
      wrapper = shallow(<BatchTestListItem
        {...props}
      />);
    });

    afterEach(() => {
      props.onFocusChange.mockClear();
    });

    test('should toggle the current state of focus', () => {
      wrapper.setState({
        focus: true,
      });
      wrapper.instance().toggleFocus();
      expect(wrapper.state().focus).toEqual(false);
    });

    test('should call the onFocusChange from props with item index and state of focus', () => {
      wrapper.setState({
        focus: false,
      });
      wrapper.instance().toggleFocus();
      expect(props.onFocusChange).toHaveBeenCalledWith(0, true);
    });
  });

  describe('onMouseUp:', () => {
    beforeEach(() => {
      props.onFocusChange = jest.fn();
      wrapper = shallow(<BatchTestListItem
        {...props}
      />);
    });

    afterEach(() => {
      props.onFocusChange.mockClear();
    });

    test('should set the current state of focus to false', () => {
      wrapper.setState({
        focus: true,
      });
      wrapper.instance().onMouseUp();
      expect(wrapper.state().focus).toEqual(false);
    });

    test('should call the onFocusChange from props with item index and state of focus', () => {
      wrapper.setState({
        focus: true,
      });
      wrapper.instance().onMouseUp();
      expect(props.onFocusChange).toHaveBeenCalledWith(props.index, false);
    });
  });

  describe('getDateFromTimestamp:', () => {
    test.skip('should return the date in US format', () => {
      wrapper = shallow(<BatchTestListItem
        {...props}
      />);
      const input = '1531180800000';
      const dateString = wrapper.instance().getDateFromTimestamp(input);
      expect(dateString).toEqual('Jul 9, 2018');
    });
  });

  describe('onClickBatchTestItem:', () => {
    test('should call the onClickBatchTestItem from props with item index and testId', () => {
      props.onClickBatchTestItem = jest.fn();
      wrapper = shallow(<BatchTestListItem
        {...props}
      />);
      wrapper.instance().onClickBatchTestItem();
      expect(props.onClickBatchTestItem).toHaveBeenCalledWith(props.index, props.batchtest.testId);
    });
  });

  describe('getStatusIcon:', () => {
    test.each`
      status | icon
      ${'FAILED'} | ${'failed'}
      ${'SUCCESS'} | ${'completed'}
      ${'QUEUED'} | ${'queued'}
    `('should get react svg with correct path for status - $status', ({ status, icon }) => {
  const path = `/images/icons/${icon}.svg`;
  const svgWrapper = shallow(wrapper.instance().getStatusIcon(status));
  const expectedWrapper = shallow(<ReactSVG path={path} />);
  expect(svgWrapper).toEqual(expectedWrapper);
});
  });

  describe('getDeleteIcon:', () => {
    test('should get react svg with correct path delete icon', () => {
      const path = '/images/actions/deleteTrash.svg';
      const svgWrapper = shallow(wrapper.instance().getDeleteIcon());
      const expectedWrapper = shallow(<ReactSVG path={path} />);
      expect(svgWrapper).toEqual(expectedWrapper);
    });
  });
});
