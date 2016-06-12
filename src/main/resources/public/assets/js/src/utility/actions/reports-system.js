
var ActionTypes = require('../action-types');
var {computeKey} = require('../reports').system;
var {queryStats} = require('../service/query');

// Define actions

var actions = {

  // Plain actions
  
  initialize: (level, reportName) => ({
    type: ActionTypes.reports.system.INITIALIZE,
    level,
    reportName,
  }),
 
  requestData: (level, reportName, t=null) => ({
    type: ActionTypes.reports.system.REQUEST_DATA,
    level,
    reportName,
    timestamp: (t || new Date()).getTime(),
  }),
  
  setData: (level, reportName, data, t=null) => ({
    type: ActionTypes.reports.system.SET_DATA,
    level,
    reportName,
    data,
    timestamp: (t || new Date()).getTime(), 
  }),
  
  setTimespan: (level, reportName, timespan) => ({
    type: ActionTypes.reports.system.SET_TIMESPAN,
    level,
    reportName,
    timespan: timespan,
  }),

  // Complex actions: functions processed by thunk middleware
  
  refreshData: (level, reportName) => (dispatch, getState) => {
    var state = getState();
    // Todo
    return Promise.reject('Todo');
  },
};

module.exports = actions;
