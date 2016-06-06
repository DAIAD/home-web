
var ActionTypes = require('../action-types');

var reduce = function (state={}, action={}) {
  var state1 = state;

  switch (action.type) {
    case ActionTypes.config.utility.REQUEST_CONFIGURATION:
      // noop
      break;
    case ActionTypes.config.utility.SET_CONFIGURATION:
      state1 = action.config;
      break;
    default:
      // noop
      break;
  }
  
  return state1;
};

module.exports = reduce;
