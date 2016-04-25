var types = require('../constants/ActionTypes');
var helpers = require('../helpers/helpers');
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
    },
    
    favourites : {
      fields : DemographicsTablesSchema.Favourites.fields,
      rows : [],
      pager : {
        index : DemographicsTablesSchema.Favourites.pager.index,
        size : DemographicsTablesSchema.Favourites.pager.size,
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

var createFavouriteRows = function(favouritesInfo){
  var favourites = [];
  favouritesInfo.forEach(function(f){
    var favourite = {
        id: f.refId,
        name: f.name,
        type: helpers.toTitleCase(f.type),
        addedOn: new Date (f.additionDateMils)
    };
    favourites.push(favourite);
  });
  return favourites;
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
    
  case types.DEMOGRAPHICS_REQUEST_FAVOURITES:
    return Object.assign({}, state, {
      isLoading : true
    });
    
  case types.DEMOGRAPHICS_RECEIVE_FAVOURITES:
    return Object.assign({}, state, {
      isLoading : false,
      success : action.success,
      errors : action.errors,
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
    
  default:
    return state || initialState;
  }
  
};

module.exports = demographics;
