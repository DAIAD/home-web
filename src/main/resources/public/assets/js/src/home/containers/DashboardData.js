var React = require('react');
var bs = require('react-bootstrap');
var injectIntl = require('react-intl').injectIntl;
var connect = require('react-redux').connect;
var FormattedMessage = require('react-intl').FormattedMessage;

var Dashboard = require('../components/sections/Dashboard');


function mapStateToProps(state) {
	return {
		firstname: state.user.profile.firstname
	};
}

DashboardData = connect(mapStateToProps)(Dashboard);
module.exports = DashboardData;
