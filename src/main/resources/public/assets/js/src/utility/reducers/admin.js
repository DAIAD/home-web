var types = require('../constants/ActionTypes');

var initialState = {
  isLoading : false,
  activity : null,
  user : {
    name : null,
    devices : null
  },
  filter : null,
  export : {
    token : null
  },
  addUser : {
    show : false,
    showErrorAlert: false,
    selectedFirstName: null,
    selectedLastName: null,
    selectedEmail: null,
    selectedGender: null,
    selectedAddress: null,
    selectedUtility : null,
    selectedPostalCode : null,
    utilities : null,
    response : {
      success : null,
      errors: null
    }    
  }
};

var admin = function(state, action) {
  var newAddUser;
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

    case types.ADMIN_EXPORT_REQUEST:
      return Object.assign({}, state, {
        export : {
          token : null
        }
      });

    case types.ADMIN_EXPORT_COMPLETE:
      return Object.assign({}, state, {
        export : {
          token : action.token
        }
      });
      
    case types.ADMIN_ADD_USER_SHOW:
      newAddUser = Object.assign({}, state.addUser, {show : true});
      return Object.assign({}, state, {
        addUser : newAddUser
      });
      
    case types.ADMIN_ADD_USER_HIDE:
      newAddUser = Object.assign({}, state.addUser, {
        show : false, 
        selectedUtility : null, 
        showErrorAlert : false,
        response : {
          errors : [],
          success : false
        }
      });
      return Object.assign({}, state, {
        addUser: newAddUser
      });
     
    case types.ADMIN_ADD_USER_SELECT_UTILITY:
      newAddUser = Object.assign({}, state.addUser, {selectedUtility : action.utility});
      return Object.assign({}, state, {
        addUser : newAddUser
      });
      
    case types.ADMIN_ADD_USER_FILL_FORM:
      newAddUser = Object.assign({}, state.addUser, {
        selectedFirstName : action.firstName,
        selectedLastName : action.lastName,
        selectedEmail : action.email,
        selectedGender : action.gender,
        selectedAddress : action.address,
        selectedPostalCode : action.postalCode
      });
      return Object.assign({}, state, {
        addUser : newAddUser
      });
      
    case types.ADMIN_ADD_USER_MAKE_REQUEST:
      return Object.assign({}, state, {
        isLoading : true
      });
      
    case types.ADMIN_ADD_USER_RECEIVE_RESPONSE:
      newAddUser = Object.assign({}, state.addUser, {response : {success : action.success, errors : action.errors}});
      return Object.assign({}, state, {
        isLoading : false,
        addUser : newAddUser
      });
      
    case types.ADMIN_ADD_USER_VALIDATION_ERRORS_OCCURRED:
      newAddUser = Object.assign({}, state.addUser, {showErrorAlert : true, response : {success : false, errors : action.errors}});
      return Object.assign({}, state, {
        isLoading : false,
        addUser : newAddUser
      });
      
    case types.ADMIN_ADD_USER_GET_UTILITIES_MAKE_REQUEST:
      return Object.assign({}, state, {
        isLoading : true
      });
      
    case types.ADMIN_ADD_USER_GET_UTILITIES_RECEIVE_RESPONSE:
      newAddUser = Object.assign({}, state.addUser, {utilities : action.utilities});
      return Object.assign({}, state, {
        isLoading : false,
        addUser : newAddUser
      });
      
    case types.ADMIN_ADD_USER_SHOW_ERROR_ALERT:
      newAddUser = Object.assign({}, state.addUser, {showErrorAlert : true, response : {success : false, errors : action.errors}});
      return Object.assign({}, state, {
        addUser : newAddUser
      });
      
      
    case types.ADMIN_ADD_USER_HIDE_ERROR_ALERT:
      newAddUser = Object.assign({}, state.addUser, {showErrorAlert : false});
      return Object.assign({}, state, {
        addUser : newAddUser
      });
      
    case types.USER_RECEIVED_LOGOUT:
      return Object.assign({}, state, {
        isLoading : false,
        activity : null,
        user : {
          name : null,
          devices : null
        },
        filter : null,
        export : {
          token : null
        }
      });

    default:
      return state || initialState;
  }
};

module.exports = admin;
