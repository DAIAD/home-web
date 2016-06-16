var types = require('../constants/ActionTypes');

const initialState = {
  isLoading: false,
  success: null,
  errors: null,
  cache: {}
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


    case types.QUERY_SET_CACHE: {
      return Object.assign({}, state, {
        cache: action.cache
      });
    }

    case types.QUERY_SAVE_TO_CACHE: {
      let newCache = Object.assign({}, state.cache);
      newCache[action.key] = {
        data: action.data,
        counter: 1
      };
      return Object.assign({}, state, {
        cache:  newCache
      });
    }

    case types.QUERY_CACHE_ITEM_REQUESTED: {
      let newCache = Object.assign({}, state.cache);
      newCache[action.key] = Object.assign({}, newCache[action.key], {counter: newCache[action.key].counter+1});
      return Object.assign({}, state, {
        cache: newCache
      });
    }

    default:
      return state;
  }
};

module.exports = query;

