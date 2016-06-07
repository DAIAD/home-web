var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  query : null,
  areas : null,
  meters : null,
  devices : null,
  regions : null
};

var _transformSeriesToRegions = function(meters, areas) {
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

  var timeline = {}, timestamp, label;

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

  console.log(timeline);

  timestamp = 1464040800000;
  label = 'Alicante';

  var instance = timeline[timestamp][label];
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

var query = function(state, action) {
  switch (action.type) {
    case types.QUERY_SUBMIT:
      return Object.assign({}, state, {
        isLoading : true,
        query : action.query,
        points : null
      });

    case types.QUERY_RESPONSE:
      if (action.success) {
        return Object.assign({}, state, {
          isLoading : false,
          areas : action.data.areas,
          meters : action.data.meters,
          devices : action.data.devices,
          regions : _transformSeriesToRegions(action.data.meters, action.data.areas)
        });
      }

      return Object.assign({}, state, {
        isLoading : false,
        areas : null,
        meters : null,
        devices : null,
        regions : null
      });

    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, state, {
        isLoading : false,
        query : null,
        areas : null,
        meters : null,
        devices : null
      });

    default:
      return state || initialState;
  }
};

module.exports = query;
