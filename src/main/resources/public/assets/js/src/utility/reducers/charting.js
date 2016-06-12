var _ = require('lodash');

var ActionTypes = require('../action-types');

var initialState = {
  field: 'volume',
  level: 'week',
  reportName: 'avg-daily-avg',
};

var reduce = function (state, action) {
  var state1 = state || initialState;

  switch (action.type) {
    case ActionTypes.charting.SET_REPORT:
      state1 = _.extend({}, state, {
        level: action.level,
        reportName: action.reportName
      });
      break;
    case ActionTypes.charting.SET_FIELD:
      state1 = _.extend({}, state, {field: action.field});
      break;
    default:
      break;
  }

  return state1;
};

module.exports = reduce;
