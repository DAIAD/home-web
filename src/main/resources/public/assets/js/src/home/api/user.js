var callAPI = require('./base');

var UserAPI = {
	login: function(username, password) {
		return callAPI('/login?application=home', {username:username, password:password}, false);
	},
	logout: function() {
		return callAPI('/logout', {});
	},
	getProfile: function() {
		return callAPI('/action/profile', null);
	}
};

module.exports = UserAPI;

