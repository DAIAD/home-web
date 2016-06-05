var loggingAPI = require('../api/logging');
var types = require('../constants/LoggingActionTypes');

var getEventsInit = function() {
  return {
    type : types.LOG_EVENT_REQUEST_INIT
  };
};

var getEventsComplete = function(success, errors, total, events, index, size) {
  return {
    type : types.LOG_EVENT_REQUEST_COMPLETE,
    success : success,
    errors : errors,
    total : total,
    events : events,
    index : index,
    size : size
  };
};

var changeIndex = function(index) {
  return {
    type : types.LOG_EVENT_CHANGE_INDEX,
    index : index
  };
};

var filterAccount = function(account) {
  return {
    type : types.LOG_EVENT_FILTER_ACCOUNT,
    account : account
  };
};

var filterLevel = function(level) {
  return {
    type : types.LOG_EVENT_FILTER_LEVEL,
    level : level
  };
};

var clearFilter = function() {
  return {
    type : types.LOG_EVENT_FILTER_CLEAR
  };
};

var LoggingActionCreators = {

  changeIndex : function(index) {
    return function(dispatch, getState) {
      dispatch(changeIndex(index));

      return loggingAPI.getEvents(getState().logging.query).then(
          function(response) {
            dispatch(getEventsComplete(response.success, response.errors, response.total, response.events,
                response.index, response.size));
          }, function(error) {
            dispatch(getEventsComplete(false, error));
          });
    };
  },

  getEvents : function() {
    return function(dispatch, getState) {
      dispatch(getEventsInit());

      return loggingAPI.getEvents(getState().logging.query).then(
          function(response) {
            dispatch(getEventsComplete(response.success, response.errors, response.total, response.events,
                response.index, response.size));
          }, function(error) {
            dispatch(getEventsComplete(false, error));
          });
    };
  },

  filterLevel : function(level) {
    return function(dispatch, getState) {
      dispatch(filterLevel(level));

      dispatch(getEventsInit());

      return loggingAPI.getEvents(getState().logging.query).then(
          function(response) {
            dispatch(getEventsComplete(response.success, response.errors, response.total, response.events,
                response.index, response.size));
          }, function(error) {
            dispatch(getEventsComplete(false, error));
          });
    };
  },

  filterAccount : function(account) {
    return {
      type : types.LOG_EVENT_FILTER_ACCOUNT,
      account : account
    };
  },

  clearFilter : function() {
    return function(dispatch, getState) {
      dispatch(clearFilter());

      dispatch(getEventsInit());

      return loggingAPI.getEvents(getState().logging.query).then(
          function(response) {
            dispatch(getEventsComplete(response.success, response.errors, response.total, response.events,
                response.index, response.size));
          }, function(error) {
            dispatch(getEventsComplete(false, error));
          });
    };
  }

};

module.exports = LoggingActionCreators;
