var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();

var QueryActions = require('./QueryActions');
var HistoryActions = require('./HistoryActions');

var { getFilteredData } = require('../utils/chart');

const setLastSession = function(session) {
  return {
    type: types.DASHBOARD_SET_LAST_SESSION,
    session: session,
  };
};

const updateInfobox = function(id, data) {
  console.log('gonna send infobox update', id, data);
  return {
    type: types.DASHBOARD_UPDATE_INFOBOX,
    id: id,
    data: data
  };
};

const DashboardActions = {
  switchToEditMode: function() {
    return {
      type: types.DASHBOARD_SWITCH_TO_EDIT
    };
  },
  switchToNormalMode: function() {
    return {
      type: types.DASHBOARD_SWITCH_TO_NORMAL
    };
  },
  updateAllInfoboxes: function() {
    return function(dispatch, getState) {
      console.log('updating all infoboxes');
      getState().section.dashboard.infobox.map(function (infobox) {
        console.log('infobox #', infobox.id);
        console.log(infobox);
        if (infobox.type === "stat") {
          dispatch(QueryActions.queryDeviceOrMeter(infobox.device, infobox.time))
          .then(x => {  console.log('updating infobox', infobox); console.log(x); return x;})
          .then(sessions =>  dispatch(updateInfobox(infobox.id, sessions)))
          .catch(error => { console.log(error); });
        }
        else if (infobox.type === "last") {
          dispatch(QueryActions.fetchLastSession(infobox.device, infobox.time))
          .then(session =>  dispatch(updateInfobox(infobox.id, session)))
          .catch(error => { console.log(error); });
        }
      });
    };
  },
  /*
  getLastSession: function(deviceKey, time) {
    return function(dispatch, getState) {
      //if (getState().section.dashboard.lastSession) { console.log('found in memory'); return true; }
      dispatch(QueryActions.queryDeviceSessions(deviceKey, time))
        .then(sessions => {
          const session = getLastSession(sessions);
          const id = session.id;
          if (!id){ return false;}

          dispatch(QueryActions.fetchDeviceSession(id, deviceKey, time))
          .then(session => {
            console.log('last session', session);
            //dispatch(setLastSession(session));
            return session;
          })
          .catch((error) => {
            return error;
          });
        });
    };
    },
    */
  updateLayout: function(layout) {
    console.log('UPDATEING LAYOUT');
    return {
      type: types.DASHBOARD_UPDATE_LAYOUT,
      layout: layout
    };
  },

};

module.exports = DashboardActions;
