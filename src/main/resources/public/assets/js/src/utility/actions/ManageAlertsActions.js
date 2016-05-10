var queryAPI = require('../api/query');
var types = require('../constants/ActionTypes');
var adminAPI = require('../api/admin');
var alertsAPI = require('../api/alerts');
var api = require('../api/base');

var getUtilitiesRequest = function(){
    console.log('3 (ManageAlertsActions)IN getUtilitiesRequest');
    return {
        type : types.GET_UTILITIES_REQUEST
    };
};

var getUtilitiesResponse = function(success, utilities, errors){
    
    console.log('6 (ManageAlertsActions) getUtilitiesResponse, utilities ' + Object.keys(utilities) + ", success " + success + ", errors " + errors);
    for(var obj in utilities){
        if(utilities.hasOwnProperty(obj)){
            for(var prop in utilities[obj]){
                if(utilities[obj].hasOwnProperty(prop)){
                    console.log(prop + ':' + utilities[obj][prop]);
                }
            }
        }    
    }
    return{
        type : types.ALERTS_GET_UTILITIES_RECEIVE_RESPONSE,
        success : success,
        utilities : utilities,
        errors : errors
    };
};

var receivedTips = function(success, errors, tips) {
  console.log('action: receivedTips');  
  return {
    type : types.ADMIN_RECEIVED_TIPS,
    success : success,
    errors : errors,
    tips : tips
  };
};

var requestedTips = function(locale) {
   console.log('action: requestedTips');
  return {
    type : types.ADMIN_REQUESTED_TIPS,
    locale : locale
  };
};
 
var ManageAlertsActions = {
    setUtility: function(event, utility) {
        console.log('(ManageAlertsActions) IN setUtility');
        return{
          type : types.ALERTS_UTILITY_SELECTED,
          utility : utility
        }; 
    }, 
    getUtilities : function(){
      
        console.log('2 (ManageAlertsActions)IN getUtilities');
        return alertsAPI.getAllUtilities().then(function(response){

            console.log('5 (ManageAlertsActions) IN getAllUtilities');
            getUtilitiesResponse(response.success, response.utilitiesInfo, response.errors);
        }, function(error) {  
            console.log('(ManageAlertsActions) IN getAllUtilities error ');
            addUserReceiveResponse(false, null, error);
        });                    
    },
    getStaticTips: function(event, utility) {
    
    console.log('action: getStaticTips');
        //TODO - resolve locale for utility
        var locale = "en";    
        if(utility.label == "DAIAD"){
            locale = "en";
        }

        return function(dispatch, getState) {
          dispatch(requestedTips(locale));

          return alertsAPI.getTips().then(function(response) {
            dispatch(receivedTips(response.success, response.errors, response.recommendations));
          }, function(error) {
            dispatch(receivedTips(false, error, null));
          });
        };                   
    }
};

module.exports = ManageAlertsActions;
