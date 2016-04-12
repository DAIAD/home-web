var api = require('./base');

var UserAPI = {
	login: function(username, password) {
		return api.submit('/login?application=utility', {username:username, password:password});
	},
	logout: function() {
		return api.submit('/logout', {});
	},
	getProfile: function(cb) {
		return api.submit('/action/profile/load', null);
	}
};

module.exports = UserAPI;
