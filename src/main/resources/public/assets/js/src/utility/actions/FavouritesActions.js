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

var FavouritesActions = {

  fetchFavouriteQueries : function() {
    return function(dispatch, getState) {
      dispatch(requestedFavouriteQueries());
      return favouritesAPI.fetchFavouriteQueries().then(function (response) {
        dispatch(receivedFavouriteQueries(response.success, response.errors, response.favourites));
      }, function (error) {
        dispatch(receivedFavouriteQueries(false, error, null));
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
  }        
    
};


module.exports = FavouritesActions;
