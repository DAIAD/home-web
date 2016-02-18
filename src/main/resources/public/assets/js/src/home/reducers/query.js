var types = require('../constants/ActionTypes');


var deviceQuery = function (state, action) {
	//initial state
	if (state === undefined) {
		state = {
			status: {
				isLoading: false,
				success: null,
				errors: null
			},
			activeDevice: null,
			filter: "showers",
			time: {
				startDate: new Date("1990-01-01").getTime(),
				endDate: new Date().getTime(),
				granularity: 0
			},
			data: [],
			//showers: []
		};
	}
 
	switch (action.type) {
		case types.DEVICE_REQUESTED_SESSION_SEARCH:
			return Object.assign({}, state, {
				status: {
					isLoading: true,
				},
			});

		case types.DEVICE_RECEIVED_SESSION_SEARCH:
			switch (action.success) {
				case true:
					/*switch (state.time.granularity) {
						case 0:
							return Object.assign({}, state, {
								showers: action.data,
								data: action.data,
								status: {
									isLoading: false,
									success: true,
									errors: null
								}
							});

						default:
							*/
					return Object.assign({}, state, {
						data: action.data,
						status: {
							isLoading: false,
							success: true,
							errors: null
						}
					});
					//}
					//break;

				case false:
					return Object.assign({}, state, {
						data: [],
						status: {
							isLoading: false,
							success: false,
							errors: action.errors
						}
					});
				}
				break;
						
			case types.DEVICE_SET_TIME:
				return Object.assign({}, state, {
					time: Object.assign({}, state.time, action.time)
				});

			case types.DEVICE_SET_ACTIVE:
				return Object.assign({}, state, {
					activeDevice: action.deviceKey
				});

			case types.DEVICE_SET_FILTER:
				return Object.assign({}, state, {
					filter: action.filter
				});
			
		default:
			return state;
	}
};

module.exports = deviceQuery;
