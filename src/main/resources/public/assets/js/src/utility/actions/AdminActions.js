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

  addUserSelectCountry: function(event, country) {
    return{
      type : types.ADMIN_ADD_USER_SELECT_COUNTRY,
      country : country.value
    };
  },

  addUserSelectGroup: function(event, group) {
    return{
      type : types.ADMIN_ADD_USER_SELECT_GROUP,
      group : group.value
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
      city : inputFormFields.city,
      postalCode : inputFormFields.postalCode
    };
  },  
  
  addUserShowErrorAlert: function(errors) {
    return{
      type : types.ADMIN_ADD_USER_SHOW_ERROR_ALERT,
      errors: errors
    };
  },
  
  addUserHideErrorAlert: function() {
    return{
      type : types.ADMIN_ADD_USER_HIDE_ERROR_ALERT
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
