var api = require('./base');

var DemographicsAPI = {
    fetchGroups: function() {
      return api.json('/action/group/list');
    },
    
    fetchFavourites: function() {
      return api.json('/action/favourite/list');
    }
  };

module.exports = DemographicsAPI;
