var types = require('../constants/ActionTypes');


var history = function (state, action) {
  //initial state
  if (state === undefined) {
    state = {
      filter: "volume",
      timeFilter: "week",
      sessionFilter: "volume",
      activeSessionIndex: null,
    };
  }
   
  switch (action.type) {
      case types.HISTORY_SET_FILTER:
        return Object.assign({}, state, {
          filter: action.filter
        });
      
      case types.HISTORY_SET_TIME_FILTER:
        return Object.assign({}, state, {
          timeFilter: action.filter
        });

      case types.HISTORY_SET_SESSION_FILTER:
        return Object.assign({}, state, {
          sessionFilter: action.filter
        });
      
      case types.HISTORY_SET_ACTIVE_SESSION_INDEX:
        const intId = parseInt(action.id);
        if (typeof(intId) !== "number") {
          throw new Error('can\'t set non-integer as index');
        }
        return Object.assign({}, state, {
          activeSessionIndex: intId
        });

      case types.HISTORY_RESET_ACTIVE_SESSION_INDEX:
        return Object.assign({}, state, {
          activeSessionIndex: null
        });

      case types.HISTORY_INCREASE_ACTIVE_SESSION_INDEX:
        if (state.activeSessionIndex===null) {
          return state;
        }

        return Object.assign({}, state, {
          activeSessionIndex: state.activeSessionIndex+1
        });
      
      case types.HISTORY_DECREASE_ACTIVE_SESSION_INDEX:
        if (state.activeSessionIndex===null) {
          return state;
        }
        return Object.assign({}, state, {
          activeSessionIndex: state.activeSessionIndex-1
        });

      default:
        return state;
  }
};

module.exports = history;

