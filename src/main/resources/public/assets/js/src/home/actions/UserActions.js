var userAPI = require('../api/user');
var types = require('../constants/ActionTypes');
var DashboardActions = require('./DashboardActions');

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

        if (csrf) dispatch(setCsrf(csrf));

        if (success && profile.configuration) {
          const configuration = JSON.parse(profile.configuration);
          if (configuration.infoboxes) dispatch(DashboardActions.setInfoboxes(configuration.infoboxes));
          if (configuration.layout) dispatch(DashboardActions.updateLayout(configuration.layout));
        }
        dispatch(receivedLogin(success, errors.length?errors[0].code:null, profile));
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
        return response;
      })
      .catch((errors) => {
        console.error('User refresh failed with errors:', errors);
        return errors;
      });
    };
  },
  saveToProfile: function(configuration) {
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
