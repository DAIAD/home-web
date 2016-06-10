var schedulerAPI = require('../api/scheduler');
var types = require('../constants/SchedulerActionTypes');

var jobChangeIndex = function(index) {
  return {
    type : types.JOB_CHANGE_INDEX,
    index : index
  };
};

var jobRequestInitialize = function() {
  return {
    type : types.JOB_REQUEST
  };
};

var jobRequestComplete = function(success, errors, total, items, index, size) {
  return {
    type : types.JOB_RESPONSE,
    success : success,
    errors : errors,
    total : total,
    items : items,
    index : index,
    size : size
  };
};

var executionChangeIndex = function(index) {
  return {
    type : types.EXECUTION_CHANGE_INDEX,
    index : index
  };
};

var executionRequestInitialize = function() {
  return {
    type : types.EXECUTION_REQUEST
  };
};

var executionRequestComplete = function(success, errors, total, items, index, size) {
  return {
    type : types.EXECUTION_RESPONSE,
    success : success,
    errors : errors,
    total : total,
    items : items,
    index : index,
    size : size
  };
};

var executionFilterByJobName = function(jobName) {
  return {
    type : types.EXECUTION_FILTER_JOB_NAME,
    jobName : (jobName === 'UNDEFINED' ? null : jobName)
  };
};

var executionFilterByExitCode = function(exitCode) {
  return {
    type : types.EXECUTION_FILTER_EXIT_CODE,
    exitCode : exitCode
  };
};

var executionClearFilter = function() {
  return {
    type : types.EXECUTION_FILTER_CLEAR
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

var enableJobInitialize = function() {
  return {
    type : types.JOB_ENABLE_REQUEST
  };
};

var enableJobComplete = function(success, errors) {
  return {
    type : types.JOB_ENABLE_RESPONSE,
    success : success,
    errors : errors
  };
};

var disableJobInitialize = function() {
  return {
    type : types.JOB_DISABLE_REQUEST
  };
};

var disableJobComplete = function(success, errors) {
  return {
    type : types.JOB_DISABLE_RESPONSE,
    success : success,
    errors : errors
  };
};

var launchJobInitialize = function() {
  return {
    type : types.JOB_LAUNCH_REQUEST
  };
};

var launchJobComplete = function(success, errors) {
  return {
    type : types.JOB_LAUNCH_RESPONSE,
    success : success,
    errors : errors
  };
};

var SchedulerActions = {

  launchJob : function(jobId) {
    return function(dispatch, getState) {
      dispatch(launchJobInitialize());

      return schedulerAPI.launchJob(jobId).then(
          function(response) {
            dispatch(launchJobComplete(response.success, response.errors));

            if (response.success) {
              dispatch(jobRequestInitialize());

              return schedulerAPI.getJobs().then(
                  function(response) {
                    dispatch(jobRequestComplete(response.success, response.errors, response.total, response.jobs,
                        response.index, response.size));
                  }, function(error) {
                    dispatch(jobRequestComplete(false, error));
                  });
            }
          }, function(error) {
            dispatch(launchJobComplete(false, error));
          });
    };
  },

  disableJob : function(jobId) {
    return function(dispatch, getState) {
      dispatch(disableJobInitialize());

      return schedulerAPI.disableJob(jobId).then(
          function(response) {
            dispatch(disableJobComplete(response.success, response.errors));

            if (response.success) {
              dispatch(jobRequestInitialize());

              return schedulerAPI.getJobs().then(
                  function(response) {
                    dispatch(jobRequestComplete(response.success, response.errors, response.total, response.jobs,
                        response.index, response.size));
                  }, function(error) {
                    dispatch(jobRequestComplete(false, error));
                  });
            }
          }, function(error) {
            dispatch(disableJobComplete(false, error));
          });
    };
  },

  enableJob : function(jobId) {
    return function(dispatch, getState) {
      dispatch(enableJobInitialize());

      return schedulerAPI.enableJob(jobId).then(
          function(response) {
            dispatch(enableJobComplete(response.success, response.errors));

            if (response.success) {
              dispatch(jobRequestInitialize());

              return schedulerAPI.getJobs().then(
                  function(response) {
                    dispatch(jobRequestComplete(response.success, response.errors, response.total, response.jobs,
                        response.index, response.size));
                  }, function(error) {
                    dispatch(jobRequestComplete(false, error));
                  });
            }
          }, function(error) {
            dispatch(enableJobComplete(false, error));
          });
    };
  },

  jobChangeIndex : function(index) {
    return jobChangeIndex(index);
  },

  getJobs : function() {
    return function(dispatch, getState) {
      dispatch(jobRequestInitialize());

      return schedulerAPI.getJobs().then(
          function(response) {
            dispatch(jobRequestComplete(response.success, response.errors, response.total, response.jobs,
                response.index, response.size));
          }, function(error) {
            dispatch(jobRequestComplete(false, error));
          });
    };
  },

  executionChangeIndex : function(index) {
    return function(dispatch, getState) {
      dispatch(executionChangeIndex(index));

      return schedulerAPI.getExecutions(getState().scheduler.query.execution).then(
          function(response) {
            dispatch(executionRequestComplete(response.success, response.errors, response.total, response.executions,
                response.index, response.size));
          }, function(error) {
            dispatch(executionRequestComplete(false, error));
          });
    };
  },

  getExecutions : function() {
    return function(dispatch, getState) {
      dispatch(executionRequestInitialize());

      return schedulerAPI.getExecutions(getState().scheduler.query.execution).then(
          function(response) {
            dispatch(executionRequestComplete(response.success, response.errors, response.total, response.executions,
                response.index, response.size));
          }, function(error) {
            dispatch(executionRequestComplete(false, error));
          });
    };
  },

  filterExecutionByJobName : function(jobName) {
    return function(dispatch, getState) {
      dispatch(executionFilterByJobName(jobName));

      dispatch(executionRequestInitialize());

      return schedulerAPI.getExecutions(getState().scheduler.query.execution).then(
          function(response) {
            dispatch(executionRequestComplete(response.success, response.errors, response.total, response.executions,
                response.index, response.size));
          }, function(error) {
            dispatch(executionRequestComplete(false, error));
          });
    };
  },

  filterExecutionByExitCode : function(exitCode) {
    return function(dispatch, getState) {
      dispatch(executionFilterByExitCode(exitCode));

      dispatch(executionRequestInitialize());

      return schedulerAPI.getExecutions(getState().scheduler.query.execution).then(
          function(response) {
            dispatch(executionRequestComplete(response.success, response.errors, response.total, response.executions,
                response.index, response.size));
          }, function(error) {
            dispatch(executionRequestComplete(false, error));
          });
    };
  },

  clearExecutionFilter : function() {
    return function(dispatch, getState) {
      dispatch(executionClearFilter());

      dispatch(executionRequestInitialize());

      return schedulerAPI.getExecutions(getState().scheduler.query.execution).then(
          function(response) {
            dispatch(executionRequestComplete(response.success, response.errors, response.total, response.executions,
                response.index, response.size));
          }, function(error) {
            dispatch(executionRequestComplete(false, error));
          });
    };
  }

};

module.exports = SchedulerActions;
