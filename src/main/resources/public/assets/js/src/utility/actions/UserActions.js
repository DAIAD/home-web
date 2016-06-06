var types = require('../constants/ActionTypes');
var UserAPI = require('../api/user');

var requestedUser = function (){
  return {
    type : types.USER_REQUEST_USER
  };
};

var receivedUserInfo = function (success, errors, userInfo){
  return {
    type : types.USER_RECEIVE_USER_INFO,
    success : success,
    errors : errors,
    userInfo : userInfo
  };
};

var receivedGroupMembershipInfo = function (success, errors, groupListInfo){
  return {
    type : types.USER_RECEIVE_GROUP_MEMBERSHIP_INFO,
    success : success,
    errors : errors,
    groupListInfo : groupListInfo
  };
};

var UserActions = {
    
  showUser : function(userId){
    return function (dispatch, getState) {
      dispatch(requestedUser());
      return UserAPI.fetchUserInfo(userId).then(function(response) {
        dispatch(receivedUserInfo(response.success, response.errors, response.userInfo));
        
        return UserAPI.fetchUserGroupMembershipInfo(userId).then(function(response) {
          dispatch(receivedGroupMembershipInfo(response.success, response.errors, response.groupListInfo));
        }, function (error) {
          dispatch(receivedGroupMembershipInfo(false, error, null));
        });
        
      }, function(error){
        dispatch(receivedUserInfo(false, error, null));
      });
    };
  },
  
  showFavouriteAccountForm : function(accountId){
    return {
      type : types.USER_SHOW_FAVOURITE_ACCOUNT_FORM,
      accountId : accountId
    };
  },
  
  hideFavouriteAccountForm : function(){
    return {
      type : types.USER_HIDE_FAVOURITE_ACCOUNT_FORM
    };
  },
  
};

module.exports = UserActions;
