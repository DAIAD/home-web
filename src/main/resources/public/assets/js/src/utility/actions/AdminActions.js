var adminAPI = require('../api/admin');
var types = require('../constants/ActionTypes');

// TODO : Remove jquery dependency
var $ = require('jquery');

var requestedActivity = function() {
  return {
    type : types.ADMIN_REQUESTED_ACTIVITY
  };
};

var receivedActivity = function(success, errors, activity) {
  return {
    type : types.ADMIN_RECEIVED_ACTIVITY,
    success : success,
    errors : errors,
    activity : activity
  };
};

var requestedSessions = function(username) {
  return {
    type : types.ADMIN_REQUESTED_SESSIONS,
    username : username
  };
};

var receivedSessions = function(success, errors, devices) {
  return {
    type : types.ADMIN_RECEIVED_SESSIONS,
    success : success,
    errors : errors,
    devices : devices
  };
};

var requestedMeters = function(username) {
  return {
    type : types.ADMIN_REQUESTED_METERS,
    username : username
  };
};

var receivedMeters = function(success, errors, meters) {
  return {
    type : types.ADMIN_RECEIVED_METERS,
    success : success,
    errors : errors,
    meters : meters
  };
};

var requestedExport = function(username) {
  return {
    type : types.ADMIN_EXPORT_REQUEST,
    username : username
  };
};

var receivedExport = function(success, errors, token) {
  return {
    type : types.ADMIN_EXPORT_COMPLETE,
    success : success,
    errors : errors,
    token : token
  };
};

var resetUserData = function() {
  return {
    type : types.ADMIN_RESET_USER_DATA
  };
};

var addUserMakeRequest = function() {
  return {
    type : types.ADMIN_ADD_USER_MAKE_REQUEST
  };
};

var addUserReceiveResponse = function(success, errors) {
  return {
    type : types.ADMIN_ADD_USER_RECEIVE_RESPONSE,
    success : success,
    errors : errors
  };
};

var addUserGetUtilitiesRequest = function(){
  return {
    type : types.ADMIN_ADD_USER_GET_UTILITIES_MAKE_REQUEST
  };
};

var addUserGetUtilitiesResponse = function(success, utilities, errors){
  return{
    type : types.ADMIN_ADD_USER_GET_UTILITIES_RECEIVE_RESPONSE,
    success : success,
    utilities : utilities,
    errors : errors
  };
};


var AdminActions = {
  showAddUserForm: function() {
    return{
      type : types.ADMIN_ADD_USER_SHOW
    };
  },

  hideAddUserForm: function() {
    return{
      type : types.ADMIN_ADD_USER_HIDE
    };
  },
  
  addUserSelectGenderMale: function() {
    return{
      type : types.ADMIN_ADD_USER_SELECT_GENDER_MALE,
    };
  },

  addUserSelectGenderFemale: function() {
    return{
      type : types.ADMIN_ADD_USER_SELECT_GENDER_FEMALE,
    };
  },

  addUserSelectUtility: function(event, utility) {
    return{
      type : types.ADMIN_ADD_USER_SELECT_UTILITY,
      utility : {
        label : utility.label,
        value : utility.value
      }
    };
  },  
  
  addUserFillForm: function(inputFormFields) {
    return{
      type : types.ADMIN_ADD_USER_FILL_FORM,
      firstName : inputFormFields.firstName,
      lastName : inputFormFields.lastName,
      email : inputFormFields.email,
      gender : inputFormFields.gender,
      address : inputFormFields.address,
      postalCode : inputFormFields.postalCode
    };
  },
  
  addUserValidationsErrorsOccurred: function(errors) {
    return {
      type : types.ADMIN_ADD_USER_VALIDATION_ERRORS_OCCURRED,
      errors : errors
    };
  },
  
  addUserShowMessageAlert: function(errors) {
    return{
      type : types.ADMIN_ADD_USER_SHOW_MESSAGE_ALERT,
      errors: errors
    };
  },
  
  addUserHideErrorAlert: function() {
    return{
      type : types.ADMIN_ADD_USER_HIDE_MESSAGE_ALERT
    };
  },
  
  addUser : function(userInfo) {
    return function(dispatch, getState) {
      dispatch(addUserMakeRequest());
      return adminAPI.createNewUser(userInfo).then(function(response){
        dispatch(addUserReceiveResponse(response.success, response.errors));
      }, function(error) {
        dispatch(addUserReceiveResponse(false, error));
      });
    };
  },

  addUserGetUtilities : function(){
    return function(dispatch, getState) {
      dispatch(addUserGetUtilitiesRequest());
      return adminAPI.getAllUtilities().then(function(response){
        dispatch(addUserGetUtilitiesResponse(response.success, response.utilitiesInfo, response.errors));
      }, function(error) {
        dispatch(addUserReceiveResponse(false, null, error));
      });
    };
  },
    
  getActivity : function() {
    return function(dispatch, getState) {
      dispatch(requestedActivity());

      return adminAPI.getActivity().then(function(response) {
        dispatch(receivedActivity(response.success, response.errors, response.accounts));
      }, function(error) {
        dispatch(receivedActivity(false, error, null));
      });
    };
  },

  getSessions : function(userKey, username) {
    return function(dispatch, getState) {
      dispatch(requestedSessions(username));

      return adminAPI.getSessions(userKey).then(function(response) {
        dispatch(receivedSessions(response.success, response.errors, response.devices));
      }, function(error) {
        dispatch(receivedSessions(false, error, null));
      });
    };
  },

  getMeters : function(userKey, username) {
    return function(dispatch, getState) {
      dispatch(requestedMeters(username));

      return adminAPI.getMeters(userKey).then(function(response) {
        dispatch(receivedMeters(response.success, response.errors, response.series));
      }, function(error) {
        dispatch(receivedMeters(false, error, null));
      });
    };
  },

  exportUserData : function(userKey, username) {
    return function(dispatch, getState) {
      dispatch(requestedExport(username));

      return adminAPI.exportUserData(userKey).then(function(response) {
        dispatch(receivedExport(response.success, response.errors, response.token));

        var content = [];
        content.push('<div id="export-download-frame" style="display: none">');
        content.push('<iframe src="/action/data/download/' + response.token + '/"></iframe>');
        content.push('</div>');

        $('#export-download-frame').remove();
        $('body').append(content.join(''));
      }, function(error) {
        dispatch(receivedMeters(false, error, null));
      });
    };
  },

  resetUserData : function() {
    return resetUserData();
  },

  setFilter : function(filter) {
    return {
      type : types.ADMIN_FILTER_USER,
      filter : filter
    };
  }
};

module.exports = AdminActions;
