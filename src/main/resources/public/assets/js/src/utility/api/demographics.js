var api = require('./base');

var DemographicsAPI = {
    fetchGroups: function() {
      return api.json('/action/group/list');
    },
    
    fetchFavourites: function() {
      return api.json('/action/favourite/list');
    },
    
    fetchCurrentGroupMembers: function(group_id) {
      return api.json('/action/group/members/current/' + group_id);
    },
    
    fetchPossibleGroupMembers: function(group_id) {
      if (!group_id){
        group_id = '';
      }
      return api.json('/action/group/members/possible/' + group_id);
    },
    
    createGroupSet : function(groupSetInfo) {
      return api.json('/action/group/set/create', groupSetInfo);
    },
    
    deleteGroup: function(group_id) {
      return api.json('/action/group/delete/' + group_id);
    },
    
    deleteFavourite : function(favourite_id) {
      return api.json('/action/favourite/delete/' + favourite_id);
    },
  };

module.exports = DemographicsAPI;
