var types = require('../constants/ActionTypes');
var alertsAPI = require('../api/alerts');

var receivedTips = function(success, errors, tips) {
  console.log('action: receivedTips');  
  return {
    type : types.ADMIN_RECEIVED_STATIC_TIPS,
    success : success,
    errors : errors,
    tips : tips
  };
};

var requestedTips = function(locale) {
   console.log('action: requestedTips');
  return {
    type : types.ADMIN_REQUESTED_STATIC_TIPS,
    locale : locale
  };
};

var requestedUtilities = function() {
  return {
    type : types.ADMIN_REQUESTED_UTILITIES,
  };
};

var receivedUtilities = function(success, errors, utilities) {
  return {
    type : types.ADMIN_RECEIVED_UTILITIES,
    success : success,
    errors : errors,
    utilities : utilities
  };
};

var clickedSaveButton = function() {
  console.log('clickedSaveButton');
  return {
    type : types.ADMIN_CLICKED_SAVE_BUTTON
  };  
};

var saveButtonResponse = function(success, errors) {
  return {
    type : types.ADMIN_SAVE_BUTTON_RESPONSE,
    success : success,
    errors : errors
  };  
};

var ManageAlertsActions = {
    disableSaveButton : function(event, disable){
      console.log('ACTION disableSaveButton');
        return{
          type : types.SAVE_BUTTON_DISABLE,
          saveButtonDisabled : disable
        };      
    },
    saveActiveTips : function(event, changedTips, locale){
      //console.log('saveActiveTips, length ' + changedTips.length);
      return function(dispatch, getState) {
        dispatch(clickedSaveButton());
        return alertsAPI.saveActiveTips(changedTips, locale).then(function(response) {   
          console.log('response.success ' + response.success);
            dispatch(saveButtonResponse(response.success, response.errors));
        }, function(error) {saveButtonResponse(false, error, null);
          });
      };      
    },
    setUtility: function(event, utility) {
        return{
          type : types.ADMIN_SELECTED_UTILITY_FILTER,
          utility : utility
        }; 
    }, 
    fetchUtilities : function(event){
      return function(dispatch, getState) {
        dispatch(requestedUtilities());
        return alertsAPI.getAllUtilities().then(function(response) {                  
            dispatch(receivedUtilities(response.success, response.errors, response.utilitiesInfo));
        }, function(error) {receivedUtilities(false, error, null);
          });
      };
    },
    getStaticTips: function(event, utility) {   
      console.log('action: getStaticTips');
      var locale;    
      if(utility.label == "DAIAD"){
        locale = "en";
      }
      else if(utility.label == "Alicante"){
        locale = "es";
      }
      else{
        locale = "en";
      }
      return function(dispatch, getState) {
        dispatch(requestedTips(locale));

        return alertsAPI.getTips(locale).then(function(response) {
          dispatch(receivedTips(response.success, response.errors, response.messages));
        }, function(error) {
          dispatch(receivedTips(false, error, null));
        });
      };                   
    }
};

module.exports = ManageAlertsActions;
