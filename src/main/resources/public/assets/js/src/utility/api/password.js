var api = require('./base');

var PasswordAPI = {

  reset: function(token, password) {
    return api.json('/action/user/password/reset/token/redeem', {
      token : token,
      password : password
    });
  }

};

module.exports = PasswordAPI;
