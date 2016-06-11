var React = require('react');
var Route = require('react-router').Route;
var IndexRoute = require('react-router').IndexRoute;
var Redirect = require('react-router').Redirect;

var HomeApp = require('../containers/HomeApp');
var Dashboard = require('../components/sections/Dashboard');
var DashboardData = require('../containers/DashboardData');
var History = require('../components/sections/History');
var HistoryData = require('../containers/HistoryData');
var Commons = require('../components/sections/Commons');
var Messages = require('../containers/MessageData');
var Profile = require('../components/sections/Profile');
var Settings = require('../components/sections/Settings');
var Devices = require('../components/sections/Devices');

module.exports = 
  (
    <Route path="/" component={HomeApp} >
        <IndexRoute component={DashboardData} />
        <Route path="/dashboard" component={DashboardData} />
        <Route path="/history" component={HistoryData}>
          <IndexRoute component={HistoryData} />
          <Route path="/history/explore" component={HistoryData} />
          <Route path="/history/forecast" component={HistoryData} />
        </Route>
        <Route path="/commons" component={Commons} />
        <Route path="/notifications" component={Messages} />
        <Route path="/settings" component={Profile} >
          <IndexRoute component={Profile} />
          <Route path="/settings/profile" component={Profile} />
        </Route>
        <Route path="settings/devices" component={Devices} />
      </Route>
    );
