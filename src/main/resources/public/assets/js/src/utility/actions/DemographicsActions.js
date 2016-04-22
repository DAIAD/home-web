var types = require('../constants/ActionTypes');
var demographicsAPI = require('../api/demographics');

var requestedGroups = function() {
  return {
    type : types.DEMOGRAPHICS_REQUEST_GROUPS
  };
};

var receivedGroups = function(success, errors, groupsInfo) {
  return {
    type : types.DEMOGRAPHICS_RECEIVE_GROUPS,
    success : success,
    errors : errors,
    groupsInfo : groupsInfo
  };
};

var DemographicActions = {

  getGroups : function() {
    return function(dispatch, getState) {
      dispatch(requestedGroups());

      return demographicsAPI.fetchGroups().then(function(response) {
        dispatch(receivedGroups(response.success, response.errors, response.groupInfo));
      }, function(error) {
        dispatch(receivedMeters(false, error, null));
      });
    };
  }
};


module.exports = DemographicActions;

