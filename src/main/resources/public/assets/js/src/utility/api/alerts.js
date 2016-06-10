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
    getUsers: function() {
      console.log('api getUsers');
      return api.json('/action/admin/trial/activity');
    },
    getAnnouncementsHistory: function() {
      return api.json('/action/admin/trial/activity');
    },
    broadcastAnnouncement: function(users, announcement) {
      var receiverAccountList = [];
      for(var obj in users){
        receiverAccountList.push({accountId : users[obj].id, username : users[obj].username, lastName : users[obj].lastName});
      }      
      return api.json('/action/announcement/broadcast', {announcement : announcement, receiverAccountList : receiverAccountList});
    }
};

module.exports = ManageAlertsAPI;
