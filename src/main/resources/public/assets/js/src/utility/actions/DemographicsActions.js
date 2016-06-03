var types = require('../constants/ActionTypes');
var demographicsAPI = require('../api/demographics');

var requestedGroupsAndFavourites = function() {
  return {
    type : types.DEMOGRAPHICS_REQUEST_GROUPS_AND_FAVOURITES
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

var receivedFavourites = function(success, errors, favouritesInfo) {
  return {
    type : types.DEMOGRAPHICS_RECEIVE_FAVOURITES,
    success : success,
    errors : errors,
    favouritesInfo : favouritesInfo
  };
}; 

var requestedNewGroupPossibleMembers = function(){
  return {
    type : types.DEMOGRAPHICS_SHOW_NEW_GROUP_FORM
  };
};

var receivedNewGroupPossibleMembers = function(success, errors, groupMembersInfo) {
  return {
    type : types.DEMOGRAPHICS_RECEIVE_NEW_GROUP_POSSIBLE_MEMBERS,
    success : success,
    errors : errors,
    possibleMembersInfo : groupMembersInfo
  };
};

var requestedGroupMembers = function() {
  return {
    type : types.DEMOGRAPHICS_REQUEST_GROUP_MEMBERS
  };
};

var receivedGroupMembers = function(success, errors, groupMembersInfo) {
  return {
    type : types.DEMOGRAPHICS_RECEIVE_GROUP_MEMBERS,
    success : success,
    errors : errors,
    groupMembersInfo : groupMembersInfo
  };
};

var groupSetCreateMakeRequest = function() {
  return {
    type : types.DEMOGRAPHICS_CREATE_GROUP_SET_MAKE_REQUEST
  };
};

var groupSetCreateReceiveResponse = function(success, errors) {
  return {
    type : types.DEMOGRAPHICS_CREATE_GROUP_SET_RECEIVE_RESPONSE,
    success : success,
    errors : errors
  };
};


var requestedGroupDeletion = function (){
  return {
    type : types.DEMOGRAPHICS_DELETE_GROUP_REQUEST_MADE
  };
};

var receivedGroupDeletionResponse = function(success, errors){
  return {
    type : types.DEMOGRAPHICS_DELETE_GROUP_RESPONSE_RECEIVED,
    success : success,
    errors : errors
  };
};


var requestedFavouriteDeletion = function (){
  return {
    type : types.DEMOGRAPHICS_DELETE_FAVOURITE_REQUEST_MADE
  };
};

var receivedFavouriteDeletionResponse = function(success, errors){
  return {
    type : types.DEMOGRAPHICS_DELETE_FAVOURITE_RESPONSE_RECEIVED,
    success : success,
    errors : errors
  };
};


var DemographicActions = {
    
  setGroupsFilter : function(groupsFilter){
    return {
      type : types.DEMOGRAPHICS_SET_GROUPS_FILTER,
      groupsFilter : groupsFilter
    };
  },
  
  setFavouritesFilter : function(favouritesFilter){
    return {
      type : types.DEMOGRAPHICS_SET_FAVOURITES_FILTER,
      favouritesFilter : favouritesFilter
    };
  },
  

  getGroupsAndFavourites : function (){
    return function(dispatch, getState) {
      dispatch(requestedGroupsAndFavourites());
      
      return demographicsAPI.fetchGroups().then(function(response) {
        dispatch(receivedGroups(response.success, response.errors, response.groupListInfo));
        
        return demographicsAPI.fetchFavourites().then(function(response) {
          dispatch(receivedFavourites(response.success, response.errors, response.favouritesInfo));
        }, function(error) {
          dispatch(receivedFavourites(false, error, null));
        });
      }, function(error) {
        dispatch(receivedGroups(false, error, null));
      });
    };
  },
  
  showNewGroupForm : function(){
    return function (dispatch, getState) {
      dispatch(requestedNewGroupPossibleMembers());
      
      return demographicsAPI.fetchPossibleGroupMembers().then(function(response) {
        dispatch(receivedNewGroupPossibleMembers(response.success, response.errors, response.groupMembersInfo));
      }, function(error){
        dispatch(receivedNewGroupPossibleMembers(false, error, null));
      });
    };
  },
  
  hideNewGroupForm : function(){
    return {
      type : types.DEMOGRAPHICS_HIDE_NEW_GROUP_FORM
    };
  },
  
  getGroupMembers : function (group_id){
    return function(dispatch, getState) {
      dispatch(requestedGroupMembers());
      
      return demographicsAPI.fetchPossibleGroupMembers(group_id).then(function(response) {
        dispatch(receivedGroupMembers(response.success, response.errors, response.groupMembersInfo));
      }, function(error) {
        dispatch(receivedGroupMembers(false, error, null));
      });
    };
  },
  
  toggleCandidateGroupMemberToAdd : function(memberId, selected){
    return {
      type : types.DEMOGRAPHICS_TOGGLE_CANDIDATE_GROUP_MEMBER_TO_ADD,
      memberId : memberId,
      selected : selected
    };
  },
  
  toggleCandidateGroupMemberToRemove : function(memberId, selected){
    return {
      type : types.DEMOGRAPHICS_TOGGLE_CANDIDATE_GROUP_MEMBER_TO_REMOVE,
      memberId : memberId,
      selected : selected
    };
  },
  
  addSelectedGroupMembers : function(){
    return {
      type : types.DEMOGRAPHICS_ADD_SELECTED_GROUP_MEMBERS
    };
  },
  
  removeSelectedGroupMembers : function(){
    return {
      type : types.DEMOGRAPHICS_REMOVE_SELECTED_GROUP_MEMBERS
    };
  },
  
  setGroupName : function(groupName){
    return {
      type : types.DEMOGRAPHICS_CREATE_GROUP_SET_NAME,
      groupName : groupName
    };
  },
  
  addGroupValidationErrorsOccurred : function(errors) {
    return {
      type : types.DEMOGRAPHICS_CREATE_GROUP_VALIDATION_ERRORS_OCCURRED,
      errors : errors
    };
  },
  
  addGroupHideErrorAlert : function() {
    return{
      type : types.DEMOGRAPHICS_CREATE_GROUP_HIDE_MESSAGE_ALERT
    };
  },
  
  createGroupSet : function (groupInfo){
    return function(dispatch, getState) {
      dispatch(groupSetCreateMakeRequest());
      
      return demographicsAPI.createGroupSet(groupInfo).then(function(response) {
        dispatch(groupSetCreateReceiveResponse(response.success, response.errors));
      }, function(error) {
        dispatch(groupSetCreateReceiveResponse(false, error));
      });
    };
  },

  showFavouriteGroupForm : function(groupId){
    return {
      type : types.DEMOGRAPHICS_SHOW_FAVOURITE_GROUP_FORM,
      groupId : groupId
    };
  },
  
  hideFavouriteGroupForm : function(){
    return {
      type : types.DEMOGRAPHICS_HIDE_FAVOURITE_GROUP_FORM
    };
  },

  resetDemograhpics : function() {
    return {
      type : types.DEMOGRAPHICS_RESET_COMPONENT
    };
  },
  
  deleteGroup : function(groupId){
    return function (dispatch, getState) {
      dispatch(requestedGroupDeletion());
      return demographicsAPI.deleteGroup(groupId).then(function(response) {
        
        dispatch(receivedGroupDeletionResponse(response.success, response.errors));
      }, function(error){
        
        dispatch(receivedGroupDeletionResponse(false, error));
      });
    };
  },
  
  showModal : function(groupId, title, body, actions){
    return {
      type : types.DEMOGRAPHICS_SHOW_MODAL,
      groupId : groupId,
      title : title,
      body : body,
      actions : actions
    };
  },
  
  hideModal : function(){
    return {
      type : types.DEMOGRAPHICS_HIDE_MODAL
    };
  },
  
  deleteFavourite : function(favouriteId){
    return function (dispatch, getState) {
      dispatch(requestedFavouriteDeletion());
      return demographicsAPI.deleteFavourite(favouriteId).then(function(response) {
        
        dispatch(receivedFavouriteDeletionResponse(response.success, response.errors));
      }, function(error){
        
        dispatch(receivedFavouriteDeletionResponse(false, error));
      });
    };
  },
  
};


module.exports = DemographicActions;

