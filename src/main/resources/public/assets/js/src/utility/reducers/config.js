var Redux = require('redux');

module.exports = Redux.combineReducers({
  utility: require('./config-utility'),
  reports: require('./config-reports'),
  overview: require('./config-overview'),
});

