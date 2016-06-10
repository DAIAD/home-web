var types = require('../constants/ActionTypes');

var initialState = {
    isLoading : false,
    accounts: null,
    candidateUsersToAdd : [],
    candidateUsersToRemove : [],
    initialUsers: null,
    addedUsers : [],
    showForm : false,
    showModal : false,
    accountId : null,
    selected : false,
    rowIdToggled: null
};

var announcements = function (state, action) {
  switch (action.type) {
    case types.ANNC_REQUESTED_USERS:
      return Object.assign({}, state, {          
        accounts : action.accounts,
        isLoading:true
      });
    case types.ANNC_RECEIVED_USERS:
      return Object.assign({}, state, {
        accounts : action.accounts,
        isLoading: false
      }); 
    case types.ANNC_INITIAL_USERS_SET_SELECTED:
      return Object.assign({}, state, {
        accounts : action.accounts
      });  
    case types.ANNC_ADDED_USERS_SET_SELECTED:
      return Object.assign({}, state, {
        addedUsers : action.addedUsers
      });     
    case types.ANNC_SET_INITIAL_USERS:
      return Object.assign({}, state, {
        initialUsers : action.initialUsers
      });    
    case types.ANNC_ADD_USERS_BUTTON_CLICKED:
      return Object.assign({}, state, {          
        addedUsers : action.addedUsers
      });
    case types.ANNC_REMOVE_USERS_BUTTON_CLICKED:
      return Object.assign({}, state, {          
        addedUsers : action.addedUsers
      }); 
    case types.ANNC_SHOW_FORM:
      return Object.assign({}, state, {          
        showForm : true
      });    
    case types.ANNC_CANCEL_SHOW_FORM:
      return Object.assign({}, state, {          
        showForm : false
      });  
    case types.ANNC_BROADCAST_ANNOUNCEMENT_RESPONSE:
      return Object.assign({}, state, {
        isLoading: false,
        showForm:false,
        addedUsers:[]
      });         
    default:
      return state || initialState;
  }
};

module.exports = announcements;