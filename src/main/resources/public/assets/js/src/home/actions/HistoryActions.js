/**
 * History Actions module.
 * Action creators for History section
 * 
 * @module HistoryActions
 */

var types = require('../constants/ActionTypes');
var { push } = require('react-router-redux');
var { getSessionById, getDeviceKeysByType, getDeviceTypeByKey, lastNFilterToLength, getIdRangeByIndex } = require('../utils/device');
var { getTimeByPeriod, getPreviousPeriod, convertGranularityToPeriod, getGranularityByDiff } = require('../utils/time');

var QueryActions = require('./QueryActions');


const setSessions = function (sessions) {
  return {
    type: types.HISTORY_SET_SESSIONS,
    sessions
  };
};

const setComparisonSessions = function (sessions) {
  return {
    type: types.HISTORY_SET_COMPARISON_SESSIONS,
    sessions
  };
};

const setSession = function (session) {
  return {
    type: types.HISTORY_SET_SESSION,
    session,
  };
};

const setDataSynced = function () {
  return {
    type: types.HISTORY_SET_DATA_SYNCED
  };
};

const setDataUnsynced = function () {
  return {
    type: types.HISTORY_SET_DATA_UNSYNCED
  };
};


/**
 * Updates all history options provided and switches to history section
 *
 * @param {Object} options - Contains all needed options for history
 * @param {String} options.deviceType - Active device type. One of AMPHIRO, METER
 * @param {Array} options.device - Array of device keys to limit active devices (if not provided all devices are active)
 * @param {String} options.metric - Active metric filter. If METER difference, if AMPHIRO volume, energy, temperature, duration
 * @param {String} options.period - Active period.
 *                                  For METER one of day, week, month, year, custom (time-based)
 *                                  for AMPHIRO one of ten, twenty, fifty (index-based)
 * @param {Object} options.time - Active time window
 * @param {Number} options.time.startDate - Start timestamp 
 * @param {Number} options.time.endDate - End timestamp
 * @param {Number} options.time.granularity - Granularity for data aggregation. One of 0: minute, 1: hour, 2: day, 3: week, 4: month
 * @param {Number} options.showerId - The active session id. Together with device indicates unique session to set active (device array must only have one entry)
 * @param {Object} options.data - If provided data will be copied to history section. Used to avoid extra fetch
 */
const linkToHistory = function(options) {
  return function(dispatch, getState) {
    const { showerId, device, deviceType, metric, period, time, data } = options;
    
    if (deviceType) dispatch(setActiveDeviceType(deviceType, false));
    if (metric) dispatch(setMetricFilter(metric));
    if (period) dispatch(setTimeFilter(period));
    if (time) dispatch(setTime(time, false));

    if (device != null && showerId != null) { 
      dispatch(setSessionFilter(metric)); 
      dispatch(setActiveSession(Array.isArray(device)?device[0]:device, showerId)); 
    }
    else { 
      dispatch(resetActiveSession()); 
    }
    
    if (data && data.length>0) { 
      dispatch(setSessions(data));
      dispatch(setDataSynced());
      }

    dispatch(push('/history'));
  };
};

/**
 * Fetches device session (uniquely defined by deviceKey, id) and updates history data
 *
 * @param {Number} id - Session id to fetch 
 * @param {String} deviceKey - Device key session id corresponds to
 * @return {Promise} Resolved or rejected promise with session data if resolved, errors if rejected
 */
const fetchDeviceSession = function (id, deviceKey) {
  return function(dispatch, getState) {
    const devFound = getState().section.history.data.find(d=>d.deviceKey===deviceKey);
    const sessions = devFound?devFound.sessions:[];
    const found = getSessionById(sessions, id); 
    
    if (found && found.measurements){
      console.log('found session in memory');
      return new Promise((resolve, reject) => resolve());
    }
    return dispatch(QueryActions.fetchDeviceSession(id, deviceKey))
    .then(session => { 
        dispatch(setSession(Object.assign({}, session, {deviceKey})));
        return session;
      })
      .catch(error => {
        console.error('error fetching sesssion', error);
      });
  };
};

