var queryAPI = require('../api/query');
var types = require('../constants/ActionTypes');

var submittedQuery = function(query) {
  return {
    type : types.QUERY_SUBMIT,
    query: query
  };
};

var receivedResponse = function(success, errors, points) {
  return {
    type : types.QUERY_RESPONSE,
    success : success,
    errors : errors,
    points : points
  };
};

var QueryActions = {
  submitQuery : function(query) {
    return function(dispatch, getState) {
      dispatch(submittedQuery(query));

      return queryAPI.submitQuery(query).then(function(response) {
        dispatch(receivedResponse(response.success, response.errors, response.points));
      }, function(error) {
        dispatch(receivedResponse(false, error, null));
      });
    };
  }
};

module.exports = QueryActions;
