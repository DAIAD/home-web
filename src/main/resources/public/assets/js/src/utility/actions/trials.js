var _ = require('lodash');

var ActionTypes = require('../action-types');

var actions = {
  
  setReferenceTime: (t) => ({
    type: ActionTypes.trials.SET_REFERENCE_TIME,
    referenceTime: t,
  }),
};

module.exports = actions;
