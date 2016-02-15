var localeAPI = require('../api/locales');
var types = require('../constants/ActionTypes');


var LocaleActions = {

	_receivedMessages: function(success, errors, locale, messages) {
		return {
			type: types.LOCALE_RECEIVED_MESSAGES,
			success: success,
			errors: errors,
			locale: locale,
			messages: messages
		};
	},
	_requestedLocaleMessages: function(locale) {
		return {
			type: types.LOCALE_REQUEST_MESSAGES,
			locale: locale
		};
	},
	fetchLocaleMessages: function(locale) {
		return function(dispatch, getState) {
			localeAPI.fetchLocaleMessages(locale, function(messages) {
				dispatch(LocaleActions._receivedMessages(true, null, locale, flattenMessages(messages)));
			},
			function(errors) {
				dispatch(LocaleActions._receivedMessages(false, errors, null, []));
			});
		};
	},
	setLocale : function(locale) {
		return function(dispatch, getState) {
			if (getState().locale.locale === locale){
				return true;
			}
			//dispatch request messages to update state
			dispatch(LocaleActions._requestedLocaleMessages(locale));
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
