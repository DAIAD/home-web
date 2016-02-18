var React = require('react');
var connect = require('react-redux').connect;

var DeviceActions = require('../actions/DeviceActions');
var Shower = require('../components/Shower');

var getSessionById = require('../utils/device').getSessionById;

var getFilteredData = function(data, filter) {
	if (!data) return;
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

var ShowerData = React.createClass({
	componentWillMount: function() {
		const id = this.props.params.id;
		this.props.fetchSession(id, this.props.activeDevice, this.props.time);
	},
	render: function() {
		return (
			<Shower {...this.props} />
		);
	}
});

function mapStateToProps(state, ownProps) {
	var sessionData = getSessionById(state.device.sessions.data, ownProps.params.id);

	return {
		time: state.device.query.time,
		filter: state.device.sessions.filter,
		activeDevice: state.device.query.activeDevice,
		chartData: getFilteredData(sessionData?sessionData.measurements:[], state.device.sessions.filter),
		listData: sessionData,
		loading: state.device.sessions.status.isLoading
		};
}

function mapDispatchToProps(dispatch) {
	return {
		setSessionFilter: function(filter) {
			dispatch(DeviceActions.setSessionFilter(filter));
		},
		setTime: function(time) {
			dispatch(DeviceActions.setTime(time));
		},
		fetchSession: function(sessionId, deviceKey, time) {
			dispatch(DeviceActions.fetchSession(sessionId, deviceKey, time));
		}
	};
}


ShowerData = connect(mapStateToProps, mapDispatchToProps)(ShowerData);
module.exports = ShowerData;
