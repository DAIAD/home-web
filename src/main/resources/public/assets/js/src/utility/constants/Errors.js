var mirrorToPath = require('../helpers/path-mirror.js');

const constants = mirrorToPath({
	'SharedErrorCode.AUTHENTICATION' : null,
	
	'ValidationError.User.NO_FIRST_NAME' : null,
	'ValidationError.User.NO_LAST_NAME' : null,
	'ValidationError.User.NO_EMAIL' : null,
	'ValidationError.User.NO_GENDER' : null,
	'ValidationError.User.NO_UTILITY' : null,
	'ValidationError.User.INVALID_EMAIL' : null,
	'ValidationError.User.TOO_LONG_FIRST_NAME' : null,
	'ValidationError.User.TOO_LONG_LAST_NAME' : null,
	'ValidationError.User.TOO_LONG_EMAIL' : null,
	'ValidationError.User.TOO_LONG_ADDRESS' : null,
	'ValidationError.User.TOO_LONG_POSTAL_CODE' : null,
	'ValidationError.Group.NO_GROUP_NAME' : null,
	'ValidationError.Group.NO_GROUP_MEMBERS' : null,
	'ValidationError.Favourite.NO_LABEL' : null,

  // Reports

  reports: {
    measurements: {
      TIMESPAN_INVALID: null,
      TIMESPAN_TOO_NARROW: null,
      TIMESPAN_TOO_WIDE: null,
    },
  },

});

module.exports = constants;
