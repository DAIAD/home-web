var mirrorToPath = require('../helpers/path-mirror.js');

var types = mirrorToPath({

  USER_REQUEST_USER : null,
  USER_RECEIVE_USER_INFO : null,

  USER_SHOW_FAVOURITE_ACCOUNT_FORM : null,
  USER_HIDE_FAVOURITE_ACCOUNT_FORM : null,

  SELECT_AMPHIRO : null,

  AMPHIRO_REQUEST : null,
  AMPHIRO_RESPONSE : null,

  METER_REQUEST : null,
  METER_RESPONSE : null,

  GROUP_DATA_REQUEST : null,
  GROUP_DATA_RESPONSE : null,
  GROUP_DATA_CLEAR : null,

  EXPORT_REQUEST : null,
  EXPORT_RESPONSE : null,

  ADD_FAVORITE_REQUEST : null,
  ADD_FAVORITE_RESPONSE : null,

  REMOVE_FAVORITE_REQUEST : null,
  REMOVE_FAVORITE_RESPONSE : null,

  AMPHIRO_CONFIG_SHOW : null,
  AMPHIRO_CONFIG_HIDE : null

});

module.exports = types;
