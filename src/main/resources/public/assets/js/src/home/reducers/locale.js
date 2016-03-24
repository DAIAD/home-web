var types = require('../constants/ActionTypes');
var locales = function (state, action) {
  if (state === undefined) state = {};
 
  switch (action.type) {
    case types.LOCALE_REQUEST_MESSAGES:
      return Object.assign({}, state, {
        status: {
          isLoading: true
        }
      });

    case types.LOCALE_RECEIVED_MESSAGES:
      switch (action.success) {
        case true:
          return Object.assign({}, state, {
            status: {
              success: true,
              errors: null,
              isLoading: false
            },
            messages: action.messages,
            locale: action.locale
          });

        case false:
          return Object.assign({}, state, {
            status: {
              success: false,
              errors: action.errors,
              isLoading: false,
            },
          });
      }
      break;

    default:
      return state;
  }
};

module.exports = locales;

