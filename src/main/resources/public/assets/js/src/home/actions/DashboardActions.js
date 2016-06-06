/**
 * Dashboard Actions module.
 * Action creators for Dashboard section
 * 
 * @module DashboardActions
 */

var types = require('../constants/ActionTypes');

var QueryActions = require('./QueryActions');
var HistoryActions = require('./HistoryActions');

var { getDeviceKeysByType, lastNFilterToLength } = require('../utils/device');
var { getTimeByPeriod, getLastShowerTime, getPreviousPeriodSoFar } = require('../utils/time');


const setLastSession = function(session) {
  return {
    type: types.DASHBOARD_SET_LAST_SESSION,
    session
  };
};

const createInfobox = function(data) {
  return {
    type: types.DASHBOARD_ADD_INFOBOX,
    data
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
    layout
  };
};


/**
 * Switches dashboard section mode 
 * @param {String} mode - Mode to switch to - default mode is normal 
 * 
 */
const switchMode = function(mode) {
  return {
    type: types.DASHBOARD_SWITCH_MODE,
    mode
  };
};

/**
 * Adds new infobox to dashboard with provided data 
 * @param {Object} data - Contains all needed options to be saved to infobox state, no check is performed
 * @return {String} id - The id of the added infobox 
 * 
 */
const addInfobox = function(data) {
  return function(dispatch, getState) {
    const infobox = getState().section.dashboard.infobox;

    // find last id and increase by one
    const lastId = infobox.length?Math.max.apply(Math, infobox.map(info => parseInt(info.id))):0;
    const id = (lastId+1).toString();
    const type = data.type;

    dispatch(createInfobox(Object.assign(data, {id})));
    dispatch(appendLayout(id, type));
    return id;
  };
};

/**
 * Updates an existing infobox with provided options.
 * Important: This action triggers data fetch 
 * 
 * @param {String} id - The id of the infobox to update 
 * @param {Object} update - Contains update options to be saved to infobox state (previous options are overriden), no check is performed
 * 
 */
const updateInfobox = function(id, data) {
  return function(dispatch, getState) {
    
    dispatch({
      type: types.DASHBOARD_UPDATE_INFOBOX,
      id,
      data: Object.assign({}, data, {synced:false}),
    });

    dispatch(updateLayoutItem(id, data.display));
    
    dispatch(fetchInfoboxData(Object.assign({}, getState().section.dashboard.infobox.find(i=>i.id===id))));
  };
};

/**
 * Updates an existing infobox with data.
 * Important: This action only sets the data returned by asynchronous fetch action and does not trigger data fetch
 * 
 * @param {String} id - The id of the infobox to update 
 * @param {Object} data - Contains data options to be saved to infobox state
 * 
 */
const setInfoboxData = function(id, data) {
  return {
    type: types.DASHBOARD_UPDATE_INFOBOX,
    id,
    data: Object.assign({}, data, {synced:true})
  };
};

/**
 * Removes an existing infobox from state
 * 
 * @param {String} id - The id of the infobox to remove 
 * 
 */
const removeInfobox = function(id) {
  return {
    type: types.DASHBOARD_REMOVE_INFOBOX,
    id: id
  };
};

/**
 * Updates layout item dimensions based on display
 * 
 * @param {String} id - The id of the infobox appearence to update
 * @param {String} display - One of stat, display
 * 
 */
const updateLayoutItem = function(id, display) {
  return function(dispatch, getState) {

    if (display==null) return;
    
    let layout = getState().section.dashboard.layout.slice();
    const layoutItemIdx = layout.findIndex(i=>i.i===id);
    if (layoutItemIdx==-1) return;

      if (display === 'stat') {
         layout[layoutItemIdx] = Object.assign({}, layout[layoutItemIdx], {w:2, h:1});
      }
      else if (display === 'chart') {
         layout[layoutItemIdx] = Object.assign({}, layout[layoutItemIdx], {w:2, h:2});
      }
      dispatch(updateLayout(layout));
  };
};

