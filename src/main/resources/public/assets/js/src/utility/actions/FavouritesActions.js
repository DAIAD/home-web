var types = require('../constants/FavouritesActionTypes');
var favouritesAPI = require('../api/favourites');
var queryAPI = require('../api/query');
var moment = require('moment');

var requestedFavouriteQueries = function () {
  return {
    type: types.FAVOURITES_REQUEST_QUERIES
  };
};

var receivedFavouriteQueries = function (success, errors, favourites) {
  return {
    type: types.FAVOURITES_RECEIVE_QUERIES,
    success: success,
    errors: errors,    
    favourites: favourites
  };
};

var addFavouriteRequest = function () {
  return {
    type: types.FAVOURITES_ADD_FAVOURITE_REQUEST
  };
};

var addFavouriteResponse = function (success, errors) {
  return {
    type: types.FAVOURITES_ADD_FAVOURITE_RESPONSE,
    success: success,
    errors: errors
  };
};

var deleteFavouriteResponse = function (success, errors) {
  return {
    type: types.FAVOURITES_DELETE_QUERY_RESPONSE,
    success: success,
    errors: errors
  };
};

var _openFavourite = function(favourite) {
    return {
      type : types.FAVOURITES_OPEN_SELECTED,
      showSelected : true,
      selectedFavourite: favourite
    };
};

var _buildTimelineQuery = function(population, source, geometry, timezone, interval) {
  var spatial = [
    {
      type : 'GROUP',
      group : 'd29f8cb8-7df6-4d57-8c99-0a155cc394c5'
    }
  ];

  if (geometry) {
    spatial.push({
      type : 'CONSTRAINT',
      geometry : geometry,
      operation : 'INTERSECT'
    });
  }

  return {
    'query' : {
      'timezone' : "Europe/Athens",
      'time' : {
        'type' : 'ABSOLUTE',
        'start' : interval[0].toDate().getTime(),
        'end' : interval[1].toDate().getTime(),
        'granularity' : 'DAY'
      },
      'population' : [
        population
      ],
      spatial : spatial,
      'source' : source,
      'metrics' : [
        'SUM'
      ]
    }
  };
};

var _getTimelineComplete = function(success, errors, data) {
  return {
    type : types.FAVOURITES_TIMELINE_RESPONSE,
    success : success,
    errors : errors,
    data : data
  };
};

var _getFeatures = function(index, timestamp, label) {
  return {
    type : types.FAVOURITES_GET_FEATURES,
    timestamp : timestamp,
    label : label,
    index : index
  };
};

var FavouritesActions = {

  setTimezone : function(timezone) {
    return {
      type : types.FAVOURITES_SET_TIMEZONE,
      timezone : timezone
    };
  },
  fetchFavouriteQueries : function() {
    return function(dispatch, getState) {
      dispatch(requestedFavouriteQueries());
      return favouritesAPI.fetchFavouriteQueries().then(function (response) {
        dispatch(receivedFavouriteQueries(response.success, response.errors, response.queries));
      }, function (error) {
        dispatch(receivedFavouriteQueries(false, error, null));
      });
    };
  },
  
  addCopy : function(favourite) {
    return function(dispatch, getState) {
      dispatch(addFavouriteRequest());
      return favouritesAPI.addFavourite(favourite).then(function (response) {
        dispatch(addFavouriteResponse(response.success, response.errors));     
        dispatch(requestedFavouriteQueries());
        return favouritesAPI.fetchFavouriteQueries().then(function (response) {
          dispatch(receivedFavouriteQueries(response.success, response.errors, response.queries));
        }, function (error) {
          dispatch(receivedFavouriteQueries(false, error, null));
        });      
          }, function (error) {
            dispatch(addFavouriteResponse(false, error));
        });
    };
  },
  deleteFavourite : function(event) {
    return function(dispatch, getState) {     
      dispatch(addFavouriteRequest());
      var fav = getState(event).favourites.favouriteToBeDeleted;
      return favouritesAPI.deleteFavourite(fav).then(function (response) {
        dispatch(deleteFavouriteResponse(response.success, response.errors));     
        dispatch(requestedFavouriteQueries());
        return favouritesAPI.fetchFavouriteQueries().then(function (response) {
          dispatch(receivedFavouriteQueries(response.success, response.errors, response.queries));
        }, function (error) {
          dispatch(receivedFavouriteQueries(false, error, null));
        });      
          }, function (error) {
            dispatch(deleteFavouriteResponse(false, error));
        });
    };
  },
  openFavourite : function(favourite) {
    return {
      type : types.FAVOURITES_OPEN_SELECTED,
      showSelected : true,
      selectedFavourite: favourite
    };
  },
  getFavouriteFeatures : function(favourite) {
    return function(dispatch, getState) {
      //dispatch(_openFavourite(favourite));
      var population = {
          utility: '80de55eb-9bde-4477-a97a-b6048a1fcc9a',
          label: 'DAIAD',
          type: 'UTILITY'
      };      
      
      //var population = favourite.query.population;
      var timezone = favourite.query.timezone;
      var interval = [moment(favourite.query.time.start), moment(favourite.query.time.end)];
      var source = favourite.query.source;
      var geometry = favourite.query.geometry;

      var query = _buildTimelineQuery(population, source, geometry, timezone, interval);

      //dispatch(_getTimelineInit(population, query));
      return queryAPI.queryMeasurements(query).then(function(response) {
        var data = {
          meters : null,
          devices : null,
          areas : null
        };
        if (response.success) {     
          data.areas = response.areas;
          data.meters = response.meters;
          data.devices = response.devices;            
        }
        dispatch(_getTimelineComplete(response.success, response.errors, data));

        dispatch(_getFeatures(0, null, null));

      }, function(error) {
        dispatch(_getTimelineComplete(false, error, null));

        dispatch(_getFeatures(0, null, null));
      });
    };
  },  
  closeFavourite : function() {
    return{
      type : types.FAVOURITES_CLOSE_SELECTED,
      showSelected : false,
      selectedFavourite : null
    };
  },
  
  setActiveFavourite : function(favourite) {

    return {
      type: types.FAVOURITES_SET_ACTIVE_FAVOURITE,
      selectedFavourite: favourite 
    };
  },
  openWarning : function(favourite) {
    return {
      type : types.FAVOURITES_DELETE_QUERY_REQUEST,
      favouriteToBeDeleted: favourite
    };
  },    
  closeWarning : function() {
    return {
      type : types.FAVOURITES_CANCEL_DELETE_QUERY,
      favouriteToBeDeleted: null
    };
  },
  resetMapState : function() {
    return {
      type : types.FAVOURITES_RESET_MAP_STATE
    };
  } 
};


module.exports = FavouritesActions;
