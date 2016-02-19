var React = require('react');
var bs = require('react-bootstrap');
var injectIntl = require('react-intl').injectIntl;
var connect = require('react-redux').connect;
var FormattedMessage = require('react-intl').FormattedMessage;

var Dashboard = require('../components/sections/Dashboard');

var DeviceActions = require('../actions/DeviceActions');

var timeUtil = require('../utils/time');
var getSessionById = require('../utils/device').getSessionById;
var getDefaultDevice = require('../utils/device').getDefaultDevice;

var getFilteredData = function(data, filter) {
	var filteredData = [];

	data.forEach(function(dato) {
		if (!dato[filter]){
			return;
		}
		filteredData.push([new Date(dato.timestamp), dato[filter]]);
	});
	
	//return array with dates instead of timestamps
	return filteredData.map(x => [new Date(x[0]),x[1]]);
};

var DashboardData = React.createClass({
	
	componentWillMount: function() {
		this.props.initDashboard(this.props.activeDevice, this.props.time);
	},
	render: function() {
		return (
				<Dashboard {...this.props} />
		);
	}
});

function mapStateToProps(state) {
	var lastSession = getSessionById(state.device.sessions.data, state.device.sessions.lastSession);
	return {
		time: state.device.query.time,
		firstname: state.user.profile.firstname,
		chartData: lastSession?getFilteredData(lastSession.measurements?lastSession.measurements:[], 'volume'):[],
		lastShower: lastSession,
		activeDevice: state.device.query.activeDevice,
		loading: (state.device.query.status.isLoading || state.device.sessions.status.isLoading),
	};
}

function mapDispatchToProps(dispatch) {
	return {

		initDashboard: function(deviceKey, time) {
			
			dispatch(DeviceActions.fetchSessionsIfNeeded(deviceKey, Object.assign({}, time, {granularity:0}))).then(
				function() { 
					dispatch(DeviceActions.fetchLastSession(deviceKey, Object.assign({}, time, {granularity:0})));
				},
				function(error) {
					console.log('failed'); 
					console.log(error);
				});
		}
	};
}

DashboardData = connect(mapStateToProps, mapDispatchToProps)(DashboardData);
module.exports = DashboardData;
