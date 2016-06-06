
var _ = require('lodash');
var sprintf = require('sprintf');

var population = require('../model/population');
var Granularity = require('../model/granularity'); 
var api = require('../api/query');
  
var queryStats = function (source, q, config) { 
  
  // Todo Build query to target action api
  
  source = source.toUpperCase();
  
  var q1 = {};
  
  return api.queryStats(q1).then(
    res => (
      // Todo shape result
      null
    )
  );
};

var queryMeasurements = function (source, field, q, config={}) {
  var defaults = queryMeasurements.defaults;
  
  // Validate parameters

  q = queryMeasurements.validate(q, config);
  
  source = source.toUpperCase();
  
  var q1 = {
    ...defaults.api.queryParams,
    source,
    time: {
      start: q.timespan[0], 
      end: q.timespan[1], 
      granularity: q.granularity,
    },
    population: q.population.map(p => p.toJSON()),
  };
 
  if (q.ranking) {
    q1.population = _.flatten(q1.population.map(p => {
      var g = population.Group.fromString(p.label);
      return q.ranking.map(r => {
        var r1 = {limit: 3, type: 'TOP', ...r, field}; 
        return {...p, 
          label: [g, new population.Ranking(r1)].join('/'),
          ranking: r1,
        };
      })
    }));
  }

  // Send query, shape result

  return api.queryMeasurements({query: q1}).then(
    res => {
      if (res.errors.length) 
        throw 'The request is rejected: ' + res.errors[0].description; 
      
      // Include common params for all series
      var params = {source, timespan: q.timespan, granularity: q.granularity}; 

      // Shape result
      var resultSets = (source == 'DEVICE')? res.devices : res.meters;
      var res1 = (resultSets || []).map(rs => {
        var [g, rr] = population.fromString(rs.label);
        console.assert((q.ranking && rr) || (!q.ranking && !rr), 
          'Check ranking descriptor');
        if (rr) {
          // Shape a result with ranking on users
          var points = rs.points.map(p => ({
            timestamp: p.timestamp,
            values: p.users.map(u => u[rr.field][rr.metric]).sort(rr.comparator),
          }));
          return _.times(rr.limit, (i) => ({
            ...params,
            metric: rr.metric,
            population: g,
            ranking: {...rr.toJSON(), index: i},
            data: points.map(p => ([p.timestamp, p.values[i] || null])),
          }));
        } else {   
          // Shape a normal timeseries result for requested metrics
          // Todo support other metrics (as client-side "average")
          return q.metrics.map(metric => ({
            ...params,
            metric,
            population: g,
            data: rs.points.map(p => ([p.timestamp, p[field][metric]])),
          }));
        }
      });
      return _.flatten(res1);
    }
  );
};

queryMeasurements.defaults = {
  api: {
    queryParams: {
      metrics: ['SUM', 'COUNT', 'AVERAGE', 'MIN', 'MAX'],
    },
  },
};

queryMeasurements.getValidators = function (q, config) {
  return {
    granularity: (granularity) => (
      Granularity.fromName(granularity.toLowerCase())?
        null : (new Error('Unknown granularity'))
    ),
    timespan: ([t0, t1]) => ( 
      (_.isNumber(t0) && _.isNumber(t1))?
        null : (new Error('Cannot read timespan'))
    ),
    metrics: (metrics) => {
      if (q.ranking) {
        return null; // a metric is n/a when a ranking is requested
      }
      if (!metrics || !_.isArray(metrics) || !metrics.length) {
        return new Error('A metric must be specified');
      }
      var metric1 = metrics.find(m => (config.metrics.indexOf(m) < 0));
      return !metric1? null : (new Error(sprintf('Unknown metric (%s)', metric1)));
    },
    ranking: (ranking) => (
      (!ranking || (_.isArray(ranking) && 
          ranking.every(r => (r.type && (config.metrics.indexOf(r.metric || '') >= 0)))
        )
      )? null : (new Error('Expected a ranking as an array of {type, metric}'))
    ),
    population: (p) => (
      p.every(p1 => (
        (p1 instanceof population.Group) || 
        (p1 instanceof population.Cluster)
      ))? null : (new Error('Expected an instance of population.(Group|Cluster)'))
    ),
  };
};

queryMeasurements.validate = function (q, config) {
  
  var err = null;
  var validators = queryMeasurements.getValidators(q, config);
  _.forEach(validators, (validator, paramName) => (
    err = validator(q[paramName]),
    err && console.error(err.message, q[paramName]),
    !err // break on error
  ));
  
  if (err) {
    throw err;
  }

  return q;
};

module.exports = {queryStats, queryMeasurements};
