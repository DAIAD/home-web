var moment = require('moment');

var types = require('../constants/ForecastingActionTypes');

var _createInitialState = function() {
  return {
    isLoading : false,
    interval : [
        moment().startOf('month'), moment().endOf('month')
    ],
    query : {
      utility : null,
      user : null
    },
    data : {
      utility : null,
      user : null
    },
    forecast : {
      utility : null,
      user : null
    },
    user : null
  };
};

var _extractChartSeries = function(interval, data, label) {
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

var admin = function(state, action) {
  switch (action.type) {
    case types.UTILITY_DATA_REQUEST:

      return Object.assign({}, state, {
        isLoading : true,
        query : {
          utility : action.query,
          user : state.query.user
        },
        data : {
          utility : null,
          user : state.data.user
        }
      });

    case types.UTILITY_DATA_RESPONSE:
      if (action.success) {
        return Object.assign({}, state, {
          isLoading : false,
          data : {
            utility : _extractChartSeries(state.interval, action.data, state.query.utility.query.population[0].label),
            user : state.data.user
          }
        });
      }

      return Object.assign({}, state, {
        isLoading : false,
        data : {
          utility : null,
          user : state.data.user
        }
      });

    case types.UTILITY_FORECAST_REQUEST:

      return Object.assign({}, state, {
        isLoading : true,
        query : {
          utility : action.query,
          user : state.query.user
        },
        forecast : {
          utility : null,
          user : state.data.user
        }
      });

    case types.UTILITY_FORECAST_RESPONSE:
      if (action.success) {
        return Object.assign({}, state, {
          isLoading : false,
          forecast : {
            utility : _extractChartSeries(state.interval, action.data, state.query.utility.query.population[0].label),
            user : state.data.user
          }
        });
      }

      return Object.assign({}, state, {
        isLoading : false,
        data : {
          utility : null,
          user : state.data.user
        }
      });

    case types.USER_DATA_REQUEST:
      return Object.assign({}, state, {
        isLoading : true,
        query : {
          utility : state.query.utility,
          user : action.query,
        },
        data : {
          utility : state.data.utility,
          user : null
        }
      });

    case types.USER_DATA_RESPONSE:
      if (action.success) {
        return Object.assign({}, state, {
          isLoading : false,
          data : {
            utility : state.data.utility,
            user : _extractChartSeries(state.interval, action.data, state.query.user.query.population[0].label)
          }
        });
      }

      return Object.assign({}, state, {
        isLoading : false,
        data : {
          utility : state.data.utility,
          user : null
        }
      });

    case types.USER_FORECAST_REQUEST:

      return Object.assign({}, state, {
        isLoading : true,
        query : {
          utility : state.query.utility,
          user : action.query
        },
        forecast : {
          utility : state.forecast.utility,
          user : null
        }
      });

    case types.USER_FORECAST_RESPONSE:
      if (action.success) {
        return Object.assign({}, state, {
          isLoading : false,
          forecast : {
            utility : state.forecast.utility,
            user : _extractChartSeries(state.interval, action.data, state.query.user.query.population[0].label)
          }
        });
      }

      return Object.assign({}, state, {
        isLoading : false,
        data : {
          utility : state.data.utility,
          user : null
        }
      });

    case types.SET_USER:
      var clearData = ((action.user == null) || (state.user == null));
      return Object.assign({}, state, {
        user : action.user,
        data : {
          utility : state.data.utility,
          user : (clearData ? null : state.data.user)
        },
        forecast : {
          utility : state.forecast.utility,
          user : (clearData ? null : state.forecast.user)
        }
      });

    case types.USER_RECEIVED_LOGOUT:
      return _createInitialState();

    default:
      return state || _createInitialState();
  }
};

module.exports = admin;
