var types = require('../constants/ActionTypes');
var helpers = require('../helpers/helpers');
var DemographicsTablesSchema = require('../constants/DemographicsTablesSchema');


var initialState = {
    isLoading : false,
    asyncResponse : {
      action : null,
      success : null,
      errors : null,
    },
    application : 'default',
    groupsFilter : '',
    groups : {
      fields : DemographicsTablesSchema.Groups.fields,
      rows : [],
      pager : {
        index : DemographicsTablesSchema.Groups.pager.index,
        size : DemographicsTablesSchema.Groups.pager.size,
        count : 1
      }
    },
    favouritesFilter : '',
    favourites : {
      fields : DemographicsTablesSchema.Favourites.fields,
      rows : [],
      pager : {
        index : DemographicsTablesSchema.Favourites.pager.index,
        size : DemographicsTablesSchema.Favourites.pager.size,
        count : 1
      }
    },
    newGroup : {
      showMessageAlert : false,
      groupMembersInfo : null,
      newGroupName : null,
      currentMembers : {},
      possibleMembers : {},
      candidateMembersToAdd : [],
      candidateMembersToRemove : []
    },
    deleteGroupForm : {
      id : null,
      modal : {
        show : false
      }
    },
    newFavourite : null
};

var createGroupRows = function(groupsInfo){
  var groups = [];
  groupsInfo.forEach(function(g){
    var group = {
        id: g.id,
        name: g.name,
        size: g.numberOfMembers,
        createdOn: new Date (g.creationDateMils)
    };
    groups.push(group);
  });
  return groups;
};

var createFavouriteRows = function(favouritesInfo){
  var favourites = [];
  favouritesInfo.forEach(function(f){
    var favourite = {
        id: f.key,
        refId: f.refId,
        name: f.name,
        type: helpers.toTitleCase(f.type),
        addedOn: new Date (f.additionDateMils)
    };
    favourites.push(favourite);
  });
  return favourites;
};

var createPossibleMembersRows = function(possibleMembersInfo){
  var possibleMembers = {};
  possibleMembersInfo.forEach(function(m){
    var possibleMember = {
        id: m.id,
        name: m.name,
        email: m.email,
        selected: false,
        registeredOn: new Date (m.registrationDateMils)
    };
    possibleMembers[m.id] = possibleMember;
  });
  return possibleMembers;
};

var getCandidateMember = function(possibleMembers, memberId){
  if(possibleMembers.hasOwnProperty(memberId)){
    return possibleMembers[memberId];
  } else {
    return null;
  }
};

