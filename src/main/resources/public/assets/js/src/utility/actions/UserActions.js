var $ = require('jquery');
var moment = require('moment');

var types = require('../constants/UserActionTypes');

var userAPI = require('../api/user');
var adminAPI = require('../api/admin');
var queryAPI = require('../api/query');

var _buildGroupQuery = function(key, label, timezone) {
  var interval = [
      moment().subtract(30, 'days').valueOf(), moment().valueOf()
  ];

  return {
    'query' : {
      'timezone' : timezone,
      'time' : {
        'type' : 'ABSOLUTE',
        'start' : interval[0],
        'end' : interval[1],
        'granularity' : 'DAY'
      },
      'population' : [
        {
          'type' : 'GROUP',
          'label' : label,
          'group' : key
        }
      ],
      'source' : 'METER',
      'metrics' : [
        'AVERAGE'
      ]
    }
  };
};

var requestedUser = function() {
  return {
    type : types.USER_REQUEST_USER
  };
};

var receivedUser = function(success, errors, user, meters, devices, configurations, groups, favorite) {
  return {
    type : types.USER_RECEIVE_USER_INFO,
    success : success,
    errors : errors,
    favorite : favorite,
    user : user,
    meters : meters,
    devices : devices,
    configurations: configurations,
    groups : groups
  };
};

var selectAmphiro = function(userKey, deviceKey) {
  return {
    type : types.SELECT_AMPHIRO,
    userKey : userKey,
    deviceKey : deviceKey
  };
};

var requestedSessions = function(userKey, deviceKey) {
  return {
    type : types.AMPHIRO_REQUEST,
    userKey : userKey,
    deviceKey : deviceKey
  };
};

var receivedSessions = function(success, errors, devices) {
  return {
    type : types.AMPHIRO_RESPONSE,
    success : success,
    errors : errors,
    devices : devices
  };
};

var requestedMeters = function(userKey) {
  return {
    type : types.METER_REQUEST,
    userKey : userKey
  };
};

var receivedMeters = function(success, errors, meters) {
  return {
    type : types.METER_RESPONSE,
    success : success,
    errors : errors,
    meters : meters
  };
};

var requestedGroup = function(groupKey, label) {
  return {
    type : types.GROUP_DATA_REQUEST,
    groupKey : groupKey,
    label : label
  };
};

var receivedGroup = function(success, errors, groupKey, meters) {
  return {
    type : types.GROUP_DATA_RESPONSE,
    success : success,
    errors : errors,
    groupKey : groupKey,
    data : (meters.length === 0 ? null : meters[0])
  };
};

var requestedExport = function(userKey, username) {
  return {
    type : types.EXPORT_REQUEST,
    userKey : userKey,
    username : username
  };
};

var receivedExport = function(success, errors, token) {
  return {
    type : types.EXPORT_RESPONSE,
    success : success,
    errors : errors,
    token : token
  };
};

var UserActions = {

  showUser : function(userId) {
    return function(dispatch, getState) {
      dispatch(requestedUser());

      return userAPI.fetchUser(userId).then(
          function(response) {
            dispatch(receivedUser(response.success, response.errors, response.user, response.meters, response.devices,
                response.configurations, response.groups, response.favorite));

            if (response.meters.length > 0) {
              return adminAPI.getMeters(response.user.id, response.user.email).then(function(response) {
                dispatch(receivedMeters(response.success, response.errors, response.series));
              }, function(error) {
                dispatch(receivedMeters(false, error, null));
              });
            }
          }, function(error) {
            dispatch(receivedUser(false, error, null));
          });
    };
  },

  getSessions : function(userKey, deviceKey) {
    return function(dispatch, getState) {
      var data = getState().user.data;

      if ((data) && (data.devices)) {
        dispatch(selectAmphiro(userKey, deviceKey));

        dispatch(receivedSessions(true, [], data.devices));
      } else {
        dispatch(requestedSessions(userKey, deviceKey));

        return adminAPI.getSessions(userKey).then(function(response) {
          dispatch(receivedSessions(response.success, response.errors, response.devices));
        }, function(error) {
          dispatch(receivedSessions(false, error, null));
        });
      }
    };
  },

  getMeters : function(userKey) {
    return function(dispatch, getState) {
      dispatch(requestedMeters(userKey));

      return adminAPI.getMeters(userKey).then(function(response) {
        dispatch(receivedMeters(response.success, response.errors, response.series));
      }, function(error) {
        dispatch(receivedMeters(false, error, null));
      });
    };
  },

  showFavouriteAccountForm : function(accountId) {
    return {
      type : types.USER_SHOW_FAVOURITE_ACCOUNT_FORM,
      accountId : accountId
    };
  },

  hideFavouriteAccountForm : function() {
    return {
      type : types.USER_HIDE_FAVOURITE_ACCOUNT_FORM
    };
  },

  clearGroupSeries : function() {
    return {
      type : types.GROUP_DATA_CLEAR
    };
  },

  getGroupSeries : function(groupKey, label, timezone) {
    return function(dispatch, getState) {
      dispatch(requestedGroup(groupKey, label));

      var query = _buildGroupQuery(groupKey, label, timezone);

      return queryAPI.queryMeasurements(query).then(function(response) {
        if (response.success) {
          dispatch(receivedGroup(response.success, response.errors, groupKey, response.meters));
        } else {
          dispatch(receivedGroup(response.success, response.errors, groupKey, []));
        }
      }, function(error) {
        dispatch(receivedGroup(false, error, null, null));
      });
    };
  },

  exportData : function(userKey, username) {
    return function(dispatch, getState) {
      dispatch(requestedExport(userKey, username));

      return adminAPI.exportUserData(userKey).then(function(response) {
        dispatch(receivedExport(response.success, response.errors, response.token));

        var content = [];
        content.push('<div id="export-download-frame" style="display: none">');
        content.push('<iframe src="/action/data/download/' + response.token + '/"></iframe>');
        content.push('</div>');

        $('#export-download-frame').remove();
        $('body').append(content.join(''));
      }, function(error) {
        dispatch(receivedExport(false, error, null));
      });
    };
  },

  addFavorite : function(userKey) {
    return function(dispatch, getState) {
      dispatch({
        type : types.ADD_FAVORITE_REQUEST,
        userKey : userKey
      });

      return userAPI.addFavorite(userKey).then(function(response) {
        dispatch({
          type : types.ADD_FAVORITE_RESPONSE,
          success : response.success,
          errors : response.errors
        });
      }, function(error) {
        dispatch({
          type : types.ADD_FAVORITE_RESPONSE,
          success : false,
          errors : error
        });
      });
    };
  },

  removeFavorite : function(userKey) {
    return function(dispatch, getState) {
      dispatch({
        type : types.REMOVE_FAVORITE_REQUEST,
        userKey : userKey
      });

      return userAPI.removeFavorite(userKey).then(function(response) {
        dispatch({
          type : types.REMOVE_FAVORITE_RESPONSE,
          success : response.success,
          errors : response.errors
        });
      }, function(error) {
        dispatch({
          type : types.REMOVE_FAVORITE_RESPONSE,
          success : false,
          errors : error
        });
      });
    };
  }

};

module.exports = UserActions;
