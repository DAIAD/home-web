var	deviceAPI = require('../api/device');
var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();

var getSessionById = require('../utils/device').getSessionById;
var getLastSession = require('../utils/device').getLastSession;
var getNextSession = require('../utils/device').getNextSession;
var getPreviousSession = require('../utils/device').getPreviousSession;


var requestedSessionsQuery = function() {
	return {
		type: types.DEVICE_REQUESTED_SESSION_SEARCH,
	};
};

var receivedSessionsQuery = function(success, errors, data) {
	return {
		type: types.DEVICE_RECEIVED_SESSION_SEARCH,
		success: success,
		errors: errors,
		data: data
	};
};

var requestedSession = function() {
	return {
		type: types.DEVICE_REQUESTED_SESSION,
	};
};

var receivedSession = function(success, errors, data, id) {
	return {
		type: types.DEVICE_RECEIVED_SESSION,
		success: success,
		errors: errors,
		data: data,
		id: id
	};
};

var DeviceActions = {
	
	querySessions: function(deviceKey, time) {
		return function(dispatch, getState) {

			dispatch(requestedSessionsQuery());

      var data = Object.assign({}, time, {deviceKey: [ deviceKey ] });
			return deviceAPI.querySessions(data).then(
        function(response) {
					dispatch(receivedSessionsQuery(response.success, response.errors, response.devices?response.devices[0].sessions:[]) );
					return response;
				},
        function(error) {
					dispatch(receivedSessionsQuery(false, error, {}));
					return error;
				});
		};
	},
	querySessionsIfEmpty: function(deviceKey, time) {
		return function(dispatch, getState) {
			//just check if empty
			if (getState().query.data.length) {
				return new Promise(function(resolve){
					resolve();
				});
			}
			else {
				return dispatch(DeviceActions.querySessions(deviceKey, time));
			}
		};
	},
	fetchSession: function(id, deviceKey, time) {
		return function(dispatch, getState) {
			//dispatch(DeviceActions.setActiveSession(id));

			var session = getSessionById(getState().query.data, id);
			//var session = getState().device.session.data;
			if (session !== undefined && session.measurements){
				console.log('found session in memory');
				return true;
			}
			if (id===null || id===undefined) {
				return false;
			}
			dispatch(requestedSession(id));

			var data = Object.assign({}, time,  {sessionId:id, deviceKey: deviceKey});

			return deviceAPI.getSession(data).then(
				function(response) {
					dispatch(receivedSession(response.success, response.errors, response.session, id));
					return response;
				},
				function(error) {
					dispatch(receivedSession(false, error, {}));
					return error;
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

      //dispatch(setLastSession(id));
     return dispatch(DeviceActions.fetchSession(id, deviceKey, time));
       
		};
  },
	fetchAllSessions: function(deviceKey, time) {
    return function(dispatch, getState) {
      console.log('fetching all sessions');
      //const session = getLastSession(getState().device.query.data);
      var sessions = getState().query.data;
      console.log('sessions');
      console.log(sessions);
      sessions.forEach(function(session) {
        const id = session.id;
        if (!id){ return false;}
        
        return dispatch(DeviceActions.fetchSession(id, deviceKey, time));
      });
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
			type: types.QUERY_SET_ACTIVE,
			deviceKey: deviceKey
		};
	},
	resetActiveDevice: function() {
		return {
			type: types.QUERY_RESET_ACTIVE,
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
