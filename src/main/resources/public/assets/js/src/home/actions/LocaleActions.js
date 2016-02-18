var localeAPI = require('../api/locales');
var types = require('../constants/ActionTypes');

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

var receivedMessages = function(success, errors, locale, messages) {
	return {
		type: types.LOCALE_RECEIVED_MESSAGES,
		success: success,
		errors: errors,
		locale: locale,
		messages: messages
	};
};

var requestedLocaleMessages = function(locale) {
	return {
		type: types.LOCALE_REQUEST_MESSAGES,
		locale: locale
	};
};

var LocaleActions = {

	fetchLocaleMessages: function(locale) {
		return function(dispatch, getState) {
			return localeAPI.fetchLocaleMessages(locale).then(
				function(messages) {
					dispatch(receivedMessages(true, null, locale, flattenMessages(messages)));
				},
				function(errors) {
					dispatch(receivedMessages(false, errors, null, []));
				});
		};
	},
	setLocale : function(locale) {
		return function(dispatch, getState) {
			if (getState().locale.locale === locale){
				return true;
			}
			//dispatch request messages to update state
			dispatch(requestedLocaleMessages(locale));
			//dispatch fetch messages to call API
			return dispatch(LocaleActions.fetchLocaleMessages(locale));
		};
	},

};

module.exports = LocaleActions;
