var api = require('./base');

var UserAPI = {

  filterUserByPrefix : function(prefix) {
    return api.json('/action/user/' + user_id);
  },

  fetchUser : function(user_id) {
    return api.json('/action/user/' + user_id);
  },

  fetchUserGroupMembershipInfo : function(user_id) {
    return api.json('/action/group/list/member/' + user_id);
  },

  addFavorite : function(userKey) {
    return api.json(`/action/favorite/${userKey}`, null, 'PUT');
  },

  removeFavorite : function(userKey) {
    return api.json(`/action/favorite/${userKey}`, null, 'DELETE');
  }
};

module.exports = UserAPI;
