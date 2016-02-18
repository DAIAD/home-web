var keyMirror = require('keymirror');

var constants = keyMirror({
	LOCALE_CHANGE : null,
	LOCALE_REQUEST_MESSAGES: null,
	LOCALE_RECEIVED_MESSAGES: null,
	
	USER_REQUESTED_LOGIN: null,
	USER_RECEIVED_LOGIN: null,
	USER_REQUESTED_LOGOUT: null,
	USER_RECEIVED_LOGOUT: null,
	USER_PROFILE_REFRESH : null,
	USER_PROFILE_UPDATE: null,
});

module.exports = constants;
