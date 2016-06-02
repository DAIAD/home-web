var types = require('../constants/ActionTypes');
var Helpers = require('../helpers/helpers');

var initialState = {
  isLoading : false,
  utility : null,
  utilities : null,
  tips : null,
  activePage: 0,
  saveButtonDisabled : true,
  show: false,
  modes: null
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
        case types.CHECKBOX_CLICKED:
          console.log('checkbox clicked');
          return Object.assign({}, state, {
              tips : action.tips
            }); 
	case types.ADMIN_REQUESTED_ADD_TIP:
          console.log('reducer requested add tip');
            return Object.assign({}, state, {
		isLoading : true
            }); 
	case types.ADMIN_ADD_TIP_RESPONSE:
          console.log('reducer add tip response');
            return Object.assign({}, state, {
		isLoading : false
            });   
	case types.ADMIN_ADD_TIP_SHOW:
          console.log('reducer show form');
            return Object.assign({}, state, {
              show : true
            }); 
	case types.ADMIN_CANCEL_ADD_TIP_SHOW:
          console.log('reducer cancel add');
            return Object.assign({}, state, {
              show : false
            });   
	case types.STATIC_TIPS_ACTIVE_PAGE:
          console.log('reducer active page');
            return Object.assign({}, state, {
                    activePage : action.activePage
            });         
        default:
          return state || initialState;
    }
};

module.exports = alerts;
