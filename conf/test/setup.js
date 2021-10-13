
import { JSDOM } from 'jsdom';
import { configure } from 'enzyme';
//import localStorage from 'mock-local-storage';
import btoa from 'btoa';
import Adapter from 'enzyme-adapter-react-16';
import React from "react";

configure({ adapter: new Adapter() });

global.fetch = require('jest-fetch-mock')

global.localStorage['clientId'] = '-1';
global.localStorage['clientName'] = 'Test Client';


global.dom = new JSDOM('<!doctype html><html lang="en"><body></body></html>');
global.window = global.dom.window;
global.document = global.window.document;
global.navigator = global.window.navigator;
global.localStorage = global.window.localStorage;
global.btoa = btoa;
global.window.btoa = btoa;

global.windowOpenFocus = jest.fn();
global.windowOpen = jest.fn().mockImplementation((url, target) => ({
  focus: windowOpenFocus
}));
global.window.open = global.windowOpen;
window.HTMLMediaElement.prototype.load = () => { /* do nothing */ };
window.HTMLMediaElement.prototype.play = () => { /* do nothing */ };
window.HTMLMediaElement.prototype.pause = () => { /* do nothing */ };
window.HTMLMediaElement.prototype.addTextTrack = () => { /* do nothing */ };

global.window.URL = {
  createObjectURL: jest.fn((file) => 'file'),
}
global.URL = global.window.URL;

global.FileReader = global.window.FileReader;

global.uiConfig = {
  featureFlags: {
    MWB_ROLE_EXTERNAL: {
      modelTrainingOutputs: 'hide',
      modelDownload: 'hide',
      kibanaLogs: 'hide',
      appHelp: 'hide',
      transformationHelp: 'hide',
      tagSearchHelp: 'hide',
      modelConfigDownload: 'hide',
      modelDelete: 'hide',
      modelConfigTemplate: 'hide',
      datasetTemplate: 'hide',
      intentGuideTemplate: 'hide',
      datasetExport: 'hide',
      datasetDelete: 'hide',
      intentGuideExport: 'hide',
      datasetExportById: 'hide',
      projectDelete: 'hide',
      downloadResults: 'hide',
      createNewVersionTrainingConfigTab: 'hide',
      createNewVersionModelReviewTab: 'hide',
      granularMessage: 'hide',
      speechBundledUnbundled: 'hide',
      intentGuideDelete: 'hide',
      intentGuideUpdate:'hide',
      intentGuideContextualActionBar: 'hide',
      resolveInconsistencies: 'hide',
      supportLink: 'hide',
    },
    DEFAULT: {
      models: 'show',
      modelTrainingOutputs: 'show',
      modelDownload: 'show',
      kibanaLogs: 'show',
      appHelp: 'show',
      transformationHelp: 'show',
      tagSearchHelp: 'show',
      modelConfigDownload: 'show',
      modelDelete: 'show',
      modelConfigTemplate: 'show',
      datasetTemplate: 'show',
      intentGuideTemplate: 'show',
      datasetExport: 'show',
      datasetDelete: 'show',
      intentGuideExport: 'show',
      datasetExportById: 'show',
      projectDelete: 'show',
      downloadResults: 'show',
      createNewVersionTrainingConfigTab: 'show',
      createNewVersionModelReviewTab: 'show',
      granularMessage: 'show',
      speechBundledUnbundled: 'show',
      intentGuideDelete: 'show',
      intentGuideUpdate: 'show',
      intentGuideContextualActionBar: 'show',
      resolveInconsistencies: 'show',
      supportLink: 'show',
    },
  },
};

global.processENV = {
  NODE_ENV: 'production',
};

const noop = () => {
  return null;
};
require.extensions['.css'] = noop;
require.extensions['.scss'] = noop;

jest.mock('react-svg', () => () => 'ReactSvG');
jest.mock('components/models/ModelReviewAce', () => () => <div />);
jest.mock('react-ace', () => () => <div />);
jest.mock('brace/ext/searchbox', () => () => <div />);
jest.mock('brace/theme/textmate', () => () => <div />);
jest.mock('brace/theme/kuroir', () => () => <div />);
jest.mock('brace/mode/json', () => () => <div />);
