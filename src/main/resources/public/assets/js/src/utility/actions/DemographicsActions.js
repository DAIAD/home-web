var types = require('../constants/ActionTypes');
var demographicsAPI = require('../api/demographics');

var requestedGroups = function() {
  return {
    type : types.DEMOGRAPHICS_REQUEST_GROUPS
  };
};

var receivedGroups = function(success, errors, groupsInfo) {
  return {
    type : types.DEMOGRAPHICS_RECEIVE_GROUPS,
    success : success,
    errors : errors,
    groupsInfo : groupsInfo
  };
};

var requestedFavourites = function() {
  return {
    type : types.DEMOGRAPHICS_REQUEST_FAVOURITES
  };
};

var receivedFavourites = function(success, errors, favouritesInfo) {
  return {
    type : types.DEMOGRAPHICS_RECEIVE_FAVOURITES,
    success : success,
    errors : errors,
    favouritesInfo : favouritesInfo
  };
}; 

var DemographicActions = {
    
  setGroupsFilter : function(groupsFilter){
    return {
      type : types.DEMOGRAPHICS_SET_GROUPS_FILTER,
      groupsFilter : groupsFilter
    };
  },
  
  setFavouritesFilter : function(favouritesFilter){
    return {
      type : types.DEMOGRAPHICS_SET_FAVOURITES_FILTER,
      favouritesFilter : favouritesFilter
    };
  },

  getGroups : function() {
    return function(dispatch, getState) {
      dispatch(requestedGroups());

      return demographicsAPI.fetchGroups().then(function(response) {
        dispatch(receivedGroups(response.success, response.errors, response.groupsInfo));
      }, function(error) {
        dispatch(receivedGroups(false, error, null));
      });
    };
  },
  
  getFavourites : function (){
    return function(dispatch, getState) {
      dispatch(requestedFavourites());
      
      return demographicsAPI.fetchFavourites().then(function(response) {
        dispatch(receivedFavourites(response.success, response.errors, response.favouritesInfo));
      }, function(error) {
        dispatch(receivedFavourites(false, error, null));
      });
    };
  }
};





module.exports = DemographicActions;

