var passwordAPI = require('../api/password');
var types = require('../constants/ActionTypes');

var resetPasswordRequest = function(token, password) {
  return {
    type : types.PASSWORD_RESET_REQUEST
  };
};

var resetPasswordResponse = function(success, errors) {
  return {
    type : types.PASSWORD_RESET_RESPONSE,
    success : success,
    errors : errors
  };
};

var PasswordActions = {
  reset : function(token, password) {
    return function(dispatch, getState) {
      dispatch(resetPasswordRequest());

      return passwordAPI.reset(token, password).then(function(response) {
        dispatch(resetPasswordResponse(response.success, response.errors));
      }, function(error) {
        dispatch(resetPasswordResponse(false, error));
      });
    };
  },

  setCaptchaValid : function(value) {
    return {
      type : types.PASSWORD_CAPTCHA_SET_VALID,
      value : value
    };
  },

  setErrors : function(errors) {
    return {
      type : types.PASSWORD_RESET_SET_ERRORS,
      errors : (errors ? (errors.length === 0 ? null : errors) : null)
    };
  },

  clearErrors : function() {
    return {
      type : types.PASSWORD_RESET_CLEAR_ERRORS
    };
  }

};
module.exports = PasswordActions;
