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
//var { getTimeByPeriod, getLastShowerTime, getPreviousPeriodSoFar } = require('../utils/time');


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
 * Wrapper to queryDeviceSessions with cache functionality
 * @param {Object} options - The queryDeviceSessions options object
 * @return {Promise} Resolve returns Object containing device sessions data in form {data: sessionsData}, reject returns possible errors
 * 
 */
const queryDeviceSessionsCache = function(options) {
  return function(dispatch, getState) {
    
    const { length, deviceKey } = options;
    if (getState().query.cache[getCacheKey('AMPHIRO', length)]) {
      
      dispatch(cacheItemRequested('AMPHIRO', length));
      return Promise.resolve(filterDataByDeviceKeys(getState().query.cache[getCacheKey('AMPHIRO', length)].data, deviceKey));
    }
    
    //fetch all items to save in cache
    const newOptions = Object.assign({}, options, {deviceKey:getDeviceKeysByType(getState().user.profile.devices, 'AMPHIRO')});
    
    return dispatch(queryDeviceSessions(newOptions))
    .then(devices => {

      dispatch(saveToCache('AMPHIRO', length, devices));
        
      //return only the items requested
      return filterDataByDeviceKeys(devices, deviceKey);
      //return response.devices;

    });

  };
};

/**
 * Query Device sessions
 * @param {Object} options - Query options
 * @param {Array} options.deviceKey - Array of device keys to query
 * @param {String} options.type - The query type. One of SLIDING, ABSOLUTE
 * @param {Number} options.startIndex - Start index for ABSOLUTE query
 * @param {Number} options.endIndex - End index for ABSOLUTE query
 * @param {Number} options.length - Length for SLIDING query
 * @return {Promise} Resolve returns Object containing device sessions data in form {data: sessionsData}, reject returns possible errors
 * 
 */
