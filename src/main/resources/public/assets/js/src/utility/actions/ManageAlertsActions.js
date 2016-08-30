var types = require('../constants/ActionTypes');
var alertsAPI = require('../api/alerts');

var receivedTips = function (success, errors, tips) {
  return {
    type: types.ADMIN_RECEIVED_STATIC_TIPS,
    success: success,
    errors: errors,
    tips: tips
  };
};

var requestedTips = function (locale) {
  return {
    type: types.ADMIN_REQUESTED_STATIC_TIPS,
    locale: locale
  };
};

var requestedUtilities = function () {
  return {
    type: types.ADMIN_REQUESTED_UTILITIES,
  };
};

var receivedUtilities = function (success, errors, utilities) {
  return {
    type: types.ADMIN_RECEIVED_UTILITIES,
    success: success,
    errors: errors,
    utilities: utilities
  };
};

var clickedActiveStatusSaveButton = function () {
  return {
    type: types.ADMIN_CLICKED_SAVE_BUTTON,
    isLoading: true
  };
};

var saveActiveStatusButtonResponse = function (success, errors) {
  return {
    type: types.ADMIN_SAVE_BUTTON_RESPONSE,
    isLoading: false,
    success: success,
    errors: errors
  };
};

var requestAddTip = function () {
  return {
    type: types.ADMIN_REQUESTED_ADD_TIP,
    saveTipDisabled: false,
    isLoading: true
  };
};

var addTipResponse = function (success, errors) {
  return {
    type: types.ADMIN_ADD_TIP_RESPONSE,
    saveTipDisabled: false,
    isLoading: false,
    show: false,
    success: success,
    errors: errors
  };
};

var requestDeleteTip = function (currentTip) {
  return {
    type: types.ADMIN_DELETE_TIP_REQUEST,
    currentTip: currentTip,
    showModal: false
    //isLoading: true
  };
};

var deleteTipResponse = function (success, errors) {
  return {
    type: types.ADMIN_DELETE_TIP_RESPONSE,
    isLoading: false,
    success: success,
    errors: errors,
    showModal: false
  };
};

var ManageAlertsActions = {
  setUtility: function (event, utility) {
    return{
      type: types.ADMIN_SELECTED_UTILITY_FILTER,
      utility: utility
    };
  },
  fetchUtilities: function (event) {
    return function (dispatch, getState) {
      dispatch(requestedUtilities());
      return alertsAPI.getAllUtilities().then(function (response) {
        dispatch(receivedUtilities(response.success, response.errors, response.utilitiesInfo));
      }, function (error) {
        receivedUtilities(false, error, null);
      });
    };
  },
  getStaticTips: function (event, utility, activePage) {
    var locale;
    if (utility.label == "DAIAD") {
      locale = "en";
    } else if (utility.label == "Alicante") {
      locale = "es";
    } else {
      locale = "en";
    }
    return function (dispatch, getState) {
      dispatch(requestedTips(locale));
      return alertsAPI.getTips(locale).then(function (response) {
        dispatch(receivedTips(response.success, response.errors, response.messages));
      }, function (error) {
        dispatch(receivedTips(false, error, null));
      });
    };
  },
  addTip: function (event, tip, utility) {
    return function (dispatch, getState) {
      dispatch(requestAddTip);
      return alertsAPI.insertTip(tip).then(function (response) {
        dispatch(addTipResponse(response.success, response.errors));          
        var locale;
        if (utility.label == "DAIAD") {
          locale = "en";
        } else if (utility.label == "Alicante") {
          locale = "es";
        } else {
          locale = "en";
        } 
        dispatch(requestedTips(locale));
        return alertsAPI.getTips(locale).then(function (response) {
          dispatch(receivedTips(response.success, response.errors, response.messages));
        }, function (error) {
          dispatch(receivedTips(false, error, null));
        });             
      }, function (error) {
        dispatch(addTipResponse(false, error, null));
      });
    };
  },
  deleteTip: function (event) {
    return function (dispatch, getState) {
      dispatch(requestDeleteTip);
      return alertsAPI.deleteTip(getState(event).alerts.currentTip).then(function (response) {
        dispatch(deleteTipResponse(response.success, response.errors));   
        var utility = getState(event).alerts.utility;
        var locale;        
        if (utility.label == "DAIAD") {
          locale = "en";
        } else if (utility.label == "Alicante") {
          locale = "es";
        } else {
          locale = "en";
        } 
        dispatch(requestedTips(locale));
        return alertsAPI.getTips(locale).then(function (response) {
          dispatch(receivedTips(response.success, response.errors, response.messages));
        }, function (error) {
          dispatch(receivedTips(false, error, null));
        });                
      }, function (error) {
        dispatch(deleteTipResponse(false, error, null));
      });
    };
  },  
   cancelDelete: function () {
    return{
      type: types.ADMIN_CANCEL_DELETE_TIP,
      currentTip: null,
      showModal: false
    };
  },
  showAddTipForm: function () {
    return{
      type: types.ADMIN_ADD_TIP_SHOW,
      currentTip: null,
      saveOff: false
    };
  }, 
  cancelAddTip: function () {
    return{
      type: types.ADMIN_CANCEL_ADD_TIP_SHOW,
      saveOff: true
    };
  },
  beganEditingTip: function () {
    return{
      type: types.ADMIN_EDITED_TIP,
      saveTipDisabled: false
    };
  },
  editTip: function (currentTip) {
    return{
      type: types.ADMIN_EDIT_TIP,
      currentTip: currentTip,
      show: true,
      saveOff: false
    };
  },
  setActivePage: function (activePage) {
    return {
      type: types.STATIC_TIPS_ACTIVE_PAGE,
      activePage: activePage
    };
  },
  setActivationChanged: function (data) {
    return {
      type: types.ADMIN_TIPS_ACTIVE_STATUS_CHANGE,
      data: data
    };
  },
  saveActiveStatusChanges: function (dispatch, changedRows, utility, activePage) {
    return function (dispatch, getState) {
      dispatch(clickedActiveStatusSaveButton());
      return alertsAPI.saveActiveTips(changedRows).then(function (response) {
        dispatch(saveActiveStatusButtonResponse(response.success, response.errors));
        var locale;
        if (utility.label == "DAIAD") {
          locale = "en";
        } else if (utility.label == "Alicante") {
          locale = "es";
        } else {
          locale = "en";
        } 
        dispatch(requestedTips(locale));
        return alertsAPI.getTips(locale).then(function (response) {
          dispatch(receivedTips(response.success, response.errors, response.messages));
        }, function (error) {
          dispatch(receivedTips(false, error, null));
        });  
      }, function (error) {
        dispatch(saveActiveStatusButtonResponse(false, error, null));
      });   
    };   
  },
  showModal : function(currentTip){
    return {
      type : types.MESSAGES_DELETE_MODAL_SHOW,
      currentTip : currentTip,
      showModal: true
    };
  }, 
  hideModal : function(){
    return {
      type : types.MESSAGES_DELETE_MODAL_HIDE,
      showModal: false
    };
  }
};

module.exports = ManageAlertsActions;
