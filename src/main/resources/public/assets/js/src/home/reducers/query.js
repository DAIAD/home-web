var types = require('../constants/ActionTypes');

const initialState = {
  isLoading: false,
  success: null,
  errors: null
};

var query = function (state, action) {
  //initial state
  if (state === undefined) {
    state = initialState;
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
            //errors: null
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
                  
    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, initialState);

    default:
      return state;
  }
};

module.exports = query;

