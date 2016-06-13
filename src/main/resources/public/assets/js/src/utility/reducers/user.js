var moment = require('moment');

var types = require('../constants/UserActionTypes');

var initialState = {
  isLoading : false,
  favorite : null,
  application : 'default',
  user : null,
  meters : null,
  devices : null,
  groups : null,
  data : {
    meters : null,
    devices : null,
    groups : {},
    deviceKey : null
  },
  interval : [
      moment().subtract(30, 'days'), moment()
  ],
  export : {
    token : null
  }
};

var _fillMeterSeries = function(interval, data) {
  var d;
  var allPoints = [];

  var ref = interval[1].clone();
  var days = interval[1].diff(interval[0], 'days') + 1;

  if ((data.length === 0) || (!data[0].values) || (data[0].values.length === 0)) {
    for (d = days; d > 0; d--) {
      allPoints.push({
        volume : 0,
        difference : 0,
        timestamp : ref.clone().toDate().getTime()
      });

      ref.subtract(1, 'days');
    }
  } else {
    var index = 0;
    var values = data[0].values;

    values.sort(function(p1, p2) {
      return (p2.timestamp - p1.timestamp);
    });

    for (d = days; d > 0; d--) {
      if (index === values.length) {
        allPoints.push({
          volume : 0,
          difference : 0,
          timestamp : ref.clone().toDate().getTime()
        });

        ref.subtract(1, 'days');
      } else if (ref.isBefore(values[index].timestamp, 'day')) {
        index++;
      } else if (ref.isAfter(values[index].timestamp, 'day')) {
        allPoints.push({
          volume : 0,
          difference : 0,
          timestamp : ref.clone().toDate().getTime()
        });

        ref.subtract(1, 'days');
      } else if (ref.isSame(values[index].timestamp, 'day')) {
        allPoints.push({
          difference : values[index].difference,
          volume : values[index].volume,
          timestamp : ref.clone().toDate().getTime()
        });

        index++;
        ref.subtract(1, 'days');
      }
    }

    allPoints.sort(function(p1, p2) {
      return (p1.timestamp - p2.timestamp);
    });

    data[0].values = allPoints;
  }

  return data;
};

var _fillGroupSeries = function(interval, data) {
  return data;
};

var user = function(state, action) {

  switch (action.type) {

    case types.USER_REQUEST_USER:
      return Object.assign({}, state, {
        isLoading : true,
        data : {
          meters : null,
          devices : null,
          groups : {},
          deviceKey : null
        }
      });

    case types.USER_RECEIVE_USER_INFO:
      return Object.assign({}, state, {
        isLoading : false,
        success : action.success,
        errors : action.errors,
        favorite : action.favorite,
        user : {
          id : action.user.id,
          firstName : action.user.firstName,
          lastName : action.user.lastName,
          email : action.user.email,
          gender : action.user.gender,
          registeredOn : new Date(action.user.registrationDateMils),
          country : action.user.country,
          city : action.user.city,
          address : action.user.address,
          postalCode : action.user.postalCode
        },
        meters : action.meters,
        devices : action.devices,
        groups : action.groups
      });

    case types.USER_SHOW_FAVOURITE_ACCOUNT_FORM:
      return Object.assign({}, state, {
        application : 'favouriteAccountForm'
      });

    case types.USER_HIDE_FAVOURITE_ACCOUNT_FORM:
      return Object.assign({}, state, {
        application : 'default'
      });

    case types.SELECT_AMPHIRO:
      return Object.assign({}, state, {
        isLoading : false,
        data : {
          meters : null,
          devices : state.data.devices,
          groups : {},
          deviceKey : action.deviceKey
        }
      });

    case types.AMPHIRO_REQUEST:
      return Object.assign({}, state, {
        isLoading : true,
        data : {
          meters : null,
          devices : null,
          groups : {},
          deviceKey : action.deviceKey
        }
      });

    case types.AMPHIRO_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
        data : {
          meters : null,
          devices : action.devices,
          groups : {},
          deviceKey : state.data.deviceKey
        }
      });

    case types.METER_REQUEST:
      return Object.assign({}, state, {
        isLoading : true,
        data : {
          meters : null,
          devices : null,
          groups : {},
          deviceKey : null
        }
      });

    case types.METER_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
        data : {
          meters : _fillMeterSeries(state.interval, action.meters),
          devices : null,
          groups : state.data.groups,
          deviceKey : null
        }
      });

    case types.GROUP_DATA_REQUEST:
      return Object.assign({}, state, {
        isLoading : true
      });

    case types.GROUP_DATA_RESPONSE:
      var data = {
        meters : state.data.meters,
        devices : state.data.devices,
        groups : state.data.groups,
        deviceKey : state.data.deviceKey
      };

      if (action.data) {
        data.groups[action.groupKey] = _fillGroupSeries(state.interval, action.data);
      }

      return Object.assign({}, state, {
        isLoading : false,
        data : data
      });

    case types.GROUP_DATA_CLEAR:
      return Object.assign({}, state, {
        isLoading : false,
        data : {
          meters : state.data.meters,
          devices : state.data.devices,
          groups : {},
          deviceKey : state.data.deviceKey
        }
      });

    case types.EXPORT_REQUEST:
      return Object.assign({}, state, {
        isLoading : true,
        export : {
          token : null
        }
      });

    case types.EXPORT_COMPLETE:
      return Object.assign({}, state, {
        isLoading : false,
        export : {
          token : action.token
        }
      });

    case types.ADD_FAVORITE_REQUEST:
    case types.REMOVE_FAVORITE_REQUEST:
      return Object.assign({}, state, {
        isLoading : true
      });

    case types.ADD_FAVORITE_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
        favorite : (action.success ? true : state.favorite)
      });

    case types.REMOVE_FAVORITE_RESPONSE:
      return Object.assign({}, state, {
        isLoading : false,
        favorite : (action.success ? false : state.favorite)
      });

    default:
      return state || initialState;
  }

};

module.exports = user;
