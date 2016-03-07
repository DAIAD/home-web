var deviceAPI = require('../api/device');
var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();

var getSessionById = require('../utils/device').getSessionById;
var getLastSession = require('../utils/device').getLastSession;
var getNextSession = require('../utils/device').getNextSession;
var getPreviousSession = require('../utils/device').getPreviousSession;


var HistoryActions = {
  
  setActiveSessionIndex: function(sessionIndex) {
    return {
      type: types.HISTORY_SET_ACTIVE_SESSION_INDEX,
      id: sessionIndex
    };
  },
  resetActiveSessionIndex: function() {
    return {
      type: types.HISTORY_RESET_ACTIVE_SESSION_INDEX
    };
  },
  increaseActiveSessionIndex: function() {
    return {
      type: types.HISTORY_INCREASE_ACTIVE_SESSION_INDEX
    };
  },
  decreaseActiveSessionIndex: function() {
    return {
      type: types.HISTORY_DECREASE_ACTIVE_SESSION_INDEX
    };
  },
  setQueryFilter: function(filter) {
    return {
      type: types.HISTORY_SET_FILTER,
      filter: filter
    };
  },
  setTimeFilter: function(filter) {
    return {
      type: types.HISTORY_SET_TIME_FILTER,
      filter: filter
    };
  },
  setSessionFilter: function(filter) {
    return {
      type: types.HISTORY_SET_SESSION_FILTER,
      filter: filter
    };
  },
};

module.exports = HistoryActions;
