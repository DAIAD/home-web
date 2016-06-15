var moment = require('moment');

var types = require('../constants/GroupCatalogActionTypes');

var _createInitialGroupState = function() {
  return {
    groups : [],
    filtered: [],
    features : null
  };
};

var _filterRows = function(rows, type, name) {
  return rows.filter( r => {
    if(name) {
      if(r.text.indexOf(name) === -1) {
        return false;
      }
    }
    if(type) {
      return (r.type == type);
    }
    
    return true;
  });
};

var _createInitialeState = function() {
  return {
    isLoading : false,
    query : {
      type: null,
      name: null,
      geometry: null
    },
    data : _createInitialGroupState(),
    interval : [
        moment().subtract(30, 'days'), moment()
    ],
    metric: 'AVERAGE',
    charts : {

    }
  };
};

var _fillGroupSeries = function(interval, label, data) {
  var d;
  var allPoints = [];

  var ref = interval[1].clone();
  var days = interval[1].diff(interval[0], 'days') + 1;

  if ((!data) || (data.points.length === 0)) {
    for (d = days; d > 0; d--) {
      allPoints.push({
        sum: 0,
        average : 0,
        timestamp : ref.clone().toDate().getTime()
      });

      ref.subtract(1, 'days');
    }
  } else {
    var index = 0;
    var points = data.points;

    points.sort(function(p1, p2) {
      return (p2.timestamp - p1.timestamp);
    });

    for (d = days; d > 0; d--) {
      if (index === points.length) {
        allPoints.push({
          sum: 0,
          average : 0,
          timestamp : ref.clone().toDate().getTime()
        });

        ref.subtract(1, 'days');
      } else if (ref.isBefore(points[index].timestamp, 'day')) {
        index++;
      } else if (ref.isAfter(points[index].timestamp, 'day')) {
        allPoints.push({
          sum: 0,
          average : 0,
          timestamp : ref.clone().toDate().getTime()
        });

        ref.subtract(1, 'days');
      } else if (ref.isSame(points[index].timestamp, 'day')) {
        allPoints.push({
          sum : points[index].volume.SUM,
          average : points[index].volume.AVERAGE,
          timestamp : ref.clone().toDate().getTime()
        });

        index++;
        ref.subtract(1, 'days');
      }
    }

    allPoints.sort(function(p1, p2) {
      return (p1.timestamp - p2.timestamp);
    });

    data.points = allPoints;
  }

  data.label = label;

  return data;
};

var _extractFeatures = function(groups) {
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

  groups = groups || [];

  for ( var index in groups) {
    if (groups[index].location) {
      var meter = groups[index].hasOwnProperty('meter') ? groups[index].meter : null;

      geojson.features.push({
        'type' : 'Feature',
        'geometry' : groups[index].location,
        'properties' : {
          'groupKey' : groups[index].id,
          'deviceKey' : meter.key,
          'name' : groups[index].fullname,
          'address' : groups[index].address,
          'meter' : {
            'key' : meter.key,
            'serial' : meter.serial
          }
        }
      });
    }
  }

  return geojson;
};

