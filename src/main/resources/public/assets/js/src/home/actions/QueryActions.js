/**
 * Query Actions module.
 * A collection of reusable action thunks 
 * that unify query calls handling,
 * regarding loading state and errors.
 * 
 * @module QueryActions
 */

var types = require('../constants/ActionTypes');
const { CACHE_SIZE } = require('../constants/HomeConstants');

var deviceAPI = require('../api/device');
var meterAPI = require('../api/meter');

var { reduceSessions, getLastSession, getDeviceTypeByKey, updateOrAppendToSession } = require('../utils/transformations');
var { getDeviceKeysByType, filterDataByDeviceKeys } = require('../utils/device');
var { lastNFilterToLength, getCacheKey } =  require('../utils/general');
var { getTimeByPeriod, getLastShowerTime, getPreviousPeriodSoFar } = require('../utils/time');


const requestedQuery = function() {
  return {
    type: types.QUERY_REQUEST_START,
  };
};

const receivedQuery = function(success, errors) {
  return {
    type: types.QUERY_REQUEST_END,
    success,
    errors,
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

    console.time('cache');
    if (getState().query.cache[getCacheKey('AMPHIRO', options.length)]) {
      console.log('found in cache');
      console.timeEnd('cache');
      dispatch(cacheItemRequested('AMPHIRO', options.length));
      return Promise.resolve(filterDataByDeviceKeys(getState().query.cache[getCacheKey('AMPHIRO', options.length)].data, deviceKeys));
    }

    dispatch(requestedQuery());

    //const data = Object.assign({}, options, {deviceKey:deviceKeys}, {csrf: getState().user.csrf});

    console.time('device');
    //fetch all items to save in cache
    const data = Object.assign({}, options, {deviceKey:getDeviceKeysByType(getState().user.profile.devices, 'AMPHIRO')}, {csrf: getState().user.csrf});

    return deviceAPI.querySessions(data)
    .then(response => {
      console.timeEnd('device');
      dispatch(receivedQuery(response.success, response.errors, response.devices) );
       
      if (!response || !response.success) {
          throw new Error (response && response.errors && response.errors.length > 0 ? response.errors[0].code : 'unknownError');
      }
      dispatch(saveToCache('AMPHIRO', options.length, response.devices));
      
      //return only the items requested
      return filterDataByDeviceKeys(response.devices, deviceKeys);
      //return response.devices;
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
        
        if (!response || !response.success) {
          throw new Error (response && response.errors && response.errors.length > 0 ? response.errors[0].code : 'unknownError');
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
    return dispatch(queryDeviceSessions(deviceKeys, {type: 'SLIDING', length: 10}))
    .then(sessions => {
      
      const reduced = reduceSessions(getState().user.profile.devices, sessions);        
      //find last
      const lastSession = reduced.reduce((curr, prev) => (curr.timestamp>prev.timestamp)?curr:prev, {}); 
      const { device, id, index, timestamp } = lastSession;

      if (!id) throw new Error(`sessionIDNotFound`);
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

    console.time('cache');
    if (getState().query.cache[getCacheKey('METER', time)]) {
      console.log('found in cache!');
      console.timeEnd('cache');
      dispatch(cacheItemRequested('METER', time));
      return Promise.resolve(filterDataByDeviceKeys(getState().query.cache[getCacheKey('METER', time)].data, deviceKeys));
    }

    dispatch(requestedQuery());
    
    //const data = Object.assign({}, time, {deviceKey:deviceKeys}, {csrf: getState().user.csrf});

    console.time('meter');
    //fetch all meters requested in order to save to cache 
    const data = Object.assign({}, time, {deviceKey:getDeviceKeysByType(getState().user.profile.devices, 'METER')}, {csrf: getState().user.csrf}); 
    
    return meterAPI.getHistory(data)
      .then((response) => {
        console.timeEnd('meter');
        dispatch(receivedQuery(response.success, response.errors, response.session));
        
        if (!response || !response.success) {
          throw new Error (response && response.errors && response.errors.length > 0 ? response.errors[0].code : 'unknownError');
        }
        dispatch(saveToCache('METER', time, response.series));

        //return only the meters requested  
        return filterDataByDeviceKeys(response.series, deviceKeys);
        //return response.series;
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
        
        if (!response || !response.success) {
          throw new Error (response && response.errors && response.errors.length > 0 ? response.errors[0].code : 'unknownError');
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
 * Fetch data based on provided options and handle query response before returning
 * 
 * @param {Object} options - Options to fetch data 
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
    const { type, deviceType, period } = options;

    let time = options.time ? options.time : getTimeByPeriod(period);

    if (!type || !deviceType || !period) throw new Error('fetchInfoboxData: Insufficient data provided');

    const device = getDeviceKeysByType(getState().user.profile.devices, deviceType);
    
    if (!device || !device.length) return new Promise((resolve, reject) => resolve()); 

    if (type === "last") {

      return dispatch(fetchLastDeviceSession(device))
      .then(response => ({data: response.data, index: response.index, device: response.device, showerId: response.id, time: response.timestamp}));

    }
    else {

      if (deviceType === 'METER') {
        
        return dispatch(queryMeterHistory(device, time))
        .then(data => ({data}))
        .then(res => {
          if (type === 'TOTAL') {
            //fetch previous period data for comparison 
            let prevTime = getPreviousPeriodSoFar(period);
            return dispatch(queryMeterHistory(device, prevTime))
            .then(prevData => Object.assign({}, res, {previous:prevData, prevTime}))
              .catch(error => { 
                console.error('Caught error in infobox previous period data fetch:', error); 
              });
          }
          else {
            return Promise.resolve(res);
          }
        });
      }
      else if (deviceType === 'AMPHIRO') {
        return dispatch(queryDeviceSessions(device, {type: 'SLIDING', length:lastNFilterToLength(period)}))
        .then(data => ({data}));
      }
    }
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

const cacheItemRequested = function(deviceType, timeOrLength) {
  return {
    type: types.QUERY_CACHE_ITEM_REQUESTED,
    key:getCacheKey(deviceType, timeOrLength),
  };
};

const setCache = function(cache) {
  return {
    type: types.QUERY_SET_CACHE,
    cache
  };
};

const saveToCache = function(deviceType, timeOrLength, data) {
  return function(dispatch, getState) {
    const { cache } = getState().query;
    if (Object.keys(cache).length >= CACHE_SIZE) {
      console.warn('Cache limit exceeded, making space...');
      
      const newCacheKeys = Object.keys(cache)
      .sort((a, b) => cache[b].counter - cache[a].counter)
      .filter((x, i) => i < Object.keys(cache).length-1);

      let newCache = {};
      newCacheKeys.forEach(key => {
        newCache[key] = cache[key];
      });
      
      dispatch(setCache(newCache));
    }
    dispatch({
      type: types.QUERY_SAVE_TO_CACHE,
      key:getCacheKey(deviceType, timeOrLength),
      data
    });
  };
};

module.exports = {
  queryDeviceSessions,
  fetchDeviceSession,
  fetchLastDeviceSession,
  queryMeterHistory,
  queryMeterStatus,
  fetchInfoboxData,
  dismissError
};
