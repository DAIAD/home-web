var localeAPI = require('../api/locales');
var types = require('../constants/ActionTypes');


var LocaleActions = {

	_receivedMessages: function(locale, messages) {
		return {
			type: types.LOCALE_RECEIVED_MESSAGES,
			locale: locale,
			messages: messages
		};
	},
	_requestLocaleMessages: function(locale) {
		return {
			type: types.LOCALE_REQUEST_MESSAGES,
			locale: locale
		};
	},
	fetchLocaleMessages: function(locale) {
		return function(dispatch, getState) {
			localeAPI.fetchLocaleMessages(locale, function(messages) {
				dispatch(LocaleActions._receivedMessages(locale, flattenMessages(messages)));
			});
		};
	},
	setLocale : function(locale) {
		return function(dispatch, getState) {
			if (getState().locale === locale && !getState().isFetching){
				return true;
			}
			//dispatch request messages to update state
			dispatch(LocaleActions._requestLocaleMessages(locale));
			//dispatch fetch messages to call API
			dispatch(LocaleActions.fetchLocaleMessages(locale));
		};
	},

};

function flattenMessages(nestedMessages, prefix) {
    return Object.keys(nestedMessages).reduce((messages, key) => {
        var value = nestedMessages[key];
        var prefixedKey = prefix ? `${prefix}.${key}` : key;

        if (typeof value === 'string') {
            messages[prefixedKey] = value;
        } else {
            Object.assign(messages, flattenMessages(value, prefixedKey));
        }

        return messages;
    }, {});
}

module.exports = LocaleActions;
