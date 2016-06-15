var moment = require('moment');

var types = require('../constants/GroupCatalogActionTypes');

var groupAPI = require('../api/group');
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

var getGroupsInit = function() {
  return {
    type : types.GROUP_CATALOG_REQUEST
  };
};

var getGroupsComplete = function(success, errors, total, groups, index, size) {
  return {
    type : types.GROUP_CATALOG_RESPONSE,
    success : success,
    errors : errors,
    total : total,
    groups : groups,
    index : index,
    size : size
  };
};

var changeIndex = function(index) {
  return {
    type : types.GROUP_CATALOG_INDEX_CHANGE,
    index : index
  };
};

var getChartInit = function(groupKey, label) {
  return {
    type : types.GROUP_CATALOG_CHART_REQUEST,
    groupKey : groupKey,
    label : label
  };
};

var getChartComplete = function(success, errors, groupKey, label, data) {
  return {
    type : types.GROUP_CATALOG_CHART_RESPONSE,
    success : success,
    errors : errors,
    groupKey : groupKey,
    label : label,
    data : data
  };
};

var deleteGroupInit = function(groupKey) {
  return {
    type : types.GROUP_CATALOG_DELETE_REQUEST,
    groupKey : groupKey
  };
};

var deleteGroupComplete = function(success, errors) {
  return {
    type : types.GROUP_CATALOG_DELETE_RESPONSE,
    success : success,
    errors : errors
  };
};

var GroupCatalogActionCreators = {

  changeIndex : function(index) {
    return changeIndex(index);
  },

  getGroups : function() {
    return function(dispatch, getState) {
      dispatch(changeIndex(0));

      dispatch(getGroupsInit());

      return groupAPI.getGroups(getState().userCatalog.query).then(
          function(response) {
            dispatch(getGroupsComplete(response.success, response.errors, response.total, response.groups,
                response.index, response.size));
          }, function(error) {
            dispatch(getGroupsComplete(false, error));
          });
    };
  },

  deleteGroup : function() {
    return function(dispatch, getState) {
      dispatch(deleteGroupInit());

      return groupAPI.getGroups(getState().userCatalog.query).then(function(response) {
        dispatch(deleteGroupComplete(response.success, response.errors));
      }, function(error) {
        dispatch(deleteGroupComplete(false, error));
      });
    };
  },

  clearChart : function() {
    return {
      type : types.GROUP_CATALOG_CLEAR_CHART
    };
  },

  getChart : function(groupKey, label, timezone) {
    return function(dispatch, getState) {
      dispatch(getChartInit(groupKey, label));

      var query = _buildGroupQuery(groupKey, label, timezone);

      return queryAPI.queryMeasurements(query).then(function(response) {
        if (response.success) {
          var data = ((response.meters) && (response.meters.length > 0)) ? response.meters[0] : null;

          dispatch(getChartComplete(response.success, response.errors, groupKey, label, data));
        } else {
          dispatch(getChartComplete(response.success, response.errors, groupKey, label, []));
        }
      }, function(error) {
        dispatch(getChartComplete(false, error));
      });
    };
  },

  addFavorite : function(groupKey) {
    return function(dispatch, getState) {
      dispatch({
        type : types.GROUP_CATALOG_ADD_FAVORITE_REQUEST,
        groupKey : groupKey
      });

      return groupAPI.addFavorite(groupKey).then(function(response) {
        dispatch({
          type : types.GROUP_CATALOG_ADD_FAVORITE_RESPONSE,
          success : response.success,
          errors : response.errors,
          groupKey : groupKey,
          favorite : true
        });
      }, function(error) {
        dispatch({
          type : types.GROUP_CATALOG_ADD_FAVORITE_RESPONSE,
          success : false,
          errors : error
        });
      });
    };
  },

  removeFavorite : function(groupKey) {
    return function(dispatch, getState) {
      dispatch({
        type : types.GROUP_CATALOG_REMOVE_FAVORITE_REQUEST,
        groupKey : groupKey
      });

      return groupAPI.removeFavorite(groupKey).then(function(response) {
        dispatch({
          type : types.GROUP_CATALOG_REMOVE_FAVORITE_RESPONSE,
          success : response.success,
          errors : response.errors,
          groupKey : groupKey,
          favorite : false
        });
      }, function(error) {
        dispatch({
          type : types.GROUP_CATALOG_REMOVE_FAVORITE_RESPONSE,
          success : false,
          errors : error
        });
      });
    };
  },

  filterByType : function(type) {
    return {
      type : types.GROUP_CATALOG_FILTER_TYPE,
      groupType : type
    };
  },

  filterByName : function(name) {
    return {
      type : types.GROUP_CATALOG_FILTER_NAME,
      name : name
    };
  },

  clearFilter : function() {
    return {
      type : types.GROUP_CATALOG_FILTER_CLEAR
    };
  },

  setChartMetric : function(metric) {
    return {
      type : types.GROUP_CATALOG_SET_METRIC,
      metric : metric
    };
  }

};

module.exports = GroupCatalogActionCreators;
