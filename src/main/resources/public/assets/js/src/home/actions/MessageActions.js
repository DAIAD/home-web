/**
 * Message Actions module.
 * Action creators for Messages section
 * 
 * @module MessageActions
 */

var { push } = require('react-router-redux');

const { MESSAGE_TYPES } = require('../constants/HomeConstants');
var types = require('../constants/ActionTypes');
var messageAPI = require('../api/message');

var QueryActions = require('./QueryActions');

const { getTypeByCategory, getInfoboxByAlertType } = require('../utils/messages');

const requestedMessages = function() {
  return {
    type: types.MESSAGES_REQUEST_START,
  };
};

const receivedMessages = function(success, errors) {
  return {
    type: types.MESSAGES_REQUEST_END,
    success,
    errors
  };
};

const requestedMessageAck = function() {
  return {
    type: types.MESSAGES_ACK_REQUEST_START,
  };
};

const receivedMessageAck = function(success, errors) {
  return {
    type: types.MESSAGES_ACK_REQUEST_END,
    success,
    errors,
  };
};

const setMessageExtra = function (id, category, extra) {
  return {
    type: types.MESSAGE_SET_EXTRA,
    id,
    category,
    extra
  };
};

const setMessageRead = function (id, category, timestamp) {
  return {
    type: types.MESSAGE_SET_READ,
    id,
    category,
    timestamp
  };
};

const setMessages = function (response) {
  let messages = {};
  const { alerts, recommendations, tips, announcements } = response;
  if (alerts.length > 0) messages.alerts = alerts;
  if (announcements.length > 0) messages.announcements = announcements;
  if (recommendations.length > 0) messages.recommendations = recommendations;
  if (tips.length > 0) messages.tips = tips;

  return {
    type: types.MESSAGES_SET,
    messages
  };
};

const appendMessages = function (type, messages) {
  if (!type || !messages) throw new Error('Not sufficient data provided for append messages', type, messages);
  if (!Array.isArray(messages)) throw new Error('Messages in append messages action must be of type array: ', messages);
  if (!(type === 'alerts' || type === 'announcements' || type === 'recommendations' || type === 'tips')) throw new Error('Append messages failed because type is not supported: ', type);
  return {
    type: types.MESSAGES_APPEND,
    category: type,
    messages
  };
};


/**
 * Updates all message options provided and switches to message section
 *
 * @param {Object} options - Contains messages section options to set active message (id, category)
 * @param {String} options.id - Message id to set active 
 * @param {Array} options.category - Message category to set active 
 */
const linkToMessage = function (options) {
  return function(dispatch, getState) {
    const { id, category } = options;

    if (category) dispatch(setActiveTab(category));
    if (id) dispatch(setActiveMessageId(id));

    dispatch(push('/notifications'));
  };
};

/**
 * Fetch messages with the given options 
 *
 * @param {Object[]} options - Fetch messages options array of objects
 * @param {String} options.type - The message type to fetch. 
 *                                One of ALERT, RECOMMENDATION_STATIC, RECOMMENDATION_DYNAMIC, ANNOUNCEMENT
 */
const fetch = function (options) {
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
};

/**
 * Fetch all messages in descending order (most recent first)
 */
const fetchAll = function () {
  return function(dispatch, getState) {
    dispatch(fetch(MESSAGE_TYPES.map(x => Object.assign({}, x, {ascending: false}))))
    .then(response => dispatch(setMessages(response)));
  };
};

/**
 * Acknowledge message
 *
 * @param {Number} id - Message id 
 * @param {String} category - The message category, a friendlier name for type 
 *                                One of alerts, tips, recommendations, announcements for  
 *                                        ALERT, RECOMMENDATION_STATIC, RECOMMENDATION_DYNAMIC, ANNOUNCEMENT
 *                                respectively
 * @param {Number} timestamp - The timestamp of the time of acknowledgement
 */
const acknowledge = function (id, category, timestamp) {
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
      dispatch(setMessageRead(id, category, timestamp));
      
      return response;
      })
      .catch((error) => {
        dispatch(receivedMessageAck(false, error));
        throw error;
      });
  };
};

/**
 * Set active message category 
 *
 * @param {String} category - The message category, a friendlier name for type 
 *                                One of alerts, tips, recommendations, announcements for  
 *                                        ALERT, RECOMMENDATION_STATIC, RECOMMENDATION_DYNAMIC, ANNOUNCEMENT
 *                                respectively
 */
const setActiveTab = function (category) {
  if (!(category === 'alerts' || category === 'announcements' || category === 'recommendations' || category === 'tips')) {
    throw new Error ('Tab needs to be one of alerts, announcements, recommendations, tips. Provided: ', category);
  }

  return {
    type: types.MESSAGES_SET_ACTIVE_TAB,
    category
  };
};

/**
 * Set active message id and acknowledge
 * Important! the message category must have been set otherwise
 *
 * @param {Number} id - The message id
 */
const setActiveMessageId = function (id) {
  return function(dispatch, getState) {
    if (!id) throw new Error('Not sufficient data provided for selecting message, missing id');

    dispatch({
      type: types.MESSAGES_SET_ACTIVE,
      id
    });

    const category = getState().messages.activeTab;
    const activeMessageIndex = getState().messages[category].findIndex(x => x.id === id);
    const activeMessage = activeMessageIndex != null ? getState().messages[category][activeMessageIndex] : null;

    dispatch(acknowledge(id, category, new Date().getTime()));
  

    if (category === 'alerts') {

      const infobox = getInfoboxByAlertType(activeMessage ? activeMessage.alert : null, activeMessage ? activeMessage.createdOn : null);
      if (!infobox) return;

      dispatch(QueryActions.fetchInfoboxData(infobox)) 
      .then(data => dispatch(setMessageExtra(id, category, {extra: Object.assign({}, infobox, data)})))
      .catch(error => {
        console.error('Oops, sth went wrong in setting message extra data', error);
      });
    }

  };
};

module.exports = {
  linkToMessage,
  fetch,
  fetchAll,
  acknowledge,
  setActiveTab,
  setActiveMessageId
};
