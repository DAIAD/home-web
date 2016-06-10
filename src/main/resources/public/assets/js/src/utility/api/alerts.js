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
    }
};

module.exports = ManageAlertsAPI;
