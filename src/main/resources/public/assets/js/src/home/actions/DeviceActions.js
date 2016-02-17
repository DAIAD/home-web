var	deviceAPI = require('../api/device');
var types = require('../constants/ActionTypes');

var getSessionById = require('../utils/device').getSessionById;

var DeviceActions = {

	_requestedSessions: function() {
		return {
			type: types.DEVICE_REQUESTED_SESSION_SEARCH,
			};
	},

	_receivedSessions: function(success, errors, data) {
		return {
			type: types.DEVICE_RECEIVED_SESSION_SEARCH,
			success: success,
			errors: errors,
			data: data
		};
	},
	searchSessions: function() {
		return function(dispatch, getState) {

			if (!getState().device.sessions.activeDevice)
				return false;

			dispatch(DeviceActions._requestedSessions());

			var data = Object.assign({}, getState().device.sessions.time, {deviceKey: [ getState().device.sessions.activeDevice ] });
			deviceAPI.searchSessions(data, function(response) {
				dispatch(DeviceActions._receivedSessions(response.success, response.errors, response.devices?response.devices[0].sessions:[]) );
			},
			function(error) {
				dispatch(DeviceActions._receivedSessions(false, error, {}));
			});
		};
	},
	_requestedSession: function() {
		return {
			type: types.DEVICE_REQUESTED_SESSION,
		};
	},
	_receivedSession: function(success, errors, data, id) {
		return {
			type: types.DEVICE_RECEIVED_SESSION,
			success: success,
			errors: errors,
			data: data,
			sessionId: id
		};
	},
	fetchSession: function(id) {
		return function(dispatch, getState) {

			//check if its already loaded
			if (getState().device.lastSession.data.id && getState().device.lastSession.data.id.toString() === id.toString()){
				return true;	
			}	
			dispatch(DeviceActions._requestedSession(id));
				var data = Object.assign({}, {sessionId:id}, getState().device.sessions.time, { deviceKey: getState().device.sessions.activeDevice });

			//var session = getSessionById(getState().device.data, id);
			var session = getState().device.lastSession.data;
			/*
			if (session !== undefined && session.measurements){
				console.log('found session in memory');
				dispatch(DeviceActions._receivedSession(true, null, session, id));
				return true;
				}
				*/

			console.log('searching session with data..');
			console.log(data);
			deviceAPI.getSession(data, function(response) {
				console.log('found!');
				console.log(response);
				
				dispatch(DeviceActions._receivedSession(response.success, response.errors, response.session, id));
			},
			function(error) {
				dispatch(DeviceActions._receivedSession(false, error, {}));
			});
		};
	},
	// time is of type Object with
	// startDate, endDate, granularity}
	setTime: function(time) {
		return function(dispatch, getState) {
			dispatch(DeviceActions._setTime(time));
			dispatch(DeviceActions.searchSessions());
		};
	},
	_setTime: function(time) {
		return {
			type: types.DEVICE_SET_TIME,
			time: time 
		};
	},
	setActiveDevice: function(deviceKey) {
		return function(dispatch, getState) {
			dispatch(DeviceActions._setActiveDevice(deviceKey));
			dispatch(DeviceActions.searchSessions());
		};
	},
	_setActiveDevice: function(deviceKey) {
		return {
			type: types.DEVICE_SET_ACTIVE,
			deviceKey: deviceKey
		};
	},
	setFilter: function(filter) {
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
