var api = require('./base');

var ManageAlertsAPI = {
    getAllUtilities: function(){
        return api.json('/action/utility/fetch/all');
    },
    getTips: function(locale){
        return api.json('/action/recommendation/static/' + locale);
    },
    saveActiveTips: function(changedTips, locale){
      console.log("API " + "/action/recommendation/static/save/" + locale, changedTips);
      return api.json('/action/recommendation/static/save/'+ locale, changedTips);
    },
    editTip: function(tip){
      console.log("editing tip " + tip);
      //return api.json('/action/recommendation/static/edit', tip);
    },
    addTip: function(tip){
      console.log("adding tip " + tip);
      //return api.json('/action/recommendation/static/add', tip);
    }
};

module.exports = ManageAlertsAPI;
