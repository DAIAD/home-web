var { push } = require('react-router-redux');

const { MESSAGE_TYPES } = require('../constants/HomeConstants');
var types = require('../constants/ActionTypes');
var messageAPI = require('../api/message');

const { getTypeByCategory } = require('../utils/messages');

const requestedMessages = function() {
  return {
    type: types.MESSAGE_REQUEST_START,
  };
};

const receivedMessages = function(success, errors) {
  return {
    type: types.MESSAGE_REQUEST_END,
    success,
    errors
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

  linkToMessage: function(options) {
    return function(dispatch, getState) {
      const { id, category } = options;

      if (category) dispatch(MessageActions.setActiveTab(category));
      if (id) dispatch(MessageActions.setActiveMessageId(id));
 
      dispatch(push('/notifications'));
    };
  },
  fetchAll: function() {
    return function(dispatch, getState) {
      dispatch(MessageActions.fetch(MESSAGE_TYPES.map(x => Object.assign({}, x, {ascending: false}))))
      .then(response => dispatch(MessageActions.setMessages(response)));
      //.then(response => dispatch(MessageActions.appendMessages('tips', [{acknowledgedOn: null, title: 'LALALA', description: 'lololololo', id: 19, type: 'RECOMMENDATION_STATIC'}])));
    };
  },
  fetch: function(options) {
    return function(dispatch, getState) {

      if (!Array.isArray(options)) throw new Error('Fetch requires array of options:', options);

      dispatch(requestedMessages());

      const data = Object.assign({}, {pagination: options}, {csrf: getState().user.csrf});

      return messageAPI.fetch(data)
      .then(response => {

        if (!response.success) {
          throw new Error (response.errors);
        }

        dispatch(receivedMessages(response.success, response.errors));

        return response;
        })
        .catch((error) => {
          dispatch(receivedMessages(false, error));
        });
    };
  },
  acknowledge: function(id, category, timestamp) {
    return function(dispatch, getState) {
      if (!id || !category || !timestamp) throw new Error(`Not sufficient data provided for message acknowledgement. (id, type, timestamp): ${id}, ${category}, ${timestamp}`);

      const message = getState().messages[category].find(x => x.id === id);
      
      if (message && message.acknowledgedOn != null) {
        return Promise.resolve();
      }
      dispatch(requestedMessageAck());

      const type = getTypeByCategory(category);
      const data = Object.assign({}, {messages: [{id, type, timestamp}]}, {csrf: getState().user.csrf});

      return messageAPI.acknowledge(data)
      .then(response => {

        if (!response.success) {
          console.error(response.errors && response.errors.length > 0 ? response.errors[0] : 'unknown');
        }
        
        dispatch(receivedMessageAck(response.success, response.errors));
        dispatch(MessageActions.setMessageRead(id, category, timestamp));
        
        return response;
        })
        .catch((error) => {
          dispatch(receivedMessageAck(false, error));
          throw error;
        });
    };
  },
  setMessageRead: function(id, category, timestamp) {
    return {
      type: types.MESSAGE_SET_READ,
      id,
      category,
      timestamp
    };
  }, 
  setMessages: function(response) {
    let messages = {};
    const { alerts, recommendations, tips, announcements } = response;
    if (alerts.length > 0) messages.alerts = alerts;
    if (announcements.length > 0) messages.announcements = announcements;
    if (recommendations.length > 0) messages.recommendations = recommendations;
    if (tips.length > 0) messages.tips = tips;

     console.log('setting messages', response, messages);
    return {
      type: types.MESSAGE_SET,
      messages
    };
  },
  appendMessages: function(type, messages) {
    if (!type || !messages) throw new Error('Not sufficient data provided for append messages', type, messages);
    if (!Array.isArray(messages)) throw new Error('Messages in append messages action must be of type array: ', messages);
    if (!(type === 'alerts' || type === 'announcements' || type === 'recommendations' || type === 'tips')) throw new Error('Append messages failed because type is not supported: ', type);
    return {
      type: types.MESSAGE_APPEND,
      category: type,
      messages
    };
  },
  setActiveTab: function(tab) {
    if (!(tab === 'alerts' || tab === 'announcements' || tab === 'recommendations' || tab === 'tips')) throw new Error ('Tab needs to be one of alerts, announcements, recommendations, tips. Provided: ', tab);

    return {
      type: types.MESSAGE_SET_ACTIVE_TAB,
      tab
    };
  },
  setActiveMessageId: function(id) {
    return function(dispatch, getState) {
      if (!id) throw new Error('Not sufficient data provided for selecting message, missing id');

      dispatch({
        type: types.MESSAGE_SET_ACTIVE,
        id
      });

      const category = getState().messages.activeTab;
      
      dispatch(MessageActions.acknowledge(id, category, new Date().getTime()));


    };
  }

};

module.exports = MessageActions;
