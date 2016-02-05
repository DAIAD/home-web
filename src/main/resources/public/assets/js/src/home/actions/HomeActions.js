var AppDispatcher = require('../dispatcher/AppDispatcher');
var HomeConstants = require('../constants/HomeConstants');

var HomeActions = {

	login : function(username, password) {
		AppDispatcher.dispatch({
			action : HomeConstants.USER_LOGIN,
			data : {
				username : username,
				password : password
			}
		});
	},

	logout : function() {
		AppDispatcher.dispatch({
			action : HomeConstants.USER_LOGOUT,
			data : {}
		});
	},

	setLocale : function(locale) {
		AppDispatcher.dispatch({
			action : HomeConstants.LOCALE_CHANGE,
			data : {
				locale : locale
			}
		});
	},

	refreshProfile : function() {
		AppDispatcher.dispatch({
			action : HomeConstants.PROFILE_REFRESH,
			data : {}
		});
	},

	updateProfile: function(profile) {
		AppDispatcher.dispatch({
			action : HomeConstants.PROFILE_UPDATE,
			data : {
				profile: profile
			}
		});
	},
	
	searchSessions: function(data) {
		AppDispatcher.dispatch({
			action : HomeConstants.DEVICE_SESSION_SEARCH,
			data : data 
		});
	},

	getSession: function(data) {
		AppDispatcher.dispatch({
			action : HomeConstants.DEVICE_SESSION_GET,
			data : data 
		});
	},

};

module.exports = HomeActions;
