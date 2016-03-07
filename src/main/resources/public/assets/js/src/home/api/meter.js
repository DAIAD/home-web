var callAPI = require('./base');

var MeterAPI = {
  getStatus: function(data) {
    return callAPI('/action/meter/status', data);
  },
  getHistory: function(data) {
    return callAPI('/action/meter/history', data);
  }
};

module.exports = MeterAPI;

