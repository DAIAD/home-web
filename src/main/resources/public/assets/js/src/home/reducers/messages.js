var types = require('../constants/ActionTypes');

const initialState = {
  activeTab: 'alerts',
  activeMessageId: null,
  alerts: [],
  recommendations: [],
  tips: [],
  announcements: []
};

var messages = function (state, action) {
  //initial state
  if (state === undefined) {
    state = initialState;
  }
   
  switch (action.type) {
    case types.MESSAGES_SET:
      return Object.assign({}, state, action.messages);

    case types.MESSAGES_APPEND: {
      let messages = state[action.category].slice().concat(action.messages);
      let newState = Object.assign({}, state);
      newState[action.category] = messages;

      return newState;
    }

    case types.MESSAGES_SET_ACTIVE_TAB:
      return Object.assign({}, state, {
        activeTab: action.category,
        activeMessageId: null
      });
    
    case types.MESSAGES_SET_ACTIVE:
      return Object.assign({}, state, {
        activeMessageId: action.id
      });

    case types.MESSAGE_SET_READ: {
      let messages = state[action.category].map(m => m.id === action.id ? Object.assign({}, m, {acknowledgedOn: action.timestamp}) : m);
      let newState = Object.assign({}, state);
      newState[action.category] = messages;
      
      return newState;
    }
  
    case types.MESSAGE_SET_EXTRA: {
      let messages = state[action.category].map(m => m.id === action.id ? Object.assign({}, m, action.extra) : m);
      let newState = Object.assign({}, state);
      newState[action.category] = messages;
      
      return newState;
    }

    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, initialState);

    default:
      return state;
  }
};

module.exports = messages;

