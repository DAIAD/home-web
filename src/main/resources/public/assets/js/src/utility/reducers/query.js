var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  query : null,
  areas : null,
  meters : null,
  devices : null
};

var query = function(state, action) {
  switch (action.type) {
    case types.QUERY_SUBMIT:
      return Object.assign({}, state, {
        isLoading : true,
        query : action.query,
        points : null
      });

    case types.QUERY_RESPONSE:
      if (action.success) {
        return Object.assign({}, state, {
          isLoading : false,
          areas : action.data.areas,
          meters : action.data.meters,
          devices : action.data.devices
        });
      }

      return Object.assign({}, state, {
        isLoading : false,
        areas : null,
        meters : null,
        devices : null
      });
      
    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, state, {
        isLoading : false,
        query : null,
        areas : null,
        meters : null,
        devices : null
      });

    default:
      return state || initialState;
  }
};

module.exports = query;
