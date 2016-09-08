var types = require('../constants/FavouritesActionTypes');

var FavouritesActions = {
    
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
    }       
    
};


module.exports = FavouritesActions;
