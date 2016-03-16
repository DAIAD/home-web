var userAPI = require('../api/user');
var types = require('../constants/ActionTypes');

var DeviceActions = require('../actions/DeviceActions');

const requestedLogin = function() {
  return {
    type:types.USER_REQUESTED_LOGIN,
    };
};

const receivedLogin = function(status, errors, profile) {
  return {
    type: types.USER_RECEIVED_LOGIN,
    status: status,
    errors: errors,
    profile: profile
  };
};

const requestedLogout = function() {
  return {
    type:types.USER_REQUESTED_LOGOUT,
    };
};

const receivedLogout = function(status, errors) {
  return {
    type: types.USER_RECEIVED_LOGOUT,
    status: status,
    errors: errors
  };
};

const setCsrf = function(csrf) {
  return {
    type: types.USER_SESSION_SET_CSRF,
    csrf: csrf 
  };
};

const UserActions = {

  login: function(username, password) {
    return function(dispatch, getState) {
      dispatch(requestedLogin());

      return userAPI.login({username, password})
      .then((response) => {
        const { csrf, success, errors, profile } = response;

        if (csrf) { dispatch(setCsrf(csrf)); }
        
        dispatch(receivedLogin(success, errors.length?errors[0].code:null, profile));
        return response;
      })
      .catch((errors) => {
        dispatch(receivedLogin(false, errors.length?errors[0].code:null, {}));
        return errors;
      });
    };
  },
  refreshProfile: function() {
    return function(dispatch, getState) {
      return userAPI.getProfile()
      .then((response) => {
        const { csrf, success, errors, profile } = response;
        
        if (csrf) { dispatch(setCsrf(csrf)); }

        dispatch(receivedLogin(success, errors.length?errors[0].code:null, profile));
        return response;
      })
      .catch((errors) => {
        dispatch(receivedLogin(false, errors.length?errors[0].code:null, {}));
        return errors;
      });
    };
  },
  logout: function() {
    return function(dispatch, getState) {
      dispatch(requestedLogout());

      return userAPI.logout()
        .then((response) => {
          const { success, errors } = response;
          dispatch(receivedLogout(success, errors.length?errors[0].code:null));
          return response;
        })
        .catch((errors) => {
          dispatch(receivedLogout(false, errors.length?errors[0].code:errors));
          return errors;
        });
    };
  },
  
};


module.exports = UserActions;
