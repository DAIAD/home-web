var moment = require('moment');

var ActionTypes = require('../action-types');

var actions = {
  
  setup: (source, field, now) => ({
    type: ActionTypes.overview.SETUP,
    source,
    field,
    now: now,
    requested: moment().valueOf(),
  }),

};

module.exports = actions;
