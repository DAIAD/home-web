var {combineReducers} = require('redux');
var {routerReducer} = require('react-router-redux');

var i18n = require('./i18n');
var session = require('./session');
var dashboard = require('./dashboard');
var demographics = require('./demographics');
var forecasting = require('./forecasting');
var group = require('./group');
var user = require('./user');
var userCatalog=  require('./user-catalog');
var upsertFavouriteForm = require('./upsertFavouriteForm');
var mode_management = require('./mode_management');
var admin = require('./admin');
var logging = require('./logging');
var alerts = require('./alerts');
var announcements = require('./announcements');
var query = require('./query');
var scheduler = require('./scheduler');
var debug = require('./debug');
var reports = require('./reports');
var config = require('./config');
var charting = require('./charting');
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
  userCatalog,
  upsertFavouriteForm,
  mode_management,
  admin,
  alerts,
  announcements,
  query,
  scheduler,
  debug,
  logging,
  routing: routerReducer,
  reports,
  charting,
  overview,
});

module.exports = rootReducer;
