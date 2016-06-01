
var _ = require('lodash');
var sprintf = require('sprintf');

var ActionTypes = require('../action-types');
var TimeSpan = require('../timespan');
var {computeKey} = require('../reports').measurements;
var population = require('../population');
var {queryMeasurements} = require('../service/query');

// Define actions

var actions = {

  // Plain actions
  
  initialize: (field, level, reportName, key, defaults) => ({
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
    timestamp: (t || new Date()).getTime(),
  }),

  setDataError: (field, level, reportName, key, errors, t=null) => ({
    type: ActionTypes.reports.measurements.SET_DATA,
    field,
    level,
    reportName,
    key,
    errors,
    timestamp: (t || new Date()).getTime(),
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
    source,
  }),

  setPopulation: (field, level, reportName, key, population) => ({
    type: ActionTypes.reports.measurements.SET_POPULATION,
    field,
    level,
    reportName,
    key,
    population,
  }),

  // Complex actions: functions processed by thunk middleware
  
  refreshData: (field, level, reportName, key) => (dispatch, getState) => {
    var state = getState();
    
    var _state = state.reports.measurements;
    
    var {config} = state;
    var _config = config.reports.byType.measurements;
    
    var k = computeKey(field, level, reportName, key);
    var report = _config.levels[level].reports[reportName];
    
    var {timespan, source, requested, population: target} = _state[k];

    var now = new Date();
    if (requested && (now.getTime() - requested < 1e+3)) {
      console.info('Skipping refresh requests arriving too fast...');
      return Promise.resolve();
    } 
    
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
   
    var q = {
      granularity: report.granularity,
      timespan: _.isString(timespan)? 
        TimeSpan.fromName(timespan).toRange(true) : timespan,
      metrics: report.metrics,
      ranking: report.ranking,
      population: _.flatten([target]),
    };
    
    dispatch(actions.requestData(field, level, reportName, key, now));
    
    return queryMeasurements(source, field, q, _config).then(
      (data) => (
        dispatch(actions.setData(field, level, reportName, key, data))
      ),
      (reason) => (
        console.error(sprintf('Cannot refresh data for %s: %s', k, reason)),
        dispatch(actions.setDataError(field, level, reportName, key, [reason]))
      )
    );
  },
};

module.exports = actions;
