var callAPI = require('./base');

var MessagesAPI = {
  fetch: function(data) {
    return callAPI('/action/message', data);
  },
  acknowledge: function(data) {
    return callAPI('/action/message/acknowledge', data);
  }
};

module.exports = MessagesAPI;

