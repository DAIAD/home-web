var keyMirror = require('keymirror');

const constants = keyMirror({
	'SharedErrorCode.AUTHENTICATION' : null,
	
	'ValidationError.NO_FIRST_NAME' : null,
	'ValidationError.NO_LAST_NAME' : null,
	'ValidationError.NO_EMAIL' : null,
	'ValidationError.NO_GENDER' : null,
	'ValidationError.NO_ADDRESS' : null,
	'ValidationError.NO_COUNTRY' : null,
	'ValidationError.NO_GROUP' : null,
	'ValidationError.NO_CITY' : null,
	'ValidationError.NO_POSTAL_CODE' : null,
	'ValidationError.INVALID_EMAIL' : null,
});

module.exports = constants;
