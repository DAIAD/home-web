var Redux = require('redux');

module.exports = Redux.combineReducers({
  measurements: require('./reports-measurements'),
  system: require('./reports-system'),
});

