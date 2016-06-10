var {combineReducers} = require('redux');
var {routerReducer} = require('react-router-redux');

var i18n = require('./i18n');
var session = require('./session');
var dashboard = require('./dashboard');
var demographics = require('./demographics');
var forecasting = require('./forecasting');
var group = require('./group');
var user = require('./user');
var upsertFavouriteForm = require('./upsertFavouriteForm');
var mode_management = require('./mode_management');
var admin = require('./admin');
var logging = require('./logging');
var alerts = require('./alerts');
var query = require('./query');
var scheduler = require('./scheduler');
var debug = require('./debug');
var reports = require('./reports');
var config = require('./config');
var overview = require('./overview');

var rootReducer = combineReducers({
  i18n,
  config,
  session,
  dashboard,
  forecasting,
  demographics,
  group,
  user,
  upsertFavouriteForm,
  mode_management,
  admin,
  alerts,
  query,
  scheduler,
  debug,
  logging,
  routing: routerReducer,
  reports,
  overview,
});

module.exports = rootReducer;
