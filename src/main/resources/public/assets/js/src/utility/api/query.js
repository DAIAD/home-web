
var api = require('./base');

var QueryAPI = {
  queryMeasurements: function(query) {
    return api.json('/action/query', query);
  },
  queryForecast: function(query) {
    return api.json('/action/data/meter/forecast', query);
  }
};

module.exports = QueryAPI;
