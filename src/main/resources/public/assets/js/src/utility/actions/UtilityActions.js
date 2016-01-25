var AppDispatcher = require('../dispatcher/AppDispatcher');
var UtilityConstants = require('../constants/UtilityConstants');

var UtilityActions = {

	login : function(username, password) {
		AppDispatcher.dispatch({
			action : UtilityConstants.USER_LOGIN,
			data : {
				username : username,
				password : password
			}
		});
	},

	logout : function() {
		AppDispatcher.dispatch({
			action : UtilityConstants.USER_LOGOUT,
			data : {}
		});
	},

	setLocale : function(locale) {
		AppDispatcher.dispatch({
			action : UtilityConstants.LOCALE_CHANGE,
			data : {
				locale : locale
			}
		});
	},

	refreshProfile : function() {
		AppDispatcher.dispatch({
			action : UtilityConstants.PROFILE_REFRESH,
			data : {}
		});
	}

};

module.exports = UtilityActions;