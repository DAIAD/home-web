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

const createInfobox = function(data) {
  return {
    type: types.DASHBOARD_ADD_INFOBOX,
    data: data
  };
};

const deleteInfobox = function(id) {
  return {
    type: types.DASHBOARD_REMOVE_INFOBOX,
    id: id
  };
};

const updateInfobox = function(id, data) {
  return {
    type: types.DASHBOARD_UPDATE_INFOBOX,
    id: id,
    data: data
  };
};

const appendLayout = function(id, type) {
  let layout = {x:0, y:0, w:1, h:1, i:id};
  if (type==='stat') {
    Object.assign(layout, {w:2, h:1, minW:2, minH:1});
  }
  else if (type === 'chart') {
    Object.assign(layout, {w:2, h:2, minW:2, minH:2});
  }
  return {
    type: types.DASHBOARD_APPEND_LAYOUT,
    layout: layout 
  };
};

const DashboardActions = {

  updateInfoboxTemp: function(data) {
    return {
      type: types.DASHBOARD_UPDATE_INFOBOX_TEMP,
      data: data
    };
  },
  resetInfoboxTemp: function() {
    return {
      type: types.DASHBOARD_RESET_INFOBOX_TEMP,
    };
  },

  switchMode: function(mode) {
    return {
      type: types.DASHBOARD_SWITCH_MODE,
      mode: mode
    };
  },
  addInfobox: function(data) {
    return function(dispatch, getState) {
      const infobox = getState().section.dashboard.infobox;
      const lastId = infobox.length?Math.max.apply(Math, infobox.map(info => parseInt(info.id))):0;
      const id = (lastId+1).toString();
      const type = data.type;
      dispatch (createInfobox(Object.assign(data, {id})));
      console.log('added infobox', id);
      dispatch(appendLayout(id, type));
      return id;
    };
  },
  removeInfobox: function(id) {
    return function(dispatch, getState) {
      dispatch(deleteInfobox(id));
    };
  },

  fetchInfoboxData: function(data) {
    return function(dispatch, getState) {
      console.log('feticing infobox', data);
      const { id, type, subtype, device, time } = data;
      console.log('fetching infobox data with subtype', subtype);
      if (subtype === "total") {
      //if (type === "stat") {
        return dispatch(QueryActions.queryDeviceOrMeter(device, time))
        .then(x => {  console.log('before updating infobox', x); return x;})
        //.then(sessions => sessions.length?sessions.map((x, i, array) => array[i-1]?Object.assign({}, array[i], {volume:(array[i].volume-array[i-1].volume)}):array[i]):sessions)
        .then(x => {  console.log('after updating infobox', x); return x;})
        
        .then(sessions =>  dispatch(updateInfobox(id, sessions)))
        .catch(error => { console.error(error); });
      }
      else if (subtype === "last") {
        return dispatch(QueryActions.fetchLastSession(device, time))
        .then(session => {console.log('before uodate', session); return session;})
        .then(session =>  dispatch(updateInfobox(id, session)))
        .catch(error => { console.error(error); });
      }
    };
  },
  fetchAllInfoboxesData: function() {
    return function(dispatch, getState) {
      
      getState().section.dashboard.infobox.map(function (infobox) {
        console.log('infobox #', infobox);
        const { id, type, subtype, device, time } = infobox;
        return dispatch(DashboardActions.fetchInfoboxData({id, type, subtype, device, time}));
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
