var types = require('../constants/UserCatalogActionTypes');

var userAPI = require('../api/user');
var adminAPI = require('../api/admin');
var groupAPI = require('../api/group');

var getAccountsInit = function() {
  return {
    type : types.USER_CATALOG_REQUEST_INIT
  };
};

var getAccountsComplete = function(success, errors, total, accounts, index, size) {
  return {
    type : types.USER_CATALOG_REQUEST_COMPLETE,
    success : success,
    errors : errors,
    total : total,
    accounts : accounts,
    index : index,
    size : size
  };
};

var changeIndex = function(index) {
  return {
    type : types.USER_CATALOG_CHANGE_INDEX,
    index : index
  };
};

var filterText = function(text) {
  return {
    type : types.USER_CATALOG_FILTER_TEXT,
    text : text
  };
};

var filterSerial = function(serial) {
  return {
    type : types.USER_CATALOG_FILTER_SERIAL,
    serial : serial
  };
};

var clearFilter = function() {
  return {
    type : types.USER_CATALOG_FILTER_CLEAR
  };
};

var meterRequestInit = function(userKey, deviceKey) {
  return {
    type : types.USER_CATALOG_METER_REQUEST,
    userKey : userKey,
    deviceKey : deviceKey
  };
};

var meterRequestComplete = function(success, errors, userKey, data) {
  return {
    type : types.USER_CATALOG_METER_RESPONSE,
    success : success,
    errors : errors,
    userKey : userKey,
    data : data
  };
};

var setGeometry = function(geometry) {
  return {
    type : types.USER_CATALOG_SET_SEARCH_GEOMETRY,
    geometry : geometry
  };
};

var UserCatalogActionCreators = {

  changeIndex : function(index) {
    return function(dispatch, getState) {
      dispatch(changeIndex(index));

      return userAPI.getAccounts(getState().userCatalog.query).then(
          function(response) {
            dispatch(getAccountsComplete(response.success, response.errors, response.total, response.accounts,
                response.index, response.size));
          }, function(error) {
            dispatch(getAccountsComplete(false, error));
          });
    };
  },

  getAccounts : function() {
    return function(dispatch, getState) {
      dispatch(getAccountsInit());

      return userAPI.getAccounts(getState().userCatalog.query).then(
          function(response) {
            dispatch(getAccountsComplete(response.success, response.errors, response.total, response.accounts,
                response.index, response.size));
          }, function(error) {
            dispatch(getAccountsComplete(false, error));
          });
    };
  },

  filterSerial : function(serial) {
    return {
      type : types.USER_CATALOG_FILTER_SERIAL,
      serial : serial
    };
  },

  filterText : function(text) {
    return {
      type : types.USER_CATALOG_FILTER_TEXT,
      text : text
    };
  },

  clearFilter : function() {
    return function(dispatch, getState) {
      dispatch(clearFilter());

      dispatch(getAccountsInit());

      return userAPI.getAccounts(getState().userCatalog.query).then(
          function(response) {
            dispatch(getAccountsComplete(response.success, response.errors, response.total, response.accounts,
                response.index, response.size));
          }, function(error) {
            dispatch(getAccountsComplete(false, error));
          });
    };
  },

  getMeter : function(userKey, deviceKey, label) {
    return function(dispatch, getState) {
      dispatch(meterRequestInit(userKey, deviceKey));

      return adminAPI.getMeters(userKey).then(function(response) {
        var data = null;

        if (response.series) {
          for ( var index in response.series) {
            if (response.series[index].deviceKey === deviceKey) {
              response.series[index].label = label + ' - ' + response.series[index].serial;
              dispatch(meterRequestComplete(response.success, response.errors, userKey, response.series[index]));
              break;
            }
          }
        }

        dispatch(meterRequestComplete(response.success, response.errors, userKey, data));
      }, function(error) {
        dispatch(meterRequestComplete(false, error, userKey, null));
      });
    };
  },

  clearChart : function() {
    return {
      type : types.USER_CATALOG_CLEAR_CHART
    };
  },

  setSearchModeText : function() {
    return {
      type : types.USER_CATALOG_SET_SEARCH_MODE,
      search : 'text'
    };
  },

  setSearchModeMap : function() {
    return {
      type : types.USER_CATALOG_SET_SEARCH_MODE,
      search : 'map'
    };
  },

  setGeometry : function(geometry) {
    return function(dispatch, getState) {
      dispatch(setGeometry(geometry));

      dispatch(getAccountsInit());

      return userAPI.getAccounts(getState().userCatalog.query).then(
          function(response) {
            dispatch(getAccountsComplete(response.success, response.errors, response.total, response.accounts,
                response.index, response.size));
          }, function(error) {
            dispatch(getAccountsComplete(false, error));
          });
    };
  },

  addFavorite : function(userKey) {
    return function(dispatch, getState) {
      dispatch({
        type : types.USER_CATALOG_ADD_FAVORITE_REQUEST,
        userKey : userKey
      });

      return userAPI.addFavorite(userKey).then(function(response) {
        dispatch({
          type : types.USER_CATALOG_ADD_FAVORITE_RESPONSE,
          success : response.success,
          errors : response.errors,
          userKey : userKey,
          favorite : true
        });
      }, function(error) {
        dispatch({
          type : types.USER_CATALOG_ADD_FAVORITE_RESPONSE,
          success : false,
          errors : error
        });
      });
    };
  },

  removeFavorite : function(userKey) {
    return function(dispatch, getState) {
      dispatch({
        type : types.USER_CATALOG_REMOVE_FAVORITE_REQUEST,
        userKey : userKey
      });

      return userAPI.removeFavorite(userKey).then(function(response) {
        dispatch({
          type : types.USER_CATALOG_REMOVE_FAVORITE_RESPONSE,
          success : response.success,
          errors : response.errors,
          userKey : userKey,
          favorite : false
        });
      }, function(error) {
        dispatch({
          type : types.USER_CATALOG_REMOVE_FAVORITE_RESPONSE,
          success : false,
          errors : error
        });
      });
    };
  },

  setSelectionMode : function(enabled) {
    return {
      type : types.USER_CATALOG_CREATE_BAG_OF_CONSUMER,
      enabled : enabled
    };
  },

  discardBagOfConsumers : function() {
    return {
      type : types.USER_CATALOG_DISCARD_BAG_OF_CONSUMER
    };
  },

  saveBagOfConsumers : function(title, members) {
    return function(dispatch, getState) {
      dispatch({
        type : types.USER_CATALOG_SAVE_BAG_OF_CONSUMER_REQUEST
      });

      return groupAPI.create(title, members).then(function(response) {
        dispatch({
          type : types.USER_CATALOG_SAVE_BAG_OF_CONSUMER_RESPONSE,
          success : response.success,
          errors : response.errors
        });
      }, function(error) {
        dispatch({
          type : types.USER_CATALOG_SAVE_BAG_OF_CONSUMER_RESPONSE,
          success : false,
          errors : error
        });
      });
    };
  },

  toggleConsumer : function(id) {
    return {
      type : types.USER_CATALOG_TOGGLE_CONSUMER,
      id : id
    };
  }

};

module.exports = UserCatalogActionCreators;
