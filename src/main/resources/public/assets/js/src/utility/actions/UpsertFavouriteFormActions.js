var types = require('../constants/ActionTypes');
var UpsertFavouriteFormAPI = require('../api/upsertFavouriteForm');


var requestFavouriteGroupInfo = function (){
  return {
    type : types.UPSERT_FAVOURITE_FORM_GROUP_INFO_REQUEST
  };
};

var receiveFavouriteGroupInfo = function (success, errors, favouriteGroupStatus){
  return {
    type : types.UPSERT_FAVOURITE_FORM_GROUP_INFO_RESPONSE,
    success : success,
    errors : errors,
    favouriteGroupStatus : favouriteGroupStatus
  };
};

var requestFavouriteAccountInfo = function (){
  return {
    type : types.UPSERT_FAVOURITE_FORM_ACCOUNT_INFO_REQUEST
  };
};

var receiveFavouriteAccountInfo = function (success, errors, favouriteAccountStatus){
  return {
    type : types.UPSERT_FAVOURITE_FORM_ACCOUNT_INFO_RESPONSE,
    success : success,
    errors : errors,
    favouriteAccountStatus : favouriteAccountStatus
  };
};

var upsertFavouriteMakeRequest = function() {
  return {
    type : types.UPSERT_FAVOURITE_FORM_UPSERT_FAVOURITE_REQUEST
  };
};

var upsertFavouriteReceiveResponse = function(success, errors) {
  return {
    type : types.UPSERT_FAVOURITE_FORM_UPSERT_FAVOURITE_RESPONSE,
    success : success,
    errors : errors
  };
};


var UpsertFavouriteFormActions = {
    
    fetchFavouriteGroupStatus : function(groupId){
      return function (dispatch, getState) {
        dispatch(requestFavouriteGroupInfo());
        
        return UpsertFavouriteFormAPI.fetchFavouriteGroupStatus(groupId).then(function(response) {
          dispatch(receiveFavouriteGroupInfo(response.success, response.errors, response.favouriteGroupInfo));
          
        }, function(error){
          dispatch(receiveFavouriteGroupInfo(false, error, null));
        });
      };
    },
    
    fetchFavouriteAccountStatus : function(accountId){
      return function (dispatch, getState) {
        dispatch(requestFavouriteAccountInfo());
        
        return UpsertFavouriteFormAPI.fetchFavouriteAccountStatus(accountId).then(function(response) {
          dispatch(receiveFavouriteAccountInfo(response.success, response.errors, response.favouriteAccountInfo));
          
        }, function(error){
          dispatch(receiveFavouriteAccountInfo(false, error, null));
        });
      };
    },
    
    setFavouriteLabel : function (newLabel){
      return {
        type : types.UPSERT_FAVOURITE_FORM_SET_FAVOURITE_LABEL,
        label : newLabel
      };
    },
    
    upsertFavourite : function(favouriteInfo) {
      return function (dispatch, getState) {
        dispatch(upsertFavouriteMakeRequest());
        
        return UpsertFavouriteFormAPI.upsertFavourite(favouriteInfo).then( function(response){
          dispatch(upsertFavouriteReceiveResponse(response.success, response.errors));
          
        } , function(error){
          dispatch(upsertFavouriteReceiveResponse(false, error));
        });
        
      };
    },
    
    upsertFavouriteValidationErrorsOccurred : function(errors) {
      return {
        type : types.UPSERT_FAVOURITE_FORM_VALIDATION_ERRORS_OCCURRED,
        errors : errors
      };
    },
    
    hideMessageAlert : function() {
      return{
        type : types.UPSERT_FAVOURITE_FORM_HIDE_MESSAGE_ALERT
      };
    },
    
};


module.exports = UpsertFavouriteFormActions;
