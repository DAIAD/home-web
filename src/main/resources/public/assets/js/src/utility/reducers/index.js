var { combineReducers } = require('redux');
var { routerReducer } = require('react-router-redux');

var i18n = require('./i18n');
var session = require('./session');
var demographics = require('./demographics');
var group = require('./group');
var upsertFavouriteForm = require('./upsertFavouriteForm');
var mode_management = require('./mode_management');
var admin = require('./admin');
var logging = require('./logging');
var alerts = require('./alerts');
var query = require('./query');
var scheduler = require('./scheduler');
var debug = require('./debug');


var rootReducer = combineReducers({
	i18n,
	session,
	demographics,
	group,
	upsertFavouriteForm,
	mode_management,
	admin,
	alerts,
	query,
	scheduler,
	debug,
	logging,
	routing: routerReducer
	
});

module.exports = rootReducer;
