var types = require('../constants/ActionTypes');

var initialState = {
  isLoading: false,
  utility: null,
  utilities: null,
  tips: null,
  activePage: 0,
  currentTip: null,
  show: false,
  saveTipDisabled: false,
  saveDisabled: true,
  data: null,
  changedRows: [],
  showModal: false,
  actions: null
};

var alerts = function (state, action) {
  switch (action.type) {
    case types.ADMIN_REQUESTED_UTILITIES:
      return Object.assign({}, state, {
        isLoading: true,
        utilities: null
      });
    case types.ADMIN_RECEIVED_UTILITIES:
      return Object.assign({}, state, {
        isLoading: false,
        utilities: action.utilities
      });
    case types.ADMIN_SELECTED_UTILITY_FILTER:
      return Object.assign({}, state, {
        isLoading: true,
        utility: action.utility,
        saveOff: true
      });
    case types.ADMIN_REQUESTED_STATIC_TIPS:
      return Object.assign({}, state, {
        isLoading: true,
        tips: null
      });
    case types.ADMIN_RECEIVED_STATIC_TIPS:
      return Object.assign({}, state, {
        isLoading: false,
        tips: action.tips
      });
    case types.ADMIN_CLICKED_SAVE_BUTTON:
      return Object.assign({}, state, {
        isLoading: true,
        changedRows: action.changedRows
      });
    case types.ADMIN_SAVE_BUTTON_RESPONSE:
      return Object.assign({}, state, {
        isLoading: false,
        changedRows: []       
      });
    case types.ADMIN_REQUESTED_ADD_TIP:
      return Object.assign({}, state, {
        isLoading: true
      });
    case types.ADMIN_ADD_TIP_RESPONSE:
      return Object.assign({}, state, {
        isLoading: false,
        show: false
      });
    case types.ADMIN_ADD_TIP_SHOW:
      return Object.assign({}, state, {
        show: true
      });
    case types.ADMIN_CANCEL_ADD_TIP_SHOW:
      return Object.assign({}, state, {
        show: false,
        currentTip: null
      });
    case types.ADMIN_EDIT_TIP:
      return Object.assign({}, state, {
        show: true,
        currentTip: action.currentTip,
        saveOff: true
      });
    case types.STATIC_TIPS_ACTIVE_PAGE:
      return Object.assign({}, state, {
        activePage: action.activePage
      });
    case types.ADMIN_EDITED_TIP:
      return Object.assign({}, state, {
        saveTipDisabled: false
      });
    case types.MESSAGES_DELETE_MODAL_SHOW:
      return Object.assign({}, state, {
        showModal:true,
        currentTip: action.currentTip
      });
    case types.MESSAGES_DELETE_MODAL_HIDE:
      return Object.assign({}, state, {
        showModal:false,
        currentTip:null
      });      
    case types.ADMIN_DELETE_TIP_REQUEST:
      return Object.assign({}, state, {
        isLoading: true,
        showModal:true,
        currentTip: action.currentTip
      });
    case types.ADMIN_DELETE_TIP_RESPONSE:
      return Object.assign({}, state, {
        isLoading: false,
        showModal:false,
        currentTip:null
      });      
    case types.ADMIN_CANCEL_DELETE_TIP:
      return Object.assign({}, state, {
        isLoading: false,
        showModal:false,
        currentTip:null
      });     
    case types.ADMIN_TIPS_ACTIVE_STATUS_CHANGE:
      return Object.assign({}, state, {
        data: action.data
      });
    case types.ADMIN_SET_SAVE_DISABLED:
      return Object.assign({}, state, {
        saveDisabled: action.disabled
      });      
    default:
      return state || initialState;
  }
};

module.exports = alerts;