var callAPI = require('./base');

var UserAPI = {
  login: function(data) {
    return callAPI('/api/v1/auth/login', data);
  },
  logout: function() {
    return callAPI('/logout', {});
  },
  getProfile: function() {
    return callAPI('/action/profile/load', {}, "GET");
  }
};

module.exports = UserAPI;

