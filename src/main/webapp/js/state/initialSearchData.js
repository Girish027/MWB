import { Map } from 'immutable';
import { SORTED_NONE } from './actions/types';

export const initialStatsResults = {
  intents: 0,
  unique: {
    percent: 0,
    tagged: 0,
    total: 0,
  },
  all: {
    percent: 0,
    tagged: 0,
    total: 0,
  },
  status: 0,
};

export const initialValidDatasetsStats = {
  isDatasetsTagged: true,
  isDatasetsValid: false,
  datasetsIntents: [],
  validDatasetMap: new Map(),
};

export const initialColumnSort = {
  check: { sorted: SORTED_NONE, selected: false }, // options none, AS
  count: { sorted: SORTED_NONE, selected: false },
  uniqueTextString: { sorted: SORTED_NONE, selected: false },
  manualTag: { sorted: SORTED_NONE, selected: false },
  suggestedTag: { sorted: SORTED_NONE, selected: false },
  comments: { sorted: SORTED_NONE, selected: false },
};
