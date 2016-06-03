var api = require('./base');

var UserAPI = {
    
  fetchUserInfo: function(user_id) {      
    return api.json('/action/user/' + user_id);
  },

  fetchUserGroupMembershipInfo: function(user_id) {      
    return api.json('/action/group/list/member/' + user_id);
  }
};

module.exports = UserAPI;