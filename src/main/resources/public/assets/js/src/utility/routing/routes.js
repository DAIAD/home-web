var React = require('react');
var Route = require('react-router').Route;
var IndexRoute = require('react-router').IndexRoute;

var App = require('../containers/App');

var analytics = require('../components/section/analytics/index');
var trials = require('../components/section/trials/index');

var Dashboard = require('../components/section/Dashboard');
var Demographics = require('../components/section/Demographics');
var ModeManagement = require('../components/section/mode_management/ModeManagement');
var User = require('../components/User');
var Group = require('../components/Group');
var Device = require('../components/Device');
var Forecasting = require('../components/section/Forecasting');
var Users = require('../components/section/Users');
var Scheduler = require('../components/section/Scheduler');
var Logging = require('../components/section/support/Logging');
var Announcements = require('../components/section/Announcements');
var ManageAlerts = require('../components/section/ManageAlerts');
var UserSettings = require('../components/section/settings/UserSettings');
var SystemSettings = require('../components/section/settings/SystemSettings');
var DataManagement = require('../components/section/support/Data');
var Development = require('../components/section/support/Development');

module.exports = (
  <Route path="/" component={App} >
    <IndexRoute component={Dashboard} />
    <Route path="/utility" component={Dashboard} />
    <Route path="/dashboard" component={Dashboard} />
    <Route path="/analytics" component={analytics.Fav} />
    <Route path="/analytics/fav" component={analytics.Fav} />
    <Route path="/analytics/basic-reports" component={analytics.BasicReports} />
    <Route path="/analytics/panel" component={analytics.ReportPanel} />
    <Route path="/trials/overview" component={trials.Overview}/>
    <Route path="/trials/pilot-reports" component={trials.PilotReports}/>
    <Route path="/forecasting" component={Forecasting} />
    <Route path="/demographics" component={Demographics} />
    <Route path="/mode/management" component={ModeManagement}/>
    <Route path="/users" component={Users} />
    <Route path="/user/:id" component={User} />
    <Route path="/group/:id" component={Group} />
    <Route path="/device/:id" component={Device} />
    <Route path="/scheduler" component={Scheduler} />
    <Route path="/announcements" component={Announcements} />
    <Route path="/manage-alerts" component={ManageAlerts} />
    <Route path="/settings/user" component={UserSettings}/>
    <Route path="/settings/system" component={SystemSettings}/>
    <Route path="/support/logging" component={Logging} />
    <Route path="/support/data" component={DataManagement}/>
    <Route path="/support/development" component={Development}/>
  </Route>
);
