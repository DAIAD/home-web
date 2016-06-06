/**
 * Locale Actions module.
 * I18N related action creators
 * 
 * @module LocaleActions
 */

var localeAPI = require('../api/locales');
var types = require('../constants/ActionTypes');

var { flattenMessages } = require('../utils/general');

const receivedMessages = function(success, errors, locale, messages) {
  return {
    type: types.LOCALE_RECEIVED_MESSAGES,
    success: success,
    errors: errors,
    locale: locale,
    messages: messages
  };
};

const requestedLocaleMessages = function(locale) {
  return {
    type: types.LOCALE_REQUEST_MESSAGES,
    locale: locale
  };
};

/**
 * Sets locale and fetches locale strings
 *
 * @param {String} locale - One of en, el, es, de, fr 
 * @return {Promise} Resolved or rejected promise with locale strings if resolved, errors if rejected
 */
const setLocale = function(locale) {
  return function(dispatch, getState) {
    if (getState().locale.locale === locale){
      return true;
    }
    //dispatch request messages to update state
    dispatch(requestedLocaleMessages(locale));
    //dispatch fetch messages to call API
    return dispatch(fetchLocaleMessages(locale));
  };
};

/**
 * Fetches locale strings
 *
 * @param {String} locale - One of en, el, es, de, fr
 * @return {Promise} Resolved or rejected promise with locale strings if resolved, errors if rejected
 */
const fetchLocaleMessages = function(locale) {
  return function(dispatch, getState) {
    return localeAPI.fetchLocaleMessages({locale, csrf: getState().user.csrf})
    .then((messages) => {
        dispatch(receivedMessages(true, null, locale, flattenMessages(messages)));
        return messages;
    })
    .catch((errors) => {
        dispatch(receivedMessages(false, errors, null, []));
        return errors;
    });
  };
};



module.exports = {
  fetchLocaleMessages,
  setLocale
};
