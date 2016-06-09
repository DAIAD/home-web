var types = require('../constants/ActionTypes');
var alertsAPI = require('../api/alerts');

var requestedCurrentUtilityUsers = function() {
  return {
    type : types.ANNC_REQUESTED_USERS,
    isLoading: true
  };
};

var receivedCurrentUtilityUsers = function(success, errors, accounts) {
  return {
    type : types.ANNC_RECEIVED_USERS,
    isLoading: false,
    success : success,
    errors : errors,
    accounts : accounts
  };
};

var AnnouncementsActions = {
  getCurrentUtilityUsers : function(event) {
    return function(dispatch, getState) {
      dispatch(requestedCurrentUtilityUsers());

      return alertsAPI.getUsers().then(function(response) {
        dispatch(receivedCurrentUtilityUsers(response.success, response.errors, response.accounts));
      }, function(error) {
        dispatch(receivedCurrentUtilityUsers(false, error, null));
      });
    };
  },
  setSelectedUser : function(accounts, accountId) {
    return{
      type: types.ANNC_USER_SET_SELECTED,
      rowIdToggled: accountId,
      accounts: accounts
    };
  },
};

module.exports = AnnouncementsActions;
