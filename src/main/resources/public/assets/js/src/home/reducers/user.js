var types = require('../constants/ActionTypes');

var user = function (state, action) {
  if (state === undefined) {
    state = {
      status: {
        isLoading: false
      },
      isAuthenticated: false,
      csrf: null,
      profile: {}
    };
  }
 
  switch (action.type) {
    case types.USER_REQUESTED_LOGIN:
      return Object.assign({}, state, {
        status: {
          isLoading: true
        }
      });

    case types.USER_RECEIVED_LOGIN:
      switch (action.status) {
        case true:
          return Object.assign({}, state, {
            status: {
              success: true,
              isLoading: false,
              errors: null
            },
            isAuthenticated: true,
            profile: action.profile,
          });
        
        case false:
          return Object.assign({}, state, {
            status: {
              success: false,
              isLoading: false,
              errors: action.errors
            },
            isAuthenticated: false,
          });
      }
      return state;

    case types.USER_REQUESTED_LOGOUT:
      return Object.assign({}, state, {
        status: {
          isLoading: true,
        }
      });

    case types.USER_RECEIVED_LOGOUT:
      switch (action.status) {
        case true:
          return Object.assign({}, state, {
            status: {
              success: true,
              isLoading: false,
              errors: null
            },
            isAuthenticated: false,
            profile: {},
          });
        
        case false:
          return Object.assign({}, state, {
            status: {
              success: false,
              isLoading: false,
              errors: action.errors
            }
          });
        }
        return state;

    case types.USER_SESSION_SET_CSRF:
      return Object.assign({}, state, {
        csrf: action.csrf 
      });

    default:
      return state;
  }
};

module.exports = user;

