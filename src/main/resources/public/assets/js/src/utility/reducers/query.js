var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  query : null,
  points : null
};

var query = function(state, action) {
  switch (action.type) {
    case types.QUERY_SUBMIT:
      return Object.assign({}, state, {
        isLoading : true,
        query : action.query,
        points : null
      });

    case types.QUERY_RESPONSE:
      console.log(JSON.stringify(action.points, null, '\t'));
      return Object.assign({}, state, {
        isLoading : false,
        points : action.points
      });

    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, state, {
        isLoading : false,
        query : null,
        points : null
      });

    default:
      return state || initialState;
  }
};

module.exports = query;
