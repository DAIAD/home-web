var types = require('../constants/ActionTypes');

var initialState = {
    isLoading : false,
    accounts: null,
    candidateUsersToAdd : [],
    candidateUsersToRemove : [],
    initialUsers: null,
    addedUsers : [],
    showModal : false,
    accountId : null,
    selected : false,
    rowIdToggled: null
};

var announcements = function (state, action) {
  var updatedAccounts = null;
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
    case types.ANNC_USER_SET_SELECTED:
      updatedAccounts = [];
      
      for(var obj in state.accounts){
        var updatedObj={};
        for(var prop in state.accounts[obj]){
          
          if(prop == "id"){
            if(state.accounts[obj][prop] == action.rowIdToggled){
              updatedObj.id = action.rowIdToggled;
              updatedObj.selected = true; //!state.accounts[obj].selected;
            }
            else{
              updatedObj = state.accounts[obj];
            }
          }
        }
        updatedAccounts.push(updatedObj);
      }

      return Object.assign({}, state, {
        accounts : updatedAccounts
      });        
    case types.ANNC_SET_INITIAL_USERS:
      return Object.assign({}, state, {
        initialUsers : action.initialUsers
      });    
    case types.ANNC_ADD_USER_ACTION:
      return Object.assign({}, state, {          
        addedUsers : action.addedUsers,
        initialUsers : action.initialUsers,
        candidateUsersToAdd : [],
      });
    case types.ANNC_REMOVE_USER_ACTION:
      return Object.assign({}, state, {
        addedUsers : action.addedUsers,
        initialUsers : action.initialUsers,
        candidateUsersToRemove : []
      });
    default:
      return state || initialState;
  }
};

module.exports = announcements;