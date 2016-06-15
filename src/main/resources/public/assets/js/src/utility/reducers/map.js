var moment = require('moment');

var types = require('../constants/MapActionTypes');

var _createMapInitialState = function(interval) {
  return {
    interval : interval,
    query : null,
    areas : null,
    meters : null,
    devices : null,
    timeline : null,
    features : null
  };
};

var _createChartInitialState = function(interval) {
  return {
    interval : interval,
    query : null,
    series : null
  };
};

var _createInitialState = function() {
  var interval = [
      moment().subtract(14, 'day'), moment()
  ];

  return {
    isLoading : false,
    interval : interval,
    population : null,
    source : 'METER',
    geometry: null,
    timezone : null,
    ranges : {
      'Last 7 Days' : [
          moment().subtract(6, 'days'), moment()
      ],
      'Last 30 Days' : [
          moment().subtract(29, 'days'), moment()
      ],
      'This Month' : [
          moment().startOf('month'), moment().endOf('month')
      ],
      'Last Month' : [
          moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')
      ]
    },
    map : _createMapInitialState(interval),
    chart : _createChartInitialState(interval),
    editor : 'interval'
  };
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

var _extractSeries = function(interval, data, label) {
  var d;
  var series = [];

  var ref = interval[1].clone();
  var days = interval[1].diff(interval[0], 'days') + 1;

  if ((data.length === 0) || (!data[0].points) || (data[0].points.length === 0)) {
    for (d = days; d > 0; d--) {
      series.push({
        volume : 0,
        date : ref.clone().toDate()
      });

      ref.subtract(1, 'days');
    }
  } else {
    var index = 0;
    var points = data[0].points;

    points.sort(function(p1, p2) {
      return (p2.timestamp - p1.timestamp);
    });

    for (d = days; d > 0; d--) {
      if (index === points.length) {
        series.push({
          volume : 0,
          date : ref.clone().toDate()
        });

        ref.subtract(1, 'days');
      } else if (ref.isBefore(points[index].timestamp, 'day')) {
        index++;
      } else if (ref.isAfter(points[index].timestamp, 'day')) {
        series.push({
          volume : 0,
          date : ref.clone().toDate()
        });

        ref.subtract(1, 'days');
      } else if (ref.isSame(points[index].timestamp, 'day')) {
        series.push({
          volume : points[index].volume.SUM,
          date : ref.clone().toDate()
        });

        index++;
        ref.subtract(1, 'days');
      }
    }
  }

  return {
    label : label,
    data : series.reverse()
  };
};

var _extractChartSeries = function(interval, data) {
  return {
    meters : _extractSeries(interval, data.meters, 'Meter'),
    devices : _extractSeries(interval, data.devices, 'Amphiro B1')
  };
};

var mapReducer = function(state, action) {
  switch (action.type) {
    case types.MAP_TIMELINE_REQUEST:
      return Object.assign({}, state, {
        query : action.query,
        areas : null,
        meters : null,
        devices : null,
        timeline : null,
        features : null,
        index : 0
      });

    case types.MAP_TIMELINE_RESPONSE:
      if (action.success) {
        var source = (state.query.query.source == 'METER') ? action.data.meters : action.data.devices;

        return Object.assign({}, state, {
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

    case types.MAP_GET_FEATURES:
      var features = (state.timeline ? state.timeline.getFeatures(action.timestamp, action.label) : null);

      return Object.assign({}, state, {
        features : features,
        index : action.index
      });

    default:
      return state || _createMapInitialState();
  }
};

var chartReducer = function(state, action) {
  switch (action.type) {
    case types.MAP_CHART_REQUEST:
      return Object.assign({}, state, {
        query : action.query,
        series : null
      });

    case types.MAP_CHART_RESPONSE:
      if (action.success) {
        return Object.assign({}, state, {
          series : _extractChartSeries(state.interval, action.data)
        });
      }

      return Object.assign({}, state, {
        series : null
      });

    default:
      return state || _createChartInitialState();
  }
};

var map = function(state, action) {
  switch (action.type) {
    case types.MAP_TIMELINE_REQUEST:
    case types.MAP_CHART_REQUEST:
      return Object.assign({}, state, {
        isLoading : true,
        map : mapReducer(state.map, action),
        chart : chartReducer(state.chart, action),
        population : action.population
      });

    case types.MAP_TIMELINE_RESPONSE:
    case types.MAP_CHART_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
        map : mapReducer(state.map, action),
        chart : chartReducer(state.chart, action)
      });

    case types.MAP_GET_FEATURES:
      return Object.assign({}, state, {
        isLoading : false,
        map : mapReducer(state.map, action),
        chart : chartReducer(state.chart, action)
      });

    case types.MAP_SELECT_EDITOR:
      return Object.assign({}, state, {
        editor : action.editor
      });

    case types.MAP_SET_EDITOR_VALUE:
      switch (action.editor) {
        case 'interval':
          return Object.assign({}, state, {
            interval : action.value
          });

        case 'source':
          return Object.assign({}, state, {
            source : action.value
          });

        case 'population':
          var group = action.value;

          switch (group.type) {
            case 'UTILITY':
              return Object.assign({}, state, {
                population : {
                  utility : group.key,
                  label : group.name,
                  type : 'UTILITY'
                }
              });
            case 'SEGMENT':
            case 'SET':
              return Object.assign({}, state, {
                population : {
                  group : group.key,
                  label : group.name,
                  type : 'GROUP'
                }
              });
          }

          return state;
        case 'spatial':
          return Object.assign({}, state, {
            geometry : action.value
          });
      }

      return state;

    case types.MAP_SET_TIMEZONE:
      return Object.assign({}, state, {
        timezone : action.timezone
      });
    case types.USER_RECEIVED_LOGOUT:
      return _createInitialState();

    default:
      return state || _createInitialState();
  }
};

module.exports = map;
