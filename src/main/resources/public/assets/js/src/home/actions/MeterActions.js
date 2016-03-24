var meterAPI = require('../api/meter');
var types = require('../constants/ActionTypes');

const requestedMeterQuery = function() {
  return {
    type: types.METER_REQUESTED_QUERY,
  };
};

const receivedMeterQuery = function(success, errors, data) {
  return {
    type: types.METER_RECEIVED_QUERY,
    success: success,
    errors: errors,
    data: data
  };
};

const requestedMeterStatus = function() {
  return {
    type: types.METER_REQUESTED_STATUS,
  };
};

const receivedMeterStatus = function(success, errors, data) {
  return {
    type: types.METER_RECEIVED_STATUS,
    success: success,
    errors: errors,
    data: data
  };
};

const MeterActions = {
  
  getHistory: function(deviceKey, time) {
    return function(dispatch, getState) {

      dispatch(requestedMeterQuery());

      const data = Object.assign({}, time, {deviceKey: [ deviceKey ] }, {csrf: getState().query.csrf});

      return meterAPI.getHistory(data)
        .then((response) => {
          dispatch(receivedMeterQuery(response.success, response.errors, response.series?response.series[0].values:[]) );
          return response;
        })
        .catch((errors) => {
          dispatch(receivedMeterQuery(false, errors, {}));
          return errors;
        });
    };
  },
  getStatus: function(deviceKey) {
    return function(dispatch, getState) {
      dispatch(requestedMeterStatus());
      
      const data = {deviceKey: [ deviceKey ], csrf: getState().query.csrf };
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

module.exports = MeterActions;
