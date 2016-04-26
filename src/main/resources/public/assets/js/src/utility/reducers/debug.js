var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  timezone: null,
  errors: null
};

var admin = function(state, action) {
  switch (action.type) {
    case types.DEBUG_CREATE_USER:
      return Object.assign({}, state, {
        isLoading : true,
        errors: null
      });

    case types.DEBUG_USER_CREATED:
      return Object.assign({}, state, {
        isLoading : false,
        errors: action.errors
      });

    case types.DEBUG_CREATE_AMPHIRO:
      return Object.assign({}, state, {
        isLoading : true,
        errors: null
      });

    case types.DEBUG_AMPHIRO_CREATED:
      return Object.assign({}, state, {
        isLoading : false,
        errors: action.errors
      });
      
    case types.DEBUG_AMPHIRO_DATA_GENERATE_REQUEST:
      return Object.assign({}, state, {
        isLoading : true,
        errors: null
      });

    case types.DEBUG_AMPHIRO_DATA_GENERATED:
      return Object.assign({}, state, {
        isLoading : false,
        errors: action.errors
      });
      
    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, state, {
        isLoading : false,
        errors: null
      });
      
    case types.DEBUG_SET_TIMEZONE:
      return Object.assign({}, state, {
        timezone : action.timezone
      });

    case types.DEBUG_SET_ERRORS:
      return Object.assign({}, state, {
        errors : action.errors
      });

    default:
      return state || initialState;
  }
};

module.exports = admin;
