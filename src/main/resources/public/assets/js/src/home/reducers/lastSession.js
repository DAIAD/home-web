var types = require('../constants/ActionTypes');

var lastDeviceSession = function (state, action) {
	//initial state
	if (state === undefined) {
		state = {
			status: {
				isLoading: false,
				success: null,
				errors: null
			},
			filter: "volume",
			data: [],
		};
	}
 
	switch (action.type) {
		
			case types.DEVICE_REQUESTED_SESSION:
				return Object.assign({}, state, {
					status: {
						isLoading: true
					},
					data: []
			});

			case types.DEVICE_RECEIVED_SESSION:
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

			case types.DEVICE_SET_SESSION_FILTER:
				return Object.assign({}, state, {
					filter: action.filter
				});

		default:
			return state;
	}
};

module.exports = lastDeviceSession;

