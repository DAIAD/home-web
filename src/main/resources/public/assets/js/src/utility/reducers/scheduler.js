var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  jobs : null,
  executions : null
};

var query = function(state, action) {
  switch (action.type) {
    case types.SCHEDULER_STATUS_REQUEST:
      return Object.assign({}, state, {
        isLoading : true
      });

    case types.SCHEDULER_STATUS_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
        jobs : (action.jobs || []),
        executions : (actions.executions || [])
      });

    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, state, {
        isLoading : false,
        jobs : [],
        executions : []
      });

    default:
      return state || initialState;
  }
};

module.exports = query;
