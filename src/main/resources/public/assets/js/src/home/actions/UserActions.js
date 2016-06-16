/**
 * User Actions module.
 * User related action creators
 * 
 * @module UserActions
 */

var userAPI = require('../api/user');
var types = require('../constants/ActionTypes');

var InitActions = require('./InitActions');

const requestedLogin = function() {
  return {
    type:types.USER_REQUESTED_LOGIN,
    };
};

const receivedLogin = function(success, errors, profile) {
  return {
    type: types.USER_RECEIVED_LOGIN,
    success,
    errors,
    profile
  };
};

const requestedLogout = function() {
  return {
    type:types.USER_REQUESTED_LOGOUT,
    };
};

const receivedLogout = function(success, errors) {
  return {
    type: types.USER_RECEIVED_LOGOUT,
    success,
    errors
  };
};

const requestedQuery = function() {
  return {
    type: types.QUERY_REQUEST_START,
  };
};

const receivedQuery = function(success, errors) {
  return {
    type: types.QUERY_REQUEST_END,
    success,
    errors,
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
        return dispatch(InitActions.initHome(profile));
      }
      return Promise.reject(response);
    })
    .catch((errors) => {
      console.error('Error caught on user login:', errors);
      return errors;
    });
  };
};

/**
 * Fetches profile and performs necessary initialization when user eefreshes page 
 *
 * @return {Promise} Resolved or rejected promise with user profile if resolved, errors if rejected
 */
const refreshProfile = function() {
  return function(dispatch, getState) {
    console.log('refreshing profile...');
    return dispatch(fetchProfile())
    .then((response) => {
      const { csrf, success, errors, profile } = response;
      
      if (csrf) { dispatch(setCsrf(csrf)); }

      dispatch(receivedLogin(success, errors.length?errors[0].code:null, profile));

      if (success) {
        return dispatch(InitActions.initHome(profile));
      } 

      return Promise.reject(response);
    })
    .catch((errors) => {
      console.error('Error caught on profile refresh:', errors);
      return errors;
    });
  };
};

/**
 * Fetches profile
 *
 * @return {Promise} Resolved or rejected promise with user profile if resolved, errors if rejected
 */
const fetchProfile = function() {
  return function(dispatch, getState) {
    return userAPI.getProfile()
    .then((response) => {
      const { success, errors, profile } = response;
      
      dispatch(receivedLogin(success, errors.length?errors[0].code:null, profile));

      return response;
    })
    .catch((errors) => {
      console.error('Error caught on profile fetch:', errors);
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

/**
 * Saves JSON data to profile  
 *
 * @param {Object} configuration - serializable object to be saved to user profile
 * @return {Promise} Resolved or rejected promise, with errors if rejected
 */
const saveToProfile = function (profile) {
  return function(dispatch, getState) {

    //TODO: country is there because of bug in backend that sets it to null otherwise causing problems
    const data = Object.assign({}, {country: 'Greece'}, profile, {csrf: getState().user.csrf});

    dispatch(requestedQuery());

    return userAPI.saveToProfile(data)
    .then((response) => {

      dispatch(receivedQuery(response.success, response.errors));
       
      if (!response || !response.success) {
        throw new Error (response && response.errors && response.errors.length > 0 ? response.errors[0].code : 'unknownError');
      }

      return response;

    })
    .catch((errors) => {
      console.error('Error caught on saveToProfile:', errors);
      dispatch(receivedQuery(false, errors));
      return errors;
    });
  };
};


module.exports = {
  login,
  logout,
  refreshProfile,
  fetchProfile,
  saveToProfile,
};
