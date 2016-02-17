var connect = require('react-redux').connect;

var DeviceActions = require('../actions/DeviceActions');
var Shower = require('../components/Shower');


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

function mapStateToProps(state, ownProps) {
	//var sessionData = getSessionById(state.device.data, ownProps.params.id);
	var sessionData = state.device.lastSession.data;

	return {
		filter: state.device.lastSession.filter,
		activeDevice:state.device.sessions.activeDevice,
		chartData: getFilteredData(sessionData?sessionData.measurements:[], state.device.lastSession.filter),
		listData: sessionData,
		loading: state.device.lastSession.status.isLoading
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
		fetchSession: function(sessionId) {
			dispatch(DeviceActions.fetchSession(sessionId));
		}
	};
}


var ShowerData = connect(mapStateToProps, mapDispatchToProps)(Shower);
module.exports = ShowerData;
