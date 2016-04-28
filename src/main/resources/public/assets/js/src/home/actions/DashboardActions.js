var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();

var QueryActions = require('./QueryActions');
var HistoryActions = require('./HistoryActions');

var { getDeviceKeysByType } = require('../utils/device');
var { getTimeByPeriod, getLastShowerTime } = require('../utils/time');

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
      dispatch(appendLayout(id, type));
      return id;
    };
  },
  updateInfobox: function(id, update) {
    return function(dispatch, getState) {
      
      dispatch({
        type: types.DASHBOARD_UPDATE_INFOBOX,
        id,
        update: Object.assign({}, update, {synced:false}),
      });

      //dispatch(DashboardActions.updateInfobox(id, update));
      dispatch(DashboardActions.updateLayoutItem(id, update.type));
      
      dispatch(DashboardActions.fetchInfoboxData(Object.assign({}, getState().section.dashboard.infobox.find(i=>i.id===id))));
    };
  },
  setInfoboxData: function(id, update) {
    return {
      type: types.DASHBOARD_UPDATE_INFOBOX,
      id,
      update: Object.assign({}, update, {synced:true})
    };
  },
  // updates layout item dimensions if type changed
  updateLayoutItem: function(id, type) {
    return function(dispatch, getState) {

      if (type==null) return;
      
      let layout = getState().section.dashboard.layout.slice();
      const layoutItemIdx = layout.findIndex(i=>i.i===id);
      if (layoutItemIdx==-1) return;

        if (type === 'stat') {
           layout[layoutItemIdx] = Object.assign({}, layout[layoutItemIdx], {w:2, h:1});
        }
        else if (type === 'chart') {
           layout[layoutItemIdx] = Object.assign({}, layout[layoutItemIdx], {w:2, h:2});
        }
        dispatch(DashboardActions.updateLayout(layout));
    };
  },
  /*
  updateInfoboxAndQuery: function(id, update) {
    return function(dispatch, getState) {
      dispatch(DashboardActions.updateInfobox(id, update));
      dispatch(DashboardActions.updateLayoutItem(id, update.type));
      
      dispatch(DashboardActions.fetchInfoboxData(Object.assign({}, getState().section.dashboard.infobox.find(i=>i.id===id))));
    };
    },
    */
  removeInfobox: function(id) {
    return {
      type: types.DASHBOARD_REMOVE_INFOBOX,
      id: id
    };
  },
  fetchInfoboxData: function(data) {
    return function(dispatch, getState) {
      const { id, type, subtype, deviceType, period } = data;
      const device = getDeviceKeysByType(getState().user.profile.devices, deviceType);
      let time = getTimeByPeriod(period);
      
      if (!device || !device.length) return new Promise((resolve, reject) => resolve()); 

      const found = getState().section.dashboard.infobox.find(x => x.id === id);

      if (found && found.synced===true) {
      //if (found && found.data && found.data.length>0){
        console.log('found infobox data in memory');
        return new Promise((resolve, reject) => resolve());
        //}
      }

      if (subtype === "last") {
        time = getLastShowerTime();

        return dispatch(QueryActions.fetchLastSession(device, time))
        //.then(session => session)
        .then(response => 
              dispatch(DashboardActions.setInfoboxData(id, {data: response.data, index: response.index, showerId: response.id, time})))
              //dispatch(DashboardActions.updateInfobox(id, {synced: true, index: response.index, showerId: response.id, device:response.device, data:response.data, period: "custom", time})))
        .catch(error => { 
          console.error('Caught error in infobox data fetch:', error); 
          dispatch(DashboardActions.setInfoboxData(id, {data: [], error})); });
      }
      else {
        return dispatch(QueryActions.queryDeviceOrMeter(device, deviceType, time))
        .then(data =>  
              //dispatch(DashboardActions.updateInfobox(id, {synced: true, data:sessions, time})))
        dispatch(DashboardActions.setInfoboxData(id, {data})))
        .catch(error => { 
          console.error('Caught error in infobox data fetch:', error); 
          //dispatch(DashboardActions.updateInfobox(id, {error})); });
          dispatch(DashboardActions.setInfoboxData(id, {data: [], error})); });
      }
    };
  },
  fetchAllInfoboxesData: function() {
    return function(dispatch, getState) {
      getState().section.dashboard.infobox.map(function (infobox) {
        if (infobox.type === 'chart' || infobox.type === 'stat')
        return dispatch(DashboardActions.fetchInfoboxData(infobox));
      });
    };
  },
  updateLayout: function(layout) {
    return {
      type: types.DASHBOARD_UPDATE_LAYOUT,
      layout
    };
  },

};

module.exports = DashboardActions;
