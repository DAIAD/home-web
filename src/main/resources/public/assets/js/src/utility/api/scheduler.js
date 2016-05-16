var api = require('./base');

var SchedulerAPI = {
  getStatus : function() {
    return api.json('/action/scheduler/jobs');
  }
};

module.exports = SchedulerAPI;
