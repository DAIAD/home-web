var types = require('../constants/ActionTypes');
var updateOrAppendToSession = require('../utils/device').updateOrAppendToSession;

var sessions = function (state, action) {
	//initial state
	if (state === undefined) {
		state = {
			status: {
				isLoading: false,
				success: null,
				errors: null
			},
			filter: "volume",
			lastSession: null,
			data: [],
		};
	}
 
	switch (action.type) {
		
			case types.DEVICE_REQUESTED_SESSION:
				return Object.assign({}, state, {
					status: {
						isLoading: true
					},
			});

			case types.DEVICE_RECEIVED_SESSION:
				switch (action.success) {
					
					case true:
						var updated = updateOrAppendToSession(state.data, action.data, action.id);
						return Object.assign({}, state, {
							status: {
								isLoading: false,
								success: true,
								errors: null
							},
							data: updated
						});
					
					case false:
						return Object.assign({}, state, {
							status: {
								isLoading: false,
								success: false,
								errors: action.errors
							},
						});
				}
				break;

			case types.DEVICE_REQUESTED_SESSIONS:
				return Object.assign({}, state, {
					status: {
						isLoading: true
					}
				});
			
			case types.DEVICE_RECEIVED_SESSIONS:
				switch (action.success) {
					
					case true:
						return Object.assign({}, state, {
							status: {
								isLoading: false,
								success: true,
								errors: null
							},
							data: action.data
						});
					
					case false:
						return Object.assign({}, state, {
							status: {
								isLoading: false,
								success: false,
								errors: action.errors
							},
						});
				}
				break;

			case types.DEVICE_LAST_SESSION_REQUESTED:
				return Object.assign({}, state, {
					lastSession: action.id
				});

			case types.DEVICE_SET_SESSION_FILTER:
				return Object.assign({}, state, {
					filter: action.filter
				});

		default:
			return state;
	}
};

module.exports = sessions;

