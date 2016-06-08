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

    dispatch(createInfobox(Object.assign(options, {id})));
    dispatch(appendLayout(id, display));

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

    dispatch(updateLayoutItem(id, data.display));
    
    dispatch(QueryActions.fetchInfoboxData(Object.assign({}, getState().section.dashboard.infobox.find(i=>i.id===id))))
    .then(res => dispatch(setInfoboxData(id, res)))
    .catch(error => { 
          console.error('Caught error in infobox data fetch:', error); 
          dispatch(setInfoboxData(id, {data: [], error: 'Oops sth went wrong, replace with sth friendly'})); 
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
 * Fetch data for all infoboxes in state 
 * 
 */
const fetchAllInfoboxesData = function() {
  return function(dispatch, getState) {
    getState().section.dashboard.infobox.map(function (infobox) {
      const { id, type, synced } = infobox;
      //if (type === 'total' || type === 'last' || type === 'efficiency' || type === 'comparison' || type === 'breakdown')
        if (synced === true) { 
          console.log('found infobox data in memory');
          return Promise.resolve(); 
        }
        return dispatch(QueryActions.fetchInfoboxData(infobox))
      .then(res =>  
          dispatch(setInfoboxData(id, res)))
        .catch(error => { 
          console.error('Caught error in infobox data fetch:', error); 
          dispatch(setInfoboxData(id, {data: [], error: 'Oops sth went wrong, replace with sth friendly'})); });

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
  fetchAllInfoboxesData,
};

