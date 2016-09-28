var { getTimeByPeriod, getLastShowerTime, getPreviousPeriodSoFar } = require('../utils/time');
var { getDeviceKeysByType } = require('../utils/device');
var { lastNFilterToLength } =  require('../utils/general');

/*
/**
 * Dashboard Actions module.
 * Action creators for Dashboard section
 * 
 * @module DashboardActions
 */

var types = require('../constants/ActionTypes');

var QueryActions = require('./QueryActions');
var HistoryActions = require('./HistoryActions');


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

const appendLayout = function(id, display, type) {
  let layout = {x:0, y:0, w:1, h:1, static:false, i:id};
  //if (display==='stat') {
  if (display === 'stat' || (display === 'chart' && type === 'budget')) {
    Object.assign(layout, {w:2, h:1, minW:2, minH:1, maxH:1});
  }
  else if (display === 'chart') {
    Object.assign(layout, {w:2, h:2, minW:2, minH:2, maxH:2});
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
const addInfobox = function(options) {
  return function(dispatch, getState) {
    const infobox = getState().section.dashboard.infobox;

    // find last id and increase by one
    const lastId = infobox.length?Math.max.apply(Math, infobox.map(info => parseInt(info.id))):0;
    const id = (lastId+1).toString();
    const display = options.display;
    const type = options.type;

    dispatch(createInfobox(Object.assign({}, options, {id})));
    dispatch(appendLayout(id, display, type));

    dispatch(updateInfobox(id, {}));
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

    dispatch(updateLayoutItem(id, data.display, data.type));

    dispatch(setDirty()); 
    
    const infobox = getState().section.dashboard.infobox.find(i=>i.id===id);
    const { type, deviceType, period } = infobox;
    const deviceKey = getDeviceKeysByType(getState().user.profile.devices, deviceType);
    
    infobox.time = infobox.time ? infobox.time : getTimeByPeriod(period);

    if (deviceType === 'METER') {
        infobox.query = {
          cache: true,
          deviceKey,
          csrf: getState().user.csrf 
        };

        if (type === 'total') {
          infobox.prevTime = getPreviousPeriodSoFar(period);
        }
    }
    else if (deviceType === 'AMPHIRO') {
      infobox.query = {
        cache: true,
        type: 'SLIDING',
        length: lastNFilterToLength(period),
        deviceKey,
        csrf: getState().user.csrf 
      };
    }

    return dispatch(QueryActions.fetchInfoboxData(Object.assign({}, infobox)))
    .then(res => dispatch(setInfoboxData(id, res)))
    .catch(error => { 
          console.error('Caught error in infobox data fetch:', error); 
          dispatch(setInfoboxData(id, {data: [], error: 'Oops sth went wrong, please refresh the page.'})); 
    });

  };
};

/**
 * Sets infoboxes 
 * 
 * @param {Object[]} infoboxes - array of objects containing infobox options as specified in {@link fetchInfoboxData}.  
 */
const setInfoboxes = function (infoboxes) {
  return {
    type: types.DASHBOARD_SET_INFOBOXES,
    infoboxes
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
  return function(dispatch, getState) {
    dispatch(setDirty());
    dispatch({
      type: types.DASHBOARD_REMOVE_INFOBOX,
      id: id
    });
  };
};

/**
 * Updates layout item dimensions based on display
 * 
 * @param {String} id - The id of the infobox appearence to update
 * @param {String} display - One of stat, display
 * 
 */
const updateLayoutItem = function(id, display, type) {
  return function(dispatch, getState) {

    if (display==null) return;
    
    let layout = getState().section.dashboard.layout.slice();
    const layoutItemIdx = layout.findIndex(i=>i.i===id);
    if (layoutItemIdx==-1) return;

      if (display === 'stat' || (display === 'chart' && type === 'budget')) {
         layout[layoutItemIdx] = Object.assign({}, layout[layoutItemIdx], {w:2, h:1});
      }
      else if (display === 'chart') {
         layout[layoutItemIdx] = Object.assign({}, layout[layoutItemIdx], {w:2, h:2});
      }
      dispatch(updateLayout(layout));
  };
};
/*
const prepareInfoboxes = function() {
  return function(dispatch, getState) {
    console.log('preparing infoboxes', getState().section.dashboard.infobox);
    return getState().section.dashboard.infobox.map(infobox => {

      const { type, deviceType, period, id } = infobox;


      if (!id || !type || !deviceType || !period) throw new Error('prepareInfoboxes: Insufficient data provided');

      const options = {};
      options.time = infobox.time ? infobox.time : getTimeByPeriod(period);
      const deviceKey = getDeviceKeysByType(getState().user.profile.devices, deviceType);

      let prevTime = null;

      if (deviceType === 'METER') {
        options.query = {
          cache: true,
          deviceKey,
          csrf: getState().user.csrf 
        };

        if (type === 'total') {
          options.prevTime = getPreviousPeriodSoFar(period);
        }
      }
      else if (deviceType === 'AMPHIRO') {
        options.query = {
          cache: true,
          type: 'SLIDING',
          length: lastNFilterToLength(period),
          deviceKey,
          csrf: getState().user.csrf 
        };
      }

      dispatch(setInfoboxData(id, options));
    });

  };
};
*/
/**
 * Fetch data for all infoboxes in state 
 * 
 */
const fetchAllInfoboxesData = function() {
  return function(dispatch, getState) {
  /*
   * sequential execution to take advantage of cache
   */
    return getState().section.dashboard.infobox.map(infobox => {
      return updateInfobox(infobox.id, {});
    })
    .reduce((prev, curr, i, arr) => prev.then(() => dispatch(curr)), Promise.resolve());
  };
};

const setInfoboxToAdd = function(data) {
  return {
    type: types.DASHBOARD_SET_INFOBOX_TEMP,
    data
  };
};

const resetInfoboxToAdd = function() {
  return {
    type: types.DASHBOARD_RESET_INFOBOX_TEMP,
  };
};

/**
 * Updates layout for react-grid-layout
 * @param {Object} layout - layout object produced by react-grid-layout
 * 
 */
const updateLayout = function(layout, dirty=true) {
  return function(dispatch, getState) {
    if (dirty) {
      dispatch(setDirty());
    }
    dispatch({
      type: types.DASHBOARD_UPDATE_LAYOUT,
      layout
    });
  };
};

/**
 * Sets dirty mode for dashboard in order to prompt for save 
 * 
 */
const setDirty = function() {
  return {
    type: types.DASHBOARD_SET_DIRTY
  };
};

/**
 * Resets mode to clean after save or user dismiss 
 * 
 */

const resetDirty = function() {
  return {
    type: types.DASHBOARD_RESET_DIRTY
  };
};

module.exports = {
  resetDirty,
  setDirty,
  switchMode,
  addInfobox,
  updateInfobox,
  setInfoboxData,
  setInfoboxes,
  updateLayoutItem,
  updateLayout,
  removeInfobox,
  fetchAllInfoboxesData,
  setInfoboxToAdd,
  resetInfoboxToAdd
};

