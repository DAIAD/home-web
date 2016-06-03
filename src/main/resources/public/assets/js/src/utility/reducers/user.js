var types = require('../constants/ActionTypes');

var initialState = {
    isLoading : false,
    application: 'default',
    userInfo : null,
    groupListInfo : null
};

var user = function(state, action) {
  
  switch (action.type) {
  
  case types.USER_REQUEST_USER:
    return Object.assign({}, state, {
      isLoading : true
    });
    
  case types.USER_RECEIVE_USER_INFO:
    return Object.assign({}, state, {
      isLoading : false,
      success : action.success,
      errors : action.errors,
      userInfo : {
        id : action.userInfo.id,
        firstName : action.userInfo.firstName,
        lastName : action.userInfo.lastName,
        email : action.userInfo.email,
        gender : action.userInfo.gender,
        registeredOn : new Date (action.userInfo.registrationDateMils),
        country : action.userInfo.country,
        city : action.userInfo.city,
        address : action.userInfo.address,
        postalCode : action.userInfo.postalCode
      }
    });
    
  case types.USER_RECEIVE_GROUP_MEMBERSHIP_INFO:
    return Object.assign({}, state, {
      isLoading : false,
      success : action.success,
      errors : action.errors,
      groupListInfo : action.groupListInfo
    });
    
  default:
    return state || initialState;
  }
  
};

module.exports = user;