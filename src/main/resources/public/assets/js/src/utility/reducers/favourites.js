var types = require('../constants/FavouritesActionTypes');

var _createMapInitialState = function(interval) {
  return {
    interval : null,
    query : null,
    areas : null,
    meters : null,
    devices : null,
    timeline : null,
    features : null,
    favourite: null
  };
};

var initialState = {
  isLoading: false,
  isActiveFavourite: false,
  favourites: null,
  showSelected: false,
  selectedFavourite: null,
  showDeleteMessage: false,
  favouriteToBeDeleted: null,
  map : _createMapInitialState(),
  features: null
};

var _extractTimeline = function(meters, areas) {
  var timeline = {}, timestamp, label, area, min = NaN, max = NaN;

  for (var m = 0; m < meters.length; m++) {
    var meter = meters[m];

    for (var p = 0; p < meter.points.length; p++) {
      var point = meter.points[p];

      timeline[point.timestamp] = timeline[point.timestamp] || {};
      timestamp = timeline[point.timestamp];

      timestamp[meter.label] = timestamp[meter.label] || {};
      label = timestamp[meter.label];

      label[meter.areaId] = label[meter.areaId] || 0;
      label[meter.areaId] += point.volume.SUM;
    }
  }

  for (timestamp in timeline) {
    for (label in timeline[timestamp]) {
      for (area in timeline[timestamp][label]) {
        var value = timeline[timestamp][label][area];
        if ((isNaN(min)) || (min > value)) {
          min = value;
        }
        if ((isNaN(max)) || (max < value)) {
          max = value;
        }
      }
    }
  }

  timeline.min = min;
  timeline.max = max;

  timeline.getAreas = function() {
    return areas;
  };

  timeline.getTimestamps = function() {
    var values = [];
    for ( var timestamp in this) {
      var value = Number(timestamp);
      if (!isNaN(value)) {
        values.push(value);
      }
    }

    return values.sort(function(t1, t2) {
      if (t1 < t2) {
        return -1;
      }
      if (t1 > t2) {
        return 1;
      }
      return 0;
    });
  };

  timeline.getFeatures = function(timestamp, label) {
    var geojson = {
      type : 'FeatureCollection',
      features : [],
      crs : {
        type : 'name',
        properties : {
          name : 'urn:ogc:def:crs:OGC:1.3:CRS84'
        }
      }
    };

    if (!timestamp) {
      var timestamps = this.getTimestamps();
      if (timestamps.length > 0) {
        timestamp = timestamps[0];
      } else {
        return geojson;
      }
    }
    if (!label) {
      if (Object.keys(this[timestamp])) {
        label = Object.keys(this[timestamp])[0];
      } else {
        return geojson;
      }
    }

    if (!this[timestamp]) {
      return geojson;
    }
    var instance = this[timestamp][label];

    if (!instance) {
      return geojson;
    }

    var areas = this.getAreas();

    for ( var index in instance) {
      geojson.features.push({
        'type' : 'Feature',
        'geometry' : areas[index].geometry,
        'properties' : {
          'label' : areas[index].label,
          'value' : instance[index]
        }
      });
    }

    return geojson;
  };

  return timeline;
};
//TODO separate map reducer
var mapReducer = function(state, action) {
  switch (action.type) {
    case types.FAVOURITES_TIMELINE_REQUEST:
      return Object.assign({}, state, {
        query : action.query,
        areas : null,
        meters : null,
        devices : null,
        timeline : null,
        features : null,
        index : 0
      });
    case types.FAVOURITES_TIMELINE_RESPONSE:
      if (action.success) {
        var source = (state.selectedFavourite.query.source == 'METER') ? action.data.meters : action.data.devices;

        return Object.assign({}, state, {
          map : mapReducer(state.map, action),
          areas : action.data.areas,
          meters : action.data.meters,
          devices : action.data.devices,
          timeline : _extractTimeline(source, action.data.areas),
          features : null
        });
      }

      return Object.assign({}, state, {
        areas : null,
        meters : null,
        devices : null,
        regions : null,
        features : null
      });      
    case types.FAVOURITES_GET_FEATURES:
      var features = (state.timeline ? state.timeline.getFeatures(action.timestamp, action.label) : null);

      return Object.assign({}, state, {
        features : features,
        index : action.index
      });  

    default:
      return state || _createMapInitialState();
  }
};

var favourites = function (state, action) {
  switch (action.type) {
    case types.FAVOURITES_SET_TIMEZONE:
      return Object.assign({}, state, {
        timezone : action.timezone
      });  
    case types.FAVOURITES_REQUEST_QUERIES:
      return Object.assign({}, state, {
        isLoading: true
      });
    case types.FAVOURITES_RECEIVE_QUERIES:
      return Object.assign({}, state, {
        isLoading: false,
        favourites: action.favourites
      });       
    case types.FAVOURITES_OPEN_SELECTED:
      return Object.assign({}, state, {
        showSelected: true,
        selectedFavourite: action.selectedFavourite
      });  
    case types.FAVOURITES_CLOSE_SELECTED:
      return Object.assign({}, state, {
        isActiveFavourite: false,
        showSelected: false,
        selectedFavourite: null
      });
    case types.FAVOURITES_SET_ACTIVE_FAVOURITE:
      return Object.assign({}, state, {
        isActiveFavourite: true,
        selectedFavourite: action.selectedFavourite
      });  
    case types.FAVOURITES_ADD_FAVOURITE_REQUEST:
      return Object.assign({}, state, {
        isLoading : true
      });
    case types.FAVOURITES_ADD_FAVOURITE_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false
      });  
    case types.FAVOURITES_DELETE_QUERY_REQUEST:
      return Object.assign({}, state, {
        showDeleteMessage : true,
        favouriteToBeDeleted: action.favouriteToBeDeleted
      });    
    case types.FAVOURITES_CONFIRM_DELETE_QUERY:
      return Object.assign({}, state, {
        isLoading : true
      });  
    case types.FAVOURITES_CANCEL_DELETE_QUERY:
      return Object.assign({}, state, {
        showDeleteMessage : false
      });       
    case types.FAVOURITES_DELETE_QUERY_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
        showDeleteMessage : false,
        isActiveFavourite: false,
        showSelected: false,        
        selectedFavourite: null
      }); 
    
    default:
      return state || initialState;
  }
};

module.exports = favourites;