var sessionAPI = require('../api/session');
var types = require('../constants/ActionTypes');

var requestedLogin = function() {
	return {
		type : types.USER_REQUESTED_LOGIN,
	};
};

var receivedLogin = function(status, errors, profile) {
	return {
		type : types.USER_RECEIVED_LOGIN,
		status : status,
		errors : errors,
		profile : profile
	};
};

var requestedLogout = function() {
	return {
		type : types.USER_REQUESTED_LOGOUT,
	};
};

var receivedLogout = function(status, errors) {
	return {
		type : types.USER_RECEIVED_LOGOUT,
		status : status,
		errors : errors
	};
};

var SessionActions = {
	login : function(username, password) {
		return function(dispatch, getState) {
			dispatch(requestedLogin());

			return sessionAPI.login(username, password).then(
					function(response) {
						dispatch(receivedLogin(response.success,
								response.errors, response.profile));
					}, function(error) {
						dispatch(receivedLogin(false, error, null));
					});
		};
	},

	logout : function() {
		return function(dispatch, getState) {
			dispatch(requestedLogout());

			return sessionAPI.logout().then(
					function(response) {
						dispatch(receivedLogout(
								response.success, response.errors));
					}, function(error) {
						dispatch(receivedLogout(false, error));
					});
		};
	},

	refreshProfile : function() {
		return function(dispatch, getState) {
			return sessionAPI.getProfile().then(
					function(response) {
					  dispatch(receivedLogin(response.success,
								response.errors, response.profile));
					}, function(error) {
						dispatch(receivedLogin(false, error, {}));
					});
		};
	}
};

// http://stackoverflow.com/questions/46155/validate-email-address-in-javascript
function validateEmail(email) {
	var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	return re.test(email);
}
module.exports = SessionActions;
