var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();

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

  queryDevice: function(deviceKey, time) {
    return function(dispatch, getState) {
      //if query not changed dont update (for now)
      //TODO: have to ask every now and then for new data
      if (!getState().section.history.dirty) {
        console.log('device data already in memory');
        return true;
      }
      return dispatch(QueryActions.queryDeviceSessions(deviceKey, time))
        .then(response => {
          if (!response.devices.length || !response.devices[0].sessions.length) { return false; }
          
          dispatch(setSessions(response.devices[0].sessions));
          dispatch(resetDataDirty());
          return response;
        })
        .catch(error => {
          console.log('oops error while getting all sessions');
          console.log(error);
        });

    }; 
  },
  getDeviceSession: function (id, deviceKey, time) {
    return function(dispatch, getState) {
      
      const sessions = getState().section.history.data;
      const session = getSessionById(sessions, id); 

      if (session !== undefined && session.measurements){
        console.log('found session in memory');
        return true;
      }
      console.log('fetching...');
      return dispatch(QueryActions.fetchDeviceSession(id, deviceKey, time))
        .then(response => { 
          if (!response.session) { return false; }
          
          return dispatch(setSession(response.session));
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
      
      if (!activeSessionIndex) { return false; }

      const activeSession = sessions[activeSessionIndex];

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
  /*
  queryDeviceAndFetchAllSessions: function(deviceKey, time) {
    return function(dispatch, getState) {
      dispatch(HistoryActions.queryDevice(deviceKey, time))
      .then(
      //TODO: this function should not be used seriously
      console.log('fetching all sessions');
      
      const sessions = getState().section.history.data;
      console.log('sessions');
      console.log(sessions);
      sessions.forEach(function(session) {
        const id = session.id;
        if (!id) { return false; }
        
        return dispatch(HistoryActions.getDeviceSession(id, deviceKey, time));
       });
    };
    },
    */
  queryMeter: function (deviceKey, time) {
    return function(dispatch, getState) {
      //if query not changed dont update (for now)
      //TODO: have to ask every now and then for new data
      if (!getState().section.history.dirty) {
        console.log('meter data already in memory');
        return true;
      }
      return dispatch(QueryActions.fetchMeterHistory(deviceKey, time))
      .then(response => {
        console.log('got history');
        console.log(response);
          if (!response.series.length || !response.series[0].values) { return false; }
          
          dispatch(setSessions(response.series[0].values));
          dispatch(resetDataDirty());
          return response;
        })
        .catch(error => {
          console.log('oops error while getting all sessions');
          console.log(error);
        });

    };
  },
  queryDeviceOrMeter: function (deviceKey, time) {
    console.log('query device or meter');
    return function(dispatch, getState) {
      const devType = getDeviceTypeByKey(getState().user.profile.devices, deviceKey);
      console.log('devtype');
      console.log(devType);
      if (devType === 'AMPHIRO') {
        return dispatch(HistoryActions.queryDevice(deviceKey, time)); 
      }
      else if (devType === 'METER') {
        return dispatch(HistoryActions.queryMeter(deviceKey, time))
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
      dispatch(HistoryActions.queryDeviceOrMeter(deviceKey, time));
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
      dispatch(HistoryActions.queryDeviceOrMeter(deviceKey, time));
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
