var types = require('../constants/ActionTypes');


var dashboard = function (state, action) {
  //initial state
  if (state === undefined) {
    state = {
      lastSession: null
    };
  }
   
  switch (action.type) {
      case types.DASHBOARD_SET_LAST_SESSION:
        return Object.assign({}, state, {
          lastSession: action.session
        });
      
      default:
        return state;
  }
};

module.exports = dashboard;

