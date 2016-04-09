var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  activity : null,
  user : {
    name : null,
    devices : null
  },
  filter : null
};

var admin = function(state, action) {
  switch (action.type) {
    case types.ADMIN_REQUESTED_ACTIVITY:
      return Object.assign({}, state, {
        isLoading : true
      });

    case types.ADMIN_RECEIVED_ACTIVITY:
      return Object.assign({}, state, {
        isLoading : false,
        activity : action.activity
      });

    case types.ADMIN_REQUESTED_SESSIONS:
      return Object.assign({}, state, {
        isLoading : true,
        user : {
          name : action.username,
          devices : null,
          meters : null
        }
      });

    case types.ADMIN_RECEIVED_SESSIONS:
      return Object.assign({}, state, {
        isLoading : false,
        user : {
          name : state.user.name,
          devices : action.devices,
          meters : null
        }
      });

    case types.ADMIN_REQUESTED_METERS:
      return Object.assign({}, state, {
        isLoading : true,
        user : {
          name : action.username,
          devices : null,
          meters : null
        }
      });

    case types.ADMIN_RECEIVED_METERS:
      return Object.assign({}, state, {
        isLoading : false,
        user : {
          name : state.user.name,
          devices : null,
          meters : action.meters
        }
      });

    case types.ADMIN_RESET_USER_DATA:
      return Object.assign({}, state, {
        isLoading : false,
        user : {
          name : null,
          devices : null,
          meters : null
        }
      });

    case types.ADMIN_FILTER_USER:
      return Object.assign({}, state, {
        filter : action.filter || null
      });

    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, state, {
        isLoading : false,
        activity : null,
        user : {
          name : null,
          devices : null
        },
        filter : null
      });

    default:
      return state || initialState;
  }
};

module.exports = admin;
