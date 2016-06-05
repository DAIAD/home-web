var types = require('../constants/SchedulerActionTypes');

var createInitialeState = function() {
  return {
    isLoading : false,
    query : {
      execution : {
        index : 0,
        size : 10,
        startDate : null,
        endDate : null,
        exitCode : 'UNDEFINED',
        jobName : 'UNDEFINED'
      }
    },
    data : {
      jobs : {
        total : 0,
        index : 0,
        size : 10,
        items : null
      },
      executions : {
        total : 0,
        index : 0,
        size : 10,
        items : null
      }
    }
  };
};

var queryReducer = function(state, action) {
  switch (action.type) {

    case types.EXECUTION_CHANGE_INDEX:

      return Object.assign({}, state, {
        execution : Object.assign({}, state.execution, {
          index : (action.index < 0 ? 0 : action.index)
        })
      });

    case types.EXECUTION_FILTER_JOB_NAME:

      return Object.assign({}, state, {
        execution : Object.assign({}, state.execution, {
          jobName : action.jobName || '',
          index : 0
        })
      });

    case types.EXECUTION_FILTER_EXIT_CODE:

      return Object.assign({}, state, {
        execution : Object.assign({}, state.execution, {
          exitCode : action.exitCode || null,
          index : 0
        })
      });

    case types.EXECUTION_FILTER_CLEAR:

      return Object.assign({}, state, {
        execution : Object.assign({}, state.execution, {
          jobName : null,
          exitCode : null,
          index : 0
        })
      });

    default:
      return state || createInitialeState();
  }
};

var dataReducer = function(state, action) {
  switch (action.type) {
    case types.JOB_RESPONSE:
      if (action.success === true) {
        return Object.assign({}, state, {
          jobs : {
            total : (action.items ? action.items.length : 0),
            index : 0,
            size : (action.items ? action.items.length : 10),
            items : action.items || []
          }
        });
      } else {
        return Object.assign({}, state, {
          jobs : {
            total : 0,
            index : 0,
            size : 10,
            items : null
          }
        });
      }
      break;

    case types.EXECUTION_RESPONSE:
      if (action.success === true) {
        return Object.assign({}, state, {
          executions : {
            total : action.total,
            index : action.index,
            size : action.size,
            items : action.items
          }
        });
      } else {
        return Object.assign({}, state, {
          executions : {
            total : 0,
            index : 0,
            size : 10,
            items : []
          }
        });
      }
      break;

    default:
      return state || createInitialeState();
  }
};

var reducer = function(state, action) {
  switch (action.type) {

    case types.JOB_CHANGE_INDEX:
      // Client side data paging
      return Object.assign({}, state);

    case types.JOB_REQUEST:
      return Object.assign({}, state, {
        isLoading : true
      });

    case types.JOB_RESPONSE:
      
      return Object.assign({}, state, {
        isLoading : false,
        data : dataReducer(state.data, action)
      });

    case types.EXECUTION_CHANGE_INDEX:

      return Object.assign({}, state, {
        isLoading : true,
        query : queryReducer(state.query, action)
      });

    case types.EXECUTION_FILTER_JOB_NAME:

      return Object.assign({}, state, {
        query : queryReducer(state.query, action)
      });

    case types.EXECUTION_FILTER_EXIT_CODE:

      return Object.assign({}, state, {
        query : queryReducer(state.query, action)
      });

    case types.EXECUTION_FILTER_CLEAR:

      return Object.assign({}, state, {
        query : queryReducer(state.query, action)
      });

    case types.EXECUTION_REQUEST:
      return Object.assign({}, state, {
        isLoading : true
      });

    case types.EXECUTION_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
        data : dataReducer(state.data, action)
      });

    case types.USER_RECEIVED_LOGOUT:
      return createInitialeState();

    case types.JOB_ENABLE_REQUEST:
      return Object.assign({}, state, {
        isLoading : true,
      });
     
    case types.JOB_ENABLE_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
      });

    case types.JOB_DISABLE_REQUEST:
      return Object.assign({}, state, {
        isLoading : true,
      });
     
    case types.JOB_DISABLE_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
      });

    default:
      return state || createInitialeState();
  }
};

module.exports = reducer;
