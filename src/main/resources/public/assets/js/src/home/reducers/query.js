var types = require('../constants/ActionTypes');

var { thisWeek } = require('../utils/time');

var updateOrAppendToSession = require('../utils/device').updateOrAppendToSession;

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
			filter: "volume",
			timeFilter: "week",
			time: {
				granularity: 0
			},
			sessionFilter: "volume",
			activeSession: null,
			activeSessionIndex: null,
			lastSession: null,
			data: [],
    };
    state.time = Object.assign({}, thisWeek());
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
			
			case types.DEVICE_RESET_ACTIVE:
				return Object.assign({}, state, {
					activeDevice: null
				});

			case types.DEVICE_SET_FILTER:
				return Object.assign({}, state, {
					filter: action.filter
				});
			
			case types.DEVICE_SET_TIME_FILTER:
				return Object.assign({}, state, {
					timeFilter: action.filter
				});

				// Sessions
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

			case types.DEVICE_SET_SESSION_FILTER:
				return Object.assign({}, state, {
					sessionFilter: action.filter
				});
			
			case types.DEVICE_SET_ACTIVE_SESSION:
				return Object.assign({}, state, {
					activeSession: action.id
				});

			case types.DEVICE_SET_ACTIVE_SESSION_INDEX:
				var intId = parseInt(action.id);
				if (typeof(intId) !== "number") {
					throw('can\'t set non-integer as index');
				}
				return Object.assign({}, state, {
					activeSessionIndex: intId
				});

			case types.DEVICE_RESET_ACTIVE_SESSION_INDEX:
				return Object.assign({}, state, {
					activeSessionIndex: null
				});

			case types.DEVICE_INCREASE_ACTIVE_SESSION_INDEX:
				if (state.activeSessionIndex===null || !state.data) {
					return state;
				}
				if (!state.data[state.activeSessionIndex+1]) {
					return state;
				}
				return Object.assign({}, state, {
					activeSessionIndex: state.activeSessionIndex+1
				});
			
			case types.DEVICE_DECREASE_ACTIVE_SESSION_INDEX:
				if (state.activeSessionIndex===null || !state.data) {
					return state;
				}
				if (!state.data[state.activeSessionIndex-1]) {
					return state;
				}
				return Object.assign({}, state, {
					activeSessionIndex: state.activeSessionIndex-1
				});

			case types.DEVICE_SET_LAST_SESSION:
				return Object.assign({}, state, {
					lastSession: action.id
				});

		default:
			return state;
	}
};

module.exports = deviceQuery;

