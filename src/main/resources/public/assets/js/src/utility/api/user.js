var callAPI = require('./base');

var UserAPI = {
	login: function(username, password) {
		return callAPI('/login?application=utility', {username:username, password:password});
	},
	logout: function() {
		return callAPI('/logout', {});
	},
	getProfile: function(cb) {
		return callAPI('/action/profile/load', null);
	}
};

module.exports = UserAPI;