var demographics = function(state, action) {
  var newGroup = null;
  var possibleMembers = null;
  var currentMembers = null;
  var candidateMembersToAdd = [];
  var candidateMembersToRemove = [];
  var newCandidateMember = null;
  var index = null;
  var newFavourite = null;
  
  switch (action.type) {
  
  case types.DEMOGRAPHICS_REQUEST_GROUPS_AND_FAVOURITES:
    return Object.assign({}, state, {
      isLoading : true
    });
  
  case types.DEMOGRAPHICS_RECEIVE_GROUPS:
    
    var fields = DemographicsTablesSchema.Groups.fields;
    return Object.assign({}, state, {
      asyncResponse : {
        action : 'GetGroups',
        success : action.success,
        errors : action.errors,
      },
      groups : {
        fields : fields,
        rows : createGroupRows(action.groupsInfo),
        pager : {
          index : DemographicsTablesSchema.Groups.pager.index,
          size : DemographicsTablesSchema.Groups.pager.size,
          count : Math.ceil(action.groupsInfo.length / DemographicsTablesSchema.Groups.pager.size)
        }
      }
    });
    
  case types.DEMOGRAPHICS_RECEIVE_FAVOURITES:
    return Object.assign({}, state, {
      isLoading : false,
      asyncResponse : {
        action : 'GetFavourites',
        success : action.success,
        errors : action.errors,
      },
      favourites : {
        fields : DemographicsTablesSchema.Favourites.fields,
        rows : createFavouriteRows(action.favouritesInfo),
        pager : {
          index : DemographicsTablesSchema.Favourites.pager.index,
          size : DemographicsTablesSchema.Favourites.pager.size,
          count : Math.ceil(action.favouritesInfo.length / DemographicsTablesSchema.Favourites.pager.size)
        }
      }
    });
    
  case types.DEMOGRAPHICS_SET_GROUPS_FILTER:
    return Object.assign({}, state, {
      groupsFilter : action.groupsFilter
    });
    
  case types.DEMOGRAPHICS_SET_FAVOURITES_FILTER:
    return Object.assign({}, state, {
      favouritesFilter : action.favouritesFilter
    });
    
  case types.DEMOGRAPHICS_SHOW_NEW_GROUP_FORM:
    return Object.assign({}, state, {
      isLoading : true,
      application : 'addNewGroup'
    });
    
  case types.DEMOGRAPHICS_RECEIVE_NEW_GROUP_POSSIBLE_MEMBERS:
    newGroup = Object.assign({}, state.newGroup);
    newGroup.possibleMembers = createPossibleMembersRows(action.possibleMembersInfo);
    
    return Object.assign({}, state, {
      isLoading : false,
      asyncResponse : {
        action : 'GetPossibleGroupMembers',
        success : action.success,
        errors : action.errors,
      },
      newGroup : newGroup
    });
    
  case types.DEMOGRAPHICS_HIDE_NEW_GROUP_FORM:
    newGroup = {
      showMessageAlert : false,
      groupMembersInfo : null,
      newGroupName : null,
      currentMembers : {},
      possibleMembers : {},
      candidateMembersToAdd : [],
      candidateMembersToRemove : []
    };
    
    return Object.assign({}, state, {
      application : 'default',
      newGroup : newGroup
    });
    
  case types.DEMOGRAPHICS_REQUEST_GROUP_MEMBERS:
    return Object.assign({}, state, {
      isLoading : true
    });
    
  case types.DEMOGRAPHICS_RECEIVE_GROUP_MEMBERS:    
    return Object.assign({}, state, {
      isLoading : false,
      asyncResponse : {
        action : 'GetGroupMembers',
        success : action.success,
        errors : action.errors,
      },
      possibleMembers : createPossibleMembersRows(action.possibleMembersInfo)
     
    });
    
  case types.DEMOGRAPHICS_TOGGLE_CANDIDATE_GROUP_MEMBER_TO_ADD:
    newGroup = Object.assign({}, state.newGroup);
    candidateMembersToAdd = JSON.parse(JSON.stringify(state.newGroup.candidateMembersToAdd));
    possibleMembers = Object.assign({}, state.newGroup.possibleMembers);
    
    switch (action.selected){
    case false:
      if(candidateMembersToAdd.indexOf(action.memberId) === -1){
        candidateMembersToAdd.push(action.memberId);
      }
      possibleMembers[action.memberId].selected = true;
      break;
      
    case true:
      index = candidateMembersToAdd.indexOf(action.memberId);
      if(index > -1){
        candidateMembersToAdd.splice(index, 1);
      }
      possibleMembers[action.memberId].selected = false;
    }
    
    newGroup.possibleMembers = possibleMembers;
    newGroup.candidateMembersToAdd = candidateMembersToAdd;
    
    return Object.assign({}, state, {
      newGroup : newGroup
    });
    
  case types.DEMOGRAPHICS_TOGGLE_CANDIDATE_GROUP_MEMBER_TO_REMOVE:
    newGroup = Object.assign({}, state.newGroup);
    candidateMembersToRemove = JSON.parse(JSON.stringify(state.newGroup.candidateMembersToRemove));
    currentMembers = Object.assign({}, state.newGroup.currentMembers);
    
    switch (action.selected){
    case false:
      if(candidateMembersToRemove.indexOf(action.memberId) === -1){
        candidateMembersToRemove.push(action.memberId);
      }
      currentMembers[action.memberId].selected = true;
      break;
      
    case true:
      index = candidateMembersToRemove.indexOf(action.memberId);
      if(index > -1){
        candidateMembersToRemove.splice(index, 1);
      }
      currentMembers[action.memberId].selected = false;
    }
    
    newGroup.currentMembers = currentMembers;
    newGroup.candidateMembersToRemove = candidateMembersToRemove;
    
    return Object.assign({}, state, {
      newGroup : newGroup
    });
    
  case types.DEMOGRAPHICS_ADD_SELECTED_GROUP_MEMBERS:
    newGroup = Object.assign({}, state.newGroup);
    currentMembers = Object.assign({}, state.newGroup.currentMembers);
    possibleMembers = Object.assign({}, state.newGroup.possibleMembers);
    candidateMembersToAdd = JSON.parse(JSON.stringify(state.newGroup.candidateMembersToAdd));
   
    candidateMembersToAdd.forEach(function(memberId) {
      if (possibleMembers.hasOwnProperty(memberId) && !currentMembers.hasOwnProperty(memberId)){
        currentMembers[memberId] = Object.assign({}, possibleMembers[memberId]);
        currentMembers[memberId].selected = false;
        delete possibleMembers[memberId];
      }
    });
    
    newGroup.currentMembers = currentMembers;
    newGroup.possibleMembers = possibleMembers;
    newGroup.candidateMembersToAdd = [];
    
    return Object.assign({}, state, {
      newGroup : newGroup
    });
    
  case types.DEMOGRAPHICS_REMOVE_SELECTED_GROUP_MEMBERS:
    newGroup = Object.assign({}, state.newGroup);
    currentMembers = Object.assign({}, state.newGroup.currentMembers);
    possibleMembers = Object.assign({}, state.newGroup.possibleMembers);
    candidateMembersToRemove = JSON.parse(JSON.stringify(state.newGroup.candidateMembersToRemove));
   
    candidateMembersToRemove.forEach(function(memberId){
        if(currentMembers.hasOwnProperty(memberId) && !possibleMembers.hasOwnProperty(memberId)){
          possibleMembers[memberId] = Object.assign({}, currentMembers[memberId]);
          possibleMembers[memberId].selected = false;
          delete currentMembers[memberId];
        }
    });
    
    newGroup.currentMembers = currentMembers;
    newGroup.possibleMembers = possibleMembers;
    newGroup.candidateMembersToRemove = [];
    
    return Object.assign({}, state, {
      newGroup : newGroup
    });
    
  case types.DEMOGRAPHICS_CREATE_GROUP_SET_NAME:
    newGroup = Object.assign({}, state.newGroup);
    newGroup.newGroupName = action.groupName;
    
    return Object.assign({}, state, {
      newGroup : newGroup
    });
    
  case types.DEMOGRAPHICS_CREATE_GROUP_VALIDATION_ERRORS_OCCURRED:
    newGroup = Object.assign({}, state.newGroup, {showMessageAlert : true});
    
    return Object.assign({}, state, {
      isLoading : false,
      asyncResponse : {
        action : 'CreateGroupSet',
        success : action.success,
        errors : action.errors,
      },
      newGroup : newGroup
    });
    
  case types.DEMOGRAPHICS_CREATE_GROUP_HIDE_MESSAGE_ALERT:
    newGroup = Object.assign({}, state.newGroup, {showMessageAlert : false});
    return Object.assign({}, state, {
      newGroup : newGroup
    });
    
  case types.DEMOGRAPHICS_CREATE_GROUP_SET_MAKE_REQUEST:
    return Object.assign({}, state, {
      isLoading : true
    });
    
  case types.DEMOGRAPHICS_CREATE_GROUP_SET_RECEIVE_RESPONSE:
    newGroup = Object.assign({}, state.newGroup);
    
    newGroup.showMessageAlert = true;
    
    if (action.success){
      newGroup.newGroupName = null;
      
      for (let m in newGroup.currentMembers) {
        if (newGroup.currentMembers.hasOwnProperty(m)) {
          newGroup.possibleMembers[m] = newGroup.currentMembers[m];
          delete newGroup.currentMembers[m];
        }
      }
    }
    return Object.assign({}, state, {
      isLoading : false,
      asyncResponse : {
        action : 'CreateGroupSet',
        success : action.success,
        errors : action.errors,
      },
      newGroup : newGroup
    });
   
  case types.DEMOGRAPHICS_SHOW_FAVOURITE_GROUP_FORM:
    return Object.assign({}, state, {
      favouriteGroupId : action.groupId,
      application : 'favouriteGroupForm'
    });
    
  case types.DEMOGRAPHICS_HIDE_FAVOURITE_GROUP_FORM:
    return Object.assign({}, state, {
      application : 'default'
    });
 
    
  case types.DEMOGRAPHICS_RESET_COMPONENT:
    return Object.assign({}, state, {
      application : 'default'
    });
    
  case types.DEMOGRAPHICS_SHOW_MODAL:
    return Object.assign({}, state, {
      deleteGroupForm : {
        id : action.groupId,
        modal : {
          show : true,
          title : action.title,
          body : action.body,
          actions : action.actions
        }
      }
    });
    
  case types.DEMOGRAPHICS_HIDE_MODAL:
    return Object.assign({}, state, {
      deleteGroupForm : {
        id : null,
        modal : {
          show : false
        }
      }
    });
    
    
  case types.DEMOGRAPHICS_DELETE_GROUP_REQUEST_MADE:
    return Object.assign({}, state, {
      isLoading : true
    });
    
  case types.DEMOGRAPHICS_DELETE_GROUP_RESPONSE_RECEIVED:
    
    return Object.assign({}, state, {
      isLoading : false,
      asyncResponse : {
        action : 'DeleteGroup',
        success : action.success,
        errors : action.errors,
      },
    });
    
  case types.DEMOGRAPHICS_DELETE_FAVOURITE_REQUEST_MADE:
    return Object.assign({}, state, {
      isLoading : true
    });
    
  case types.DEMOGRAPHICS_DELETE_FAVOURITE_RESPONSE_RECEIVED:
    
    return Object.assign({}, state, {
      isLoading : false,
      asyncResponse : {
        action : 'DeleteFavourite',
        success : action.success,
        errors : action.errors,
      },
    });
    
    
  default:
    return state || initialState;
  }
  
};

module.exports = demographics;
