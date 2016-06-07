/**
 * User Actions module.
 * User related action creators
 * 
 * @module UserActions
 */

var userAPI = require('../api/user');
var types = require('../constants/ActionTypes');
var DashboardActions = require('./DashboardActions');

const { fetchAll:fetchAllMessages } = require('./MessageActions');
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

      if (success) dispatch(fetchAllMessages());
      
      if (success && profile.configuration) {
          const configuration = JSON.parse(profile.configuration);
          if (configuration.infoboxes) dispatch(DashboardActions.setInfoboxes(configuration.infoboxes));
          if (configuration.layout) dispatch(DashboardActions.updateLayout(configuration.layout));
      }
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

      if (success) dispatch(fetchAllMessages());
      
      if (success && profile.configuration) {
          const configuration = JSON.parse(profile.configuration);
          if (configuration.infoboxes) dispatch(DashboardActions.setInfoboxes(configuration.infoboxes));
          if (configuration.layout) dispatch(DashboardActions.updateLayout(configuration.layout));
        }
      return response;
    })
    .catch((errors) => {
      console.error('User refresh failed with errors:', errors);
      return errors;
    });
  };
};

/**
 * Saves JSON data to profile  
 *
 * @param {Object} configuration - serializable object to be saved to user profile
 * @return {Promise} Resolved or rejected promise, with errors if rejected
 */
const saveToProfile = function (configuration) {
  return function(dispatch, getState) {

    const data = Object.assign({}, {configuration}, {csrf: getState().user.csrf});
    console.log('gonna save...', data);

    return userAPI.saveToProfile(data)
    .then((response) => {
      console.log('saved to profile', response);
      return response;

    })
    .catch((errors) => {
      console.error('User save to profile failed with errors:', errors);
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
      console.error('User login failed with errors:', errors);
      dispatch(receivedLogout(success, errors.length?errors[0].code:null));
      return errors;
    });
  };
};
  

module.exports = {
  login,
  logout,
  refreshProfile,
  saveToProfile
};
