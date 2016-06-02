var types = require('../constants/ActionTypes');

var messageAPI = require('../api/message');

const requestedMessages = function() {
  return {
    type: types.MESSAGE_REQUEST_START,
  };
};

const receivedMessages = function(success, errors, data) {
  return {
    type: types.MESSAGE_REQUEST_END,
    success,
    errors,
    data
  };
};

const requestedMessageAck = function() {
  return {
    type: types.MESSAGE_ACK_REQUEST_START,
  };
};

const receivedMessageAck = function(success, errors) {
  return {
    type: types.MESSAGE_ACK_REQUEST_END,
    success,
    errors,
  };
};

const MessageActions = {
  fetch: function(options) {
    return function(dispatch, getState) {

      if (!Array.isArray(options)) throw new Error('Fetch requires array of options:', options);

      dispatch(requestedMessages());

      const data = Object.assign({}, {pagination: options}, {csrf: getState().user.csrf});

      console.log('requesting messages ', data);
      return messageAPI.fetch(data)
      .then(response => {
        console.log('got ', response);

        if (!response.success) {
          throw new Error (response.errors);
        }

        const { alerts, recommendations, tips, announcements } = response;
        dispatch(receivedMessages(response.success, response.errors, {alerts, recommendations, tips, announcements}));
        return response;
        })
        .catch((error) => {
          dispatch(receivedMessages(false, error));
        });
    };
  },
  acknowledge: function(id, type, timestamp) {
    return function(dispatch, getState) {
      if (!id || !type || !timestamp) throw new Error(`Not sufficient data provided for message acknowledgement. (id, type, timestamp): ${id}, ${type}, ${timestamp}`);

      dispatch(requestedMessageAck());

      const data = Object.assign({}, {messages: [{id, type, timestamp}]}, {csrf: getState().user.csrf});

      console.log('acknowledging message with', data);
      return messageAPI.acknowledge(data)
      .then(response => {
        console.log('got ', response);

        if (!response.success) {
          throw new Error (response.errors);
        }
        
        dispatch(receivedMessageAck(response.success, response.errors));
        return response;
        })
        .catch((error) => {
          dispatch(receivedMessageAck(false, error));
          throw error;
        });
    };
  }

};

module.exports = MessageActions;
