var callAPI = require('./base');
var formAPI = require('./form');

var UserAPI = {
  login: function(data) {
    
    const { csrf, username, password } = data;
  
    let formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);
    
    return formAPI('/login?application=home', { csrf, data: formData});
    //return callAPI('/api/v1/rest/auth/login', data);
  },
  logout: function() {
    return formAPI('/logout', {});
  },
  getProfile: function() {
    return callAPI('/action/profile/load', {}, "GET");
  }
};

module.exports = UserAPI;

