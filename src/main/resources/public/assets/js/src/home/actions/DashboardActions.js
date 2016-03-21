var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();

var QueryActions = require('./QueryActions');
var { getLastSession } = require('../utils/device');

const setLastSession = function(session) {
  return {
    type: types.DASHBOARD_SET_LAST_SESSION,
    session: session,
  };
};

const DashboardActions = {
  
  getLastSession: function(deviceKey, time) {
    return function(dispatch, getState) {
      if (getState().section.dashboard.lastSession) { console.log('found in memory'); return true; }
      dispatch(QueryActions.queryDeviceSessions(deviceKey, time))
        .then(sessions => {
          const session = getLastSession(sessions);
          const id = session.id;
          if (!id){ return false;}

          dispatch(QueryActions.fetchDeviceSession(id, deviceKey, time))
          .then(session => {
            dispatch(setLastSession(session));
            return session;
          })
          .catch((error) => {
            return error;
          });
        });
    };
  },

};

module.exports = DashboardActions;
