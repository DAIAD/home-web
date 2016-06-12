var mirrorToPath = require('../helpers/path-mirror.js');

var types = mirrorToPath({

  TIMELINE_REQUEST : null,
  TIMELINE_RESPONSE : null,

  GET_FEATURES : null,

  CHART_REQUEST : null,
  CHART_RESPONSE : null,

  COUNTER_REQUEST : null,
  COUNTER_RESPONSE : null,

  USER_RECEIVED_LOGOUT : null

});

module.exports = types;
