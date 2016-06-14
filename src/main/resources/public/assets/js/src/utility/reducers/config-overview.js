// This kind of "overview" reports is just a standard (i.e. "canned") subset
// of ordinary measurement reports on water consumption.

var ActionTypes = require('../action-types');

var initialState = {
  
  // Configure available per-unit reports 
  // Note report names refer to config.reports.measurements
  reports: {
    day: {
      title: 'Total daily consumption',
      level: 'hour',
      reportName: 'sum',
      startsAt: 'day',
      duration: [-2, 'day'],
    },
    week: {
      title: 'Total weekly consumption',
      level: 'day',
      reportName: 'sum',
      startsAt: 'isoweek',
      duration: [-2, 'week'],
    },
    month: {
      title: 'Total monthly consumption',
      level: 'day',
      reportName: 'sum',
      startsAt: 'month',
      duration: [-2, 'month'],
    },
    year: {
      title: 'Total yearly consumption',
      level: 'month',
      reportName: 'sum',
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
