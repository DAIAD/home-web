var types = require('../constants/ActionTypes');

var initialState = {
	isAuthenticated : false,
	profile : null,
	errors : null
};

var session = function(state, action) {
	switch (action.type) {
	case types.USER_REQUESTED_LOGIN:
		return Object.assign({}, state);

	case types.USER_RECEIVED_LOGIN:
		switch (action.status) {
		case true:
			return Object.assign({}, state, {
				isAuthenticated : true,
				profile : action.profile,
				errors : null
			});

		case false:
			return Object.assign({}, state, {
				isAuthenticated : false,
				profile : null,
				errors : action.errors
			});
		}
		break;

	case types.USER_REQUESTED_LOGOUT:
		return Object.assign({}, state);

	case types.USER_RECEIVED_LOGOUT:
		switch (action.status) {
		case true:
			return Object.assign({}, state, {
				isAuthenticated : false,
				profile : null,
				errors : null
			});

		case false:
			return Object.assign({}, state, {
				isAuthenticated : false,
				profile : null,
				errors : action.errors
			});
		}
		break;

	default:
		return state || initialState;
	}
};

module.exports = session;
