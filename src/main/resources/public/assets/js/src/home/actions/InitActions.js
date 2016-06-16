/**
 * User Actions module.
 * User related action creators
 * 
 * @module UserActions
 */

var types = require('../constants/ActionTypes');
var LocaleActions = require('./LocaleActions');
var DashboardActions = require('./DashboardActions');
var HistoryActions = require('./HistoryActions');
var FormActions = require('./FormActions');

const { fetchAll:fetchAllMessages } = require('./MessageActions');

const { getMeterCount } = require('../utils/device');

/**
 * Call all necessary actions to initialize app with profile data 
 *
 * @param {Object} profile - profile object as returned from server
 */
const initHome = function (profile) {
  return function(dispatch, getState) {

    dispatch(fetchAllMessages());

    if (profile.configuration) {
        const configuration = JSON.parse(profile.configuration);
        if (configuration.infoboxes) dispatch(DashboardActions.setInfoboxes(configuration.infoboxes));
        if (configuration.layout) dispatch(DashboardActions.updateLayout(configuration.layout, false));

    }
    
    if (getMeterCount(getState().user.profile.devices) === 0) {
      dispatch(HistoryActions.setActiveDeviceType('AMPHIRO', true));
      
      dispatch(FormActions.setForm('infoboxToAdd',{
        deviceType: 'AMPHIRO',
        type: 'totalVolumeStat',
        title : 'Shower volume',
      }));
    }
    else {
      dispatch(HistoryActions.setActiveDeviceType('METER', true));
      }

    dispatch(LocaleActions.setLocale(profile.locale));
    const { firstname, lastname, email, username, locale, address, zip, country, timezone } = profile;
    const profileData = { firstname, lastname, email, username, locale, address, zip, country, timezone };
    dispatch(FormActions.setForm('profileForm', profileData));
    
    return dispatch(DashboardActions.fetchAllInfoboxesData())
            .then(() => ({success:true, profile}));

  };
};

/**
 * Action dispatched when application has been initialized 
 * (used for loading locale messages & reload if active session
 *
 */
const setReady = function() {
  return {
    type: types.HOME_IS_READY
  };
};

module.exports = {
  initHome,
  setReady
};
