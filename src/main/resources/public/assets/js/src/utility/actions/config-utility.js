
var _ = require('lodash');
var sprintf = require('sprintf');

var ActionTypes = require('../action-types');
var configureEntity = require('../service/configure');

var actions = {

  // Plain actions 

  requestConfiguration: () => ({
    type: ActionTypes.config.utility.REQUEST_CONFIGURATION,
  }), 

  setConfiguration: (config) => ({
    type: ActionTypes.config.utility.SET_CONFIGURATION,
    config,
  }),

  // Thunk actions

  configure: () => (dispatch, getState) => {
    var state = getState();

    if (!_.isEmpty(state.config.utility)) {
      console.info('Configuration for "utility" is already present; Skipping');
      return Promise.resolve();
    }

    dispatch(actions.requestConfiguration());
    
    return configureEntity('utility').then(
      res => (dispatch(actions.setConfiguration(res.utility))),
      reason => (
        console.error('Cannot configure "utility": %s', reason), null
      )
    );
  },
};

module.exports = actions;
