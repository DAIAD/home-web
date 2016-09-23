var api = require('./base');

var FavouritesAPI = {
    
    fetchFavouriteQueries: function() {
      return api.json('/action/data/query/load');
    },
    addFavourite: function(request) {
      return api.json('/action/data/query/store', request);
    },
    updateQuery: function(query) {
      //return api.json('/action/favourite/update/query/', query);
    },
    deleteQuery: function(query) {
      //return api.json('/action/favourite/delete/query/', query);
    }
  };

module.exports = FavouritesAPI;
