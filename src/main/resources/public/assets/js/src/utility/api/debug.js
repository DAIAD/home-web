var moment = require('moment');

var api = require('./base');

var DebugAPI = {

  createUser : function(password) {
    return api.json('/action/debug/user/create', {
      password : password
    });
  },

  createAmphiro : function() {
    return api.json('/action/debug/amphiro/create');
  },

  generateAmphiroData : function(timezone, files) {
    return api.sendFile('/action/debug/amphiro/data/generate', files, {
      timezone : timezone,
      type: 'AMPHIRO_DATA'
    });
  }

};

module.exports = DebugAPI;
