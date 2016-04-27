var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  group : null
};

var alerts = function(state, action) {
  switch (action.type) {
    case types.ALERTS_GROUP_SELECTED:
      return Object.assign({}, state, {
        group : action.group
      });

    default:
      return state || initialState;
  }
};

module.exports = alerts;
