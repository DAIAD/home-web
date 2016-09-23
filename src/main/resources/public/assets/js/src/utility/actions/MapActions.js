var adminAPI = require('../api/admin');
var queryAPI = require('../api/query');
var favouritesAPI = require('../api/favourites');

var types = require('../constants/MapActionTypes');

var addFavouriteRequest = function () {
  return {
    type: types.MAP_ADD_FAVOURITE_REQUEST
  };
};

var addFavouriteResponse = function (success, errors) {
  return {
    type: types.MAP_ADD_FAVOURITE_RESPONSE,
    success: success,
    errors: errors
  };
};

var _buildTimelineQuery = function(population, source, geometry, timezone, interval) {
  var spatial = [
    {
      type : 'GROUP',
      group : 'd29f8cb8-7df6-4d57-8c99-0a155cc394c5'
    }
  ];

  if (geometry) {
    spatial.push({
      type : 'CONSTRAINT',
      geometry : geometry,
      operation : 'INTERSECT'
    });
  }

  return {
    'query' : {
      'timezone' : timezone,
      'time' : {
        'type' : 'ABSOLUTE',
        'start' : interval[0].toDate().getTime(),
        'end' : interval[1].toDate().getTime(),
        'granularity' : 'DAY'
      },
      'population' : [
        population
      ],
      spatial : spatial,
      'source' : source,
      'metrics' : [
        'SUM'
      ]
    }
  };
};

var _buildChartQuery = function(key, name, timezone, interval) {
  return {
    'query' : {
      'timezone' : timezone,
      'time' : {
        'type' : 'ABSOLUTE',
        'start' : interval[0].toDate().getTime(),
        'end' : interval[1].toDate().getTime(),
        'granularity' : 'DAY'
      },
      'population' : [
        {
          'type' : 'UTILITY',
          'label' : name,
          'utility' : key
        }
      ],
      'source' : 'METER',
      'metrics' : [
        'SUM'
      ]
    }
  };
};

var _getTimelineInit = function(population, query) {
  return {
    type : types.MAP_TIMELINE_REQUEST,
    query : query,
    population : population
  };
};

var _getTimelineComplete = function(success, errors, data) {
  return {
    type : types.MAP_TIMELINE_RESPONSE,
    success : success,
    errors : errors,
    data : data
  };
};

var _getFeatures = function(index, timestamp, label) {
  return {
    type : types.MAP_GET_FEATURES,
    timestamp : timestamp,
    label : label,
    index : index
  };
};

var _getChartInit = function(key, name, timezone, query) {
  return {
    type : types.MAP_CHART_REQUEST,
    query : query,
    population : {
      key : key,
      name : name,
      timezone : timezone
    }
  };
};

var _getChartComplete = function(success, errors, data) {
  return {
    type : types.MAP_CHART_RESPONSE,
    success : success,
    errors : errors,
    data : data
  };
};

var _setEditorValue = function(editor, value) {
  return {
    type : types.MAP_SET_EDITOR_VALUE,
    editor : editor,
    value : value
  };
};

var MapActions = {
  setEditor : function(key) {
    return {
      type : types.MAP_SELECT_EDITOR,
      editor : key
    };
  },

  setEditorValue : function(editor, value) {
    return function(dispatch, getState) {
      dispatch(_setEditorValue(editor, value));

      var population = getState().map.population;
      var timezone = getState().map.timezone;
      var interval = getState().map.interval;
      var source = getState().map.source;
      var geometry = getState().map.geometry;

      var query = _buildTimelineQuery(population, source, geometry, timezone, interval);

      dispatch(_getTimelineInit(population, query));
      return queryAPI.queryMeasurements(query).then(function(response) {
        var data = {
          meters : null,
          devices : null,
          areas : null
        };
        if (response.success) {     
          data.areas = response.areas;
          data.meters = response.meters;
          data.devices = response.devices;            
        }
        dispatch(_getTimelineComplete(response.success, response.errors, data));

        dispatch(_getFeatures(0, null, null));

      }, function(error) {
        dispatch(_getTimelineComplete(false, error, null));

        dispatch(_getFeatures(0, null, null));
      });
    };
  },

  getChart : function(key, name, timezone) {
    return function(dispatch, getState) {
      var query = _buildChartQuery(key, name, timezone, getState().map.interval);

      dispatch(_getChartInit(key, name, timezone, query));

      return queryAPI.queryMeasurements(query).then(function(response) {
        var data = {
          meters : null,
          devices : null
        };

        if (response.success) {
          data.meters = response.meters;
          data.devices = response.devices;             
        }
        dispatch(_getChartComplete(response.success, response.errors, data));
      }, function(error) {
        dispatch(_getChartComplete(false, error, null));
      });
    };
  },

  getTimeline : function(population) {
    return function(dispatch, getState) {
      var timezone = getState().map.timezone;
      var interval = getState().map.interval;
      var source = getState().map.source;
      var geometry = getState().map.geometry;

      var query = _buildTimelineQuery(population, source, geometry, timezone, interval);

      dispatch(_getTimelineInit(population, query));

      return queryAPI.queryMeasurements(query).then(function(response) {
        var data = {
          meters : null,
          devices : null,
          areas : null
        };
        if (response.success) {
          data.areas = response.areas;
          data.meters = response.meters;
          data.devices = response.devices;        
        }
        dispatch(_getTimelineComplete(response.success, response.errors, data));

        dispatch(_getFeatures(0, null, null));

      }, function(error) {
        dispatch(_getTimelineComplete(false, error, null));

        dispatch(_getFeatures(0, null, null));
      });
    };
  },

  getFeatures : function(index, timestamp, label) {
    return _getFeatures(index, timestamp, label);
  },

  setTimezone : function(timezone) {
    return {
      type : types.MAP_SET_TIMEZONE,
      timezone : timezone
    };
  },
  addFavourite : function(favourite) {
    return function(dispatch, getState) {
      dispatch(addFavouriteRequest());
      return favouritesAPI.addFavourite(favourite).then(function (response) {
        dispatch(addFavouriteResponse(response.success, response.errors));
      }, function (error) {
        dispatch(addFavouriteResponse(false, error));
      });
    };
  }
};

module.exports = MapActions;
