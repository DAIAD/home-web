
var _ = require('lodash');
var moment = require('moment');

var ActionTypes = require('../action-types');

var initialState = {
  referenceTime: moment().valueOf(), // approx. when page was loaded
};

var reduce = function (state, action) {
  var state1 = state || initialState;

  switch (action.type) {
    case ActionTypes.overview.SET_REFERENCE_TIME:
      state1 = _.extend({}, state, {referenceTime: action.timestamp});
      break;
    default:
      break;
  }

  return state1;
};

module.exports = reduce;
