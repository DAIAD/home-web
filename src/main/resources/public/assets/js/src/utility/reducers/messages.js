var types = require('../constants/ActionTypes');

var initialState = {
  isLoading: false,
  messages: null,
  showReceivers: false,
  receivers: null,
  selectedMessage: null
};

var messages = function (state, action) {
  switch (action.type) {
    case types.MESSAGES_REQUESTED_STATISTICS:
      return Object.assign({}, state, {
        isLoading: true
      });    
    case types.MESSAGES_RECEIVED_STATISTICS:
      return Object.assign({}, state, {
        isLoading: false,
        showReceivers: false,
        messages: action.messages
      });   
    case types.MESSAGES_SHOW_RECEIVERS:
      return Object.assign({}, state, {
        showReceivers: true
      });     
    case types.MESSAGES_REQUESTED_RECEIVERS:
      return Object.assign({}, state, {
        isLoading: true
      }); 
    case types.MESSAGES_RECEIVED_RECEIVERS:
      return Object.assign({}, state, {
        isLoading: false,
        receivers: action.receivers,
        showReceivers: true
      });   
    case types.MESSAGES_SELECTED_MESSAGE:
      return Object.assign({}, state, {
        selectedMessage: action.selectedMessage
      });       
    case types.MESSAGES_RETURN_BACK:
      return Object.assign({}, state, {
        showReceivers: false,
        isLoading: false,
        selectedMessage: null,
        receivers: null
      });      
    default:
      return state || initialState;
  }
};

module.exports = messages;