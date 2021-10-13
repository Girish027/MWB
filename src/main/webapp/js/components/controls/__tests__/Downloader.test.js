import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import Downloader from 'components/controls/Downloader';
import { Download } from '@tfs/ui-components';

describe('<Downloader />', () => {
  const file = new Blob([]);
  const props = {
    file,
    fileName: 'download.file',
    icon: Download,
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<Downloader
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly with just the icon', () => {
      wrapper = shallow(<Downloader
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly with just the icon and label', () => {
      wrapper = shallow(<Downloader
        {...props}
      >
        {' '}
Download
        {' '}
      </Downloader>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly with just label', () => {
      wrapper = shallow(<Downloader
        {...props}
        icon={undefined}
      >
        {' '}
Download
        {' '}
      </Downloader>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    let event;
    beforeAll(() => {
      event = {
        preventDefault: jest.fn(),
      };
    });

    beforeEach(() => {
      wrapper = shallow(<Downloader
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('onClick:', () => {
      beforeEach(() => {
        wrapper.instance().downloadAvailableResource = jest.fn();
      });

      test('should prevent default behaviour of triggered event and update state', () => {
        wrapper.instance().onClick(event);
        expect(event.preventDefault).toHaveBeenCalled();
      });

      test('should download file if it is available', () => {
        wrapper.instance().onClick(event);
        expect(wrapper.instance().downloadAvailableResource).toHaveBeenCalledWith(props.file, props.fileName);
      });

      test('should not try to download file if it is not available', () => {
        wrapper.setProps({
          file: undefined,
        });
        wrapper.update();
        wrapper.instance().onClick(event);
        expect(wrapper.instance().downloadAvailableResource).not.toHaveBeenCalled();
      });
    });

    describe('downloadAvailableResource:', () => {
      let click;
      beforeEach(() => {
        click = jest.fn();
        document.createElement = jest.fn().mockImplementationOnce(() => ({
          style: {},
          click,
        }));
        document.body.appendChild = jest.fn();
        document.body.removeChild = jest.fn();
      });

      test('should prevent default behaviour of triggered event and update state', () => {
        wrapper.instance().downloadAvailableResource(props.file, props.fileName);
        expect(document.createElement).toHaveBeenCalledWith('a');
        expect(document.body.appendChild).toHaveBeenCalledWith({
          style: {
            display: 'none',
          },
          href: props.file,
          download: props.fileName,
          click,
        });
        expect(click).toHaveBeenCalled();
        expect(document.body.removeChild).toHaveBeenCalledWith({
          style: {
            display: 'none',
          },
          href: props.file,
          download: props.fileName,
          click,
        });
      });
    });
  });
});
