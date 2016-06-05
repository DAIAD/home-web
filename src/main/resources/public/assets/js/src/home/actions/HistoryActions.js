var types = require('../constants/ActionTypes');
require('es6-promise').polyfill();
var { push } = require('react-router-redux');
var { getSessionById, getDeviceKeysByType, getDeviceTypeByKey, lastNFilterToLength, getIdRangeByIndex } = require('../utils/device');
var { getPreviousPeriod, convertGranularityToPeriod, getGranularityByDiff } = require('../utils/time');

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

const HistoryActions = {
  
  linkToHistory: function(options) {
    return function(dispatch, getState) {
      const { id, showerId, device, deviceType, metric, period, time, index, data } = options;
      
      if (deviceType) dispatch(HistoryActions.setActiveDeviceType(deviceType, false));
      if (metric) dispatch(HistoryActions.setQueryFilter(metric));
      if (period) dispatch(HistoryActions.setTimeFilter(period));
      if (time) dispatch(HistoryActions.setTime(time, false));

      if (index != null && showerId != null) { 
        dispatch(HistoryActions.setSessionFilter(metric)); 
        dispatch(HistoryActions.setActiveSession(Array.isArray(device)?device[0]:device, showerId)); 
      }
      else { 
        dispatch(HistoryActions.resetActiveSession()); 
      }
      
      if (data && data.length>0) { 
        dispatch(setSessions(data));
        dispatch(setDataSynced());
        }
 
      dispatch(push('/history'));
    };
  },
  
  getDeviceSession: function (id, deviceKey) {
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
  },
  // time is of type Object with
  //  startDate of type string (unix timestamp),
  //  endDate of type string (unix timestamp)
  //  granularity of type int (0-4)
  setTime: function(time, query=true) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_SET_TIME,
        time
      });
      if (query) { 
        dispatch(setDataUnsynced());
        dispatch(HistoryActions.query());
      }
    };
  },
  // update time used to calculate granularity and update time with
  // startDate and/or endDate
  updateTime: function(time, query=true) {
    return function(dispatch, getState) {
      let { startDate, endDate } = time;
      startDate = startDate || getState().section.history.time.startDate;
      endDate = endDate || getState().section.history.time.endDate;

      const granularity = getGranularityByDiff(startDate, endDate);

      dispatch(HistoryActions.setTime({startDate, endDate, granularity}, query));
    };
  },
  query: function () {
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
        dispatch(QueryActions.fetchMeterHistory(getState().section.history.activeDevice, getState().section.history.time))
          .then(sessions => dispatch(setSessions(sessions)))
          .then(() => dispatch(setDataSynced()))
          .catch(error => { 
            console.error('Caught error in history meter query:', error); 
            dispatch(setSessions([]));
            dispatch(setDataSynced());
          });
      }


        if (getState().section.history.comparison === 'last') {
          dispatch(QueryActions.queryDeviceOrMeter(getState().section.history.activeDevice, getState().section.history.activeDeviceType, getPreviousPeriod(convertGranularityToPeriod(getState().section.history.time.granularity), getState().section.history.time.startDate)))
          .then(sessions => dispatch(setComparisonSessions(sessions)))
          .catch(error => { 
            dispatch(setComparisonSessions([]));
            console.error('Caught error in history comparison query:', error); 
            });
        }
    };
  },
  setComparison: function(comparison) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_SET_COMPARISON,
        comparison
      });
      if (comparison == null) dispatch(setComparisonSessions([]));

      dispatch(HistoryActions.query());
    };
  },
  setActiveDevice: function(deviceKeys, query=true) {
    
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_SET_ACTIVE_DEVICE,
        deviceKey: deviceKeys
      });
      
      if (query) { 
        dispatch(setDataUnsynced());
        dispatch(HistoryActions.query());
      }
    };
  },
  setActiveDeviceType: function(deviceType, query=true) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_SET_ACTIVE_DEVICE_TYPE,
        deviceType
      });
      dispatch(HistoryActions.setActiveDevice(getDeviceKeysByType(getState().user.profile.devices, deviceType), false));
      
      //set default options when switching
      if (deviceType === 'AMPHIRO') {
        dispatch(HistoryActions.setQueryFilter('volume'));
        dispatch(HistoryActions.setTimeFilter('ten'));
        dispatch(HistoryActions.setSortFilter('id'));
      }
      else if (deviceType === 'METER') {
        dispatch(HistoryActions.setQueryFilter('difference'));
        dispatch(HistoryActions.setTimeFilter('year'));
        dispatch(HistoryActions.setSortFilter('timestamp'));
      }
      
      if (query) { 
        dispatch(setDataUnsynced());
        dispatch(HistoryActions.query());
      }
    };
  },
   
  resetActiveDevice: function(query=true) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_RESET_ACTIVE_DEVICE,
      });
      
      if (query) { 
        dispatch(setDataUnsynced());
        dispatch(HistoryActions.query());
      }
    };
  },
  setActiveDeviceIfNone: function(deviceKey) {
    return function(dispatch, getState) {
      if (getState().query.activeDevice) {
        return true;
      }
      else {
        return dispatch(QueryActions.setActiveDevice(deviceKey));
      }
    };
  },
  
  setActiveSession: function(device, id, timestamp) {
    return function(dispatch, getState) {
      dispatch({
        type: types.HISTORY_SET_ACTIVE_SESSION,
        device,
        id: id || timestamp
      });
      if (id != null && device != null) {
        dispatch(HistoryActions.getDeviceSession(id, device, getState().section.history.time));
      }
    };
  },
  
  resetActiveSession: function() {
    return {
      type: types.HISTORY_RESET_ACTIVE_SESSION
    };
  },

  setQueryFilter: function(filter) {
    return {
      type: types.HISTORY_SET_FILTER,
      filter
    };
  },
  setTimeFilter: function(filter) {
    return {
      type: types.HISTORY_SET_TIME_FILTER,
      filter
    };
  },
  setSessionFilter: function(filter) {
    return {
      type: types.HISTORY_SET_SESSION_FILTER,
      filter
    };
  },  
  setSortFilter: function(filter) {
    return {
      type: types.HISTORY_SET_SORT_FILTER,
      filter
    };
  },
  setSortOrder: function(order) {
    if (order !== 'asc' && order !== 'desc') throw new Error('order must be asc or desc');
    return {
      type: types.HISTORY_SET_SORT_ORDER,
      order
    };
  },
};

module.exports = HistoryActions;
