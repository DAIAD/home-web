var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  utility : null,
  utilities : null,
  tips : null,
  activePage: 0,
  saveButtonDisabled : true
};

var alerts = function(state, action) {
    switch (action.type) {
        case types.SAVE_BUTTON_DISABLE:
          console.log('reducer SAVE_BUTTON_DISABLE');
          return Object.assign({}, state, { 
              isLoading : false,
              saveButtonDisabled : action.saveButtonDisabled
          });
        case types.ADMIN_REQUESTED_UTILITIES:
          return Object.assign({}, state, { 
              isLoading : true,
              utilities : null
          });
        case types.ADMIN_RECEIVED_UTILITIES:
            return Object.assign({}, state, { 
                isLoading : false,
                utilities : action.utilities
            });
        case types.ADMIN_SELECTED_UTILITY_FILTER: 
            return Object.assign({}, state, { 
                isLoading : true,
                utility : action.utility
            });

        case types.ADMIN_REQUESTED_STATIC_TIPS:
            return Object.assign({}, state, { 
                isLoading : true,
                tips : null
            });
        case types.ADMIN_RECEIVED_STATIC_TIPS:
            return Object.assign({}, state, { 
                isLoading : false,
                tips : action.tips
            });
        case types.ADMIN_CLICKED_SAVE_BUTTON:
            return Object.assign({}, state, { 
                isLoading : true
            });            
        case types.ADMIN_SAVED_ACTIVE_TIPS:
            return Object.assign({}, state, { 
                isLoading : false,
                changedTips : action.changedTips
            });
        case types.ADMIN_SAVE_BUTTON_RESPONSE:
            return Object.assign({}, state, { 
                isLoading : false
            }); 
	case types.MESSAGES_SET_ACTIVE_PAGE:
            return Object.assign({}, state, {
		activePage : action.activePage
            });          
        case types.CHECKBOX_CLICKED:
          console.log('checkbox clicked');
          return Object.assign({}, state, {
              tips : action.tips
            });           
        default:
          return state || initialState;
    }
};

module.exports = alerts;
