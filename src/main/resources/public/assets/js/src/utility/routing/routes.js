var React = require('react');
var Route = require('react-router').Route;
var IndexRoute = require('react-router').IndexRoute;

var App = require('../containers/App');

var Dashboard = require('../components/section/Dashboard');
var Demographics = require('../components/section/Demographics');
var UserSettings = require('../components/section/settings/UserSettings');
var SystemSettings = require('../components/section/settings/SystemSettings');

module.exports = (
	<Route path="/" component={App} >
		<IndexRoute component={Dashboard} />
		<Route path="/dashboard" component={Dashboard} />
		<Route path="/demographics" component={Demographics} />
		<Route path="/settings/user" component={UserSettings}/>
		<Route path="/settings/system" component={SystemSettings}/>
	</Route>
);