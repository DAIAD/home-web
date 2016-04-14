var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false
};

var admin = function(state, action) {
  switch (action.type) {
    case types.DEBUG_CREATE_USER:
      return Object.assign({}, state, {
        isLoading : true
      });

    case types.DEBUG_USER_CREATED:
      return Object.assign({}, state, {
        isLoading : false
      });

    case types.DEBUG_CREATE_AMPHIRO:
      return Object.assign({}, state, {
        isLoading : true
      });

    case types.DEBUG_AMPHIRO_CREATED:
      return Object.assign({}, state, {
        isLoading : false
      });

    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, state, {
        isLoading : false
      });

    default:
      return state || initialState;
  }
};

module.exports = admin;
