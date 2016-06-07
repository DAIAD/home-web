/**
 * Query Actions module.
 * A collection of reusable action thunks 
 * that unify query calls handling,
 * regarding loading state and errors.
 * 
 * @module QueryActions
 */

var types = require('../constants/ActionTypes');

var deviceAPI = require('../api/device');
var meterAPI = require('../api/meter');

var { reduceSessions, getLastSession, getDeviceTypeByKey, updateOrAppendToSession } = require('../utils/transformations');

const requestedQuery = function() {
  return {
    type: types.QUERY_REQUEST_START,
  };
};

const receivedQuery = function(success, errors) {
  return {
    type: types.QUERY_REQUEST_END,
    success: success,
    errors: errors,
  };
};

/**
 * Query Device sessions
 * @param {Array} deviceKeys - Array of device keys to query
 * @param {Object} options - Query options
 * @param {String} options.type - The query type. One of SLIDING, ABSOLUTE
 * @param {Number} options.startIndex - Start index for ABSOLUTE query
 * @param {Number} options.endIndex - End index for ABSOLUTE query
 * @param {Number} options.length - Length for SLIDING query
 * @return {Promise} Resolve returns Object containing device sessions data in form {data: sessionsData}, reject returns possible errors
 * 
 */
const queryDeviceSessions = function(deviceKeys, options) {
  return function(dispatch, getState) {
    
    if (!deviceKeys) throw new Error(`Not sufficient data provided for device sessions query: deviceKey:${deviceKeys}`);

    dispatch(requestedQuery());

    const data = Object.assign({}, options, {deviceKey:deviceKeys}, {csrf: getState().user.csrf});

    return deviceAPI.querySessions(data)
    .then(response => {
      dispatch(receivedQuery(response.success, response.errors, response.devices) );
      
      if (!response.success) {
        throw new Error (response.errors);
      }
        return response.devices;
      })
      .catch((error) => {
        dispatch(receivedQuery(false, error));
        throw error;
      });
  };
};
  
/**
 * Fetch specific device session
 * @param {String} deviceKey - Device keys to query
 * @param {Number} options - Session id to query
 * @return {Promise} Resolve returns Object containing device session data, reject returns possible errors
 * 
 */
const fetchDeviceSession = function(id, deviceKey) {
  return function(dispatch, getState) {
    
    if (!id || !deviceKey) throw new Error(`Not sufficient data provided for device session fetch: id: ${id}, deviceKey:${deviceKey}`);
    
    dispatch(requestedQuery());

    const data = Object.assign({}, {sessionId:id, deviceKey: deviceKey}, {csrf: getState().user.csrf});

    return deviceAPI.getSession(data)
      .then((response) => {
        dispatch(receivedQuery(response.success, response.errors, response.session));
        if (!response.success) {
          throw new Error (response.errors);
        }
        return response.session;
      })
      .catch((errors) => {
        dispatch(receivedQuery(false, error));
        throw error;
      });
  };
};
                      

/**
 * Fetch last session for array of devices
 * @param {String} deviceKeys - Device keys to query
 * @return {Promise} Resolve returns Object containing last session data for all devices provided (last session between devices is computed using timestamp), reject returns possible errors
 * 
 */
const fetchLastDeviceSession = function(deviceKeys) {
  return function(dispatch, getState) {
    return dispatch(queryDeviceSessions(deviceKeys, {type: 'SLIDING', length: 1}))
    .then(sessions => {
      
      const reduced = reduceSessions(getState().user.profile.devices, sessions);        
      //find last
      const lastSession = reduced.reduce((curr, prev) => (curr.timestamp>prev.timestamp)?curr:prev, {});
       
      const { device, id, index, timestamp } = lastSession;

      if (!id) throw new Error(`last session id doesnt exist in response: ${response}`);
      const devSessions = sessions.find(x=>x.deviceKey === device);
      
      return dispatch(fetchDeviceSession(id, device))
      .then(session => ({data: updateOrAppendToSession([devSessions], Object.assign({}, session, {deviceKey:device})), device, index, id, timestamp}) )
      .catch(error => { throw error; });
    });
  };
};
  
/**
 * Query Meter for historic session data
 * @param {Array} deviceKeys - Array of device keys to query
 * @param {Object} time - Query time window
 * @param {Number} time.startDate - Start timestamp for query
 * @param {Number} time.endDate - End timestamp for query
 * @param {Number} time.granularity - Granularity for data aggregation. One of 0: minute, 1: hour, 2: day, 3: week, 4: month
 * @return {Promise} Resolve returns Object containing meter sessions data in form {data: sessionsData}, reject returns possible errors
 * 
 */                 
const queryMeterHistory = function(deviceKeys, time) {
  return function(dispatch, getState) {
    if (!deviceKeys || !time || !time.startDate || !time.endDate) throw new Error(`Not sufficient data provided for meter history query: deviceKey:${deviceKeys}, time: ${time}`);

    dispatch(requestedQuery());
    
    const data = Object.assign({}, time, {deviceKey:deviceKeys}, {csrf: getState().user.csrf});
    return meterAPI.getHistory(data)
      .then((response) => {
        dispatch(receivedQuery(response.success, response.errors, response.session));
        if (!response.success) {
          throw new Error (response.errors);
        }
        return response.series;
      })
      .catch((error) => {
        dispatch(receivedQuery(false, error));
        throw error;
      });
  };
};

/**
 * Query Meter for current meter status
 * @param {Array} deviceKeys - Array of device keys to query
 * @return {Promise} Resolve returns Object containing meter sessions data in form {data: sessionsData}, reject returns possible errors
 * 
 */    
const queryMeterStatus = function(deviceKeys) {
  return function(dispatch, getState) {

    if (!deviceKeys) throw new Error(`Not sufficient data provided for meter status: deviceKeys:${deviceKeys}`);

    dispatch(requestedMeterStatus());
    
    const data = {deviceKey: deviceKeys, csrf: getState().user.csrf };
    return meterAPI.getStatus(data)
      .then((response) => {
        dispatch(receivedMeterStatus(response.success, response.errors, response.devices?response.devices:[]) );
        
        if (!response.success) {
          throw new Error (response.errors);
        }
        return response;
      })
      .catch((error) => {
        dispatch(receivedQuery(false, error));
        throw error;
      });
  };
};
  
 /**
 * Dismiss error after acknowledgement
 */
const dismissError = function() {
  return {
    type: types.QUERY_DISMISS_ERROR
  };
};

module.exports = {
  queryDeviceSessions,
  fetchDeviceSession,
  fetchLastDeviceSession,
  queryMeterHistory,
  queryMeterStatus,
  dismissError
};
