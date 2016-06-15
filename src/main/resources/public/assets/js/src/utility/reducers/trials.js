var moment = require('moment');

var ActionTypes = require('../action-types');

var initialState = {
  referenceTime: null, //moment('2016-03-01T00:00:00Z').valueOf(), 
};

var reduce = function (state, action) {
  var state1 = state || initialState;

  switch (action.type) {
    case ActionTypes.trials.SET_REFERENCE_TIME:
      state1 = _.extend({}, state, {referenceTime: action.referenceTime});
      break;
    default:
      break;
  }

  return state1;
};

module.exports = reduce;