const queryDeviceSessions = function(options) {
  return function(dispatch, getState) {
    
    const { length, type, deviceKey } = options;
    
    if (!deviceKey) throw new Error(`Not sufficient data provided for device sessions query: deviceKey:${deviceKey}`);
    
    dispatch(requestedQuery());

    //const data = Object.assign({}, options, {deviceKey:deviceKey}, {csrf: getState().user.csrf});
 
    const data = Object.assign({}, options);

    return deviceAPI.querySessions(data)
    .then(response => {
      dispatch(receivedQuery(response.success, response.errors, response.devices) );

      if (!response || !response.success) {
          throw new Error (response && response.errors && response.errors.length > 0 ? response.errors[0].code : 'unknownError');
      }
      return response.devices;      
      //dispatch(saveToCache('AMPHIRO', options.length, response.devices));
      //return only the items requested
      //return filterDataByDeviceKeys(response.devices, deviceKey);
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
 * @param {String} deviceKey - Device keys to query
 * @return {Promise} Resolve returns Object containing last session data for all devices provided (last session between devices is computed using timestamp), reject returns possible errors
 * 
 */
const fetchLastDeviceSession = function(deviceKey) {
  return function(dispatch, getState) {
    return dispatch(queryDeviceSessions(deviceKey, {type: 'SLIDING', length: 10}))
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
 * Wrapper to queryMeterHistory with cache functionality
 * @param {Object} options - The queryMeterHistory options object
 * @return {Promise} Resolve returns Object containing meter sessions data in form {data: sessionsData}, reject returns possible errors
 * 
 */
const queryMeterHistoryCache = function(options) {
  return function(dispatch, getState) {
    
    const { deviceKey, time } = options;

    if (getState().query.cache[getCacheKey('METER', time)]) {
      //console.log('found in cache!');
      dispatch(cacheItemRequested('METER', time));
      return Promise.resolve(filterDataByDeviceKeys(getState().query.cache[getCacheKey('METER', time)].data, deviceKey));
    }

    //fetch all meters requested in order to save to cache 
    const newOptions = Object.assign({}, options, {time, deviceKey:getDeviceKeysByType(getState().user.profile.devices, 'METER')}); 
            
    return dispatch(queryMeterHistory(newOptions))
    .then(series => {

      dispatch(saveToCache('METER', time, series));
      
      //return only the meters requested  
      return filterDataByDeviceKeys(series, deviceKey);
      //return response.series;
    });

  };
};
/**
 * Query Meter for historic session data
 * @param {Object} options - Query options
 * @param {Array} options.deviceKey - Array of device keys to query
 * @param {Object} options.time - Query time window
 * @param {Number} options.time.startDate - Start timestamp for query
 * @param {Number} options.time.endDate - End timestamp for query
 * @param {Number} options.time.granularity - Granularity for data aggregation. One of 0: minute, 1: hour, 2: day, 3: week, 4: month
 * @return {Promise} Resolve returns Object containing meter sessions data in form {data: sessionsData}, reject returns possible errors
 * 
 */                 
const queryMeterHistory = function(options) {
  return function(dispatch, getState) {
    const { deviceKey, time } = options;
    if (!deviceKey || !time || !time.startDate || !time.endDate) throw new Error(`Not sufficient data provided for meter history query: deviceKey:${deviceKey}, time: ${time}`);

    dispatch(requestedQuery());
    
    const data = Object.assign({}, time, options);
    
    return meterAPI.getHistory(data)
      .then((response) => {
        dispatch(receivedQuery(response.success, response.errors, response.session));
        
        if (!response || !response.success) {
          throw new Error (response && response.errors && response.errors.length > 0 ? response.errors[0].code : 'unknownError');
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
 * @param {Array} deviceKey - Array of device keys to query
 * @return {Promise} Resolve returns Object containing meter sessions data in form {data: sessionsData}, reject returns possible errors
 * 
 */    
const queryMeterStatus = function(deviceKey) {
  return function(dispatch, getState) {

    if (!deviceKey) throw new Error(`Not sufficient data provided for meter status: deviceKeys:${deviceKey}`);

    dispatch(requestedMeterStatus());
    
    const data = {deviceKey, csrf: getState().user.csrf };
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
 * @param {Boolean} options.cache - Whether cache query functions should be called or not 
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
    const { type, deviceType, time, prevTime, query } = options;
    const cache = query.cache || false;
    const { deviceKey } = query;

    //let time = options.time ? options.time : getTimeByPeriod(period);

    if (!type || !deviceType || !deviceKey) {
      console.error('fetchInfoboxData: Insufficient data provided (need type, deviceType, deviceKey):', options);
      throw new Error('fetchInfoboxData: Insufficient data provided:');
    }

    //const device = getDeviceKeysByType(getState().user.profile.devices, deviceType);

    let queryMeter, queryDevice;
    if (cache) {
      queryMeter = queryMeterHistoryCache;
      queryDevice = queryDeviceSessionsCache;
    }
    else {
      queryMeter = queryMeterHistory;
      queryDevice = queryDeviceSessions;
    }

    //If no device in array just return 
    if (!deviceKey || !deviceKey.length) return Promise.resolve(); 

    if (deviceType === 'METER') {      
      return dispatch(queryMeter(Object.assign({}, options.query, {time})))
      .then(data => ({data}))
      .then(res => {
        if (type === 'total' && prevTime) {
          //fetch previous period data for comparison 
          //let prevTime = getPreviousPeriodSoFar(period);
          return dispatch(queryMeter(Object.assign({}, options.query, {time:prevTime})))
          //return dispatch(queryMeter({deviceKey:device, time:prevTime, csrf: getState().user.csrf}))
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
      if (type === "last") {
        return dispatch(fetchLastDeviceSession(deviceKey))
        .then(response => ({data: response.data, index: response.index, device: response.device, showerId: response.id, time: response.timestamp}));
      }
      else {
        return dispatch(queryDevice(Object.assign({}, options.query)))
        //return dispatch(queryDevice({deviceKey:device, type: 'SLIDING', length:lastNFilterToLength(period), csrf: getState().user.csrf}))
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
      console.warn('Cache limit exceeded, making space by emptying LRU...');
      
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
  queryDeviceSessionsCache,
  queryDeviceSessions,
  fetchDeviceSession,
  fetchLastDeviceSession,
  queryMeterHistoryCache,
  queryMeterHistory,
  queryMeterStatus,
  fetchInfoboxData,
  dismissError
};
