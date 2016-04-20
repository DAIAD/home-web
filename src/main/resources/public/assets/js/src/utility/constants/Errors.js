var keyMirror = require('keymirror');

const constants = keyMirror({
	'SharedErrorCode.AUTHENTICATION' : null,
	
	'ValidationError.NO_FIRST_NAME' : null,
	'ValidationError.NO_LAST_NAME' : null,
	'ValidationError.NO_EMAIL' : null,
	'ValidationError.NO_GENDER' : null,
	'ValidationError.NO_UTILITY' : null,
	'ValidationError.INVALID_EMAIL' : null,
	'ValidationError.TOO_LONG_FIRST_NAME' : null,
	'ValidationError.TOO_LONG_LAST_NAME' : null,
	'ValidationError.TOO_LONG_EMAIL' : null,
	'ValidationError.TOO_LONG_ADDRESS' : null,
	'ValidationError.TOO_LONG_POSTAL_CODE' : null
});

module.exports = constants;
