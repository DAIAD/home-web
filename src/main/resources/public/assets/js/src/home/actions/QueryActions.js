require('es6-promise').polyfill();

var types = require('../constants/ActionTypes');

var deviceAPI = require('../api/device');
var meterAPI = require('../api/meter');


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
  
  queryDeviceSessions: function(deviceKey, time) {
    return function(dispatch, getState) {
      
      dispatch(requestedQuery());

      const data = Object.assign({}, time, {deviceKey: [ deviceKey ] }, {csrf: getState().user.csrf});
      
      return deviceAPI.querySessions(data)
      .then((response) => {
        dispatch(receivedQuery(response.success, response.errors, response.devices) );
          return response;
        })
        .catch((errors) => {
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
          return response;
        })
        .catch((errors) => {
          dispatch(receivedQuery(false, errors));
          return errors;
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
          return response;
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
