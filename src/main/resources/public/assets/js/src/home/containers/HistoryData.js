var React = require('react');
var connect = require('react-redux').connect;

var History = require('../components/sections/History');

var DeviceActions = require('../actions/DeviceActions');

var getAvailableDevices = require('../utils/device').getAvailableDevices;


var HistoryData = React.createClass({
	componentWillMount: function() {
		if (this.props.activeDevice) {
			this.props.querySessions(this.props.activeDevice, this.props.time);
		}
		//this.props.fetchSessionsIfNeeded(this.props.activeDevice, this.props.time);
	},
	componentWillReceiveProps: function(nextProps) {
		if (!this.props.activeDevice && nextProps.activeDevice) {
			this.props.querySessions(nextProps.activeDevice, this.props.time);
		}
	},
	render: function() {
		return (
			<History {...this.props} />
		);
	}
});

function mapStateToProps(state, ownProps) {
	return {
		time: state.device.query.time,
		metricFilter: state.device.query.filter,
		timeFilter: state.device.query.timeFilter,
		devices: getAvailableDevices(state.user.profile.devices),
		activeDevice: state.device.query.activeDevice,
		loading: state.device.query.status.isLoading 
		};
}

function mapDispatchToProps(dispatch, ownProps) {
	return {
		setQueryFilter: function(filter) {
			dispatch(DeviceActions.setQueryFilter(filter));
		},
		setTime: function(time) {
			dispatch(DeviceActions.setTime(time));
		},
		setTimeFilter: function(filter) {
			dispatch(DeviceActions.setTimeFilter(filter));
		},
		querySessions: function(deviceKey, time) {
			dispatch(DeviceActions.querySessions(deviceKey, time));
		},
		querySessionsIfEmpty: function(deviceKey, time) {
			dispatch(DeviceActions.querySessionsIfEmpty(deviceKey, time));
		},
		setActive: function(deviceKey) {
			dispatch(DeviceActions.setActiveDevice(deviceKey));
		},
	};
}


HistoryData = connect(mapStateToProps, mapDispatchToProps)(HistoryData);
module.exports = HistoryData;
