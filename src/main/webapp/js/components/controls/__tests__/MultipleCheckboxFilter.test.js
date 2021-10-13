import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import MultipleCheckboxFilter from 'components/controls/MultipleCheckboxFilter';

const defaultProps = {
  label: 'Test Label',
  options: [
    { value: '0', label: 'Zero' },
    { value: '1', label: 'One' },
    { value: '2', label: 'Two' },
    { value: '3', label: 'Three' },
  ],
  value: ['3'],
  onChange: () => {},
};

describe('<MultipleCheckboxFilter />', () => {
  let wrapper;

  describe('Snapshots', () => {
    test('Creating an instance with label and 3 options - row format', () => {
      // should have 3 Checkboxes with 3rd one checked.
      // should have Label
      wrapper = shallow(<MultipleCheckboxFilter
        {...defaultProps}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('Creating an instance with initial value and className and no label- row format', () => {
      // should have 3 Checkboxes with 1st and 3rd  checked.
      // should not have Label
      // should have the custom classname
      const currentProps = {
        ...defaultProps,
        value: ['1', '3'],
        className: 'CustomClassName',
      };
      delete currentProps.label;
      wrapper = shallow(<MultipleCheckboxFilter
        {...currentProps}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders in a Grid format', () => {
      // should have 3 Checkboxes with 3rd one checked.
      // should have Label
      wrapper = shallow(<MultipleCheckboxFilter
        {...defaultProps}
        showAsGrid
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders in a Grid format - no label', () => {
      // should have 3 Checkboxes with 3rd one checked.
      wrapper = shallow(<MultipleCheckboxFilter
        {...defaultProps}
        showAsGrid
        label={undefined}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders checkboxes in disabled state', () => {
      // should have 3 Checkboxes with 3rd one checked.
      // should have Label
      wrapper = shallow(<MultipleCheckboxFilter
        {...defaultProps}
        showAsGrid
        disabled
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('onChange:', () => {
    beforeEach(() => {
      defaultProps.onChange = jest.fn();
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    test('should update parent with new Value when option is unchecked', () => {
      wrapper = shallow(<MultipleCheckboxFilter
        {...defaultProps}
        value={['1', '2', '3']}
      />);
      wrapper.instance().onChange({}, defaultProps.options[2]);
      expect(defaultProps.onChange).toHaveBeenCalledWith(['1', '3'], '2');
    });

    test('should update parent with new Value when option is checked', () => {
      const option = { value: '', label: 'Three' };
      wrapper = shallow(<MultipleCheckboxFilter
        {...defaultProps}
        value={['1', '2']}
      />);
      wrapper.instance().onChange({}, defaultProps.options[3]);
      expect(defaultProps.onChange).toHaveBeenCalledWith(['1', '2', '3'], '3');
    });

    test('should not update parent with new Value if allowEmpty is false and change was triggered on last checkbox', () => {
      wrapper = shallow(<MultipleCheckboxFilter
        {...defaultProps}
        value={['1']}
        allowEmpty={false}
      />);
      wrapper.instance().onChange({}, defaultProps.options[1]);
      expect(defaultProps.onChange).not.toHaveBeenCalledWith([]);
      expect(defaultProps.onChange.mock.calls.length).toEqual(0);
    });

    test('should update parent with new Value if allowEmpty is true and change was triggered on last checkbox', () => {
      wrapper = shallow(<MultipleCheckboxFilter
        {...defaultProps}
        value={['1']}
        allowEmpty
      />);
      wrapper.instance().onChange({}, defaultProps.options[1]);
      expect(defaultProps.onChange).not.toHaveBeenCalledWith(['1']);
      expect(defaultProps.onChange.mock.calls.length).toEqual(1);
    });
  });
});
