var types = require('../constants/ActionTypes');

var { updateOrAppendToSession } = require('../utils/transformations');
var { thisYear } = require('../utils/time');

const initialState = {
  filter: "difference",
  timeFilter: "year",
  sortFilter: "timestamp",
  sortOrder: "desc",
  activeDevice: [],
  activeDeviceType: "METER",
  activeSessionFilter: "volume",
  activeSession: null,
  synced: false,
  comparison: null,
  data: [],
  comparisonData: [],
  time: thisYear()
};
 
var history = function (state, action) {
  //initial state
  if (state === undefined) {
    state = initialState;
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

    
    case types.HISTORY_SET_ACTIVE_SESSION:
      return Object.assign({}, state, {
        activeSession: [action.device, action.id]
      });

    case types.HISTORY_RESET_ACTIVE_SESSION:
      return Object.assign({}, state, {
        activeSession: null
      });

    case types.HISTORY_SET_COMPARISON:
      return Object.assign({}, state, {
        comparison: action.comparison
      });
    
    case types.HISTORY_SET_SORT_FILTER:
      return Object.assign({}, state, {
        sortFilter: action.filter
      });
    
    case types.HISTORY_SET_SORT_ORDER:
      return Object.assign({}, state, {
        sortOrder: action.order
      });

    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, initialState);

    default:
      return state;
  }
};

module.exports = history;

