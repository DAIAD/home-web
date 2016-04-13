var debugAPI = require('../api/debug');
var types = require('../constants/ActionTypes');

var createUser = function() {
  return {
    type : types.DEBUG_CREATE_USER
  };
};

var userCreated = function(success, errors) {
  return {
    type : types.DEBUG_USER_CREATED,
    success : success,
    errors : errors
  };
};

var createAmphiro = function() {
  return {
    type : types.DEBUG_CREATE_AMPHIRO
  };
};

var amphiroCreated = function(success, errors) {
  return {
    type : types.DEBUG_AMPHIRO_CREATED,
    success : success,
    errors : errors
  };
};

var DebugActions = {
  createUser : function(password) {
    return function(dispatch, getState) {
      dispatch(createUser());

      return debugAPI.createUser(password).then(function(response) {
        dispatch(userCreated(response.success, response.errors));
      }, function(error) {
        dispatch(userCreated(false, error));
      });
    };
  },
  createAmphiro : function() {
    return function(dispatch, getState) {
      dispatch(createAmphiro());

      return debugAPI.createAmphiro().then(function(response) {
        dispatch(amphiroCreated(response.success, response.errors));
      }, function(error) {
        dispatch(amphiroCreated(false, error));
      });
    };
  }
};

module.exports = DebugActions;
