var moment = require('moment');

var api = require('./base');

var AdminAPI = {
  getActivity : function() {
    return api.json('/action/admin/trial/activity');
  },
  getSessions : function(userKey) {
    var endDate = moment().valueOf();
    var startDate = moment().subtract(20, 'days').valueOf();

    return api.json('/action/device/session/query',{
      userKey : userKey,
      granularity : 1,
      deviceKey : null,
      startDate : startDate,
      endDate : endDate
    });
  },
};

module.exports = AdminAPI;
