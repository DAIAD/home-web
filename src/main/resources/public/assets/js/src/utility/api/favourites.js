var api = require('./base');

var FavouritesAPI = {
    
    fetchFavouriteQueries: function() {
      return api.json('/action/data/query/load');
    },
    addFavourite: function(request) {
      return api.json('/action/data/query/store', request);
    },
    updateFavourite: function(request) {
      return api.json('/action/data/query/update', request);
    },
    deleteFavourite: function(request) {
      return api.json('/action/data/query/delete', request);
    }
  };

module.exports = FavouritesAPI;
