var types = require('../constants/ActionTypes');

var query = function (state, action) {
  //initial state
  if (state === undefined) {
    state = {
      isLoading: false,
      success: null,
      errors: null
    };
  }
   
  switch (action.type) {
    case types.QUERY_REQUEST_START:
    case types.MESSAGES_REQUEST_START:
    case types.MESSAGES_ACK_REQUEST_START:
      return Object.assign({}, state, {
        isLoading: true,
      });

    case types.QUERY_REQUEST_END:
    case types.MESSAGES_REQUEST_END:
    case types.MESSAGES_ACK_REQUEST_END:
      switch (action.success) {
        case true:
          return Object.assign({}, state, {
            isLoading: false,
            success: true,
            errors: null
          });

        case false:
          return Object.assign({}, state, {
            isLoading: false,
            success: false,
            errors: action.errors
          });
        }
        break;

    case types.QUERY_DISMISS_ERROR:
      return Object.assign({}, state, {
        errors: null
      });
                  
    default:
      return state;
  }
};

module.exports = query;

