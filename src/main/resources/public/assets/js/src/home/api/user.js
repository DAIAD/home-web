var callAPI = require('./base');

var UserAPI = {
	login: function(username, password, cb, fl) {
		return callAPI('/login?application=home', {username:username, password:password}, cb, fl, false);
	},
	logout: function(cb, fl) {
		return callAPI('/logout', {}, cb, fl);
	},
	getProfile: function(cb, fl) {
		return callAPI('/action/profile', null, cb, fl);
	}
};

module.exports = UserAPI;

