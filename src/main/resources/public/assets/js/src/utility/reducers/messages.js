var types = require('../constants/ActionTypes');
var moment = require('moment');

var initialState = {
  isLoading: false,
  messages: null,
  showReceivers: false,
  receivers: null,
  selectedMessage: null,
  editor: null,
  interval: [moment().subtract(14, 'day'), moment()]
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
    case types.MESSAGES_SET_EDITOR_VALUE:
      switch (action.editor) {
        case 'interval':
          console.log('returning interval..');
          console.log(action.editor);
          console.log(action.value);          
          return Object.assign({}, state, {
            interval : action.value
          });
        case 'population':
          var group = action.value;

          switch (group.type) {
            case 'UTILITY':
              return Object.assign({}, state, {
                population : {
                  utility : group.key,
                  label : group.name,
                  type : 'UTILITY'
                }
              });
            case 'SEGMENT':
            case 'SET':
              var label = group.name;
              if (group.type === 'SEGMENT') {
                label = group.cluster + ': ' + label;
              }
              return Object.assign({}, state, {
                population : {
                  group : group.key,
                  label : label,
                  type : 'GROUP'
                }
              });
          }

          return state;
        case 'spatial':
          return Object.assign({}, state, {
            geometry : action.value
          });
      }

      return state;      
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