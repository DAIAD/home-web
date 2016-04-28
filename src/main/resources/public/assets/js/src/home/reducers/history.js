var types = require('../constants/ActionTypes');

var { updateOrAppendToSession } = require('../utils/device');


var history = function (state, action) {
  //initial state
  if (state === undefined) {
    state = {
      filter: "difference",
      timeFilter: "year",
      activeDevice: [],
      activeDeviceType: "METER",
      activeSessionFilter: "volume",
      activeSessionIndex: null,
      synced: false,
      comparison: null,
      data: [],
      comparisonData: [],
      time: {}
    };
  }
   
  switch (action.type) {
    
    case types.HISTORY_SET_SESSIONS:
      return Object.assign({}, state, {
        data: action.sessions
      });
    
    case types.HISTORY_SET_COMPARISON_SESSIONS:
      return Object.assign({}, state, {
        comparisonData: action.sessions
      });

    case types.HISTORY_SET_SESSION:
      const updated = updateOrAppendToSession(state.data, action.session, action.session.id);
      return Object.assign({}, state, {
        data: updated
      });

    case types.HISTORY_SET_DATA_SYNCED:
      return Object.assign({}, state, {
        synced: true
      });
    
    case types.HISTORY_SET_DATA_UNSYNCED:
      return Object.assign({}, state, {
        synced: false
      });

    case types.HISTORY_SET_TIME:
      return Object.assign({}, state, {
        time: Object.assign({}, state.time, action.time)
      });

    case types.HISTORY_SET_ACTIVE_DEVICE:
      return Object.assign({}, state, {
        activeDevice: action.deviceKey
      });

    case types.HISTORY_SET_ACTIVE_DEVICE_TYPE:
      return Object.assign({}, state, {
        activeDeviceType: action.deviceType
      });
   
    case types.HISTORY_RESET_ACTIVE_DEVICE:
      return Object.assign({}, state, {
        activeDevice: null
      });
    
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
        activeSessionFilter: action.filter
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
    
    case types.HISTORY_SET_COMPARISON:
      return Object.assign({}, state, {
        comparison: action.comparison
      });

    default:
      return state;
  }
};

module.exports = history;

