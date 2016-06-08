var queryAPI = require('../api/query');
var types = require('../constants/ForecastingActionTypes');

/**
 * Query builders
 */
var _buildUtilityQuery = function(id, name, timezone, from, to) {
  return {
    'query' : {
      'timezone' : timezone,
      'time' : {
        'type' : 'ABSOLUTE',
        'start' : from,
        'end' : to,
        'granularity' : 'DAY'
      },
      'population' : [
        {
          'type' : 'UTILITY',
          'label' : name,
          'utility' : id
        }
      ],
      'source' : 'METER',
      'metrics' : [
        'SUM'
      ]
    }
  };
};

var _buildUserQuery = function(id, name, timezone, from, to) {
  return {
    'query' : {
      'timezone' : timezone,
      'time' : {
        'type' : 'ABSOLUTE',
        'start' : from,
        'end' : to,
        'granularity' : 'DAY'
      },
      'population' : [
        {
          'type' : 'USER',
          'label' : name,
          'users' : [
            id
          ]
        }
      ],
      'source' : 'METER',
      'metrics' : [
        'SUM'
      ]
    }
  };
};

/**
 * Utility actions
 */
var getUtilityDataInit = function(query) {
  return {
    type : types.UTILITY_DATA_REQUEST,
    query : query
  };
};

var getUtilityDataComplete = function(success, errors, data) {
  return {
    type : types.UTILITY_DATA_RESPONSE,
    success : success,
    errors : errors,
    data : data
  };
};

var getUtilityForecastInit = function(query) {
  return {
    type : types.UTILITY_FORECAST_REQUEST,
    query : query
  };
};

var getUtilityForecastComplete = function(success, errors, data) {
  return {
    type : types.UTILITY_FORECAST_RESPONSE,
    success : success,
    errors : errors,
    data : data
  };
};

/**
 * User actions
 */
var getUserDataInit = function(query) {
  return {
    type : types.USER_DATA_REQUEST,
    query : query
  };
};

var getUserDataComplete = function(success, errors, data) {
  return {
    type : types.USER_DATA_RESPONSE,
    success : success,
    errors : errors,
    data : data
  };
};

var getUserForecastInit = function(query) {
  return {
    type : types.USER_FORECAST_REQUEST,
    query : query
  };
};

var getUserForecastComplete = function(success, errors, data) {
  return {
    type : types.USER_FORECAST_RESPONSE,
    success : success,
    errors : errors,
    data : data
  };
};

var ForecastingActions = {
  getUtilityData : function(id, name, timezone) {
    return function(dispatch, getState) {
      var interval = getState().forecasting.interval;

      var query = _buildUtilityQuery(id, name, timezone, interval[0].toDate().getTime(), interval[1].toDate().getTime());

      dispatch(getUtilityDataInit(query));

      return queryAPI.queryMeasurements(query).then(function(response) {
        dispatch(getUtilityDataComplete(response.success, response.errors, response.meters));
      }, function(error) {
        dispatch(getUtilityDataComplete(false, error, null));
      });
    };
  },

  getUtilityForecast : function(id, name, timezone) {
    return function(dispatch, getState) {
      var interval = getState().forecasting.interval;

      var query = _buildUtilityQuery(id, name, timezone, interval[0].toDate().getTime(), interval[1].toDate().getTime());

      dispatch(getUtilityForecastInit(query));

      return queryAPI.queryForecast(query).then(function(response) {
        dispatch(getUtilityForecastComplete(response.success, response.errors, response.meters));
      }, function(error) {
        dispatch(getUtilityForecastComplete(false, error, null));
      });
    };
  },

  getUserData : function(id, name, timezone) {
    return function(dispatch, getState) {
      var interval = getState().forecasting.interval;

      var query = _buildUserQuery(id, name, timezone, interval[0].toDate().getTime(), interval[1].toDate().getTime());

      dispatch(getUserDataInit(query));

      return queryAPI.queryMeasurements(query).then(function(response) {
        dispatch(getUserDataComplete(response.success, response.errors, response.meters));
      }, function(error) {
        dispatch(getUserDataComplete(false, error, null));
      });
    };
  },

  getUserForecast : function(id, name, timezone) {
    return function(dispatch, getState) {
      var interval = getState().forecasting.interval;

      var query = _buildUserQuery(id, name, timezone, interval[0].toDate().getTime(), interval[1].toDate().getTime());

      dispatch(getUserForecastInit(query));

      return queryAPI.queryForecast(query).then(function(response) {
        dispatch(getUserForecastComplete(response.success, response.errors, response.meters));
      }, function(error) {
        dispatch(getUserForecastComplete(false, error, null));
      });
    };
  },

  setUser : function(user) {
    return {
      type : types.SET_USER,
      user : user
    };
  }

};

module.exports = ForecastingActions;
