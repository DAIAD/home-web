var moment = require('moment');

var api = require('./base');

var AdminAPI = {
  getActivity : function() {
    return api.json('/action/admin/trial/activity');
  },

  getSessions : function(userKey) {
    var endDate = moment().valueOf();
    var startDate = moment().subtract(30, 'days').valueOf();

    return api.json('/action/device/session/query', {
      userKey : userKey,
      granularity : 1,
      deviceKey : null,
      startDate : startDate,
      endDate : endDate
    });
  },

  getMeters : function(userKey) {
    var endDate = moment().valueOf();
    var startDate = moment().subtract(30, 'days').valueOf();

    return api.json('/action/meter/history', {
      userKey : userKey,
      granularity : 2,
      deviceKey : null,
      startDate : startDate,
      endDate : endDate
    });
  },

  exportUserData : function(userKey) {
    return api.json('/action/data/export', {
      type : 'USER_DATA',
      userKey : userKey,
      deviceKeys : [],
      startDateTime : null,
      endDateTime : null,
      timezone : 'Europe/Athens'
    });
  },
  
  createNewUser : function (userInfo){
    return api.json('/action/user/create', userInfo);
  },
};

module.exports = AdminAPI;
