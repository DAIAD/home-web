var api = require('./base');

var ManageAlertsAPI = {
    getAllUtilities: function(){
        return api.json('/action/utility/fetch/corresponding');
    },    
    getTips: function(locale){      
        return api.json('/action/recommendation/static/' + locale);
    },
    saveActiveTips: function(changedRows){
      return api.json('/action/recommendation/static/status/save/', changedRows);
    },
    insertTip: function(tip){
      return api.json('/action/recommendation/static/insert', tip);
    },
    deleteTip: function(tip){
      return api.json('/action/recommendation/static/delete', tip);
    },
    getCurrentUtilityUsers: function() {
      console.log('api getCurrentUtilityUsers');
      return api.json('/action/admin/trial/activity');
    },
};

module.exports = ManageAlertsAPI;
