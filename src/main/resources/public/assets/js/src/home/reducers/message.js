var types = require('../constants/ActionTypes');

var message = function (state, action) {
  //initial state
  if (state === undefined) {
    state = {
      alerts: [],
      recommendations: [],
      tips: [],
      announcements: []
    };
  }
   
  switch (action.type) {
    case types.MESSAGE_REQUEST_END:
      switch (action.success) {
        case true:
          return Object.assign({}, state, action.data);

        default:
          return state;
      }
      
      break;
      
    default:
      return state;
  }
};

module.exports = message;

