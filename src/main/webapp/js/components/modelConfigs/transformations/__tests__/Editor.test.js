import React from 'react';
import { mount, shallow } from 'enzyme';
import Editor from 'components/modelConfigs/transformations/Editor';
import toJSON from 'enzyme-to-json';

describe('<Editor />', () => {
  let wrapper;
  let props = {};
  const { featureFlags } = global.uiConfig;
  const regexReplaceTransformation = {
    'non-breaking-space-regex': {
      type: 'regex-replace',
      mappings: {
        '/\\xao/i': ' ',
      },
    },
  };
  const stemsNoCaseTransformation = {
    'chat-shortcuts': {
      type: 'stems-nocase',
      mappings: {
        '2moro': 'Tomorrow',
        '2nite': 'Tonight',
      },
    },
  };
  const stemsTransformation = {
    'word-substitutions': {
      type: 'stems',
      mappings: {
        '_class_debit_card\'s': '_class_debit_card is',
        'account\'s': 'account',
      },
    },
  };

  const wordClassSubstRegxTransformation = {
    'email-regex': {
      type: 'wordclass-subst-regex',
      mappings: {
        '/(([\\w_\\.-])+@([\\d\\w\\.-])+\\.([a-z\\.]){2,6})/i': '_class_email',
      },
    },
  };

  const stemsUrlTransformation = {
    ewewr: {
      comments: '',
      type: 'stems',
      url: 'sffsdfsdfsdsf',
    },
  };

  const stemsNoCaseUrlTransformation = {
    ewewr: {
      comments: '',
      type: 'stems-nocase-url',
      url: 'sffsdfsdfsdsf',
    },
  };

  const inputTransformation = {
    fsfdsf: {
      comments: '',
      type: 'input-match',
      mappings: {
        '"sdfsdfsdfsdf"': 'dsfdsfddf',
      },
    },
  };

  const wordClassSubstTextTransformation = {
    sdqwewe: {
      comments: '',
      type: 'wordclass-subst-text',
      mappings: {
        ewrewew: '_class_ereew',
      },
    },
  };

  const regexRemovalTransformation = {
    'html-encoding': {
      type: 'regex-removal',
      list: [
        '/%[0-9]+/',
      ],
    },
  };

  const spellCheckTransformation = {
    'enchant-spellcheck-enhance': {
      type: 'spell-checking',
    },
  };

  const stopWordTransformation = {
    stops: {
      type: 'stop-words',
      list: [
        'been',
        'be',
      ],
    },
  };

  //  const DateTransformation = {
  //    type: 'url transformation',
  //    comments: '',
  //  };
  //
  //  const AddressTransformation = {
  //    type: 'url transformation',
  //    comments: '',
  //  };

  beforeAll(() => {
    props = {
      transformation: 'whitespace-normalization',
      userFeatureConfiguration: featureFlags.DEFAULT,
      modelViewReadOnly: false,
      dispatch: jest.fn(),
      isTransformationValid: true,
      onUpdateTransformation: jest.fn(),
    };
  });

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = mount(<Editor />);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly with string type transformation', () => {
      wrapper = shallow(<Editor
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly with string type transformation', () => {
      wrapper = shallow(<Editor
        {...props}
        transformation={regexReplaceTransformation}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    describe('specificEditor:', () => {
      test('should render REGEX_REPLACE component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={regexReplaceTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should render REGEX_REMOVAL component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={regexRemovalTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should render WORDCLASS_SUBST_REGEX component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={wordClassSubstRegxTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should render WORDCLASS_SUBST_TEXT component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={wordClassSubstTextTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should render STOP_WORDS component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={stopWordTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should render SPELL_CHECKING component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={spellCheckTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should render STEMS component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={stemsTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should render STEMS_NOCASE_URL component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={stemsNoCaseUrlTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should render STEMS_URL component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={stemsUrlTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should render STEMS_NOCASE component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={stemsNoCaseTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should render INPUT_MATCH component', () => {
        wrapper = shallow(<Editor
          {...props}
          transformation={inputTransformation}
        />);
        wrapper.instance().specificEditor(false);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      //      test('should render url Transformation for Date', () => {
      //        wrapper = shallow(<Editor
      //          {...props}
      //          transformation={DateTransformation}
      //        />);
      //        wrapper.instance().specificEditor(false);
      //        expect(toJSON(wrapper)).toMatchSnapshot();
      //      });
      //
      //      test('should render url Transformation for Address', () => {
      //        wrapper = shallow(<Editor
      //          {...props}
      //          transformation={AddressTransformation}
      //        />);
      //        wrapper.instance().specificEditor(false);
      //        expect(toJSON(wrapper)).toMatchSnapshot();
      //      });
    });
  });

  describe('Functionality:', () => {
    beforeAll(() => {
      wrapper = shallow(<Editor
        {...props}
        transformation={inputTransformation}
      />);
    });

    describe('onUpdateTags:', () => {
      test('should call dispatch onUpdateTransformation action on onUpdateTags call', () => {
        wrapper.instance().onUpdateTags([]);
        expect(props.onUpdateTransformation).toHaveBeenCalled();
      });
    });

    describe('onUpdateList:', () => {
      test('should call dispatch onUpdateTransformation action on onUpdateList call', () => {
        wrapper.instance().onUpdateList([], regexRemovalTransformation);
        expect(props.onUpdateTransformation).toHaveBeenCalled();
      });
    });

    describe('onUpdateMapping:', () => {
      test('should call dispatch onUpdateTransformation action on onUpdateMapping call', () => {
        wrapper.instance().onUpdateMapping([], regexRemovalTransformation);
        expect(props.onUpdateTransformation).toHaveBeenCalled();
      });
    });

    describe('onTabSelected:', () => {
      test('should set selectedTabIndex state in component', () => {
        wrapper.instance().onTabSelected('selectedTab', 1);
        expect(wrapper.state().selectedTabIndex).toEqual(1);
      });
    });
  });
});
