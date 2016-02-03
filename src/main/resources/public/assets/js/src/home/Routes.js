var React = require('react');

var Route = require('react-router').Route;
var IndexRoute = require('react-router').IndexRoute;
var Redirect = require('react-router').Redirect;

var HomeApp = require('./components/HomeApp');

var ContentRoot = require('./components/ContentRoot');
var MainSection = require('./components/MainSection.react');

var Dashboard = require('./components/sections/Dashboard');
var History = require('./components/sections/History');
var Commons = require('./components/sections/Commons');
var Notifications = require('./components/sections/Notifications');
var Insights = require('./components/sections/Notifications/Insights');
var Alerts = require('./components/sections/Notifications/Alerts');
var Tips = require('./components/sections/Notifications/Tips');
var Profile = require('./components/sections/Profile');
var Settings = require('./components/sections/Settings');
var Devices = require('./components/sections/Devices');

module.exports = 
	(
		<Route path="/" component={ContentRoot} >
				<IndexRoute component={Dashboard} />
				<Route path="/dashboard" component={Dashboard} />
				<Route path="/history" component={History} />
				<Route path="/commons" component={Commons} />
				<Route path="/notifications" component={Notifications}>
					<Route path="/notifications/:id" component={Notifications} />
					<Route path="insights" component={Insights} />
					<Route path="alerts" component={Alerts} />
					<Route path="tips" component={Tips} />
				</Route>
				<Route path="/settings" component={Settings} >
					<Route path="profile" component={Profile} />
					<Route path="devices" component={Devices} />
				</Route>
			</Route>
		);

