
var ActionTypes = require('../action-types');

var initialState = {
  
  // Note 
  //  * These are all monthly reports at a detail level of week.
  //  * The report names refer to config.reports.measurements
  reports: [
    {
      title: 'Average of weekly consumption',
      level: 'week',
      reportName: 'avg',
      startsAt: 'month',
      duration: [+1, 'month'],
    }, 
    {
      title: 'Average weekly max/min water consumption',
      level: 'week',
      reportName: 'avg-daily-peak',
      startsAt: 'month',
      duration: [+1, 'month'],
    }, {
      title: 'Top/Bottom 3 consumers',
      level: 'week',
      reportName: 'top-k',
      startsAt: 'month',
      duration: [+1, 'month'],
    },
  ],

  population: [
    'UTILITY',
    'CLUSTER:Income',
    'CLUSTER:Household Members',
  ],

  period: {
    start: '2016-03-01T00:00:00Z',
    duration: 4, // months
  },
};

var reduce = function (state, action) {
  
  // Note This part of configuration does not ever change
  return initialState;

};

module.exports = reduce;
