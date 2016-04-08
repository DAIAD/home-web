require('es6-promise').polyfill();

var types = require('../constants/ActionTypes');

var deviceAPI = require('../api/device');
var meterAPI = require('../api/meter');

var { getLastSession, getSessionIndexById, getDeviceTypeByKey } = require('../utils/device');

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
      else if (type === -1) throw new Error('device keys ',deviceKeys, ' of unequal type');
      
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
  queryDeviceSessions: function(deviceKey, time) {
    return function(dispatch, getState) {
      
      dispatch(requestedQuery());

      //const data = Object.assign({}, time, {deviceKey: [ deviceKey ] }, {csrf: getState().user.csrf});
      const data = Object.assign({}, time, {deviceKey}, {csrf: getState().user.csrf});
      return deviceAPI.querySessions(data)
      .then(response => {
        dispatch(receivedQuery(response.success, response.errors, response.devices) );
        //if (!response.devices.length || !response.devices[0].sessions.length) { return []; }
        if (!response || !Array.isArray(response.devices) || !Array.isArray(response.devices[0].sessions)) throw new Error('response of queryDeviceSessions with:', deviceKey, time, 'is not of type array');
          //return response.devices[0].sessions;
          //return response.devices.map(device => device.sessions);
          return response.devices;
        })
        .catch((errors) => {
          console.log('ooops...', errors);
          dispatch(receivedQuery(false, errors));
          return errors;
        });
    };
  },
  fetchDeviceSession: function(id, deviceKey, time) {
    return function(dispatch, getState) {
      if (id===null || id===undefined) throw new Error('cannot fetch device sessions without valid id:', id);
      
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

        let sessions = [];
        response.forEach(device => {
          sessions.push({devKey:device.deviceKey, sessions:device.sessions, lastSession:getLastSession(device.sessions)});
        });
        const lastSession = sessions.reduce((curr, prev) => curr.lastSession.timestamp>prev.lastSession.timestamp?curr:prev);
        const deviceKey = lastSession.devKey;
        const id = lastSession.lastSession.id;
        if (!id) throw new Error('last session id doesnt exist in:', response);
        
        const index = getSessionIndexById(sessions.find(session=>session.devKey===deviceKey).sessions, id);
        
        return dispatch(QueryActions.fetchDeviceSession(id, deviceKey, time))
        .then(session => [Object.assign({}, session, {index})])
        .catch(error => error);
      });
    };
  },
  fetchMeterHistory: function(deviceKey, time) {
    return function(dispatch, getState) {
      dispatch(requestedQuery());
      //const data = Object.assign({}, time, {deviceKey: [ deviceKey ] }, {csrf: getState().user.csrf});
      const data = Object.assign({}, time, {deviceKey}, {csrf: getState().user.csrf});
      return meterAPI.getHistory(data)
        .then((response) => {
          dispatch(receivedQuery(response.success, response.errors, response.session));
          //if (!response.series.length || !response.series[0].values) return []; 
          //TODO: throw new Error returns it
          if (!response || !Array.isArray(response.series)) throw new Error(`fetchMeterHistory with: deviceKey: ${deviceKey}, time:${time} failed`);
          
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
  fetchMeterStatus: function(deviceKey) {
    return function(dispatch, getState) {
      dispatch(requestedMeterStatus());
      
      const data = {deviceKey: [ deviceKey ], csrf: getState().user.csrf };
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