/**
 * Fetch data based on provided options and save to infobox
 * 
 * @param {Object} options - Options to fetch data 
 * @param {String} options.id - The id of the infobox to update
 * @param {String} options.deviceType - The type of device to query. One of METER, AMPHIRO
 * @param {String} options.period - The period to query.
 *                                  For METER one of day, week, month, year, custom (time-based)
 *                                  for AMPHIRO one of ten, twenty, fifty (index-based)
 * @param {String} options.type - The infobox type. One of: 
 *                                total (total metric consumption for period and deviceType),
 *                                last (last shower - only for deviceType AMPHIRO),
 *                                efficiency (energy efficiency for period - only for deviceType AMPHIRO, metric energy),
 *                                breakdown (Water breakdown analysis for period - only for deviceType METER, metric difference (volume difference). Static for the moment),
 *                                forecast (Computed forecasting for period - only for deviceType METER, metric difference (volume difference). Static for the moment),
 *                                comparison (Comparison for period and comparison metric - only for deviceType METER. Static for the moment),
 *                                budget (User budget information. Static for the moment)
 *
 */
const fetchInfoboxData = function(options) {
  return function(dispatch, getState) {
    const { id, type, deviceType, period } = options;

    if (!id || !type || !deviceType || !period) throw new Error('fetchInfoboxData: Insufficient data provided');

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
            dispatch(setInfoboxData(id, {data: response.data, index: response.index, device: response.device, showerId: response.id, time: response.timestamp})))
      .catch(error => { 
        //log error in console for debugging and display friendly message
        console.error('Caught error in infobox data fetch:', error); 
        dispatch(setInfoboxData(id, {data: [], error:'Oops, sth went wrong..replace with something friendly'})); });
    }
    //total or efficiency
    else {

      //fetch previous period data for comparison 
      if (deviceType === 'METER') {
        let prevTime = getPreviousPeriodSoFar(period);
        dispatch(QueryActions.queryMeterHistory(device, prevTime))
        .then(data => {
            return dispatch(setInfoboxData(id, {previous:data, time:prevTime}));})
          .catch(error => { 
            console.error('Caught error in infobox previous period data fetch:', error); 
            dispatch(setInfoboxData(id, {previous: [], error: 'Oops sth went wrong, replace with sth friendly'})); });
             

      return dispatch(QueryActions.queryMeterHistory(device, time))
      .then(data =>  
        dispatch(setInfoboxData(id, {data, time})))
      .catch(error => { 
        console.error('Caught error in infobox data fetch:', error); 
        dispatch(setInfoboxData(id, {data: [], error: 'Oops sth went wrong, replace with sth friendly'})); });
      }
      else {
        return dispatch(QueryActions.queryDeviceSessions(device, {type: 'SLIDING', length:lastNFilterToLength(period)}))
        .then(data =>  
          dispatch(setInfoboxData(id, {data})))
        .catch(error => { 
          console.error('Caught error in infobox data fetch:', error); 
          dispatch(setInfoboxData(id, {data: [], error: 'Oops sth went wrong, replace with sth friendly'})); });
      }
    }
  };
};

/**
 * Fetch data for all infoboxes in state 
 * 
 */
const fetchAllInfoboxesData = function() {
  return function(dispatch, getState) {
    getState().section.dashboard.infobox.map(function (infobox) {
      const { type } = infobox;
      if (type === 'total' || type === 'last' || type === 'efficiency' || type === 'comparison' || type === 'breakdown')
      return dispatch(fetchInfoboxData(infobox));
    });
  };
};


/**
 * Updates layout for react-grid-layout
 * @param {Object} layout - layout object produced by react-grid-layout
 * 
 */
const updateLayout = function(layout) {
  return {
    type: types.DASHBOARD_UPDATE_LAYOUT,
    layout
  };
};

module.exports = {
  switchMode,
  addInfobox,
  updateInfobox,
  setInfoboxData,
  updateLayoutItem,
  updateLayout,
  removeInfobox,
  fetchInfoboxData,
  fetchAllInfoboxesData,
};

