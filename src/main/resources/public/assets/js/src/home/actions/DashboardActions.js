var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();

var QueryActions = require('./QueryActions');
var HistoryActions = require('./HistoryActions');

var { getDeviceKeysByType, lastNFilterToLength } = require('../utils/device');
var { getTimeByPeriod, getLastShowerTime, getPreviousPeriodSoFar } = require('../utils/time');

const setLastSession = function(session) {
  return {
    type: types.DASHBOARD_SET_LAST_SESSION,
    session: session,
  };
};

const createInfobox = function(data) {
  return {
    type: types.DASHBOARD_ADD_INFOBOX,
    data
  };
};

const appendLayout = function(id, display) {
  let layout = {x:0, y:0, w:1, h:1, i:id};
  if (display==='stat') {
    Object.assign(layout, {w:2, h:1});
  }
  else if (display === 'chart') {
    Object.assign(layout, {w:2, h:2});
  }
  return {
    type: types.DASHBOARD_APPEND_LAYOUT,
    layout
  };
};


const DashboardActions = {

  switchMode: function(mode) {
    return {
      type: types.DASHBOARD_SWITCH_MODE,
      mode
    };
  },
  addInfobox: function(data) {
    return function(dispatch, getState) {
      const infobox = getState().section.dashboard.infobox;
      const maxId = infobox.length?Math.max.apply(Math, infobox.map(info => parseInt(info.id))):0;
      const id = (maxId+1).toString();
      const display = data.display;
      dispatch (createInfobox(Object.assign(data, {id})));
      dispatch(appendLayout(id, display));
      
      dispatch(DashboardActions.fetchInfoboxData(Object.assign({}, getState().section.dashboard.infobox.find(i=>i.id===id))));

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

      dispatch(DashboardActions.updateLayoutItem(id, update.display));
      
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

      if (type === "last") {

        return dispatch(QueryActions.fetchLastDeviceSession(device))
        .then(response => 
              dispatch(DashboardActions.setInfoboxData(id, {data: response.data, index: response.index, device: response.device, showerId: response.id, time: response.timestamp})))
        .catch(error => { 
          //log error in console for debugging and display friendly message
          console.error('Caught error in infobox data fetch:', error); 
          dispatch(DashboardActions.setInfoboxData(id, {data: [], error:'Oops, sth went wrong..replace with something friendly'})); });
      }
      //total or efficiency
      else {

        //fetch previous period data for comparison 
        if (deviceType === 'METER') {
          let prevTime = getPreviousPeriodSoFar(period);
          dispatch(QueryActions.queryDeviceOrMeter(device, deviceType, prevTime))
          .then(data => {
              return dispatch(DashboardActions.setInfoboxData(id, {previous:data, time:prevTime}));})
            .catch(error => { 
              console.error('Caught error in infobox previous period data fetch:', error); 
              dispatch(DashboardActions.setInfoboxData(id, {previous: [], error: 'Oops sth went wrong, replace with sth friendly'})); });
               

        return dispatch(QueryActions.fetchMeterHistory(device, time))
        .then(data =>  
          dispatch(DashboardActions.setInfoboxData(id, {data, time})))
        .catch(error => { 
          console.error('Caught error in infobox data fetch:', error); 
          dispatch(DashboardActions.setInfoboxData(id, {data: [], error: 'Oops sth went wrong, replace with sth friendly'})); });
        }
        else {
          return dispatch(QueryActions.queryDeviceSessions(device, {type: 'SLIDING', length:lastNFilterToLength(period)}))
          .then(data =>  
            dispatch(DashboardActions.setInfoboxData(id, {data})))
          .catch(error => { 
            console.error('Caught error in infobox data fetch:', error); 
            dispatch(DashboardActions.setInfoboxData(id, {data: [], error: 'Oops sth went wrong, replace with sth friendly'})); });
        }
      }
    };
  },
  fetchAllInfoboxesData: function() {
    return function(dispatch, getState) {
      getState().section.dashboard.infobox.map(function (infobox) {
        const { type } = infobox;
        if (type === 'total' || type === 'last' || type === 'efficiency' || type === 'comparison' || type === 'breakdown')
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
