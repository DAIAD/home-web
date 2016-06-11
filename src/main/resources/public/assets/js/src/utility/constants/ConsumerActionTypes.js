var mirrorToPath = require('../helpers/path-mirror.js');

var types = mirrorToPath({

  SET_USER : null,

  UTILITY_DATA_REQUEST : null,
  UTILITY_DATA_RESPONSE : null,

  UTILITY_FORECAST_REQUEST : null,
  UTILITY_FORECAST_RESPONSE : null,

  USER_DATA_REQUEST : null,
  USER_DATA_RESPONSE : null,

  USER_FORECAST_REQUEST : null,
  USER_FORECAST_RESPONSE : null,

  USER_RECEIVED_LOGOUT : null
});

module.exports = types;
