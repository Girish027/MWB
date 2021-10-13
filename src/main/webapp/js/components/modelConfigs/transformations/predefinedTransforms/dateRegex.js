import Constants from 'constants/Constants';

const dateRegex = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.WORDCLASS_SUBST_REGEX,
    mappings: {},

  };
  regex.mappings = {
    '/(?:\\d{1,2}\\s*[\\s/\\.-]\\s*\\d{1,2}\\s*[\\s/\\.-]\\s*(?:\\d{4}|\\d{2}))/i': '_class_date',
    '/(?:(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed)\\s*(?:[\\s,/\\.-]|of)\\s*)?(?:(?:(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?)\\s*(?:[\\s,/\\.-]|of)\\s*(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?)\\s*(?:[\\s,/\\.-]|of)\\s*\\b(?:january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sept|sep|oct|nov|dec)\\b)|(?:(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?)\\s*(?:[\\s,/\\.-]|of)\\s*\\b(?:january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sept|sep|oct|nov|dec)\\b\\s*(?:[\\s,/\\.-]|of)\\s*(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?))|(?:\\b(?:january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sept|sep|oct|nov|dec)\\b\\s*(?:[\\s,/\\.-]|of)\\s*(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?)\\s*(?:[\\s,/\\.-]|of)\\s*(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?)))(?:\\s*(?:[\\s,/\\.-]|of)\\s*(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed))?/i': '_class_date',
    '/(?:(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed)\\s*(?:[\\s,/\\.-]|of)\\s*)?(?:(?:(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?)\\s*(?:[\\s,/\\.-]|of)?\\s*(?:january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sept|sep|oct|nov|dec)\\s*(?:[\\s,/\\.-]|of)?\\s*(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?))|(?:(?:january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sept|sep|oct|nov|dec)(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?))|(?:(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?)(?:january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sept|sep|oct|nov|dec)))(?:\\s*(?:[\\s,/\\.-]|of)\\s*(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed))?/i': '_class_date',
    '/(?:(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed)\\s*(?:[\\s,/\\.-]|of)\\s*)?(?:(?:(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?)\\s*(?:[\\s,/\\.-]|of)\\s*\\b(?:january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sept|sep|oct|nov|dec)\\b)|(?:\\b(?:january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sept|sep|oct|nov|dec)\\b\\s*(?:[\\s,/\\.-]|of)\\s*(?:\\d{4}|\\d{1,2}(?:nd|th|rd|st)?)))(?:\\s*(?:[\\s,/\\.-]|of)\\s*(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed))?/i': '_class_date',
    '/(?:\\d{1,2}\\s*[/]\\s*(?:\\d{4}|\\d{2}))|(?:\\d{1,2}\\s*[\\.]\\s*\\d{4})/i': '_class_date',
    '/\\b(?:january|february|march|april|june|july|august|september|october|november|december|jan|feb|mar|jun|jul|aug|sept|sep|oct|nov|dec)\\b/i': '_class_date',
    '/\\b(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed)\\b/i': '_class_date',
  };

  return regex;
};

export default dateRegex;
