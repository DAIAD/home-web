var types = require('../constants/FavouritesActionTypes');
var moment = require('moment');

var initialState = {
  isLoading: false,
  isActiveFavourite: false,
  favourites: null,
  showSelected: false,
  selectedFavourite: null,
  showDeleteMessage: false,
  favouriteToBeDeleted: null
};

var favourites = function (state, action) {
  switch (action.type) {
    case types.FAVOURITES_REQUEST_QUERIES:
      return Object.assign({}, state, {
        isLoading: true
      });
    case types.FAVOURITES_RECEIVE_QUERIES:
      return Object.assign({}, state, {
        isLoading: false,
        favourites: action.favourites
      });       
    case types.FAVOURITES_OPEN_SELECTED:
      return Object.assign({}, state, {
        showSelected: true,
        selectedFavourite: action.selectedFavourite
      });  
    case types.FAVOURITES_CLOSE_SELECTED:
      return Object.assign({}, state, {
        isActiveFavourite: false,
        showSelected: false,
        selectedFavourite: null
      });
    case types.FAVOURITES_SET_ACTIVE_FAVOURITE:
      return Object.assign({}, state, {
        isActiveFavourite: true,
        selectedFavourite: action.selectedFavourite
      });  
    case types.FAVOURITES_ADD_FAVOURITE_REQUEST:
      return Object.assign({}, state, {
        isLoading : true
      });
    case types.FAVOURITES_ADD_FAVOURITE_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false
      });  
    case types.FAVOURITES_DELETE_QUERY_REQUEST:
      return Object.assign({}, state, {
        showDeleteMessage : true,
        favouriteToBeDeleted: action.favouriteToBeDeleted
      });    
    case types.FAVOURITES_CONFIRM_DELETE_QUERY:
      return Object.assign({}, state, {
        isLoading : true
      });  
    case types.FAVOURITES_CANCEL_DELETE_QUERY:
      return Object.assign({}, state, {
        showDeleteMessage : false
      });       
    case types.FAVOURITES_DELETE_QUERY_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
        showDeleteMessage : false,
        isActiveFavourite: false,
        showSelected: false,        
        selectedFavourite: null
      });         
    default:
      return state || initialState;
  }
};

module.exports = favourites;