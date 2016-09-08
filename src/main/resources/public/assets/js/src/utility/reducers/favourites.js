var types = require('../constants/FavouritesActionTypes');

var initialState = {
  isLoading: false,
  favourites: null,
  showSelected: false,
  selectedFavourite: null
};

var favourites = function (state, action) {
  switch (action.type) {
    case types.FAVOURITES_OPEN_SELECTED:
      return Object.assign({}, state, {
        showSelected: true,
        selectedFavourite: action.selectedFavourite
      });  
    case types.FAVOURITES_CLOSE_SELECTED:
      return Object.assign({}, state, {
        showSelected: false,
        selectedFavourite: null
      });         
    default:
      return state || initialState;
  }
};

module.exports = favourites;