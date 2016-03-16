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
        .then((response) => {
          if (!response.devices.length || !response.devices[0].sessions.length) { return false; }
          const session = getLastSession(response.devices[0].sessions);
          const id = session.id;
          if (!id){ return false;}

          dispatch(QueryActions.fetchDeviceSession(id, deviceKey, time))
          .then(response => {
            dispatch(setLastSession(response.session?response.session:{}));
            return response;
          })
          .catch((error) => {
            return error;
          });
        });
    };
  },

};

module.exports = DashboardActions;
