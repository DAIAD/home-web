var userAPI = require('../api/user');
var types = require('../constants/ActionTypes');

var requestedLogin = function() {
	return {
		type:types.USER_REQUESTED_LOGIN,
		};
};

var receivedLogin = function(status, errors, profile) {
	return {
		type: types.USER_RECEIVED_LOGIN,
		status: status,
		errors: errors,
		profile: profile
	};
};

var requestedLogout = function() {
	return {
		type:types.USER_REQUESTED_LOGOUT,
		};
};

var receivedLogout = function(status, errors) {
	return {
		type: types.USER_RECEIVED_LOGOUT,
		status: status,
		errors: errors
	};
};

var UserActions = {

	login: function(username, password) {
		return function(dispatch, getState) {
			dispatch(requestedLogin());

			return userAPI.login(username, password).then(
				function(response) {
					dispatch(receivedLogin(response.success, response.errors, response.profile));
					return response;

				},
				function(error) {
					dispatch(receivedLogin(false, error, {}));
					return error;
				});
		};
	},
	refreshProfile: function() {
		return function(dispatch, getState) {
			return userAPI.getProfile().then(
				function(response) {
					dispatch(receivedLogin(response.success, response.errors, response.profile));
					return response;
				},
				function (error) {
					dispatch(receivedLogin(false, error, {}));
					reject(error);
					return error;
				});
		};
	},
	logout: function() {
		return function(dispatch, getState) {
			dispatch(requestedLogout());

			return userAPI.logout().then(
				function(response) {
					dispatch(receivedLogout(response.success, response.errors));
					return response;
				},
				function(error) {
					dispatch(receivedLogout(false, error));
					return error;
				});
		};
	},

};


module.exports = UserActions;
