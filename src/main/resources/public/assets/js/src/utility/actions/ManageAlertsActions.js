var queryAPI = require('../api/query');
var types = require('../constants/ActionTypes');



var ManageAlertsActions = {
  setGroup: function(event, group) {
      console.log('IN action');
    return{
      type : types.ALERTS_GROUP_SELECTED,
      group : group
    };
  }
};

module.exports = ManageAlertsActions;
