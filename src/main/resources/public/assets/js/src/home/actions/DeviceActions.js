var	deviceAPI = require('../api/device');
var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();

var getSessionById = require('../utils/device').getSessionById;
var getLastSession = require('../utils/device').getLastSession;

var requestLastSession = function(id) {
	return {
		type: types.DEVICE_LAST_SESSION_REQUESTED,
		id: id
	};
};

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

var DeviceActions = {
	
	querySessions: function(deviceKey, time) {
		return function(dispatch, getState) {

			dispatch(requestedSessionsQuery());

			var data = Object.assign({}, time, {deviceKey: [ deviceKey ] });
			
			return deviceAPI.querySessions(data).then(
				function(response) {
					dispatch(receivedSessionsQuery(response.success, response.errors, response.devices?response.devices[0].sessions:[]) );
				},
				function(error) {
					dispatch(receivedSessionsQuery(false, error, {}));
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
	fetchSession: function(id, deviceKey, time) {
		return function(dispatch, getState) {

			var session = getSessionById(getState().device.sessions.data, id);
			//var session = getState().device.session.data;
			
			if (session !== undefined && session.measurements){
				console.log('found session in memory');
				return true;
				}
				
			dispatch(requestedSession(id));

			var data = Object.assign({}, time,  {sessionId:id, deviceKey: deviceKey});

			console.log('searching session..');
			return deviceAPI.getSession(data).then(
				function(response) {
					dispatch(receivedSession(response.success, response.errors, response.session, id));
				},
				function(error) {
					dispatch(receivedSession(false, error, {}));
				});
		};
	},

	fetchLastSession: function(deviceKey, time) {
		return function(dispatch, getState) {
			const session = getLastSession(getState().device.sessions.data);
			const id = session.id;
			if (!id){ return false;}

			if (getState().device.sessions.lastSession !== id) {
				dispatch(requestLastSession(id));
				return dispatch(DeviceActions.fetchSession(id, deviceKey, time));
			}
			else {
				return new Promise(function(resolve){
					resolve();
				});
			}
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
	setActiveDevice: function(deviceKey) {
		return {
			type: types.DEVICE_SET_ACTIVE,
			deviceKey: deviceKey
		};
	},
	setActiveDeviceIfNone: function(deviceKey) {
		return function(dispatch, getState) {
			if (getState().device.query.activeDevice) {
				return true;
			}
			else {
				dispatch(DeviceActions.setActiveDevice(deviceKey));
			}
		};
	},
	setQueryFilter: function(filter) {
		return {
			type: types.DEVICE_SET_FILTER,
			filter: filter
		};
	},
	setSessionFilter: function(filter) {
		return {
			type: types.DEVICE_SET_SESSION_FILTER,
			filter: filter
		};
	}

};

module.exports = DeviceActions;
