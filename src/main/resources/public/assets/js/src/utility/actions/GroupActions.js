var types = require('../constants/ActionTypes');
var GroupAPI = require('../api/group');

var requestedGroup = function (){
  return {
    type : types.GROUP_REQUEST_GROUP 
  };
};

var receivedGroupInfo = function (success, errors, groupInfo){
  return {
    type : types.GROUP_RECEIVE_GROUP_INFO,
    success : success,
    errors : errors,
    groupInfo : groupInfo
  };
};

var receivedGroupMembers = function (success, errors, groupMembersInfo){
  return {
    type : types.GROUP_RECEIVE_GROUP_MEMBERS,
    success : success,
    errors : errors,
    members : groupMembersInfo
  };
};


var GroupActions = {
    
  showGroup : function(groupId){
    return function (dispatch, getState) {
      dispatch(requestedGroup());
      
      return GroupAPI.fetchGroupInfo(groupId).then(function(response) {
        dispatch(receivedGroupInfo(response.success, response.errors, response.groupInfo));
        
        return GroupAPI.fetchGroupMembers(groupId).then( function (response) {
          dispatch(receivedGroupMembers(response.success, response.errors, response.groupMembersInfo));
        }, function (error) {
          dispatch(receivedGroupMembers(false, error, null));
        });
        
      }, function(error){
        dispatch(receivedGroupInfo(false, error, null));
      });
    };
  },
  
  showFavouriteGroupForm : function(groupId){
    return {
      type : types.GROUP_SHOW_FAVOURITE_GROUP_FORM,
      groupId : groupId
    };
  },
  
  hideFavouriteGroupForm : function(){
    return {
      type : types.GROUP_HIDE_FAVOURITE_GROUP_FORM
    };
  },

  resetDemograhpics : function() {
    return {
      type : types.GROUP_RESET_COMPONENT
    };
  },
  
  showFavouriteAccountForm : function (accountId){
    return {
      type : types.GROUP_SHOW_FAVOURITE_ACCOUNT_FORM,
      accountId : accountId
    };
  },
  
  hideFavouriteAccountForm : function(){
    return {
      type : types.GROUP_HIDE_FAVOURITE_ACCOUNT_FORM
    };
  },
  
};

module.exports = GroupActions;
