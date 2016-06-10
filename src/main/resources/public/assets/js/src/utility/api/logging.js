var api = require('./base');

var LoggingApi = {

  getEvents : function(query) {
    return api.json('/action/admin/logging/events', {
      query : query
    });
  }

};

module.exports = LoggingApi;
