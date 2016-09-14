var types = require('../constants/ActionTypes');
var passwordAPI = require('../api/password');

var initialState = {
  isLoading : false,
  errors : null,
  reset : {
    isCaptchaValid : false
  },
  success: false
};

var password = function(state, action) {
  switch (action.type) {
    case types.PASSWORD_RESET_REQUEST:
      return Object.assign({}, state, {
        isLoading : true,
        errors : null
      });

    case types.PASSWORD_RESET_RESPONSE:
      if (action.success) {
        return Object.assign({}, state, {
          isLoading : false,
          errors : null,
          success: true
        });
      }

      return Object.assign({}, state, {
        isLoading : false,
        errors : action.errors,
        success: false
      });

    case types.PASSWORD_CAPTCHA_SET_VALID:
      return Object.assign({}, state, {
        reset : {
          isCaptchaValid : action.value
        }
      });

    case types.PASSWORD_RESET_CLEAR_ERRORS:
      return Object.assign({}, state, {
        errors : null
      });
      
    case types.PASSWORD_RESET_SET_ERRORS:
      console.log(action);
      return Object.assign({}, state, {
        errors : action.errors
      });
      
    default:
      return state || initialState;
  }
};

module.exports = password;
