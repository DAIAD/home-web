var React = require('react');
var Route = require('react-router').Route;
var IndexRoute = require('react-router').IndexRoute;

var App = require('../containers/App');

var Dashboard = require('../components/section/Dashboard');
var Analytics = require('../components/section/Analytics');
var Demographics = require('../components/section/Demographics');
var ModeManagement = require('../components/section/mode_management/ModeManagement');
var User = require('../components/User');
var Group = require('../components/Group');
var Device = require('../components/Device');
var Forecasting = require('../components/section/Forecasting');
var Search = require('../components/section/Search');
var Scheduler = require('../components/section/Scheduler');
var Alerts = require('../components/section/Alerts');
var Announcements = require('../components/section/Announcements');
var UserSettings = require('../components/section/settings/UserSettings');
var SystemSettings = require('../components/section/settings/SystemSettings');
var Reporting = require('../components/section/Reporting');

module.exports = (
	<Route path="/" component={App} >
		<IndexRoute component={Dashboard} />
		<Route path="/dashboard" component={Dashboard} />
		<Route path="/analytics" component={Analytics} />
		<Route path="/forecasting" component={Forecasting} />
		<Route path="/demographics" component={Demographics} />
		<Route path="/mode/management" component={ModeManagement}/>
		<Route path="/user/:id" component={User} />
		<Route path="/group/:id" component={Group} />
		<Route path="/device/:id" component={Device} />
		<Route path="/search" component={Search} />
		<Route path="/scheduler" component={Scheduler} />
		<Route path="/alerts" component={Alerts} />
		<Route path="/announcements" component={Announcements} />
		<Route path="/settings/user" component={UserSettings}/>
		<Route path="/settings/system" component={SystemSettings}/>
		<Route path="/report" component={Reporting}/>
	</Route>
);