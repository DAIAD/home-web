var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  utility : null,
  utilities : null,
  tips : null
};

var alerts = function(state, action) {
    switch (action.type) {
        case types.ALERTS_UTILITY_SELECTED:
            console.log('reducer: ALERTS_UTILITY_SELECTED');  
            return Object.assign({}, state, { 
                isLoading : true,
                utility : action.utility
            });

        case types.ADMIN_REQUESTED_TIPS:
            console.log('reducer: ADMIN_REQUESTED_TIPS');
            return Object.assign({}, state, { 
                isLoading : true,
                tips : null
            });

        case types.ADMIN_RECEIVED_TIPS:
            console.log('reducer: ADMIN_RECEIVED_TIPS');
            return Object.assign({}, state, { 
                isLoading : false,
                tips : action.tips
            });

        default:
          return state || initialState;
    }
};

module.exports = alerts;
