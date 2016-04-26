var localeAPI = require('../api/locale');
var types = require('../constants/ActionTypes');

function flattenMessages(nestedMessages, prefix = '') {
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

var localeNeedsLoading = function(i18n, locale, force) {
	if(i18n.data.hasOwnProperty(locale)) {
		if(i18n.isLoading) {
			return false;
		}
		return force;
	}
	
	return true;
};

var requestedMessages = function(locale) {
	return {
		type: types.LOCALE_REQUEST_MESSAGES,
		locale: locale
	};
};

var receivedMessages = function(locale, messages) { 
	return {
		type: types.LOCALE_RECEIVED_MESSAGES,
		locale: locale,
		messages: messages
	};
};

var LocaleActions = {
	setLocale : function(locale, force = false) {
		return function(dispatch, getState) {
			if((!force) && (!localeNeedsLoading(getState().i18n, locale, force))) {
				dispatch(receivedMessages(locale, getState().i18n.data[locale].messages));
				
				return;
			}
			
			dispatch(requestedMessages(locale));
			
			return localeAPI.fetchMessages(locale).then(
				function(messages) {
					dispatch(receivedMessages(locale, flattenMessages(messages)));
				});
		};
	}

};

module.exports = LocaleActions;
