var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var History = require('../components/sections/History');

var DeviceActions = require('../actions/DeviceActions');

var { getAvailableDevices } = require('../utils/device');
var timeUtil = require('../utils/time');


var HistoryData = React.createClass({
	componentWillMount: function() {
		if (this.props.activeDevice) {
			this.props.initHistory(this.props.activeDevice, this.props.time);
		}
	},
	componentWillReceiveProps: function(nextProps) {
		if (!this.props.activeDevice && nextProps.activeDevice) {
			this.props.initHistory(nextProps.activeDevice, this.props.time);
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
    loading: state.device.query.status.isLoading,
    nextPeriod: timeUtil.getNextPeriod(state.device.query.timeFilter, state.device.query.time.endDate),
    previousPeriod: timeUtil.getPreviousPeriod(state.device.query.timeFilter, state.device.query.time.endDate),

		};
}

function mapDispatchToProps(dispatch, ownProps) {
	return {
		setQueryFilter: function(filter) {
			return dispatch(DeviceActions.setQueryFilter(filter));
		},
		setTime: function(time) {
			return dispatch(DeviceActions.setTime(time));
		},
		setTimeFilter: function(filter) {
			 return dispatch(DeviceActions.setTimeFilter(filter));
    },
    initHistory: function(deviceKey, time) {
      this.querySessions(deviceKey, time).then(
        (response) => this.fetchAllSessions(deviceKey, time), 
        (error) => console.log(error));
    },
		querySessions: function(deviceKey, time) {
			return dispatch(DeviceActions.querySessions(deviceKey, time));
    },
    fetchAllSessions: function(deviceKey, time) {
      return dispatch(DeviceActions.fetchAllSessions(deviceKey, time));
    },
		querySessionsIfEmpty: function(deviceKey, time) {
			return dispatch(DeviceActions.querySessionsIfEmpty(deviceKey, time));
		},
		setActive: function(deviceKey) {
			return dispatch(DeviceActions.setActiveDevice(deviceKey));
		},
	};
}


HistoryData = connect(mapStateToProps, mapDispatchToProps)(HistoryData);
HistoryData = injectIntl(HistoryData);
module.exports = HistoryData;
