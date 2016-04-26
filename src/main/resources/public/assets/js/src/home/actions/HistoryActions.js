var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();
var { push } = require('react-router-redux');
var { getSessionById, getReducedDeviceType, getDeviceTypeByKey } = require('../utils/device');
var { getPreviousPeriod, convertIntToGranularity } = require('../utils/time');

var QueryActions = require('./QueryActions');


const setSessions = function (sessions) {
  return {
    type: types.HISTORY_SET_SESSIONS,
    sessions
  };
};

const setComparisonSessions = function (sessions) {
  return {
    type: types.HISTORY_SET_COMPARISON_SESSIONS,
    sessions
  };
};

const setSession = function (session) {
  return {
    type: types.HISTORY_SET_SESSION,
    session,
  };
};

const setDataSynced = function () {
  return {
    type: types.HISTORY_SET_DATA_SYNCED
  };
};

const setDataUnsynced = function () {
  return {
    type: types.HISTORY_SET_DATA_UNSYNCED
  };
};

const HistoryActions = {
  
  linkToHistory: function(options) {
    return function(dispatch, getState) {
      const { id, showerId, device, metric, period, time, index, data } = options;
      
      console.log('linking to history', options);
      if (device) dispatch(HistoryActions.setActiveDevice(device, false));
      if (metric) dispatch(HistoryActions.setQueryFilter(metric));
      if (period) dispatch(HistoryActions.setTimeFilter(period));
      if (time) dispatch(HistoryActions.setTime(time, false));

      if (index != null && showerId != null) { 
        dispatch(HistoryActions.setSessionFilter(metric)); 
        dispatch(HistoryActions.setActiveSessionIndex(index)); 
      }
      else { 
        dispatch(HistoryActions.resetActiveSessionIndex()); 
      }
      
      if (data && data.length>0) { 
        dispatch(setSessions(data));
        dispatch(setDataSynced());
        }
 
      dispatch(push('/history'));
    };
  },
  
  getDeviceSession: function (id, deviceKey, time) {
    return function(dispatch, getState) {
      const devFound = getState().section.history.data.find(d=>d.deviceKey===deviceKey);
      const sessions = devFound?devFound.sessions:[];
      const found = getSessionById(sessions, id); 
      
      if (found && found.measurements){
        console.log('found session in memory');
        return new Promise((() => getState().section.history.data), (() => getState().query.errors));
      }
      return dispatch(QueryActions.fetchDeviceSession(id, deviceKey, time))
      .then(session => { 
          dispatch(setSession(Object.assign({}, session, {deviceKey})));
          return session;
        })
        .catch(error => {
          console.log('error fetching active sesssion');
          console.log(error);
        });
    };
  },
  getActiveSession: function(deviceKey, time) {
    return function(dispatch, getState) {
      const devices = getState().section.history.data;
      const activeSessionIndex = getState().section.history.activeSessionIndex;
      //TODO: have to find session index in device
      
      if (activeSessionIndex===null) { return false; }

      const foundDev = devices.find(s=>s.deviceKey===deviceKey);
      const activeSession = foundDev?foundDev.sessions[activeSessionIndex]:null;
      if (!activeSession) { return false; }
           
      const devType = getDeviceTypeByKey(getState().user.profile.devices, deviceKey);
      
      if (devType === 'AMPHIRO') {
        if (!activeSession.id) { return false; }
        return dispatch(HistoryActions.getDeviceSession(activeSession.id, deviceKey, time));
      }
      else {
        return true;
      }
    };
  },
  getDeviceSessions: function(deviceKeys, time) {
    return function(dispatch, getState) {
      //if query not changed dont update (for now)
      //TODO: have to ask every now and then for new data
      /*
      if (getState().section.history.synced) {
        console.log('device data already in memory', getState().section.history.data);
        return new Promise((() => getState().section.history.data), (() => getState().query.errors));
        }
        */
      return dispatch(QueryActions.queryDeviceSessions(deviceKeys, time))
      .then(sessions => {
        //dispatch(setDataSynced());
          //dispatch(setSessions(sessions));
          return sessions;
        });
    }; 
  },
  getMeterSessions: function (deviceKeys, time) {
    return function(dispatch, getState) {
      //if query not changed dont update (for now)
      //TODO: have to ask every now and then for new data
      /*
      if (getState().section.history.synced) {
        console.log('meter data already in memory');
        return new Promise((() => getState().section.history.data), (() => getState().query.errors));
        }
        */
      return dispatch(QueryActions.fetchMeterHistory(deviceKeys, time))
        .then(sessions => {
          //dispatch(setSessions(sessions));
          //dispatch(setDataSynced());
          return sessions;
        })
        .catch(error => {
          console.log('oops error while getting all sessions');
          console.error(error);
        });
    };
  },
  getDeviceOrMeterSessions: function (deviceKeys, time) {
    return function(dispatch, getState) {
      
      if (!deviceKeys || !deviceKeys.length) return new Promise((resolve, reject) => resolve()).then(() => { dispatch(setSessions([])); return [];});
      
      console.log('getting device or met sessions', deviceKeys);
      const devType = getReducedDeviceType(getState().user.profile.devices, deviceKeys);
      
      if (devType === 'AMPHIRO') {
        return dispatch(HistoryActions.getDeviceSessions(deviceKeys, time)); 
      }
      else if (devType === 'METER') {
        return dispatch(HistoryActions.getMeterSessions(deviceKeys, time));
        //.then(() => dispatch(HistoryActions.setQueryFilter('volume')));
      }
      else {
        throw new Error(`device of type ${devType} not supported`);
      }
    };
  },
  // time is of type Object with
  //  startDate of type string (unix timestamp),
  //  endDate of type string (unix timestamp)
  //  granularity of type int (0-4)
  setTime: function(time, query=true) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_SET_TIME,
        time
      });
      if (getState().section.history.synced && query) { 
        dispatch(setDataUnsynced());
        dispatch(HistoryActions.query());
      }
    };
  },
  query: function () {
    return function(dispatch, getState) {
      dispatch(HistoryActions.getDeviceOrMeterSessions(getState().section.history.activeDevice, getState().section.history.time))
        .then(sessions => dispatch(setSessions(sessions)))
        .then(sessions => dispatch(setDataSynced()))
        .catch(errors => { console.error('oops', errors); });

        if (getState().section.history.comparison === 'last') {
          dispatch(HistoryActions.getDeviceOrMeterSessions(getState().section.history.activeDevice, getPreviousPeriod(convertIntToGranularity(getState().section.history.time.granularity), getState().section.history.time.startDate)))
            .then(sessions => dispatch(setComparisonSessions(sessions)))
            .catch(errors => { console.error('oops', errors); });
        }
    };
  },
  setComparison: function(comparison) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_SET_COMPARISON,
        comparison
      });
      dispatch(HistoryActions.query());
    };
  },
  setActiveDevice: function(deviceKeys, query=true) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_SET_ACTIVE_DEVICE,
        deviceKey: deviceKeys
      });
      if (getState().section.history.synced && query) { 
        dispatch(setDataUnsynced());
        dispatch(HistoryActions.query());
      }
    };
  },
  addToActiveDevices: function(deviceKey, query=true) {
    return function(dispatch, getState) {
      
      let active = getState().section.history.activeDevice.slice();
      
      if (!active.includes(deviceKey)) {
        active.push(deviceKey);
        
        dispatch(HistoryActions.setActiveDevice(active));
        
        if (getState().section.history.synced && query) { 
          dispatch(setDataUnsynced());
          dispatch(HistoryActions.query());
        } 
      }
      
    };
  },
  removeFromActiveDevices: function(deviceKey, query=true) {
    return function(dispatch, getState) {
      
      let active = getState().section.history.activeDevice;
      if (active.includes(deviceKey)) {
        
        dispatch(HistoryActions.setActiveDevice(active.filter(x=>x!==deviceKey)));
        if (getState().section.history.synced && query) { 
          dispatch(setDataUnsynced());
          dispatch(HistoryActions.query());
        }
      }
      
    };
  }, 
  resetActiveDevice: function(query=true) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_RESET_ACTIVE_DEVICE,
      });
      
      if (getState().section.history.synced && query) { 
        dispatch(setDataUnsynced());
        dispatch(HistoryActions.query());
      }
    };
  },
  setActiveDeviceIfNone: function(deviceKey) {
    return function(dispatch, getState) {
      if (getState().query.activeDevice) {
        return true;
      }
      else {
        return dispatch(QueryActions.setActiveDevice(deviceKey));
      }
    };
  },
  setActiveSessionIndex: function(index, id, device) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_SET_ACTIVE_SESSION_INDEX,
        id: index
      });
      if (id != null && device != null) {
        dispatch(HistoryActions.getDeviceSession(id, device, getState().section.history.time));
      }
      //getDeviceSession: function (id, deviceKey, time) {
    };
  },
  resetActiveSessionIndex: function() {
    return {
      type: types.HISTORY_RESET_ACTIVE_SESSION_INDEX
    };
  },
  increaseActiveSessionIndex: function(id, device) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_INCREASE_ACTIVE_SESSION_INDEX
      });
      dispatch(HistoryActions.getDeviceSession(id, device, getState().section.history.time));
    };
  },
  decreaseActiveSessionIndex: function(id, device) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_DECREASE_ACTIVE_SESSION_INDEX
      });
      dispatch(HistoryActions.getDeviceSession(id, device, getState().section.history.time));

    };
  },
  setQueryFilter: function(filter) {
    return {
      type: types.HISTORY_SET_FILTER,
      filter: filter
    };
  },
  setTimeFilter: function(filter) {
    return {
      type: types.HISTORY_SET_TIME_FILTER,
      filter: filter
    };
  },
  setSessionFilter: function(filter) {
    return {
      type: types.HISTORY_SET_SESSION_FILTER,
      filter: filter
    };
  },
  
};

module.exports = HistoryActions;
