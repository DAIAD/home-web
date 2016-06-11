var types = require('../constants/ActionTypes');
var alertsAPI = require('../api/alerts');

var requestedCurrentUtilityUsers = function() {
  return {
    type : types.ANNC_REQUESTED_USERS,
    isLoading: true
  };
};

var receivedCurrentUtilityUsers = function(success, errors, accounts) {
  var initialUsers = [];
  for(var obj in accounts){
    var currentId, currentUsername, currentLastName, elementTemp;  
    for(var prop in accounts[obj]){    
      if(prop == "accountId"){
        currentId = accounts[obj][prop];
      } 
      else if(prop == "lastName"){
        currentLastName = accounts[obj][prop];
      }
      else if(prop == "username"){
        currentUsername = accounts[obj][prop];
      } 
    }  
    elementTemp = {id: currentId, lastName: currentLastName, username : currentUsername, selected: false};
    initialUsers.push(elementTemp);
  }  
  return {
    type : types.ANNC_RECEIVED_USERS,
    isLoading: false,
    success : success,
    errors : errors,
    accounts : initialUsers
  };
};

var requestedAnnouncementsHistory = function() {
  return {
    type : types.ANNC_REQUESTED_ANNOUNCEMENT_HISTORY,
    isLoading: true
  };
};

var receivedAnnouncementsHistory = function(success, errors, announcements) {
  return {
    type : types.ANNC_RECEIVED_ANNOUNCEMENT_HISTORY,
    isLoading: false,
    success: success,
    errors: errors,
    announcements: announcements
  };
};

var requestedBroadcast = function() {
  return {
    type : types.ANNC_BROADCAST_ANNOUNCEMENT_REQUEST,
    isLoading: true
  };
};

var broadcastAnnouncementResponse = function(success, errors) {
  return {
    type : types.ANNC_BROADCAST_ANNOUNCEMENT_RESPONSE,
    success: success,
    errors: errors,
    isLoading: false
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
  getAnnouncementHistory : function(event) {
    return function(dispatch, getState) {
      dispatch(requestedAnnouncementsHistory());

      return alertsAPI.getAnnouncements().then(function(response) {
        dispatch(receivedAnnouncementsHistory(response.success, response.errors, response.messages));
      }, function(error) {
        dispatch(receivedAnnouncementsHistory(false, error, []));
      });
    };
  },  
  setSelectedUser : function(accounts, accountId, selected) {
    var changedAccounts = [];
    for(var obj in accounts){
      var currentId, currentUsername, currentLastName, elementTemp, tempSelected;          
      for(var prop in accounts[obj]){
        if(prop == "id"){
          if(accounts[obj][prop] == accountId){
            tempSelected = !selected;
          }
          else{
            tempSelected = accounts[obj].selected;
          }
          currentId = accounts[obj][prop];
        }
        else if(prop == "lastName"){
          currentLastName = accounts[obj][prop];
        }
        else if(prop == "username"){
          currentUsername = accounts[obj][prop];
        }    
      } 
      elementTemp = {id: currentId, lastName: currentLastName, username : currentUsername, selected: tempSelected};
      changedAccounts.push(elementTemp); 
    }  
    return{
      type: types.ANNC_INITIAL_USERS_SET_SELECTED,
      rowIdToggled: accountId,
      accounts: changedAccounts
    };
  },
  setSelectedAddedUser : function(addedUsers, accountId, selected) {
    var changedAccounts = [];
    for(var obj in addedUsers){
      var currentId, currentUsername, currentLastName, elementTemp, tempSelected;          
      for(var prop in addedUsers[obj]){
        if(prop == "id"){
          if(addedUsers[obj][prop] == accountId){
            tempSelected = !selected;
          }
          else{
            tempSelected = addedUsers[obj].selected;
          }
          currentId = addedUsers[obj][prop];
        }
        else if(prop == "lastName"){
          currentLastName = addedUsers[obj][prop];
        }
        else if(prop == "username"){
          currentUsername = addedUsers[obj][prop];
        }    
      } 
      elementTemp = {id: currentId, lastName: currentLastName, username : currentUsername, selected: tempSelected};
      changedAccounts.push(elementTemp); 
    }  
    return{
      type: types.ANNC_ADDED_USERS_SET_SELECTED,
      rowIdToggled: accountId,
      addedUsers: changedAccounts
    };
  },  
  addUsers: function(addedUsers){
    return{
      type: types.ANNC_ADD_USERS_BUTTON_CLICKED,
      addedUsers: addedUsers
    };    
  },
  removeUsers: function(remainingAddedUsers){
    return{
      type: types.ANNC_REMOVE_USERS_BUTTON_CLICKED,
      addedUsers: remainingAddedUsers
    };    
  },
  showForm: function(){
    return{
      type: types.ANNC_SHOW_FORM,
      showForm: true
    };    
  }, 
  cancelShowForm: function () {
    return{
      type: types.ANNC_CANCEL_SHOW_FORM,
      showForm: false
    };
  },  
  broadCastAnnouncement: function (event, users, announcement) { 
    return function(dispatch, getState) {
      dispatch(requestedBroadcast());
      return alertsAPI.broadcastAnnouncement(users, announcement).then(function(response) {
        dispatch(broadcastAnnouncementResponse(response.success, response.errors));
    
        dispatch(requestedAnnouncementsHistory());
        return alertsAPI.getAnnouncements().then(function(response) {
          dispatch(receivedAnnouncementsHistory(response.success, response.errors, response.messages));
        }, function(error) {
          dispatch(receivedAnnouncementsHistory(false, error, []));
        });        
      }, function(error) {
        dispatch(broadcastAnnouncementResponse(false, error, null));
      });
    };
  }, 
  setFilter : function(filter) {
    return {
      type : types.ANNC_FILTER_USERS,
      filter : filter
    };
  }
};

module.exports = AnnouncementsActions;
