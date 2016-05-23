var schedulerAPI = require('../api/scheduler');
var types = require('../constants/ActionTypes');

var requestStatus = function() {
  return {
    type : types.SCHEDULER_STATUS_REQUEST
  };
};

var receivedStatus = function(success, jobs, executions, errors) {
  return {
    type : types.SCHEDULER_STATUS_RESPONSE,
    success : success,
    jobs : jobs,
    executions : executions,
    errors : errors
  };
};

var SchedulerActions = {
  getStatus : function() {
    return function(dispatch, getState) {
      dispatch(requestStatus());

      return schedulerAPI.getStatus().then(function(response) {
        dispatch(receivedStatus(response.success, response.jobs, response.executions, response.errors));
      }, function(errors) {
        dispatch(receivedStatus(false, null, null, errors));
      });
    };
  }
};

module.exports = SchedulerActions;
