import React from 'react';
import { mount, shallow } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import TransformationItems from 'components/modelConfigs/transformations/TransformationItems';
import toJSON from 'enzyme-to-json';
import * as actionsConfigs from 'state/actions/actions_configs';

describe('<TransformationItems />', () => {
  const data = {
    modelName: '',
    name: '',
  };
  let wrapper;
  let props = {};
  const transformations = [
    {
      'non-breaking-space-regex': {
        type: 'regex-replace',
        mappings: {
          '/\\xao/i': ' ',
        },
      },
    },
    'whitespace-normalization',
    {
      'chat-shortcuts': {
        type: 'stems-nocase',
        mappings: {
          XOXO: 'Hugs and Kisses',
        },
      },
    },
    {
      'word-substitutions': {
        type: 'stems',
        mappings: {
          '_class_debit_card\'s': '_class_debit_card is',
          'account\'s': 'account',
          youve: 'you have',
        },
        comments: 'These are client specific abbreviations',
      },
    },
    {
      'email-regex': {
        type: 'wordclass-subst-regex',
        mappings: {
          '/(([\\w_\\.-])+@([\\d\\w\\.-])+\\.([a-z\\.]){2,6})/i': '_class_email',
        },
      },
    },

    {
      'html-encoding': {
        type: 'regex-removal',
        list: [
          '/%[0-9]+/',
        ],
      },
    },
    {
      'enchant-spellcheck-enhance': {
        type: 'spell-checking',
      },
    },
    'case-normalization',
    'training-data-stems',
    {
      stops: {
        type: 'stop-words',
        list: [
          'been',
        ],
      },
    },
  ];

  beforeAll(() => {
    props = {
      transformations,
      onDeleteTransformation: jest.fn(),
      onUpdateTransformationItems: jest.fn(),
      dispatch: jest.fn(),
      onSelectItem: jest.fn(),
      onCreateTransformation: jest.fn(),
      onAddPredefined: jest.fn(),
      showTransformationDeleteDialog: false,
      showTransformationAddDialog: false,
      showTransformationPredefinedDialog: false,
      modelViewReadOnly: false,
      currentIndex: 0,
    };
  });

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/projects/-1/models/createconfig']}
          initialIndex={0}
        >
          <TransformationItems
            data={data}
          />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly with string type transformation', () => {
      wrapper = shallow(<TransformationItems
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders getItems correctly with listItems', () => {
      wrapper = shallow(<TransformationItems
        {...props}
      />);
      wrapper.instance().getItems(transformations);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders getItems correctly with listItems as undefined', () => {
      wrapper = shallow(<TransformationItems
        {...props}
      />);
      wrapper.instance().getItems(undefined);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders getItems correctly with listItems as undefined', () => {
      wrapper = shallow(<TransformationItems
        {...props}
        currentItem="case-normalization"
      />);
      wrapper.instance().getItems(undefined);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders getItems correctly with listItems as undefined', () => {
      wrapper = shallow(<TransformationItems
        {...props}
        currentItem={{
          'non-breaking-space-regex': {
            type: 'regex-replace',
            mappings: {
              '/\\xao/i': ' ',
            },
          },
        }}
      />);
      wrapper.instance().getItems(undefined);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should not call dispatch action on onDragEnd call without destination and source', () => {
      const result = 'eree';
      wrapper.instance().onDragEnd(result);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should not call dispatch action on onDragEnd call without destination and source', () => {
      const state = {
        transformations,
      };
      wrapper.instance().componentDidUpdate(props, state);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders getItems correctly with listItems as undefined', () => {
      wrapper = shallow(<TransformationItems
        {...props}
        showTransformationAddDialog
        showTransformationPredefinedDialog
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeAll(() => {
      actionsConfigs.showTransformationAddDialog = jest.fn(() => 'showTransformationAddDialog');
      actionsConfigs.showTransformationPredefinedDialog = jest.fn(() => 'showTransformationPredefinedDialog');
      actionsConfigs.showTransformationDeleteDialog = jest.fn(() => 'showTransformationDeleteDialog');
    });

    beforeEach(() => {
      wrapper = shallow(<TransformationItems
        {...props}
      />);
      wrapper.setState({
        transformations,
      });
      wrapper.update();
    });

    afterEach(() => {
      jest.clearAllMocks();
    });


    describe('onCreateTransformation:', () => {
      test('should call dispatch action on onCreateTransformation call', () => {
        const item = 'xyz';
        wrapper.instance().onCreateTransformation(item);
        expect(props.dispatch).toHaveBeenCalledWith('showTransformationAddDialog');
        expect(actionsConfigs.showTransformationAddDialog).toHaveBeenCalledWith(false);
        expect(props.onCreateTransformation).toHaveBeenCalledWith(item);
      });
    });

    describe('onCancelAddTransformation:', () => {
      test('should call dispatch action on onCancelAddTransformation call', () => {
        wrapper.instance().onCancelAddTransformation();
        expect(props.dispatch).toHaveBeenCalledWith('showTransformationAddDialog');
        expect(actionsConfigs.showTransformationAddDialog).toHaveBeenCalledWith(false);
      });
    });

    describe('onAddTransformation:', () => {
      test('should call dispatch action on onAddTransformation call', () => {
        wrapper.instance().onAddTransformation();
        expect(props.dispatch).toHaveBeenCalledWith('showTransformationAddDialog');
        expect(actionsConfigs.showTransformationAddDialog).toHaveBeenCalledWith(true);
      });
    });

    describe('onAddPredefined:', () => {
      test('should call dispatch action on onAddPredefined call', () => {
        wrapper.instance().onAddPredefined();
        expect(props.dispatch).toHaveBeenCalledWith('showTransformationPredefinedDialog');
        expect(actionsConfigs.showTransformationPredefinedDialog).toHaveBeenCalledWith(true);
      });
    });

    describe('onCancelAddPredefinedTransformation:', () => {
      test('should call dispatch action on onCancelAddPredefinedTransformation call', () => {
        wrapper.instance().onCancelAddPredefinedTransformation();
        expect(props.dispatch).toHaveBeenCalledWith('showTransformationPredefinedDialog');
        expect(actionsConfigs.showTransformationPredefinedDialog).toHaveBeenCalledWith(false);
      });
    });

    describe('onAddPredefinedTransformations:', () => {
      test('should call dispatch action on onCancelAddPredefinedTransformation call', () => {
        wrapper.instance().onAddPredefinedTransformations();
        expect(props.dispatch).toHaveBeenCalledWith('showTransformationPredefinedDialog');
        expect(actionsConfigs.showTransformationPredefinedDialog).toHaveBeenCalledWith(false);
      });
    });

    describe('onSelect:', () => {
      test('should call dispatch action on onSelect call', () => {
        const event = {
          preventDefault: jest.fn(),
        };
        wrapper.instance().onSelect(event, 0);
        expect(props.onSelectItem).toHaveBeenCalled();
      });
    });

    describe('onCancelDelete:', () => {
      test('should call dispatch action on onCancelDelete call', () => {
        wrapper.instance().onCancelDelete();
        expect(props.dispatch).toHaveBeenCalledWith('showTransformationDeleteDialog');
        expect(actionsConfigs.showTransformationDeleteDialog).toHaveBeenCalledWith(false);
      });
    });

    describe('okToDelete:', () => {
      test('should call dispatch action on okToDelete call', () => {
        wrapper.instance().okToDelete();
        expect(props.dispatch).toHaveBeenCalledWith('showTransformationDeleteDialog');
        expect(actionsConfigs.showTransformationDeleteDialog).toHaveBeenCalledWith(false);
        expect(props.onDeleteTransformation).toHaveBeenCalled();
      });
    });

    describe('onDelete:', () => {
      test('should call dispatch action on onDelete call', () => {
        const event = {
          preventDefault: jest.fn(),
          stopPropagation: jest.fn(),
        };
        wrapper.instance().onDelete(event, 0);
        expect(props.dispatch).toHaveBeenCalledWith('showTransformationDeleteDialog');
        expect(actionsConfigs.showTransformationDeleteDialog).toHaveBeenCalledWith(true);
        expect(event.preventDefault).toHaveBeenCalledWith();
        expect(event.stopPropagation).toHaveBeenCalledWith();
      });
    });

    describe('onDragEnd:', () => {
      test('should call dispatch action on onDragEnd call', () => {
        const result = {
          destination: {
            index: 0,
          },
          source: {
            index: 1,
          },
        };
        wrapper.instance().onDragEnd(result);
        expect(props.onUpdateTransformationItems).toHaveBeenCalled();
      });
    });
  });
});
