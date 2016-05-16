var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();
var { push } = require('react-router-redux');

var { getSessionById, getReducedDeviceType, getDeviceTypeByKey } = require('../utils/device');

var QueryActions = require('./QueryActions');


const setSessions = function (sessions) {
  return {
    type: types.HISTORY_SET_SESSIONS,
    sessions: sessions
  };
};

const setSession = function (session) {
  return {
    type: types.HISTORY_SET_SESSION,
    session: session,
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
      const { id, device, metric, period, time, index, data } = options;
      
      if ((index !== null && index !==undefined)) { 
        dispatch(HistoryActions.setSessionFilter(metric)); 
        dispatch(HistoryActions.setActiveSessionIndex(index)); 
      }
      else { 
        dispatch(HistoryActions.resetActiveSessionIndex()); 
      }

      if (device) dispatch(HistoryActions.setActiveDevice(device));
      if (metric) dispatch(HistoryActions.setQueryFilter(metric));
      if (period) dispatch(HistoryActions.setTimeFilter(period));
      if (time) dispatch(HistoryActions.setTime(time));
      
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
      if (getState().section.history.synced) {
        console.log('device data already in memory', getState().section.history.data);
        return new Promise((() => getState().section.history.data), (() => getState().query.errors));
      }
      return dispatch(QueryActions.queryDeviceSessions(deviceKeys, time))
      .then(sessions => {
          dispatch(setDataSynced());
          dispatch(setSessions(sessions));
          return sessions;
        });
    }; 
  },
  getMeterSessions: function (deviceKeys, time) {
    return function(dispatch, getState) {
      //if query not changed dont update (for now)
      //TODO: have to ask every now and then for new data
      if (getState().section.history.synced) {
        console.log('meter data already in memory');
        return new Promise((() => getState().section.history.data), (() => getState().query.errors));
      }
      return dispatch(QueryActions.fetchMeterHistory(deviceKeys, time))
        .then(sessions => {
          dispatch(setSessions(sessions));
          dispatch(setDataSynced());
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
      //if (!Array.isArray(deviceKey)) throw new Error(`deviceKey ${deviceKey} must be of type array`);

      if (!deviceKeys || !deviceKeys.length) return new Promise((resolve, reject) => resolve()).then(() => dispatch(setSessions([])));
      
      console.log('getting device or met sessions', deviceKeys);
      const devType = getReducedDeviceType(getState().user.profile.devices, deviceKeys);
      
      if (devType === 'AMPHIRO') {
        return dispatch(HistoryActions.getDeviceSessions(deviceKeys, time)); 
      }
      else if (devType === 'METER') {
        return dispatch(HistoryActions.getMeterSessions(deviceKeys, time))
          .then(() => dispatch(HistoryActions.setQueryFilter('volume')));
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
  setTime: function(time) {
    return function(dispatch, getState) {
      if (getState().section.history.synced) { 
        dispatch(setDataUnsynced());
      }
      return dispatch({
        type: types.HISTORY_SET_TIME,
        time: time 
      });
    };
  },
  setTimeAndQuery: function (deviceKey, time) {
    return function(dispatch, getState) {
      dispatch(HistoryActions.setTime(time));
      dispatch(HistoryActions.getDeviceOrMeterSessions(deviceKey, time));
    };
  },
  setActiveDevice: function(deviceKeys) {
    return function(dispatch, getState) {
      if (getState().section.history.synced) { 
        dispatch(setDataUnsynced());
      }
      return dispatch({
        type: types.HISTORY_SET_ACTIVE_DEVICE,
        deviceKey: deviceKeys
      });
    };
  },
  addToActiveDevices: function(deviceKey, time) {
    return function(dispatch, getState) {
      
      let active = getState().section.history.activeDevice.slice();
      
      if (!active.includes(deviceKey)) {
        active.push(deviceKey);
        if (getState().section.history.synced) { 
          dispatch(setDataUnsynced());
        } 
        dispatch(HistoryActions.setActiveDeviceAndQuery(active, time));
      }
      
    };
  },
  removeFromActiveDevices: function(deviceKey, time) {
    return function(dispatch, getState) {
      
      let active = getState().section.history.activeDevice;
      if (active.includes(deviceKey)) {
        if (getState().section.history.synced) { 
          dispatch(setDataUnsynced());
        }
        dispatch(HistoryActions.setActiveDeviceAndQuery(active.filter(x=>x!==deviceKey), time));
      }
      
    };
  },
  setActiveDeviceAndQuery: function (deviceKeys, time) {
    return function(dispatch, getState) {
      dispatch(HistoryActions.setActiveDevice(deviceKeys));
      dispatch(HistoryActions.getDeviceOrMeterSessions(deviceKeys, time));
    };
  },
  resetActiveDevice: function() {
    return function(dispatch, getState) {
      if (getState().section.history.synced) { 
        dispatch(setDataUnsynced());
      }
      return dispatch({
        type: types.HISTORY_RESET_ACTIVE_DEVICE,
      });
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
  setActiveSessionIndex: function(sessionIndex) {
    return {
      type: types.HISTORY_SET_ACTIVE_SESSION_INDEX,
      id: sessionIndex
    };
  },
  resetActiveSessionIndex: function() {
    return {
      type: types.HISTORY_RESET_ACTIVE_SESSION_INDEX
    };
  },
  increaseActiveSessionIndex: function() {
    return {
      type: types.HISTORY_INCREASE_ACTIVE_SESSION_INDEX
    };
  },
  decreaseActiveSessionIndex: function() {
    return {
      type: types.HISTORY_DECREASE_ACTIVE_SESSION_INDEX
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
