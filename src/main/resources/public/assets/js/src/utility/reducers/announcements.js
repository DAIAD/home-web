var types = require('../constants/ActionTypes');

var initialState = {
    isLoading : false,
    candidateUsersToAdd : [],
    candidateUsersToRemove : [],
    addedUsers : [],
    currentUsers: [],
    showModal : false
};

var announcements = function (state, action) {
  switch (action.type) {
    case types.ANNC_REQUESTED_USERS:
      return Object.assign({}, state, {          
        currentUsers : action.currentUsers,
        isLoading:true
      });
    case types.ANNC_RECEIVED_USERS:
      return Object.assign({}, state, {
        currentUsers : action.currentUsers,
        isLoading:false
      });    
    case types.ANNC_ADD_USER_ACTION:
      return Object.assign({}, state, {          
        addedUsers : action.addedUsers,
        currentUsers : action.currentUsers,
        candidateUsersToAdd : [],
      });
    case types.ANNC_REMOVE_USER_ACTION:
      return Object.assign({}, state, {
        addedUsers : action.addedUsers,
        currentUsers : action.currentUsers,
        candidateUsersToRemove : []
      });
    default:
      return state || initialState;
  }
};

module.exports = announcements;