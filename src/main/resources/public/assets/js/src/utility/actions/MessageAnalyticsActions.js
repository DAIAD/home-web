
var mapTypes = require('../constants/MapActionTypes');
var types = require('../constants/ActionTypes');
var alertsAPI = require('../api/alerts');

var _getFeatures = function(index, timestamp, label) {
  return {
    type : mapTypes.MAP_GET_FEATURES,
    timestamp : timestamp,
    label : label,
    index : index
  };
};

var _setEditorValue = function(editor, value) {
  return {
    type : mapTypes.MAP_SET_EDITOR_VALUE,
    editor : editor,
    value : value
  };
};

var requestedMessageStatistics = function () {
  return {
    type: types.MESSAGES_REQUESTED_STATISTICS
  };
};

var receivedMessageStatistics = function (success, errors, messageStatistics) {
  return {
    type: types.MESSAGES_RECEIVED_STATISTICS,
    success: success,
    errors: errors,    
    messages: messageStatistics
  };
};

var buildQuery = function(population, timezone, interval) {

  return {
    'query' : {
      'timezone' : timezone,
      'time' : {
        'type' : 'ABSOLUTE',
        'start' : interval[0].toDate().getTime(),
        'end' : interval[1].toDate().getTime()
      },
      'population' : [
        population
      ]
    }
  };
};

var MessageAnalyticsActions = {
  setEditor : function(key) {
    return {
      type : mapTypes.MAP_SELECT_EDITOR,
      editor : key
    };
  },

  setEditorValue : function(editor, value) {
    return function(dispatch, getState) {
      dispatch(_setEditorValue(editor, value));
      dispatch(requestedMessageStatistics());
      var query = buildQuery(getState(event).map.population, getState(event).map.timezone, getState(event).map.interval);
      return alertsAPI.getMessageStatistics(query).then(function (response) {
        dispatch(receivedMessageStatistics(response.success, response.errors, response.alertStatistics));
      }, function (error) {
        dispatch(receivedMessageStatistics(false, error, null));
      });
    };
  },

  getFeatures : function(index, timestamp, label) {
    return _getFeatures(index, timestamp, label);
  },

  setTimezone : function(timezone) {
    return {
      type : mapTypes.MAP_SET_TIMEZONE,
      timezone : timezone
    };
  },
  
  fetchMessages: function (event) {
    return function (dispatch, getState) {
      dispatch(requestedMessageStatistics());
      var query = buildQuery(getState(event).map.population, getState(event).map.timezone, getState(event).map.interval);
      return alertsAPI.getMessageStatistics(query).then(function (response) {
        dispatch(receivedMessageStatistics(response.success, response.errors, response.alertStatistics));
      }, function (error) {
        dispatch(receivedMessageStatistics(false, error, null));
      });
    };
  }
};

module.exports = MessageAnalyticsActions;
