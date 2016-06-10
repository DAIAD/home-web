
var _ = require('lodash');

var ActionTypes = require('../action-types');
var reports = require('../reports');

var assertInitialized = (r, key) => (
  console.assert(_.isObject(r), 
    'Expected an initialized entry for report: ' + key)
);

var reduce = function (state={}, action={}) {
 
  var type = _.find(ActionTypes.reports.system, v => (v == action.type));
  
  if (!type)
    return state; // not interested

  var {level, reportName} = action;
  if (level == null || reportName == null)
    return state; // malformed action; dont touch state

  var key = reports.system.computeKey(level, reportName);
  var r = null; 
  if (key in state) {
    // Clone existing state for (level, reportName)
    r = _.extend({}, state[key]);
  } 

  switch (type) {
    case 'INITIALIZE':
      // Initialize parameters for report (level, reportName)
      // See more on the meaning of each field at store.js.
      if (r == null) {
        r = { // new entry
          timespan: config.levels[level].reports[reportName].timespan,  // as default
          points: null,     // data points, aka series
          invalid: true,    // data that needs to be refreshed?
          requested: null,  // time of last successfull attempt to fetch series
          finished: null,   // time of last successfull update of series
        };
      } else {
        r = null; // already initialized; dont touch state 
      }
      break;
    case 'REQUEST_DATA':
      assertInitialized(r, key);
      // Keep current series data, until fresh arrive
      _.extend(r, {
        finished: false,
        requested: action.timestamp,
      });
      break;
    case 'SET_DATA':
      assertInitialized(r, key);
      _.extend(r, {
        finished: action.timestamp,
        invalid: false,
        points: action.data, // Todo: re-shape result?
      });
      break;
    case 'SET_TIMESPAN':
      assertInitialized(r, key);
      if (r.timespan != action.timespan) {
        _.extend(r, {
          timespan: action.timespan,
          invalid: true,
        });
      } else {
        r = null; // unchanged; dont touch state
      }
      break;
    default:
      r = null; // unknown action; dont touch state
      break;
  }
  
  // Compute new state, if r is touched
  return r? _.extend({}, state, {[key]: r}) : state;
};

module.exports = reduce;
