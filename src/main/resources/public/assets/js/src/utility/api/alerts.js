var api = require('./base');

var ManageAlertsAPI = {
    fetchTips: function(filter) {
        return api.json('/action/profile/tips/list', filter);
    },

    fetchFilterOptions: function(){
            return api.json('/action/profile/modes/filter/options');
    },
    getAllUtilities: function(){
        console.log('api: getAllUtilities');
        return api.json('/action/utility/fetch/all');
    },
    getTips: function(){
        console.log('api: getTips');
        return api.json('/action/recommendation/static/en');
    }        
};

module.exports = ManageAlertsAPI;
