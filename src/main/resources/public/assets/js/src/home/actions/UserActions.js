/**
 * User Actions module.
 * User related action creators
 * 
 * @module UserActions
 */

var userAPI = require('../api/user');
var types = require('../constants/ActionTypes');
var DashboardActions = require('./DashboardActions');
var HistoryActions = require('./HistoryActions');

const { fetchAll:fetchAllMessages } = require('./MessageActions');
const { MESSAGE_TYPES } = require('../constants/HomeConstants');

const { getMeterCount } = require('../utils/device');

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

      // Actions that need to be dispatched on login
      if (success) {
        dispatch(initHome(profile));
      }
      return response;
    })
    .catch((errors) => {
      console.error('Error caught on user login:', errors);
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

      if (success) {
        dispatch(initHome(profile));
      } 

      return response;
    })
    .catch((errors) => {
      console.error('Error caught on profile refresh:', errors);
      return errors;
    });
  };
};

/**
 * Call all necessary actions to initialize app with profile data 
 *
 * @param {Object} profile - profile object as returned from server
 */
const initHome = function (profile) {
  return function(dispatch, getState) {

    dispatch(fetchAllMessages());
      
    if (profile.configuration) {
        const configuration = JSON.parse(profile.configuration);
        if (configuration.infoboxes) dispatch(DashboardActions.setInfoboxes(configuration.infoboxes));
        if (configuration.layout) dispatch(DashboardActions.updateLayout(configuration.layout, false));

    }
    dispatch(DashboardActions.fetchAllInfoboxesData());
    
    if (getMeterCount(getState().user.profile.devices) === 0) {
      dispatch(HistoryActions.setActiveDeviceType('AMPHIRO', true));
      
      dispatch(DashboardActions.setInfoboxToAdd({
        deviceType: 'AMPHIRO',
        type: 'totalVolumeStat',
        title : 'Shower volume',
      }));
    }
    else {
      dispatch(HistoryActions.setActiveDeviceType('METER', true));
    }
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

    const data = Object.assign({}, {configuration: JSON.stringify(configuration)}, {csrf: getState().user.csrf});

    return userAPI.saveToProfile(data)
    .then((response) => {
      return response;

    })
    .catch((errors) => {
      console.error('Error caught on saveToProfile:', errors);
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

      dispatch(receivedLogout(true, errors.length?errors[0].code:null));
      console.error('Error caught on logout:', errors);
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
