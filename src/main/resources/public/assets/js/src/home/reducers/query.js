var types = require('../constants/ActionTypes');

var { thisWeek } = require('../utils/time');
var { updateOrAppendToSession } = require('../utils/device');

var query = function (state, action) {
  //initial state
  if (state === undefined) {
    state = {
      status: {
        isLoading: false,
        success: null,
        errors: null
      },
      activeDevice: null,
      time: {
        granularity: 0
      },
      data: [],
    };
    state.time = Object.assign({}, state.time, thisWeek());
  }
   
  switch (action.type) {
    case types.DEVICE_REQUESTED_SESSION_SEARCH:
      return Object.assign({}, state, {
        status: {
          isLoading: true,
        },
      });

    case types.DEVICE_RECEIVED_SESSION_SEARCH:
      switch (action.success) {
        case true:
          return Object.assign({}, state, {
            data: action.data,
            status: {
              isLoading: false,
              success: true,
              errors: null
            }
          });

        case false:
          return Object.assign({}, state, {
            data: [],
            status: {
              isLoading: false,
              success: false,
              errors: action.errors
            }
          });
        }
        break;
            
      case types.QUERY_SET_TIME:
        return Object.assign({}, state, {
          time: Object.assign({}, state.time, action.time)
        });

      case types.QUERY_SET_ACTIVE:
        return Object.assign({}, state, {
          activeDevice: action.deviceKey
        });
      
      case types.QUERY_RESET_ACTIVE:
        return Object.assign({}, state, {
          activeDevice: null
        });
      
      case types.QUERY_RESET:
        return Object.assign({}, state, {
          activeDevice: null,
          data: [],
          status: {
            isLoading: false,
            success: null,
            errors: null
          }
        });
      // Sessions
      case types.DEVICE_REQUESTED_SESSION:
        return Object.assign({}, state, {
          status: {
            isLoading: true
          },
      });

      case types.DEVICE_RECEIVED_SESSION:
        switch (action.success) {
          
          case true:
            var updated = updateOrAppendToSession(state.data, action.data, action.id);
            return Object.assign({}, state, {
              status: {
                isLoading: false,
                success: true,
                errors: null
              },
              data: updated
            });
          
          case false:
            return Object.assign({}, state, {
              status: {
                isLoading: false,
                success: false,
                errors: action.errors
              },
            });
        }
        break;
          
      case types.METER_REQUESTED_QUERY:
        return Object.assign({}, state, {
          status: {
            isLoading: true,
          },
        });

      case types.METER_RECEIVED_QUERY:
        switch (action.success) {
          case true:
            return Object.assign({}, state, {
              data: action.data,
              status: {
                isLoading: false,
                success: true,
                errors: null
              }
            });

          case false:
            return Object.assign({}, state, {
              data: [],
              status: {
                isLoading: false,
                success: false,
                errors: action.errors
              }
            });
          }
          break;
  
        case types.METER_REQUESTED_STATUS:
          return Object.assign({}, state, {
            status: {
              isLoading: true,
            },
          });
        
        case types.METER_RECEIVED_STATUS:
          switch (action.success) {
            case true:
              return Object.assign({}, state, {
                data: action.data,
                status: {
                  isLoading: false,
                  success: true,
                  errors: null
                }
              });

            case false:
              return Object.assign({}, state, {
                data: [],
                status: {
                  isLoading: false,
                  success: false,
                  errors: action.errors
                }
              });
            }
            break;

    default:
      return state;
  }
};

module.exports = query;

