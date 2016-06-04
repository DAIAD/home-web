
var api = require('./base');

var QueryAPI = {
  queryMeasurements: function(query) {
    return api.json('/action/query', query);
  }
};

module.exports = QueryAPI;
