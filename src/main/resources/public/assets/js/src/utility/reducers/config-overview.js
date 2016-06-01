// This kind of "overview" reports is just a standard (i.e. "canned") subset
// of ordinary measurement reports on water consumption.

var ActionTypes = require('../action-types');

var initialState = {
  
  sections: {
    'utility': {
      title: 'Utility',
      // Configure reports, refer to config.reports.measurements
      reports: {
        day: {
          level: 'hour',
          reportName: 'hourly-sum',
          startsAt: 'day',
          duration: [-2, 'day'],
        },
        week: {
          level: 'day',
          reportName: 'daily-sum',
          startsAt: 'isoweek',
          duration: [-2, 'week'],
        },
        month: {
          level: 'day',
          reportName: 'daily-sum',
          startsAt: 'month',
          duration: [-2, 'month'],
        },
        //year: {
        //  level: 'month',
        //  reportName: 'monthly-sum',
        //  startsAt: 'year',
        //  duration: [-2, 'year'],
        //},
      },
    },
    'per-efficiency': {
      title: 'Per Customer Efficiency',  
    },
    'per-household-size': {
      title: 'Per Household Size',  
    },
    'per-household-members': {
      title: 'Per Household Members',  
    },
    'per-income': {
      title: 'Per Income',  
    },
    'per-age': {
      title: 'Per Age',  
    },
  } 
};

var reduce = function (state, action) {
  
  // Note This part of configuration does not ever change
  return initialState;

};

module.exports = reduce;
