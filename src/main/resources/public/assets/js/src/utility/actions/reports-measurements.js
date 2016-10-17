
var _ = require('lodash');
var moment = require('moment');
var sprintf = require('sprintf');

var ActionTypes = require('../action-types');
var {computeKey} = require('../reports').measurements;
var TimeSpan = require('../model/timespan');
var population = require('../model/population');
var {toUtcTime} = require('../helpers/timestamps');
var {queryMeasurements} = require('../service/query');
var favouritesAPI = require('../api/favourites');


var addFavouriteRequest = function () {
  return {
    type: ActionTypes.reports.measurements.ADD_FAVOURITE_REQUEST
  };
};

var addFavouriteResponse = function (success, errors) {
  return {
    type: ActionTypes.reports.measurements.ADD_FAVOURITE_RESPONSE,
    success: success,
    errors: errors
  };
};

// Define actions

var actions = {

  // Plain actions
  
  initialize: (field, level, reportName, key, defaults={}) => ({
    type: ActionTypes.reports.measurements.INITIALIZE,
    field,
    level,
    reportName,
    key,
    source: defaults.source,
    timespan: defaults.timespan,
    population: defaults.population,
  }),
  
  requestData: (field, level, reportName, key, t=null) => ({
    type: ActionTypes.reports.measurements.REQUEST_DATA,
    field,
    level,
    reportName,
    key,
    timestamp: (t || new Date()).getTime(),
  }),
  
  setData: (field, level, reportName, key, data, t=null) => ({
    type: ActionTypes.reports.measurements.SET_DATA,
    field,
    level,
    reportName,
    key,
    data,
    timestamp: (t || new Date()).getTime()
  }),

  setDataError: (field, level, reportName, key, errors, t=null) => ({
    type: ActionTypes.reports.measurements.SET_DATA,
    field,
    level,
    reportName,
    key,
    errors,
    timestamp: (t || new Date()).getTime()
  }),

  setTimespan: (field, level, reportName, key, timespan) => ({
    type: ActionTypes.reports.measurements.SET_TIMESPAN,
    field,
    level,
    reportName,
    key,
    timespan,
  }),
  
  setSource: (field, level, reportName, key, source) => ({
    type: ActionTypes.reports.measurements.SET_SOURCE,
    field,
    level,
    reportName,
    key,
    source
  }),

  setPopulation: (field, level, reportName, key, population) => ({
    type: ActionTypes.reports.measurements.SET_POPULATION,
    field,
    level,
    reportName,
    key,
    population
  }),
  
  addFavourite : function(favourite) {
    return function(dispatch, getState) {
      dispatch(addFavouriteRequest());
      return favouritesAPI.addFavourite(favourite).then(function (response) {
        dispatch(addFavouriteResponse(response.success, response.errors));
      }, function (error) {
        dispatch(addFavouriteResponse(false, error));
      });
    };
  },
  updateFavourite : function(favourite) {
    return function(dispatch, getState) {
      dispatch(addFavouriteRequest());
      return favouritesAPI.updateFavourite(favourite).then(function (response) {
        dispatch(addFavouriteResponse(response.success, response.errors));
      }, function (error) {
        dispatch(addFavouriteResponse(false, error));
      });
    };  
  },
  // Complex actions: functions processed by thunk middleware
  
  refreshData: (field, level, reportName, key) => (dispatch, getState) => {
    var state = getState();
    
    var _state = state.reports.measurements;
    
    var {config} = state;
    var {metrics, levels} = config.reports.byType.measurements;
    
    var k = computeKey(field, level, reportName, key);
    var report = levels[level].reports[reportName];
    
    var {timespan: ts, source, requested, population: target} = _state[k];

    // Throttle requests
    var now = new Date();
    if (requested && (now.getTime() - requested < 1e+3)) {
      console.info('Skipping refresh requests arriving too fast...');
      return Promise.resolve();
    } 
    
    // Prepare population target
    if (!target) {
      // Assume target is the entire utility
      target = new population.Utility(config.utility.key, config.utility.name);
    } else if (target instanceof population.Cluster) {
      // Expand to all groups inside target cluster
      target = config.utility.clusters
        .find(c => (c.key == target.key))
          .groups.map(g => (new population.ClusterGroup(target.key, g.key)));
    } else {
      console.assert(target instanceof population.Group, 
        'Expected an instance of population.Group');
    }
    
    // Prepare literal time range, re-order if needed
    var t0, t1, timezone = 'Etc/GMT';
    if (_.isString(ts)) {
      // Interpret this named range, as if you were at UTC+0 
      [t0, t1] = TimeSpan.fromName(ts, 0).toRange();
    } else {
      // Set global timezone, move to UTC while keeping local time
      [t0, t1] = ts;
      if (t0 > t1) {
        let t = t0; t0 = t1; t1 = t;
      }
      t0 = toUtcTime(t0);
      t1 = toUtcTime(t1);
    }
    console.assert(
      moment.isMoment(t0) && t0.isUTC() && moment.isMoment(t1) && t1.isUTC(),
      'Expected 2 moment instances both flagged as UTC!'); 
    t1.add(1, level); // a closure time slot

    // Prepare the entire query
    var q = {
      granularity: report.granularity,
      timespan: [t0.valueOf(), t1.valueOf()], 
      metrics: report.metrics,
      ranking: report.ranking,
      population: _.flatten([target]),
    };
   
    // Dispatch, return promise
    dispatch(actions.requestData(field, level, reportName, key, now));
    var pq = queryMeasurements(source, field, q, {metrics, timezone})
      .then(
        (data) => (
          dispatch(actions.setData(field, level, reportName, key, data))
        ),
        (reason) => (
          console.error(sprintf('Cannot refresh data for %s: %s', k, reason)),
          dispatch(actions.setDataError(field, level, reportName, key, [reason]))
        )
      );

    return pq;
  },
};

module.exports = actions;
