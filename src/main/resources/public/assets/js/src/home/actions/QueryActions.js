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

  queryDeviceOrMeter: function(deviceKeys, time) {
    return function(dispatch, getState) {
      //const type = getDeviceTypeByKey(getState().user.profile.devices, deviceKey);
      if (!Array.isArray(deviceKeys)) throw Error('device keys ', deviceKeys, 'must be of type Array');
      const type = deviceKeys.map(deviceKey => getDeviceTypeByKey(getState().user.profile.devices, deviceKey)).reduce((prev, curr) => prev!==curr?-1:curr);

      if (!type) throw new Error('type not found');
      else if (type === -1) throw new Error('device keys of unequal type');
      
      if (type === 'AMPHIRO') {
        return dispatch(QueryActions.queryDeviceSessions(deviceKeys, time));
      }
      else if (type === 'METER') {
        return dispatch(QueryActions.fetchMeterHistory(deviceKeys, time));
      }
      else {
        throw new Error('type is of unrecognized type', type);
      }
    };
  },
  queryDeviceSessions: function(deviceKeys, time) {
    return function(dispatch, getState) {
      
      if (!deviceKeys || !time) throw new Error(`Not sufficient data provided for device sessions query: deviceKey:${deviceKeys}, time: ${time}`);

      dispatch(requestedQuery());

      //const data = Object.assign({}, time, {deviceKey: [ deviceKey ] }, {csrf: getState().user.csrf});
      const data = Object.assign({}, time, {deviceKey:deviceKeys}, {csrf: getState().user.csrf});
      
      return deviceAPI.querySessions(data)
      .then(response => {
        dispatch(receivedQuery(response.success, response.errors, response.devices) );
        
        if (!response || response.success === false) {
          throw new Error (`device sessions query was not successful`);
        }
        if (!Array.isArray(response.devices) || !Array.isArray(response.devices[0].sessions)) throw new Error(`response of queryDeviceSessions with:  ${deviceKeys}, ${time} is not of type array`);

          return response.devices;
        })
        .catch((errors) => {
          console.error(errors);
          dispatch(receivedQuery(false, errors));
          return errors;
        });
    };
  },
  fetchDeviceSession: function(id, deviceKey, time) {
    return function(dispatch, getState) {
      
      if (!id || !deviceKey || !time) throw new Error(`Not sufficient data provided for device session fetch: id: ${id}, deviceKey:${deviceKey}, time: ${time}`);
      //if (id===null || id===undefined) throw new Error('cannot fetch device sessions without valid id:', id);
      
      else if (!deviceKey) throw new Error('device key must be given:', deviceKey);
      dispatch(requestedQuery());

      const data = Object.assign({}, time,  {sessionId:id, deviceKey: deviceKey}, {csrf: getState().user.csrf});

      return deviceAPI.getSession(data)
        .then((response) => {
          dispatch(receivedQuery(response.success, response.errors, response.session));
          //if (!response.session) { return {}; }
          if (!response || !response.session) throw new Error('response of fetchDeviceSession with:', id, deviceKey, time, 'doesnt exist');
          return response.session;
        })
        .catch((errors) => {
          dispatch(receivedQuery(false, errors));
          return errors;
        });
    };
  },
  fetchLastSession: function(deviceKeys, time) {
    return function(dispatch, getState) {
      return dispatch(QueryActions.queryDeviceSessions(deviceKeys, time))
      .then(response => {
        
        const sessions = response;
        const reduced = reduceSessions(getState().user.profile.devices, sessions);        
        
        //find last
        const lastSession = reduced.reduce((curr, prev) => (curr.timestamp>prev.timestamp)?curr:prev);
         
        const { device, id, index } = lastSession;
        if (!id) throw new Error(`last session id doesnt exist in response: ${response}`);
        const devSessions = sessions.find(x=>x.deviceKey === device);
        
        return dispatch(QueryActions.fetchDeviceSession(id, device, time))
        .then(session => ({data: updateOrAppendToSession([devSessions], Object.assign({}, session, {deviceKey:device})), device:device, index}) )
        //.then(sessions => {console.log('so sessions are', sessions); return sessions;})
        //.then(session => ({data: updateOrAppendToSession(sessions, Object.assign({}, session, {deviceKey:device})), device:device, index}) )
        .catch(error => error);
      });
    };
  },
  fetchMeterHistory: function(deviceKeys, time) {
    return function(dispatch, getState) {
      if (!deviceKeys || !time) throw new Error(`Not sufficient data provided for meter history query: deviceKey:${deviceKeys}, time: ${time}`);

      dispatch(requestedQuery());
      //const data = Object.assign({}, time, {deviceKey: [ deviceKey ] }, {csrf: getState().user.csrf});
      const data = Object.assign({}, time, {deviceKey:deviceKeys}, {csrf: getState().user.csrf});
      return meterAPI.getHistory(data)
        .then((response) => {
          dispatch(receivedQuery(response.success, response.errors, response.session));
          //if (!response.series.length || !response.series[0].values) return []; 
          //TODO: throw new Error returns it
          if (!response || !Array.isArray(response.series)) throw new Error(`fetchMeterHistory with: deviceKey: ${deviceKeys}, time:${time} failed`);
          
          //return response.series[0].values.map((session, i, array) => Object.assign({}, session, {volume: session.volume-array[0].volume}));
          //return response.series[0].values;
          //return response.series.map(meter => meter.values);
          return response.series;
        })
        .catch((errors) => {
          dispatch(receivedQuery(false, errors));
          return errors;
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

          return response;
        })
        .catch((errors) => {
          dispatch(receivedMeterStatus(false, errors, {}));
          return errors;
        });
    };
  }

};

module.exports = QueryActions;
