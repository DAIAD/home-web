//var queryAPI = require('../api/query');
var types = require('../constants/ActionTypes');
//var adminAPI = require('../api/admin');
var alertsAPI = require('../api/alerts');
//var api = require('../api/base');

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
   console.log('action: requestedUtilities');
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
 
var ManageAlertsActions = {
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

          for (var key in response){
              console.log("response["+ key +"]="+ response[key]);
          }
          dispatch(receivedTips(response.success, response.errors, response.messages));
        }, function(error) {
          dispatch(receivedTips(false, error, null));
        });
      };                   
    }
};

module.exports = ManageAlertsActions;
