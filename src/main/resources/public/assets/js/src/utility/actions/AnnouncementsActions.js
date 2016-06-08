var types = require('../constants/ActionTypes');
var alertsAPI = require('../api/alerts');

var requestedCurrentUtilityUsers = function() {
  return {
    type : types.ANNC_REQUESTED_USERS
  };
};

var receivedCurrentUtilityUsers = function(success, errors, currentUsers) {
  return {
    type : types.ANNC_RECEIVED_USERS,
    success : success,
    errors : errors,
    currentUsers : currentUsers
  };
};

var AnnouncementsActions = {
  getCurrentUtilityUsers : function(event) {
    console.log('actions getCurrentUtilityUsers');
    return function(dispatch, getState) {
      dispatch(requestedCurrentUtilityUsers());

      return alertsAPI.getCurrentUtilityUsers().then(function(response) {
        dispatch(receivedCurrentUtilityUsers(response.success, response.errors, response.accounts));
      }, function(error) {
        dispatch(receivedCurrentUtilityUsers(false, error, null));
      });
    };
  }
};

module.exports = AnnouncementsActions;
