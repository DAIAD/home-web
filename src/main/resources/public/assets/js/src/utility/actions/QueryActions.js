var queryAPI = require('../api/query');
var types = require('../constants/ActionTypes');

var submittedQuery = function(query) {
  return {
    type : types.QUERY_SUBMIT,
    query : query
  };
};

var receivedResponse = function(success, errors, data) {
  return {
    type : types.QUERY_RESPONSE,
    success : success,
    errors : errors,
    data : data
  };
};

var QueryActions = {
  submitQuery : function(query) {
    return function(dispatch, getState) {
      dispatch(submittedQuery(query));

      return queryAPI.queryMeasurements(query).then(function(response) {
        var data = {
          meters : null,
          devices : null,
          areas : null
        };
        if (response.success) {
          data.areas = response.areas;
          data.meters = response.meters;
          data.devices = response.devices;
        }
        dispatch(receivedResponse(response.success, response.errors, data));
      }, function(error) {
        dispatch(receivedResponse(false, error, null));
      });
    };
  }
};

module.exports = QueryActions;