/**
 * Sets active time window in history section
 *
 * @param {Object} time - Active time window
 * @param {Number} time.startDate - Start timestamp 
 * @param {Number} time.endDate - End timestamp
 * @param {Number} time.granularity - Granularity for data aggregation. One of 0: minute, 1: hour, 2: day, 3: week, 4: month
 * @param {Bool} query=true - If true performs query based on active filters to update data
 */
const setTime = function(time, query=true) {
  return function(dispatch, getState) {
    dispatch({
      type: types.HISTORY_SET_TIME,
      time
    });
    if (query) { 
      dispatch(setDataUnsynced());
      dispatch(fetchData());
    }
  };
};

/**
 * Updates active time window in history section
 * Same as setTime, only without providing granularity which is computed based on difference between startDate, endDate
 * See {@link setTime}
 */
const updateTime = function(time, query=true) {
  return function(dispatch, getState) {
    let { startDate, endDate } = time;
    startDate = startDate || getState().section.history.time.startDate;
    endDate = endDate || getState().section.history.time.endDate;

    const granularity = getGranularityByDiff(startDate, endDate);

    dispatch(setTime({startDate, endDate, granularity}, query));
  };
};

/**
 * Performs query based on selected history section filters and saves data
 */
const fetchData = function () {
  return function(dispatch, getState) {
    if (getState().section.history.activeDeviceType === 'AMPHIRO') {
      
      if (getState().section.history.activeDevice.length === 0) {
        dispatch(setSessions([]));
        dispatch(setDataSynced());
        return;
      }

      dispatch(QueryActions.queryDeviceSessions(getState().section.history.activeDevice, {type: 'SLIDING', length: lastNFilterToLength(getState().section.history.timeFilter)}))
        .then(sessions => dispatch(setSessions(sessions)))
        .then(() => dispatch(setDataSynced()))
        .catch(error => { 
          console.error('Caught error in history device query:', error); 
          dispatch(setSessions([]));
          dispatch(setDataSynced());
        });
    }
    else if (getState().section.history.activeDeviceType === 'METER') {
      dispatch(QueryActions.queryMeterHistory(getState().section.history.activeDevice, getState().section.history.time))
        .then(sessions => dispatch(setSessions(sessions)))
        .then(() => dispatch(setDataSynced()))
        .catch(error => { 
          console.error('Caught error in history meter query:', error); 
          dispatch(setSessions([]));
          dispatch(setDataSynced());
        });
    }


      if (getState().section.history.comparison === 'last') {
        dispatch(QueryActions.queryMeterHistory(getState().section.history.activeDevice, getPreviousPeriod(convertGranularityToPeriod(getState().section.history.time.granularity), getState().section.history.time.startDate)))
        .then(sessions => dispatch(setComparisonSessions(sessions)))
        .catch(error => { 
          dispatch(setComparisonSessions([]));
          console.error('Caught error in history comparison query:', error); 
          });
      }
  };
};

/**
 * Sets comparison filter. Currently active only for deviceType METER
 *
 * @param {String} comparison - Comparison filter. One of: last (compare with user data from last period) 
 * @param {Bool} query=true - If true performs query based on active filters to update data
 */
const setComparison = function(comparison, query=true) {
  return function(dispatch, getState) {
    dispatch({
      type: types.HISTORY_SET_COMPARISON,
      comparison
    });
    if (comparison == null) dispatch(setComparisonSessions([]));

    if (query) {
      dispatch(fetchData());
    }
  };
};

/**
 * Sets active device type. 
 * All available devices of selected type are activated 
 * and default values are provided for deviceType dependent filters
 *
 * @param {Array} deviceType - Active device type. One of AMPHIRO, METER  
 * @param {Bool} query=true - If true performs query based on active filters to update data
 */
