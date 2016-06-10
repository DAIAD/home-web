var types = require('../constants/ActionTypes');

var initialState = {
  isAuthenticated : false,
  isLoading : false,
  profile : null,
  errors : null
};

var session = function(state, action) {
  switch (action.type) {
    case types.USER_REQUESTED_LOGIN:
      return Object.assign({}, state, {
        isLoading : true
      });

    case types.USER_RECEIVED_LOGIN:
      switch (action.status) {
        case true:
          if(document) {
            document.cookie = 'daiad-utility-session=true; path=/';
          }
          
          return Object.assign({}, state, {
            isAuthenticated : true,
            profile : action.profile,
            errors : null,
            isLoading : false
          });

        case false:
          return Object.assign({}, state, {
            isAuthenticated : false,
            profile : null,
            errors : action.errors,
            isLoading : false
          });
      }
      break;

    case types.USER_REQUESTED_LOGOUT:
      return Object.assign({}, state, {
        isLoading : true
      });

    case types.USER_RECEIVED_LOGOUT:
      if(document) {
        document.cookie = 'daiad-utility-session=false; path=/;expires=Thu, 01 Jan 1970 00:00:01 GMT;';
      }

      switch (action.status) {
        case true:
          return Object.assign({}, state, {
            isAuthenticated : false,
            profile : null,
            errors : null,
            isLoading : false
          });

        case false:
          return Object.assign({}, state, {
            isAuthenticated : false,
            profile : null,
            errors : action.errors,
            isLoading : false
          });
      }
      break;

    default:
      return state || initialState;
  }
};

module.exports = session;
