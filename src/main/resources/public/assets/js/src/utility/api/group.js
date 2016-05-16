var api = require('./base');

var GroupAPI = {
    fetchGroupInfo: function(group_id) {      
      return api.json('/action/group/' + group_id);
    },
    
    fetchGroupMembers: function(group_id) {
      return api.json('/action/group/members/current/' + group_id);
    },
  };

module.exports = GroupAPI;