var dataReducer = function(state, action) {
  switch (action.type) {
    case types.GROUP_CATALOG_ADD_FAVORITE_RESPONSE:
    case types.GROUP_CATALOG_REMOVE_FAVORITE_RESPONSE:
      var oldState = state.groups || [], newState = [];
      
      oldState.forEach( g => {
        if(g.key === action.groupKey) {
          g.favorite = action.favorite;
        }
        newState.push(g);
      });
      
      return Object.assign({}, state, {
        groups : newState
      });


    case types.GROUP_CATALOG_FILTER_NAME :
    case types.GROUP_CATALOG_FILTER_TYPE : 
    case types.GROUP_CATALOG_FILTER_CLEAR :
      return {
        groups : state.groups || [],
        filtered : _filterRows(state.groups || [], action.groupType, action.name),
        features : _extractFeatures(state.groups || [])
      };
    
    case types.GROUP_CATALOG_RESPONSE:
      if (action.success === true) {
        action.groups.forEach( g => {
          if(g.type == 'SEGMENT') {
            g.text = g.cluster + ': ' + g.name;
          } else {
            g.text = g.name;
          }
        });
        
        action.groups.sort( (a, b) => {
          if (a.text < b.text) {
            return -1;
          }
          
          if (a.text > b.text) {
            return 1;
          }

          return 0;
        });
        
        return {
          total : action.total || 0,
          index : action.index || 0,
          size : action.size || 10,
          groups : action.groups || [],
          filtered : _filterRows(action.groups || [], action.groupType, action.name),
          features : _extractFeatures(action.groups || [])
        };
      } else {
        return {
          total : 0,
          index : 0,
          size : 10,
          groups : [],
          filtered: [],
          features : _extractFeatures([])
        };
      }
      break;

    default:
      return state || _createInitialGroupState();
  }
};

var reducer = function(state, action) {
  switch (action.type) {
    case types.GROUP_CATALOG_INDEX_CHANGE:
      
      return Object.assign({}, state);

    case types.GROUP_CATALOG_REQUEST:
    case types.GROUP_CATALOG_DELETE_REQUEST:
    case types.GROUP_CATALOG_CHART_REQUEST:
    case types.GROUP_CATALOG_ADD_FAVORITE_REQUEST:
    case types.GROUP_CATALOG_REMOVE_FAVORITE_REQUEST:
      
      return Object.assign({}, state, {
        isLoading : true
      });
      
    case types.GROUP_CATALOG_RESPONSE:
      action.groupType = state.query.type;
      action.name = state.query.name;
      
      return Object.assign({}, state, {
        isLoading : false,
        data : dataReducer(state.data, action)
      });

    case types.GROUP_CATALOG_ADD_FAVORITE_RESPONSE:
    case types.GROUP_CATALOG_REMOVE_FAVORITE_RESPONSE:
      
      if (action.success === true) {
        return Object.assign({}, state, {
          isLoading : false,
          data : dataReducer(state.data, action)
        });
      }
      return Object.assign({}, state, {
        isLoading : false,
      });

    case types.GROUP_CATALOG_DELETE_RESPONSE:
      
      return Object.assign({}, state, {
        isLoading : false
      });

    case types.GROUP_CATALOG_CHART_RESPONSE:
      var charts = state.charts;
 
      if (action.data) {
        charts[action.groupKey] = _fillGroupSeries(state.interval, action.label, action.data);
      }

      return Object.assign({}, state, {
        isLoading : false,
        charts:charts
      });

    case types.GROUP_CATALOG_CLEAR_CHART:
      return Object.assign({}, state, {
        isLoading : false,
        charts : {}
      });

    case types.GROUP_CATALOG_FILTER_NAME :
      action.groupType = state.query.type;
      
      return Object.assign({}, state, {
        query : Object.assign({}, state.query, {
          name: action.name|| null
        }),
        data : dataReducer(state.data, action)
      });
      
    case types.GROUP_CATALOG_FILTER_TYPE:
      action.name = state.query.name;

      return Object.assign({}, state, {
        query : Object.assign({}, state.query, {
          type: (action.groupType === 'UNDEFINED' ? null : action.groupType)
        }),
        data : dataReducer(state.data, action)
      });
      
    case types.GROUP_CATALOG_FILTER_CLEAR :
      return Object.assign({}, state, {
        query : Object.assign({}, state.query, {
          name: null,
          type: null
        }),
        data : dataReducer(state.data, action)
      });
      
    case types.GROUP_CATALOG_SET_METRIC:
      return Object.assign({}, state, {
        metric: action.metric || 'AVERAGE'
      });

    default:
      return state || _createInitialeState();
  }
};

module.exports = reducer;
