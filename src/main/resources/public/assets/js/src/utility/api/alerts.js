var api = require('./base');

var ManageAlertsAPI = {
    getAllUtilities: function(){
        return api.json('/action/utility/fetch/all');
    },
    getTips: function(locale){
        return api.json('/action/recommendation/static/' + locale);
    }        
};

module.exports = ManageAlertsAPI;
