var types = require('../constants/FavouritesActionTypes');
var favouritesAPI = require('../api/favourites');

var requestedFavouriteQueries = function () {
  return {
    type: types.FAVOURITES_REQUEST_QUERIES
  };
};

var receivedFavouriteQueries = function (success, errors, favourites) {
  return {
    type: types.FAVOURITES_RECEIVE_QUERIES,
    success: success,
    errors: errors,    
    favourites: favourites
  };
};

var addFavouriteRequest = function () {
  return {
    type: types.FAVOURITES_ADD_FAVOURITE_REQUEST
  };
};

var addFavouriteResponse = function (success, errors) {
  return {
    type: types.FAVOURITES_ADD_FAVOURITE_RESPONSE,
    success: success,
    errors: errors
  };
};

var deleteFavouriteResponse = function (success, errors) {
  return {
    type: types.FAVOURITES_DELETE_QUERY_RESPONSE,
    success: success,
    errors: errors
  };
};

var FavouritesActions = {

  fetchFavouriteQueries : function() {
    return function(dispatch, getState) {
      dispatch(requestedFavouriteQueries());
      return favouritesAPI.fetchFavouriteQueries().then(function (response) {
        dispatch(receivedFavouriteQueries(response.success, response.errors, response.queries));
      }, function (error) {
        dispatch(receivedFavouriteQueries(false, error, null));
      });
    };
  },
  
  addCopy : function(favourite) {
    return function(dispatch, getState) {
      dispatch(addFavouriteRequest());
      return favouritesAPI.addFavourite(favourite).then(function (response) {
        dispatch(addFavouriteResponse(response.success, response.errors));     
        dispatch(requestedFavouriteQueries());
        return favouritesAPI.fetchFavouriteQueries().then(function (response) {
          dispatch(receivedFavouriteQueries(response.success, response.errors, response.queries));
        }, function (error) {
          dispatch(receivedFavouriteQueries(false, error, null));
        });      
          }, function (error) {
            dispatch(addFavouriteResponse(false, error));
        });
    };
  },
  deleteFavourite : function(event) {
    return function(dispatch, getState) {     
      dispatch(addFavouriteRequest());
      var fav = getState(event).favourites.favouriteToBeDeleted;
      return favouritesAPI.deleteFavourite(fav).then(function (response) {
        dispatch(deleteFavouriteResponse(response.success, response.errors));     
        dispatch(requestedFavouriteQueries());
        return favouritesAPI.fetchFavouriteQueries().then(function (response) {
          dispatch(receivedFavouriteQueries(response.success, response.errors, response.queries));
        }, function (error) {
          dispatch(receivedFavouriteQueries(false, error, null));
        });      
          }, function (error) {
            dispatch(deleteFavouriteResponse(false, error));
        });
    };
  },
  openFavourite : function(favourite) {
    return {
      type : types.FAVOURITES_OPEN_SELECTED,
      showSelected : true,
      selectedFavourite: favourite
    };
  },
  
  closeFavourite : function() {
    return{
      type : types.FAVOURITES_CLOSE_SELECTED,
      showSelected : false,
      selectedFavourite : null
    };
  },
  
  setActiveFavourite : function(favourite) {

    return {
      type: types.FAVOURITES_SET_ACTIVE_FAVOURITE,
      selectedFavourite: favourite 
    };
  },
  openWarning : function(favourite) {
    return {
      type : types.FAVOURITES_DELETE_QUERY_REQUEST,
      favouriteToBeDeleted: favourite
    };
  },    
  closeWarning : function() {
    return {
      type : types.FAVOURITES_CANCEL_DELETE_QUERY,
      favouriteToBeDeleted: null
    };
  },
  resetMapState : function() {
    return {
      type : types.FAVOURITES_RESET_MAP_STATE
    };
  } 
};


module.exports = FavouritesActions;
