var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var SessionsList = require('../components/SessionsList');

var DeviceActions = require('../actions/DeviceActions');

var getSessionById = require('../utils/device').getSessionById;
var getSessionByIndex = require('../utils/device').getSessionByIndex;

var getFilteredData = function(data, filter) {
	if (!data) return [];
	var filteredData = [];
	
	data.forEach(function(dato) {
		if (!dato[filter]){
			return;
		}
		filteredData.push([dato.timestamp, dato[filter]]);
	});
	return [filteredData.map(x => [new Date(x[0]),x[1]])];
};

var HistoryList = React.createClass({
	componentWillMount: function() {
		//this.props.fetchSessionsIfNeeded(this.props.activeDevice, this.props.time);
	},
	render: function() {
		return (
			<SessionsList {...this.props} />
		);
	}
});

function mapStateToProps(state, ownProps) {
	var activeSessionData = getSessionByIndex(state.device.query.data, state.device.query.activeSessionIndex);
	var activeSessionDataMeasurements = [];
 	if (activeSessionData) {
		activeSessionDataMeasurements = activeSessionData.measurements?activeSessionData.measurements:[];
	}
	var disabledNextSession = true;
	var disabledPreviousSession = true;
	if (state.device.query.activeSessionIndex!==null) {
		if (state.device.query.data[state.device.query.activeSessionIndex+1]) {
			disabledNextSession = false;
		}
		if (state.device.query.data[state.device.query.activeSessionIndex-1]) {
			disabledPreviousSession = false;
		}
	}
	return {
		time: state.device.query.time,
		activeDevice: state.device.query.activeDevice,
		sessionFilter: state.device.query.sessionFilter,
		sessions: state.device.query.data,
		activeSession: state.device.query.activeSession,
		disabledNext: disabledNextSession,
		disabledPrevious: disabledPreviousSession,
		activeSessionIndex: state.device.query.activeSessionIndex,
		activeSessionData: activeSessionData,
		activeSessionChartData: getFilteredData(activeSessionDataMeasurements, state.device.query.sessionFilter),
		queryData: state.device.query.data,
		showModal: state.device.query.activeSessionIndex!==null?true:false,
		loading: state.device.query.status.isLoading 
		};
}

function mapDispatchToProps(dispatch, ownProps) {
	return {
		setActiveSession: function(sessionId) {
			dispatch(DeviceActions.setActiveSession(sessionId));
		},
		setActiveSessionIndex: function(sessionIndex) {
			dispatch(DeviceActions.setActiveSessionIndex(sessionIndex));
		},
		resetActiveSessionIndex: function() {
			dispatch(DeviceActions.resetActiveSessionIndex());
		},
		setSessionFilter: function(filter) {
			dispatch(DeviceActions.setSessionFilter(filter));
		},
		fetchSession: function(sessionId, deviceKey, time) {
			dispatch(DeviceActions.fetchSession(sessionId, deviceKey, time));
		},
		getNextSession: function(deviceKey, time) {
			dispatch(DeviceActions.increaseActiveSessionIndex());
			dispatch(DeviceActions.fetchActiveSession(deviceKey, time));
		},
		getPreviousSession: function(deviceKey, time) {
			dispatch(DeviceActions.decreaseActiveSessionIndex());
			dispatch(DeviceActions.fetchActiveSession(deviceKey, time));
		}
	};
}


HistoryList = connect(mapStateToProps, mapDispatchToProps)(HistoryList);
HistoryList = injectIntl(HistoryList);
module.exports = HistoryList;
