var types = require('../constants/ActionTypes');

var messages = function (state, action) {
  //initial state
  if (state === undefined) {
    state = {
      activeTab: 'alerts',
      activeMessageId: null,
      alerts: [],
      recommendations: [],
      tips: [],
      announcements: []
    };
  }
   
  switch (action.type) {
    case types.MESSAGE_SET:
      return Object.assign({}, state, action.messages);

    case types.MESSAGE_APPEND: {
      let messages = state[action.category].slice().concat(action.messages);
      let newState = Object.assign({}, state);
      newState[action.category] = messages;

      return newState;
    }

    case types.MESSAGE_SET_READ: {
      let messages = state[action.category].map(m => m.id === action.id ? Object.assign({}, m, {acknowledgedOn: action.timestamp}) : m);
      let newState = Object.assign({}, state);
      newState[action.category] = messages;
      
      return newState;
    }

    case types.MESSAGE_SET_ACTIVE_TAB:
      return Object.assign({}, state, {
        activeTab: action.category,
        activeMessageId: null
      });
    
    case types.MESSAGE_SET_ACTIVE:
      return Object.assign({}, state, {
        activeMessageId: action.id
      });
 
    default:
      return state;
  }
};

module.exports = messages;