const setActiveDeviceType = function(deviceType, query=true) {
  return function(dispatch, getState) {
    dispatch({
      type: types.HISTORY_SET_ACTIVE_DEVICE_TYPE,
      deviceType
    });
    dispatch(setActiveDevice(getDeviceKeysByType(getState().user.profile.devices, deviceType), false));
    
    //set default options when switching
    if (deviceType === 'AMPHIRO') {
      dispatch(setMetricFilter('volume'));
      dispatch(setTimeFilter('ten'));
      dispatch(setSortFilter('id'));
    }
    else if (deviceType === 'METER') {
      dispatch(setMetricFilter('difference'));
      dispatch(setTimeFilter('year'));
      dispatch(setTime(getTimeByPeriod('year')));
      dispatch(setSortFilter('timestamp'));
    }
    
    if (query) { 
      dispatch(setDataUnsynced());
      dispatch(fetchData());
    }
  };
};

/**
 * Sets active devices. 
 *
 * @param {Array} deviceKeys - Device keys to set active. Important: Device keys must only be of one deviceType (METER or AMPHIRO)  
 * @param {Bool} query=true - If true performs query based on active filters to update data
 */
const setActiveDevice = function(deviceKeys, query=true) {
  
  return function(dispatch, getState) {
    dispatch({
      type: types.HISTORY_SET_ACTIVE_DEVICE,
      deviceKey: deviceKeys
    });
    
    if (query) { 
      dispatch(setDataUnsynced());
      dispatch(fetchData());
    }
  };
};

/**
 * Sets active session by either deviceKey & id, or by timestamp. 
 * Device key and id are provided for unique sessions (for deviceType AMPHIRO)
 * Timestamp is used as unique identifier for aggragated sessions (for deviceType METER)
 *
 * @param {String} deviceKey - device key for unique session
 * @param {Number} id - id for unique session
 * @param {Number} timestamp - timestamp for aggragated session
 */
const setActiveSession = function(deviceKey, id, timestamp) {
  return function(dispatch, getState) {
    dispatch({
      type: types.HISTORY_SET_ACTIVE_SESSION,
      device: deviceKey,
      id: id || timestamp
    });
    if (id != null && deviceKey != null) {
      dispatch(fetchDeviceSession(id, deviceKey, getState().section.history.time));
    }
  };
};

/**
 * Resets active session to null. 
 */
const resetActiveSession = function() {
  return {
    type: types.HISTORY_RESET_ACTIVE_SESSION
  };
};

/**
 * Sets metric filter for history section. 
 *
 * @param {String} filter - metric filter 
 */
const setMetricFilter = function(filter) {
  return {
    type: types.HISTORY_SET_FILTER,
    filter
  };
};

/**
 * Sets time/period filter for history section. 
 *
 * @param {String} filter - time/period filter 
 */
const setTimeFilter = function(filter) {
  return {
    type: types.HISTORY_SET_TIME_FILTER,
    filter
  };
};


/**
 * Sets session metric filter for active session in history section. 
 *
 * @param {String} filter - session metric filter 
 */
const setSessionFilter = function(filter) {
  return {
    type: types.HISTORY_SET_SESSION_FILTER,
    filter
  };
};

 /**
 * Sets sort filter for sessions list in history section. 
 *
 * @param {String} filter - session list sort filter 
 */
const setSortFilter = function(filter) {
  return {
    type: types.HISTORY_SET_SORT_FILTER,
    filter
  };
};

 /**
 * Sets sort order for sessions list in history section. 
 *
 * @param {String} order - session list order. One of asc, desc 
 */
const setSortOrder = function(order) {
  if (order !== 'asc' && order !== 'desc') throw new Error('order must be asc or desc');
  return {
    type: types.HISTORY_SET_SORT_ORDER,
    order
  };
};

module.exports = {
  linkToHistory,
  fetchDeviceSession,
  fetchData,
  setTime,
  updateTime,
  setComparison,
  setActiveDevice,
  setActiveDeviceType,
  setActiveSession,
  resetActiveSession,
  setMetricFilter,
  setTimeFilter,
  setSessionFilter,
  setSortFilter,
  setSortOrder
};
//module.exports = HistoryActions;
