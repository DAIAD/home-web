var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();

var QueryActions = require('./QueryActions');
var HistoryActions = require('./HistoryActions');

var { getLastSession } = require('../utils/device');

const setLastSession = function(session) {
  return {
    type: types.DASHBOARD_SET_LAST_SESSION,
    session: session,
  };
};

const DashboardActions = {
  updateInfobox: function(id, data) {
    return {
      type: types.DASHBOARD_UPDATE_INFOBOX,
      id: id,
      data: data
    };
  },
  queryDevice: function(deviceKey, time) {
    return function(dispatch, getState) {
      console.log('querying device with', deviceKey, time);

      return dispatch(QueryActions.queryDeviceSessions(deviceKey, time))
      .then(sessions => {
        console.log('got', sessions);
          //dispatch(setSessions(sessions));
          //dispatch(resetDataDirty());
          return sessions;
        })
        .catch(error => {
          console.log(`oops error (${error}) while getting all sessions`);
        });

    }; 
  },
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
            console.log('last session', session);
            dispatch(setLastSession(session));
            return session;
          })
          .catch((error) => {
            return error;
          });
        });
    };
  },
  updateLayout: function(layout) {
    console.log('UPDATEING LAYOUT');
    return {
      type: types.DASHBOARD_UPDATE_LAYOUT,
      layout: layout
    };
  },

};

module.exports = DashboardActions;
