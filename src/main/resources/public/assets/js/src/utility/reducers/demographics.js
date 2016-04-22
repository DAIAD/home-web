var types = require('../constants/ActionTypes');
var DemographicsTablesSchema = require('../constants/DemographicsTablesSchema');

var initialState = {
    isLoading : false,
    groups : {
      fields : DemographicsTablesSchema.Groups.fields,
      rows : [],
      pager : {
        index : DemographicsTablesSchema.Groups.pager.index,
        size : DemographicsTablesSchema.Groups.pager.size,
        count : 1
      }
    }
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

var demographics = function(state, action) {
  switch (action.type) {
  case types.DEMOGRAPHICS_REQUEST_GROUPS:
    return Object.assign({}, state, {
      isLoading : true
    });
    
  case types.DEMOGRAPHICS_RECEIVE_GROUPS:
    return Object.assign({}, state, {
      isLoading : false,
      success : action.success,
      errors : action.errors,
      groups : {
        fields : DemographicsTablesSchema.Groups.fields,
        rows : createGroupRows(action.groupsInfo),
        pager : {
          index : DemographicsTablesSchema.Groups.pager.index,
          size : DemographicsTablesSchema.Groups.pager.size,
          count : Math.ceil(action.groupsInfo.length / DemographicsTablesSchema.Groups.pager.size)
        }
      }
    });
    
  default:
    return state || initialState;
  }
  
};

module.exports = demographics;
