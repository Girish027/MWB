import dateFormat from 'dateformat';

export const getDateFromTimestamp = timestamp => new Date(parseInt(timestamp)).toLocaleDateString('en-US', {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
});

export const getFormattedDate = (timestamp) => {
  const dateObj = new Date(timestamp);
  return dateFormat(dateObj, 'mmmm dS, yyyy, h:MM:ss TT');
};
