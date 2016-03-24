var callAPI = require('./base');

var DeviceAPI = {
  querySessions: function(data) {
    return callAPI('/action/device/session/query', data);
  },
  queryMeasurements: function(data) {
    return callAPI('/action/device/measurement/query', data);
  },
  getSession: function(data) {
    return callAPI('/action/device/session', data);
  }
};

module.exports = DeviceAPI;

