var meterAPI = require('../api/meter');
var types = require('../constants/ActionTypes');

var requestedMeterQuery = function() {
  return {
    type: types.METER_REQUESTED_QUERY,
  };
};

var receivedMeterQuery = function(success, errors, data) {
  return {
    type: types.METER_RECEIVED_QUERY,
    success: success,
    errors: errors,
    data: data
  };
};

var requestedMeterStatus = function() {
  return {
    type: types.METER_REQUESTED_STATUS,
  };
};

var receivedMeterStatus = function(success, errors, data) {
  return {
    type: types.METER_RECEIVED_STATUS,
    success: success,
    errors: errors,
    data: data
  };
};

var MeterActions = {
  
  getHistory: function(deviceKey, time) {
    return function(dispatch, getState) {

      dispatch(requestedMeterQuery());

      var data = Object.assign({}, time, {deviceKey: [ deviceKey ] });
      return meterAPI.getHistory(data).then(
        function(response) {
          dispatch(receivedMeterQuery(response.success, response.errors, response.series?response.series[0].values:[]) );
          return response;
        },
        function(error) {
          dispatch(receivedMeterQuery(false, error, {}));
          return error;
        });
    };
  },
  getStatus: function(deviceKey) {
    return function(dispatch, getState) {
      dispatch(requestedMeterStatus());
      
      var data = {deviceKey: [ deviceKey ] };
      return meterAPI.getStatus(data).then(
        function(response) {
          dispatch(receivedMeterStatus(response.success, response.errors, response.devices?response.devices:[]) );
          return response;
        },
        function(error) {
          console.log(error);
          dispatch(receivedMeterStatus(false, error, {}));
          return error;
        });
    };
  }
};

module.exports = MeterActions;
