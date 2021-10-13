import React from 'react';

const myelement = (
  <div
    style={
      {
        lineHeight: '0.7',
        paddingTop: '25px',
        paddingLeft: '30px',
        fontFamily: 'LatoWeb',
        color: '#28a745',
        fontSize: '14px',
      }
    }
  >
    <p style={{ fontFamily: 'LatoWeb', color: '#28a745', fontSize: '16px' }}>
      Date Formats Accepted and Normalized
    </p>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      today/tomorrow
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like january (the) twenty-third (of) 2019
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like jan twenty two 20
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like 12(th) of january 2019
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like january(the) 30(th) 2019
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
     unambiguous dates in european format like 31/05/2019
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      (days of month between 13/31)
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like 10/12(th)/2019
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like twenty-first (of) january 2019
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like january 31st (with no year)
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      relative dates like 'this Thursday', 'next Friday'
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      days of week like 'Thurs.' or 'Thursday'
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like twenty-first (of) jan
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like january (the) 29th
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like 12th (of) january
    </li>
    <li style={{ color: '#28a745', paddingTop: '15px' }}>
      dates like 12th
    </li>
  </div>
);

export default myelement;
