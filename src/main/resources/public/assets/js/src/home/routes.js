var React = require('react');
var Route = require('react-router').Route;
var IndexRoute = require('react-router').IndexRoute;
var Redirect = require('react-router').Redirect;

//Containers
var HomeApp = require('./containers/HomeApp');

//Components
var Dashboard = require('./components/sections/Dashboard');
var DashboardData = require('./containers/DashboardData');
var History = require('./components/sections/History');
var HistoryData = require('./containers/HistoryData');
var Shower = require('./components/Shower');
var ShowerData = require('./containers/ShowerData');
var Commons = require('./components/sections/Commons');
var Notifications = require('./components/sections/Notifications');
var Profile = require('./components/sections/Profile');
var Settings = require('./components/sections/Settings');
var Devices = require('./components/sections/Devices');

module.exports = 
	(
		<Route path="/" component={HomeApp} >
				<IndexRoute component={DashboardData} />
				<Route path="/dashboard" component={DashboardData} />
				<Route path="/history" component={HistoryData} />
				<Route path="/history/:id" component={ShowerData} />
				<Route path="/commons" component={Commons} />
				<Route path="/notifications" component={Notifications} />
				<Route path="/notifications/:id" component={Notifications} />
				<Route path="/settings" component={Settings} >
					<IndexRoute component={Devices} />
					<Route path="profile" component={Profile} />
					<Route path="devices" component={Devices} />
				</Route>
			</Route>
		);
