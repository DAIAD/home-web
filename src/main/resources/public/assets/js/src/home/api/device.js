var callAPI = require('./base');

var DeviceAPI = {
	searchSessions: function(data, cb, fl) {
		return callAPI('/action/device/session/query', data, cb, fl);
	},
	getSession: function(data, cb, fl) {
		return callAPI('/action/device/session', data, cb, fl);
	}
};

module.exports = DeviceAPI;

