// This kind of "overview" reports is just a standard (i.e. "canned") subset
// of ordinary measurement reports on water consumption.

var ActionTypes = require('../action-types');

var initialState = {
  
  // Configure reports, names refer to config.reports.measurements
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
    year: {
      level: 'month',
      reportName: 'monthly-sum',
      startsAt: 'year',
      duration: [-2, 'year'],
    },
  },
 
  sections: {
    'utility': {
      title: 'Utility',
      population: null,
    },
    'per-efficiency': {
      title: 'Per Customer Efficiency',  
      population: null,
    },
    'per-household-size': {
      title: 'Per Household Size',  
      population: null,
    },
    'per-household-members': {
      title: 'Per Household Members',  
      population: null,
    },
    'per-income': {
      title: 'Per Income',  
      population: null,
    },
    'per-age': {
      title: 'Per Age',  
      population: null,
    },
  } 
};

var reduce = function (state, action) {
  
  // Note This part of configuration does not ever change
  return initialState;

};

module.exports = reduce;
