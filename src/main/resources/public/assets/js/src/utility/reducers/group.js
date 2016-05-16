var types = require('../constants/ActionTypes');

var initialState = {
    isLoading : false,
    application: 'default',
    groupInfo : null,
    currentMembers : null
};

var createMembersRows = function(membersInfo){
  var members = {};
  membersInfo.forEach(function(m){
    var member = {
        id: m.id,
        user: m.name,
        registeredOn: new Date (m.registrationDateMils),
        email: m.email
    };
    members[m.id] = member;
  });
  return members;
};

var group = function(state, action) {
  
  switch (action.type) {
  
  case types.GROUP_REQUEST_GROUP:
    return Object.assign({}, state, {
      isLoading : true
    });
    
  case types.GROUP_RECEIVE_GROUP_INFO:
    return Object.assign({}, state, {
      success : action.success,
      errors : action.errors,
      groupInfo : {
        name : action.groupInfo.name,
        description : action.groupInfo.name,
        createdOn : new Date (action.groupInfo.creationDateMils),
        country : action.groupInfo.country,
        size : action.groupInfo.numberOfMembers
      }
    });
    
  case types.GROUP_RECEIVE_GROUP_MEMBERS:
    return Object.assign({}, state, {
      isLoading : false,
      success : action.success,
      errors : action.errors,
      currentMembers : createMembersRows(action.members)
    });
    
  case types.GROUP_SHOW_FAVOURITE_GROUP_FORM:
    return Object.assign({}, state, {
      application : 'favouriteGroupForm'
    });
    
  case types.GROUP_HIDE_FAVOURITE_GROUP_FORM:
    return Object.assign({}, state, {
      application : 'default'
    });
 
    
  case types.GROUP_RESET_COMPONENT:
    return Object.assign({}, state, {
      application : 'default'
    });
    
  case types.GROUP_SHOW_FAVOURITE_ACCOUNT_FORM:
    return Object.assign({}, state, {
      application : 'favouriteAccountForm',
      accountId : action.accountId
    });
    
  case types.GROUP_HIDE_FAVOURITE_ACCOUNT_FORM:
    return Object.assign({}, state, {
      application : 'default'
    });
    
  default:
    return state || initialState;
  }
  
};

module.exports = group;