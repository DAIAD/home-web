var events = require('events');
var assign = require('object-assign');

var $ = require('jquery');

var AppDispatcher = require('../dispatcher/AppDispatcher');

var HomeConstants = require('../constants/HomeConstants');

var DEVICE_SESSION_SEARCH = 'DEVICE_SESSION_SEARCH';
var	DEVICE_SESSION_GET = 'DEVICE_SESION_GET';
var	DEVICE_MEASUREMENT_SEARCH = 'DEVICE_MEASUREMENT_SEARCH';

var _sessions = [];
var _measurements = [];


function updateCsrfToken(crsf) {
	$('meta[name=_csrf]').attr('content', crsf);
	$('input[name=_csrf]').val(crsf);
}

function deviceSessionSearch(data) {
		data._csrf = $('meta[name=_csrf]').attr('content');

		var request = {
			type : "POST",
			url : '/action/device/session/query',
			contentType: "application/json;",
			data: JSON.stringify(data),
			beforeSend : function(xhr) {
				xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
						'content'));
			},
		};
		$.ajax(request).done(function(data, textStatus, request) {
			updateCsrfToken(request.getResponseHeader('X-CSRF-TOKEN'));
			if(data.success) {
				_sessions = data.devices[0].sessions;
				
			} else {
				_sessions = [];
				
			}

			DeviceStore.emitSessionsFetched();
		}).fail(function(jqXHR, textStatus, errorThrown) {
			updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));
			
			switch (jqXHR.status) {
			case 403:
				break;
			}
			
			_sessions = [];
		
			DeviceStore.emitSessionsFetched();
		});	
}
function deviceSessionMeasurementsFetch(data) {
		data._csrf = $('meta[name=_csrf]').attr('content');

		var request = {
			type : "POST",
			url : '/action/device/session',
			contentType: "application/json;",
			data: JSON.stringify(data),
			beforeSend : function(xhr) {
				xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
						'content'));
			},
		};
		$.ajax(request).done(function(data, textStatus, request) {
			updateCsrfToken(request.getResponseHeader('X-CSRF-TOKEN'));
			if(data.success) {
				
				_measurements = data.session.measurements;
				
			} else {
				_measurements = [];
				
			}

			DeviceStore.emitSessionMeasurementsFetched();
		}).fail(function(jqXHR, textStatus, errorThrown) {
			updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));
			
			switch (jqXHR.status) {
			case 403:
				break;
			}
			
			_measurements = [];
		
			DeviceStore.emitSessionMeasurementsFetched();
		});	
}

var DeviceStore = assign({}, events.EventEmitter.prototype, {

	getSessionMeasurements: function() {
		return _measurements;
	},
	getSessions: function() {
		console.log(_sessions);
		return _sessions;
	},
	getSessionsShowers: function() {
		var showers = [];
		_sessions.forEach(function(session) {
			var count;
			if (session.count) {
				count = session.count;
			}
			else{
				count = 1;
			}
			showers.push([
				new Date(session.timestamp),
				count
			]);
		});
		return showers;
	},
	getSessionsDuration: function() {
		return this.getSessionsMetric('duration');
	},
	getSessionsVolume: function() {
		return this.getSessionsMetric('volume');
	},
	getSessionsTemperature: function() {
		return this.getSessionsMetric('temperature');
	},
	getSessionsEnergy: function() {
		return this.getSessionsMetric('energy');
	},
	getSessionsFlow: function() {
		return this.getSessionsMetric('flow');
	},


	getSessionsMetric: function(metric) {
		if (metric === 'showers'){
			return this.getSessionsShowers();
		}
		var output = [];
		_sessions.forEach(function(session) {
			if (!session[metric]){
				return;
			}
			output.push([
				new Date(session.timestamp),
				session[metric]
			]);
		});
		return output;
	},


	addSessionSearchListener : function(callback) {
		this.on(DEVICE_SESSION_SEARCH, callback);
	},

	removeSessionSearchListener : function(callback) {
		this.removeListener(DEVICE_SESSION_SEARCH, callback);
	},

	emitSessionsFetched: function(args) {
		this.emit(DEVICE_SESSION_SEARCH, args);
	},

	addSessionGetListener: function(callback) {
		this.on(DEVICE_SESSION_GET, callback);
	},
	
	removeSessionGetListener: function(callback) {
		this.removeListener(DEVICE_SESSION_GET, callback);
	},

	emitSessionMeasurementsFetched: function(args) {
		this.emit(DEVICE_SESSION_GET, args);
	},
});

AppDispatcher.register(function(message) {
	var action = message.action;
	var data = message.data;
	switch (action) {
		case HomeConstants.DEVICE_SESSION_SEARCH:
			deviceSessionSearch(data);
			break;
		case HomeConstants.DEVICE_SESSION_GET:
			deviceSessionMeasurementsFetch(data);
			break;

		default:
			break;
		}
});

module.exports = DeviceStore;
