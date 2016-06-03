var api = require('./base');

var UpsertFavouriteFormAPI = {
    
    fetchFavouriteGroupStatus: function(group_id) {
      return api.json('/action/favourite/check/group/' + group_id);
    },
    
    fetchFavouriteAccountStatus: function(account_id) {
      return api.json('/action/favourite/check/account/' + account_id);
    },
    
    upsertFavourite: function(favouriteInfo) {
      return api.json('/action/favourite/upsert', favouriteInfo);
    }
  };

module.exports = UpsertFavouriteFormAPI;
