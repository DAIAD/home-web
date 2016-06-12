var moment = require('moment');

var types = require('../constants/DashboardActionTypes');

var _createStatisticsInitialState = function() {
  return {
    counters : null
  };
};

var _createMapInitialState = function() {
  return {
    interval : [
        moment().subtract(14, 'day'), moment()
    ],
    query : null,
    areas : null,
    meters : null,
    devices : null,
    timeline : null,
    features : null
  };
};

var _createChartInitialState = function() {
  return {
    interval : [
        moment().subtract(14, 'day'), moment()
    ],
    query : null,
    series : null
  };
};

var _createInitialState = function() {
  return {
    isLoading : false,
    interval : [
        moment().subtract(14, 'day'), moment()
    ],
    statistics : _createStatisticsInitialState(),
    map : _createMapInitialState(),
    chart : _createChartInitialState(),
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
      if (ref.isSame(points[index].timestamp, 'day')) {
        series.push({
          volume : points[index].volume.SUM,
          date : ref.clone().toDate()
        });
        index++;
      } else {
        series.push({
          volume : 0,
          date : ref.clone().toDate()
        });
      }
      ref.subtract(1, 'days');
    }
  }

  return {
    label : label,
    data : series.reverse()
  };
};

var _extractChartSeries = function(interval, data) {
  var series = {};

  series.meters = _extractSeries(interval, data.meters, 'Meter');
  series.devices = _extractSeries(interval, data.devices, 'Amphiro B1');

  return series;
};

var statisticsReducer = function(state, action) {
  switch (action.type) {
    case types.COUNTER_REQUEST:
      return Object.assign({}, state, {
        counters : null
      });

    case types.COUNTER_RESPONSE:
      if (action.success) {
        return Object.assign({}, state, {
          counters : action.counters
        });
      }

      return Object.assign({}, state, {
        counters : null
      });

    default:
      return state || _createStatisticsInitialState();
  }
};

var mapReducer = function(state, action) {
  switch (action.type) {
    case types.TIMELINE_REQUEST:
      return Object.assign({}, state, {
        query : action.query,
        areas : null,
        meters : null,
        devices : null,
        timeline : null,
        features : null,
        index : 0
      });

    case types.TIMELINE_RESPONSE:
      if (action.success) {
        return Object.assign({}, state, {
          areas : action.data.areas,
          meters : action.data.meters,
          devices : action.data.devices,
          timeline : _extractTimeline(action.data.meters, action.data.areas),
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

    case types.GET_FEATURES:
      var features = (state.timeline ? state.timeline.getFeatures(action.timestamp, action.label) : null);

      return Object.assign({}, state, {
        features : features,
        index: action.index
      });

    default:
      return state || _createMapInitialState();
  }
};

var chartReducer = function(state, action) {
  switch (action.type) {
    case types.CHART_REQUEST:
      return Object.assign({}, state, {
        query : action.query,
        series : null
      });

    case types.CHART_RESPONSE:
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

var dashboard = function(state, action) {
  switch (action.type) {
    case types.TIMELINE_REQUEST:
    case types.CHART_REQUEST:
    case types.COUNTER_REQUEST:
      return Object.assign({}, state, {
        isLoading : true,
        statistics : statisticsReducer(state.statistics, action),
        map : mapReducer(state.map, action),
        chart : chartReducer(state.chart, action)
      });

    case types.TIMELINE_RESPONSE:
    case types.CHART_RESPONSE:
    case types.COUNTER_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
        statistics : statisticsReducer(state.statistics, action),
        map : mapReducer(state.map, action),
        chart : chartReducer(state.chart, action)
      });

    case types.GET_FEATURES:
      return Object.assign({}, state, {
        isLoading : false,
        statistics : statisticsReducer(state.statistics, action),
        map : mapReducer(state.map, action),
        chart : chartReducer(state.chart, action)
      });

    case types.USER_RECEIVED_LOGOUT:
      return _createInitialState();

    default:
      return state || _createInitialState();
  }
};

module.exports = dashboard;
