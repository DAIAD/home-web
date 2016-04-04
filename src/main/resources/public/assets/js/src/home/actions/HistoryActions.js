var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();
var { push } = require('react-router-redux');

var { getSessionById, getDeviceTypeByKey } = require('../utils/device');

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
const setDataDirty = function () {
  return {
    type: types.HISTORY_SET_DATA_DIRTY
  };
};

const resetDataDirty = function () {
  return {
    type: types.HISTORY_RESET_DATA_DIRTY
  };
};

const HistoryActions = {
  
  linkToHistory: function(options) {
    return function(dispatch, getState) {
      const { device, metric, period, time, index } = options;
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

      dispatch(push('/history'));
    };
  },
  
  getDeviceSession: function (id, deviceKey, time) {
    return function(dispatch, getState) {
      
      const sessions = getState().section.history.data;
      const session = getSessionById(sessions, id); 

      if (session !== undefined && session.measurements){
        console.log('found session in memory');
        return new Promise((() => getState().section.history.data), (() => getState().query.errors));
      }
      console.log('fetching...');
      return dispatch(QueryActions.fetchDeviceSession(id, deviceKey, time))
        .then(session => { 
          dispatch(setSession(session));
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
      const sessions = getState().section.history.data;
      const activeSessionIndex = getState().section.history.activeSessionIndex;
      
      if (activeSessionIndex===null) { return false; }

      const activeSession = sessions[activeSessionIndex];
      console.log(activeSession);
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
  getDeviceSessions: function(deviceKey, time) {
    return function(dispatch, getState) {
      //if query not changed dont update (for now)
      //TODO: have to ask every now and then for new data
      if (!getState().section.history.dirty) {
        console.log('device data already in memory');
        return new Promise((() => getState().section.history.data), (() => getState().query.errors));
      }
      return dispatch(QueryActions.queryDeviceSessions(deviceKey, time))
        .then(sessions => {
          dispatch(resetDataDirty());
          dispatch(setSessions(sessions));
          return sessions;
        })
        .catch(error => {
          console.log('oops error while getting all sessions');
          console.log(error);
        });

    }; 
  },
  getMeterSessions: function (deviceKey, time) {
    return function(dispatch, getState) {
      //if query not changed dont update (for now)
      //TODO: have to ask every now and then for new data
      if (!getState().section.history.dirty) {
        console.log('meter data already in memory');
        return new Promise((() => getState().section.history.data), (() => getState().query.errors));
      }
      console.log('getting data', deviceKey, time);
      return dispatch(QueryActions.fetchMeterHistory(deviceKey, time))
        .then(x => {  console.log('before updating infobox', x); return x;})
        //  .then(sessions => sessions.length?sessions.map((x, i, array) => array[i-1]?Object.assign({}, array[i], {volume:(array[i].volume-array[i-1].volume)}):array[i]):sessions)
        .then(sessions => {
          dispatch(setSessions(sessions));
          dispatch(resetDataDirty());
          return sessions;
        })
        .catch(error => {
          console.log('oops error while getting all sessions');
          console.error(error);
        });
    };
  },
  getDeviceOrMeterSessions: function (deviceKey, time) {
    return function(dispatch, getState) {
      const devType = getDeviceTypeByKey(getState().user.profile.devices, deviceKey);
      if (devType === 'AMPHIRO') {
        return dispatch(HistoryActions.getDeviceSessions(deviceKey, time)); 
      }
      else if (devType === 'METER') {
        return dispatch(HistoryActions.getMeterSessions(deviceKey, time))
          .then(() => dispatch(HistoryActions.setQueryFilter('volume')));
      }
    };
  },
  // time is of type Object with
  //  startDate of type string (unix timestamp),
  //  endDate of type string (unix timestamp)
  //  granularity of type int (0-4)
  setTime: function(time) {
    return function(dispatch, getState) {
      if (!getState().section.history.dirty) { 
        dispatch(setDataDirty());
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
  setActiveDevice: function(deviceKey) {
    return function(dispatch, getState) {
      if (!getState().section.history.dirty) { 
        dispatch(setDataDirty());
      }
      return dispatch({
        type: types.HISTORY_SET_ACTIVE_DEVICE,
        deviceKey: deviceKey
      });
    };
  },
  setActiveDeviceAndQuery: function (deviceKey, time) {
    return function(dispatch, getState) {
      dispatch(HistoryActions.setActiveDevice(deviceKey));
      dispatch(HistoryActions.getDeviceOrMeterSessions(deviceKey, time));
    };
  },
  resetActiveDevice: function() {
    return function(dispatch, getState) {
      if (!getState().section.history.dirty) { 
        dispatch(setDataDirty());
      }
      return dispatch({
        type: types.HISTORY_RESET_ACTIVE_DEVICE,
      });
    };
  },
  resetQuery: function() {
    return function(dispatch, getState) {
      if (!getState().section.history.dirty) { 
        dispatch(setDataDirty());
      }
      return dispatch({
        type: types.HISTORY_RESET,
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
