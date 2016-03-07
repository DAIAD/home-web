var callAPI = require('./base');

var UserAPI = {
  login: function(username, password) {
    return callAPI('/login?application=home', {username, password}, "POST", "application/x-www-form-urlencoded; charset=UTF-8", false);
  },
  logout: function() {
    return callAPI('/logout', {});
  },
  getProfile: function() {
    return callAPI('/action/profile/load', null, "GET");
  }
};

module.exports = UserAPI;

