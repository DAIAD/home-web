var api = require('./base');

var GroupAPI = {
    fetchGroupInfo: function(group_id) {      
      return api.json('/action/group/' + group_id);
    },
    
    fetchGroupMembers: function(group_id) {
      return api.json('/action/group/members/current/' + group_id);
    },
    
    getGroups: function(query) {
      return api.json('/action/group' , query);
    },
    
    addFavorite: function(groupKey) {
      return api.json(`/action/group/favorite/${groupKey}` , null, 'PUT');
    },
    
    removeFavorite: function(groupKey) {
      return api.json(`/action/group/favorite/${groupKey}` , null, 'DELETE');
    },
    
    deleteGroup: function(groupKey) {
      return api.json(`/action/group/${groupKey}` , null, 'DELETE');
    }
  };

module.exports = GroupAPI;
