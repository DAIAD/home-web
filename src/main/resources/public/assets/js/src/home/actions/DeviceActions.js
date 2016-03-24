var deviceAPI = require('../api/device');
var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();

var getSessionById = require('../utils/device').getSessionById;
var getLastSession = require('../utils/device').getLastSession;
var getNextSession = require('../utils/device').getNextSession;
var getPreviousSession = require('../utils/device').getPreviousSession;


const requestedSessionsQuery = function() {
  return {
    type: types.DEVICE_REQUESTED_SESSION_SEARCH,
  };
};

const receivedSessionsQuery = function(success, errors, data) {
  return {
    type: types.DEVICE_RECEIVED_SESSION_SEARCH,
    success: success,
    errors: errors,
    data: data
  };
};

const requestedSession = function() {
  return {
    type: types.DEVICE_REQUESTED_SESSION,
  };
};

const receivedSession = function(success, errors, data, id) {
  return {
    type: types.DEVICE_RECEIVED_SESSION,
    success: success,
    errors: errors,
    data: data,
    id: id
  };
};

const DeviceActions = {
  
  querySessions: function(deviceKey, time) {
    return function(dispatch, getState) {
      
      dispatch(requestedSessionsQuery());

      const data = Object.assign({}, time, {deviceKey: [ deviceKey ] }, {csrf: getState().query.csrf});
      
      return deviceAPI.querySessions(data)
      .then((response) => {
          dispatch(receivedSessionsQuery(response.success, response.errors, response.devices?response.devices[0].sessions:[]) );
          return response;
        })
        .catch((errors) => {
          dispatch(receivedSessionsQuery(false, errors, {}));
          return errors;
        });
    };
  },
  fetchSession: function(id, deviceKey, time) {
    return function(dispatch, getState) {
      
      if (id===null || id===undefined) {
        return false;
      }
      
      const session = getSessionById(getState().query.data, id);
      if (session !== undefined && session.measurements){
        console.log('found session in memory');
        return true;
      }
      
      dispatch(requestedSession(id));

      const data = Object.assign({}, time,  {sessionId:id, deviceKey: deviceKey}, {csrf: getState().query.csrf});

      return deviceAPI.getSession(data)
        .then((response) => {
          dispatch(receivedSession(response.success, response.errors, response.session, id));
          return response;
        })
        .catch((errors) => {
          dispatch(receivedSession(false, errors, {}));
          return errors;
        });
    };
  },
  fetchActiveSession: function(deviceKey, time) {
    return function(dispatch, getState) {
      const sessions = getState().query.data;
      const activeSessionIndex = getState().section.history.activeSessionIndex;
        if (getState().section.history.activeSessionIndex===null || !getState().query.data[activeSessionIndex] || !getState().query.data[activeSessionIndex].id) {
          return false;
        }
      
      const activeSessionId = getState().query.data[activeSessionIndex].id;
      return dispatch(DeviceActions.fetchSession(activeSessionId, deviceKey, time));
    };
    },
  fetchLastSession: function(deviceKey, time) {
    return function(dispatch, getState) {
      const session = getLastSession(getState().query.data);
      const id = session.id;
      if (!id){ return false;}

     return dispatch(DeviceActions.fetchSession(id, deviceKey, time));
       
    };
  },
  fetchAllSessions: function(deviceKey, time) {
    return function(dispatch, getState) {
      //TODO: this function should not be used seriously
      console.log('fetching all sessions');
      
      const sessions = getState().query.data;
      console.log('sessions');
      console.log(sessions);
      sessions.forEach(function(session) {
        const id = session.id;
        if (!id){ return false;}
        
        return dispatch(DeviceActions.fetchSession(id, deviceKey, time));
      });
    };
  },
  setCsrf: function(csrf) {
    return {
      type: types.QUERY_SET_CSRF,
      csrf: csrf 
    };
  },
  // time is of type Object with
  // startDate, endDate, granularity}
  setTime: function(time) {
    return {
      type: types.QUERY_SET_TIME,
      time: time 
    };
  },
  setActiveDevice: function(deviceKey) {
    return {
      type: types.QUERY_SET_ACTIVE_DEVICE,
      deviceKey: deviceKey
    };
  },
  resetActiveDevice: function() {
    return {
      type: types.QUERY_RESET_ACTIVE_DEVICE,
    };
  },
  resetQuery: function() {
    return {
      type: types.QUERY_RESET,
    };
  },
  setActiveDeviceIfNone: function(deviceKey) {
    return function(dispatch, getState) {
      if (getState().query.activeDevice) {
        return true;
      }
      else {
        return dispatch(DeviceActions.setActiveDevice(deviceKey));
      }
    };
  },
};

module.exports = DeviceActions;
