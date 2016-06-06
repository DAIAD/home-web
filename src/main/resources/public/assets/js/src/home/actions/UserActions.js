/**
 * User Actions module.
 * User related action creators
 * 
 * @module UserActions
 */

var userAPI = require('../api/user');
var types = require('../constants/ActionTypes');

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

/**
 * Performs user login 
 *
 * @param {String} username
 * @param {String} password
 * @return {Promise} Resolved or rejected promise with user profile if resolved, errors if rejected
 */
const login = function(username, password) {
  return function(dispatch, getState) {
    dispatch(requestedLogin());

    return userAPI.login(username, password)
    .then((response) => {
      const { csrf, success, errors, profile } = response;

      if (csrf) { dispatch(setCsrf(csrf)); }
      
      dispatch(receivedLogin(success, errors.length?errors[0].code:null, profile));
      return response;
    })
    .catch((errors) => {
      console.error('User login failed with errors:', errors);
      return errors;
    });
  };
};

/**
 * Fetches profile when user refreshes page 
 *
 * @return {Promise} Resolved or rejected promise with user profile if resolved, errors if rejected
 */
const refreshProfile = function() {
  return function(dispatch, getState) {
    return userAPI.getProfile()
    .then((response) => {
      const { csrf, success, errors, profile } = response;
      
      if (csrf) { dispatch(setCsrf(csrf)); }

      dispatch(receivedLogin(success, errors.length?errors[0].code:null, profile));
      return response;
    })
    .catch((errors) => {
      console.error('User refresh failed with errors:', errors);
      return errors;
    });
  };
};

/**
 * Performs user logout 
 *
 * @return {Promise} Resolved or rejected promise, errors if rejected
 */
const logout = function() {
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
};
  

module.exports = {
  login,
  logout,
  refreshProfile
};
