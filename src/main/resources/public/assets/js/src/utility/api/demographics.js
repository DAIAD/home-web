var api = require('./base');

var DemographicsAPI = {
    fetchGroups: function() {
      return api.json('/action/group/list');
    }
  };

module.exports = DemographicsAPI;