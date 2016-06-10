var api = require('./base');

var SchedulerAPI = {
    
  getJobs : function() {
    return api.json('/action/scheduler/jobs');
  },
  
  getExecutions: function(query) {
    return api.json('/action/scheduler/executions', {
      query : query
    });
  },
  
  disableJob: function(jobId) {
    return api.json('/action/scheduler/job/disable/'+ jobId, null, "PUT");
  },
  
  enableJob: function(jobId) {
    return api.json('/action/scheduler/job/enable/'+ jobId, null, "PUT");
  },
  
  launchJob: function(jobId) {
    return api.json('/action/scheduler/job/launch/'+ jobId, null, "PUT");
  },
  
  viewExecutionMessage: function(executionId) {
    return api.json('/action/scheduler/execution/' + executionId + '/message/', null);
  }

};

module.exports = SchedulerAPI;
