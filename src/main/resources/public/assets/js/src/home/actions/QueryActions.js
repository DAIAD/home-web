require('es6-promise').polyfill();

var types = require('../constants/ActionTypes');

var deviceAPI = require('../api/device');
var meterAPI = require('../api/meter');

var { getLastSession, getDeviceTypeByKey } = require('../utils/device');

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

  queryDeviceOrMeter: function(deviceKey, time) {
    return function(dispatch, getState) {
      const type = getDeviceTypeByKey(getState().user.profile.devices, deviceKey);
      if (type === 'AMPHIRO') {
        return dispatch(QueryActions.queryDeviceSessions(deviceKey, time));
      }
      else if (type === 'METER') {
        return dispatch(QueryActions.fetchMeterHistory(deviceKey, time));
      }
    };
  },
  queryDeviceSessions: function(deviceKey, time) {
    return function(dispatch, getState) {
      
      dispatch(requestedQuery());

      const data = Object.assign({}, time, {deviceKey: [ deviceKey ] }, {csrf: getState().user.csrf});
      return deviceAPI.querySessions(data)
      .then((response) => {
        dispatch(receivedQuery(response.success, response.errors, response.devices) );
          if (!response.devices.length || !response.devices[0].sessions.length) { return []; }
          return response.devices[0].sessions;
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
      
      if (id===null || id===undefined) {
        return false;
      }
            
      dispatch(requestedQuery());

      const data = Object.assign({}, time,  {sessionId:id, deviceKey: deviceKey}, {csrf: getState().user.csrf});

      return deviceAPI.getSession(data)
        .then((response) => {
          dispatch(receivedQuery(response.success, response.errors, response.session));
          if (!response.session) { return {}; }
          return response.session;
        })
        .catch((errors) => {
          dispatch(receivedQuery(false, errors));
          return errors;
        });
    };
  },
  fetchLastSession: function(deviceKey, time) {
    return function(dispatch, getState) {
      return dispatch(QueryActions.queryDeviceSessions(deviceKey, time))
      .then(sessions => {
          const session = getLastSession(sessions);
          const id = session.id;
          if (!id){ return false;}

          return dispatch(QueryActions.fetchDeviceSession(id, deviceKey, time))
          .then(session => {
            //dispatch(setLastSession(session));
            return session;
          })
          .catch((error) => {
            return error;
          });
        });

    };
  },
  fetchMeterHistory: function(deviceKey, time) {
    return function(dispatch, getState) {

      dispatch(requestedQuery());

      const data = Object.assign({}, time, {deviceKey: [ deviceKey ] }, {csrf: getState().user.csrf});

      return meterAPI.getHistory(data)
        .then((response) => {
          dispatch(receivedQuery(response.success, response.errors, response.session));
          if (!response.series.length || !response.series[0].values) { return []; }
          return response.series[0].values;
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
