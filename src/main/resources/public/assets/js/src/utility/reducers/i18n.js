var types = require('../constants/ActionTypes');

var initialState = {
	locale : null,
	isLoading : false,
	data : {}
};

var i18n = function(state, action) {
	switch (action.type) {
	case types.LOCALE_REQUEST_MESSAGES:
		return Object.assign({}, state, {
			isLoading : true
		});

	case types.LOCALE_RECEIVED_MESSAGES:
		var newState = Object.assign({}, {
			isLoading : false,
			data : Object.keys(state.data).reduce(function(next, locale) {
				next[locale] = state.data[locale];

				return next;
			}, {}) || {},
			locale : action.locale
		});

		newState.data[action.locale] = {
			messages : action.messages,
			lastUpdated : new Date()
		};

		return newState;

	default:
		return state || initialState;
	}
};

module.exports = i18n;
