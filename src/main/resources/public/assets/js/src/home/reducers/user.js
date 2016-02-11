var types = require('../constants/ActionTypes');

var user = function (state, action) {
	if (state === undefined) {
		state = {
			isLoading: false,
			isAuthenticated: false,
			profile: {}
		};
	}
 
	switch (action.type) {
		case types.USER_REQUESTED_LOGIN:
			return Object.assign({}, state, {
				isLoading: true,
			});

		case types.USER_RECEIVED_LOGIN:
			switch (action.status) {
				case true:
					return Object.assign({}, state, {
						isAuthenticated: true,
						isLoading: false,
						profile: action.profile,
						errors: null
					});
				
				case false:
					return Object.assign({}, state, {
						isAuthenticated: false,
						isLoading: false,
						errors: action.errors
					});
				}
				break;

		case types.USER_REQUESTED_LOGOUT:
			return Object.assign({}, state, {
				isLoading: true,
			});

		case types.USER_RECEIVED_LOGOUT:
			switch (action.status) {
				case true:
					return Object.assign({}, state, {
						isAuthenticated: false,
						isLoading: false,
						profile: {},
						errors: null
					});
				
				case false:
					return Object.assign({}, state, {
						isLoading: false,
						errors: action.errors
					});
				}
				break;

		default:
			return state;
	}
};

module.exports = user;

