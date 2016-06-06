var callAPI = require('./base');
var formAPI = require('./form');

var UserAPI = {
  login: function(username, password) {
    const formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);
    
    return formAPI('/login?application=home', { formData });
  },
  logout: function(data) {
    return formAPI('/logout', data);
  },
  getProfile: function() {
    return callAPI('/action/profile/load', {}, "GET");
  },
  saveToProfile: function(data) {
    return callAPI('/action/profile/save', data);
  }
};

module.exports = UserAPI;

