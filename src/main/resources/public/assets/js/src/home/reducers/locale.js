var types = require('../constants/ActionTypes');
var locales = function (state, action) {
	if (state === undefined) state = {};
 
	switch (action.type) {
		case types.LOCALE_REQUEST_MESSAGES:
			return Object.assign({}, state, {
				isLoading: true,
			});

		case types.LOCALE_RECEIVED_MESSAGES:
			return Object.assign({}, state, {
				isLoading: false,
				messages: action.messages,
				locale: action.locale
			});

		default:
			return state;
	}
};

module.exports = locales;

