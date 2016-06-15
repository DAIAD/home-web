var api = require('./base');

var GroupAPI = {
    fetchGroupInfo: function(group_id) {      
      return api.json('/action/group/' + group_id);
    },
    
    fetchGroupMembers: function(group_id) {
      return api.json('/action/group/members/current/' + group_id);
    },
    
    create: function(title, members) {
      var data = {
          title: title,
          members:members
      };
      
      return api.json('/action/group' , data, 'PUT');
    },
    
    remove: function(groupKey){
      return api.json(`/action/group/${groupKey}`, null, 'DELETE');
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
