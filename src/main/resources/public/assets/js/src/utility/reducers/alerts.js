var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  utility : null,
  utilities : null,
  tips : null
};

var alerts = function(state, action) {
    switch (action.type) {
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

        default:
          return state || initialState;
    }
};

module.exports = alerts;
