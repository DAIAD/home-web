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
		if (this.props.activeDevice) {
			this.props.initDashboard(this.props.activeDevice);
		}
	},
	componentWillReceiveProps: function(nextProps) {
		if (!this.props.activeDevice && nextProps.activeDevice) {
			this.props.initDashboard(nextProps.activeDevice);
		}
	},
	render: function() {
		return (
				<Dashboard {...this.props} />
		);
	}
});

function mapStateToProps(state, ownProps) {
	var lastSession = getSessionById(state.device.query.data, state.device.query.lastSession);
	return {
		time: state.device.query.time,
		activeDevice: state.device.query.activeDevice,
		firstname: state.user.profile.firstname,
		chartData: lastSession?(getFilteredData(lastSession.measurements?lastSession.measurements:[], 'volume')):[],
		chartFormatter: (x) => ownProps.intl.formatTime(x, { hour: 'numeric', minute: 'numeric'}),
		lastShower: lastSession,
		loading: state.device.query.status.isLoading,
	};
}

function mapDispatchToProps(dispatch) {
	return {

		initDashboard: function(deviceKey) {
			const time = Object.assign({}, timeUtil.thisMonth(), {granularity: 0});

			dispatch(DeviceActions.querySessions(deviceKey, time)).then(
				function(response) { 
					dispatch(DeviceActions.fetchLastSession(deviceKey, time));
				},
				function(error) {
					console.log(error);
				});
		}
	};
}

DashboardData = connect(mapStateToProps, mapDispatchToProps)(DashboardData);
DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
