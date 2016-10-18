var mirrorToPath = require('../helpers/path-mirror.js');

var types = mirrorToPath({
  MAP_SET_TIMEZONE : null,

  MAP_TIMELINE_REQUEST : null,
  MAP_TIMELINE_RESPONSE : null,

  MAP_GET_FEATURES : null,

  MAP_CHART_REQUEST : null,
  MAP_CHART_RESPONSE : null,

  USER_RECEIVED_LOGOUT : null,

  MAP_SELECT_EDITOR : null,
  MAP_SET_EDITOR_VALUE : null,
  MAP_ADD_FAVOURITE_REQUEST : null,
  MAP_ADD_FAVOURITE_RESPONSE : null  
});

module.exports = types;
