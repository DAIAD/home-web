var userAPI = require('../api/user');
var types = require('../constants/ActionTypes');

const { fetch:fetchMessages } = require('./MessageActions');
const { MESSAGE_TYPES } = require('../constants/HomeConstants');

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

      return userAPI.login(username, password)
      .then((response) => {
        const { csrf, success, errors, profile } = response;

        if (csrf) { dispatch(setCsrf(csrf)); }
        
        dispatch(receivedLogin(success, errors.length?errors[0].code:null, profile));

        if (success) dispatch(fetchMessages(MESSAGE_TYPES));

        return response;
      })
      .catch((errors) => {
        console.error('User login failed with errors:', errors);
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

        if (success) dispatch(fetchMessages(MESSAGE_TYPES));

        return response;
      })
      .catch((errors) => {
        console.error('User refresh failed with errors:', errors);
        return errors;
      });
    };
  },
  logout: function() {
    return function(dispatch, getState) {
      dispatch(requestedLogout());

      const csrf = getState().user.csrf;

      return userAPI.logout({csrf})
        .then((response) => {
          const { success, errors } = response;

          dispatch(receivedLogout(success, errors.length?errors[0].code:null));
          return response;
        })
        .catch((errors) => {
          console.error('User logout failed with errors:', errors);
          return errors;
        });
    };
  },
  
};


module.exports = UserActions;
