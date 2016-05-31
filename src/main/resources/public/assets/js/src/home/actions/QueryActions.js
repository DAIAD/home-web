require('es6-promise').polyfill();

var types = require('../constants/ActionTypes');

var deviceAPI = require('../api/device');
var meterAPI = require('../api/meter');

var { reduceSessions, getLastSession, getSessionIndexById, getDeviceTypeByKey, updateOrAppendToSession } = require('../utils/device');

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

const QueryActions = {
 
  queryDeviceOrMeter: function(deviceKeys, type, time) {
    return function(dispatch, getState) {
      if (!Array.isArray(deviceKeys)) throw Error('device keys ', deviceKeys, 'must be of type Array');

      if (!deviceKeys.length) return new Promise(resolve => resolve([]));
      if (!type || !(type === 'AMPHIRO' || type === 'METER')) throw new Error('type not found');

      if (type === 'AMPHIRO') {
        return dispatch(QueryActions.queryDeviceSessions(deviceKeys, {type: 'SLIDING', length: 10}))
               .catch(error => { throw error; });
      }
      else if (type === 'METER') {
        return dispatch(QueryActions.fetchMeterHistory(deviceKeys, time))
               .catch(error => { throw error; });
      }
    };
  },
  queryDeviceSessions: function(deviceKeys, options) {
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
  },
  fetchDeviceSession: function(id, deviceKey) {
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
        .catch((error) => {
          dispatch(receivedQuery(false, error));
          throw error;
        });
    };
  },
  fetchLastDeviceSession: function(deviceKeys) {
    return function(dispatch, getState) {
      return dispatch(QueryActions.queryDeviceSessions(deviceKeys, {type: 'SLIDING', length: 1}))
      .then(sessions => {
        
        const reduced = reduceSessions(getState().user.profile.devices, sessions);        
        //find last
        const lastSession = reduced.reduce((curr, prev) => (curr.timestamp>prev.timestamp)?curr:prev, {});
         
        const { device, id, index, timestamp } = lastSession;

        if (!id) throw new Error(`last session id doesnt exist in response: ${response}`);
        const devSessions = sessions.find(x=>x.deviceKey === device);
        
        return dispatch(QueryActions.fetchDeviceSession(id, device))
        .then(session => ({data: updateOrAppendToSession([devSessions], Object.assign({}, session, {deviceKey:device})), device, index, id, timestamp}) )
        .catch(error => { throw error; });
      });
    };
  },
  fetchMeterHistory: function(deviceKeys, time) {
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
  },
  fetchMeterStatus: function(deviceKeys) {
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
  }

};

module.exports = QueryActions;
