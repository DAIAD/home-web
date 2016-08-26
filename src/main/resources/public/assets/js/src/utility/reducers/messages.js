var types = require('../constants/ActionTypes');

var initialState = {
  isLoading: false,
  messages: null
};

var messages = function (state, action) {
  switch (action.type) {
    case types.MESSAGES_REQUESTED_STATISTICS:
      return Object.assign({}, state, {
        isLoading: true
      });    
    case types.MESSAGES_RECEIVED_STATISTICS:
      return Object.assign({}, state, {
        isLoading: true,
        messages: action.messages
      });        
    default:
      return state || initialState;
  }
};

module.exports = messages;