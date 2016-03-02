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

var requestedSessions = function() {
	return {
		type: types.DEVICE_REQUESTED_SESSIONS,
		};
};

var receivedSessions = function(success, errors, data) {
	return {
		type: types.DEVICE_RECEIVED_SESSIONS,
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

var setLastSession = function(id) {
	return {
		type: types.DEVICE_SET_LAST_SESSION,
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
			if (getState().device.query.data.length) {
				return new Promise(function(resolve){
					resolve();
				});
			}
			else {
				return dispatch(DeviceActions.querySessions(deviceKey, time));
			}
		};
	},
	/*
	fetchSessions: function(deviceKey, time) {
		return function(dispatch, getState) {

			dispatch(requestedSessions());

			var data = Object.assign({}, time, {deviceKey: [ deviceKey ] });
			return deviceAPI.querySessions(data).then(
				function(response) {
					dispatch(receivedSessions(response.success, response.errors, response.devices?response.devices[0].sessions:[]) );
				},
				function(error) {
					dispatch(receivedSessions(false, error, {}));
				});
		};
	},	
	fetchSessionsIfNeeded: function(deviceKey, time) {
		return function(dispatch, getState) {
			//no real check for new sessions
			//should implement sth
			if (getState().device.sessions.data.length>1) {
				return new Promise(function(resolve){
					resolve();
				});
			}
			else {
				return dispatch(DeviceActions.fetchSessions(deviceKey, time));
			}
		};
		},
	*/
	fetchSession: function(id, deviceKey, time) {
		return function(dispatch, getState) {
			//dispatch(DeviceActions.setActiveSession(id));

			var session = getSessionById(getState().device.query.data, id);
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
			const sessions = getState().device.query.data;
			const activeSessionIndex = getState().device.query.activeSessionIndex;
				if (getState().device.query.activeSessionIndex===null || !getState().device.query.data[activeSessionIndex] || !getState().device.query.data[activeSessionIndex].id) {
					return false;
				}
			
			const activeSessionId = getState().device.query.data[activeSessionIndex].id;
			return dispatch(DeviceActions.fetchSession(activeSessionId, deviceKey, time));
		};
	},
	fetchNextSession: function(deviceKey, time) {
		return function(dispatch, getState) {
			const sessions = getState().device.query.data;
			const nextSession = getState().device.query.activeSessionIndex; 

			if (!nextSession) { return false; }
			
			return dispatch(DeviceActions.fetchSession(nextSession, deviceKey, time));
		};
	},
	fetchPreviousSession: function(deviceKey, time) {
		return function(dispatch, getState) {
			const sessions = getState().device.query.data;
			const previousSession = getPreviousSession(sessions, id);

			if (!previousSession) { return false; }
			
			return dispatch(DeviceActions.fetchSession(previousSession, deviceKey, time));
		};
	},
	fetchLastSession: function(deviceKey, time) {
    return function(dispatch, getState) {
      const session = getLastSession(getState().device.query.data);
			const id = session.id;
			if (!id){ return false;}

		  dispatch(setLastSession(id));
        
     return dispatch(DeviceActions.fetchSession(id, deviceKey, time));
       
		};
  },
	fetchAllSessions: function(deviceKey, time) {
    return function(dispatch, getState) {
      console.log('fetching all sessions');
      //const session = getLastSession(getState().device.query.data);
      var sessions = getState().device.query.data;
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
			type: types.DEVICE_SET_TIME,
			time: time 
		};
	},
	setActiveSession: function(sessionId) {
		return {
			type: types.DEVICE_SET_ACTIVE_SESSION,
			id: sessionId
		};
	},
	setActiveSessionIndex: function(sessionIndex) {
		return {
			type: types.DEVICE_SET_ACTIVE_SESSION_INDEX,
			id: sessionIndex
		};
	},
	resetActiveSessionIndex: function() {
		return {
			type: types.DEVICE_RESET_ACTIVE_SESSION_INDEX
		};
	},
	increaseActiveSessionIndex: function() {
		return {
			type: types.DEVICE_INCREASE_ACTIVE_SESSION_INDEX
		};
	},
	decreaseActiveSessionIndex: function() {
		return {
			type: types.DEVICE_DECREASE_ACTIVE_SESSION_INDEX
		};
	},
	setActiveDevice: function(deviceKey) {
		return {
			type: types.DEVICE_SET_ACTIVE,
			deviceKey: deviceKey
		};
	},
	resetActiveDevice: function() {
		return {
			type: types.DEVICE_RESET_ACTIVE,
		};
	},
	setActiveDeviceIfNone: function(deviceKey) {
		return function(dispatch, getState) {
			if (getState().device.query.activeDevice) {
				return true;
			}
			else {
				return dispatch(DeviceActions.setActiveDevice(deviceKey));
			}
		};
	},
	setQueryFilter: function(filter) {
		return {
			type: types.DEVICE_SET_FILTER,
			filter: filter
		};
	},
	setTimeFilter: function(filter) {
		return {
			type: types.DEVICE_SET_TIME_FILTER,
			filter: filter
		};
	},
	setSessionFilter: function(filter) {
		return {
			type: types.DEVICE_SET_SESSION_FILTER,
			filter: filter
		};
	},



};

module.exports = DeviceActions;
