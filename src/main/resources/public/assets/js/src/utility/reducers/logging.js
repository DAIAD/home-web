var types = require('../constants/LoggingActionTypes');

var createInitialeState = function() {
  return {
    isLoading : false,
    query : {
      index : 0,
      size : 10,
      startDate : null,
      endDate : null,
      level : null,
      account : null
    },
    data : {
      total : 0,
      index : 0,
      size : 10,
      events : null
    },
    timeout: null
  };
};

var logging = function(state, action) {
  switch (action.type) {
    case types.LOG_EVENT_CHANGE_INDEX:

      return Object.assign({}, state, {
        isLoading : true,
        query : Object.assign({}, state.query, {
          index : (action.index < 0 ? 0 : action.index)
        })
      });

    case types.LOG_EVENT_FILTER_ACCOUNT:

      return Object.assign({}, state, {
        query : Object.assign({}, state.query, {
          account : action.account || ''
        })
      });

    case types.LOG_EVENT_FILTER_LEVEL:

      return Object.assign({}, state, {
        query : Object.assign({}, state.query, {
          level : action.level || null
        })
      });

    case types.LOG_EVENT_FILTER_CLEAR:

      return Object.assign({}, state, {
        query : Object.assign({}, state.query, {
          account : null,
          level : null
        })
      });

    case types.LOG_EVENT_REQUEST_INIT:
      if(state.timeout) {
        clearTimeout(timeout);
      }
      
      return Object.assign({}, state, {
        isLoading : true,
        timeout: null
      });

    case types.LOG_EVENT_REQUEST_COMPLETE:
      if (action.success === true) {
        action.events.forEach(function(e) {
          if (!e.account) {
            e.account = '-';
          }
        });
        return Object.assign({}, state, {
          isLoading : false,
          data : {
            total : action.total || 0,
            index : action.index || 0,
            size : action.size || 10,
            events : action.events || []
          }
        });
      } else {
        return Object.assign({}, state, {
          isLoading : false,
          data : {
            total : 0,
            index : 0,
            size : 10,
            events : []
          }
        });
      }
      break;

    case types.USER_RECEIVED_LOGOUT:
      return createInitialeState();

    default:
      return state || createInitialeState();
  }
};

module.exports = logging;
