var types = require('../constants/ActionTypes');

var initialState = {
    isloading: false,
    favouriteInfo : null,
    showMessageAlert: false,
    success: null,
    errors: null
};


var upsertFavouriteForm = function(state, action) {
  
  var favouriteInfo;
  var showMessageAlert;
  
  switch (action.type) {
  
  case types.UPSERT_FAVOURITE_FORM_ACCOUNT_INFO_REQUEST:
    return Object.assign({}, state, {
      isLoading : true
    });
    
  case types.UPSERT_FAVOURITE_FORM_ACCOUNT_INFO_RESPONSE:
    favouriteInfo = {};
    if (action.success){
      favouriteInfo.id = action.favouriteAccountStatus.key;
      favouriteInfo.itemId = action.favouriteAccountStatus.refId;
      favouriteInfo.label = action.favouriteAccountStatus.name;
      favouriteInfo.accountName = action.favouriteAccountStatus.accountName;
      favouriteInfo.accountEmail = action.favouriteAccountStatus.email;
      favouriteInfo.accountGender = action.favouriteAccountStatus.gender;
      favouriteInfo.accountDevicesNum = action.favouriteAccountStatus.numOfDevices;
      favouriteInfo.accountCity = action.favouriteAccountStatus.city;
      favouriteInfo.accountCountry = action.favouriteAccountStatus.country;
      favouriteInfo.accountCreatedOn = new Date (action.favouriteAccountStatus.accountCreationDateMils);
      favouriteInfo.addedOn = action.favouriteAccountStatus.additionDateMils > 0 ? new Date (action.favouriteAccountStatus.additionDateMils) : null;
      favouriteInfo.included = action.favouriteAccountStatus.key ? true : false;
      showMessageAlert = false;
    } else {
      showMessageAlert = true;
    }
    
    return Object.assign({}, state, {
      isLoading : false,
      favouriteInfo : favouriteInfo,
      showMessageAlert : showMessageAlert,
      success : action.success,
      errors : action.errors
    });
  
  case types.UPSERT_FAVOURITE_FORM_GROUP_INFO_REQUEST:
    return Object.assign({}, state, {
      isLoading : true
    });

  
  case types.UPSERT_FAVOURITE_FORM_GROUP_INFO_RESPONSE:
    favouriteInfo = {};

    if (action.success){
      favouriteInfo.id = action.favouriteGroupStatus.key;
      favouriteInfo.itemId = action.favouriteGroupStatus.refId;
      favouriteInfo.label = action.favouriteGroupStatus.name;
      favouriteInfo.groupName = action.favouriteGroupStatus.groupName;
      favouriteInfo.groupSize = action.favouriteGroupStatus.size;
      favouriteInfo.groupCountry = action.favouriteGroupStatus.country;
      favouriteInfo.groupCreatedOn = new Date (action.favouriteGroupStatus.groupCreationDateMils);
      favouriteInfo.addedOn = action.favouriteGroupStatus.additionDateMils > 0 ? new Date (action.favouriteGroupStatus.additionDateMils) : null;
      favouriteInfo.included = action.favouriteGroupStatus.key ? true : false;
      showMessageAlert = false;
    } else {
      showMessageAlert = true;
    }
    
    return Object.assign({}, state, {
      isLoading : false,
      favouriteInfo : favouriteInfo,
      showMessageAlert : showMessageAlert,
      success : action.success,
      errors : action.errors
    });
  
    
  case types.UPSERT_FAVOURITE_FORM_SET_FAVOURITE_LABEL:
    favouriteInfo = Object.assign({}, state.favouriteInfo);
    favouriteInfo.label = action.label;
    
    return Object.assign({}, state, {
      favouriteInfo : favouriteInfo
    });
    
    
  case types.DEMOGRAPHICS_FAVOURITE_GROUP_FORM_HIDE_MESSAGE_ALERT:
    return Object.assign({}, state, {
      showMessageAlert : false
    });
    
    
  case types.UPSERT_FAVOURITE_FORM_UPSERT_FAVOURITE_REQUEST:
    return Object.assign({}, state, {
      isLoading : true
    });
    
    
  case types.UPSERT_FAVOURITE_FORM_UPSERT_FAVOURITE_RESPONSE:
    favouriteInfo = Object.assign({}, state.favouriteInfo);
    
    if(action.success){
      if(!state.favouriteInfo.included){
        favouriteInfo.justAdded = true;
      } else {
        favouriteInfo.justAdded = false;
      }
      favouriteInfo.included = true;
    }
    
    return Object.assign({}, state, {
      isLoading : false,
      favouriteInfo : favouriteInfo,
      showMessageAlert : true,
      success : action.success,
      errors : action.errors
    });
    
    
  case types.UPSERT_FAVOURITE_FORM_VALIDATION_ERRORS_OCCURRED:    
    
    return Object.assign({}, state, {
      isLoading : false,
      success : false,
      errors : action.errors,
      showMessageAlert : true
    });
    

    
    
  
  default:
    return state || initialState;
  }
  
};

module.exports = upsertFavouriteForm;
