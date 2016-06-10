
var ActionTypes = require('../action-types');

var actions = {
  
  setReferenceTime: (t) => ({
    type: ActionTypes.overview.SET_REFERENCE_TIME,
    timestamp: t,
  }),

};

module.exports = actions;
