var React = require('react');
var connect = require('react-redux').connect;

var DeviceActions = require('../actions/DeviceActions');
var History = require('../components/sections/History');

var getAvailableDevices = require('../utils/device').getAvailableDevices;
var getAvailableDeviceKeys = require('../utils/device').getAvailableDeviceKeys;
var getDefaultDevice = require('../utils/device').getDefaultDevice;

var getCount = function(metrics) {
	return metrics.count?metrics.count:1;
};

var getTimestampIndex = function(points, timestamp) {
	  return points.findIndex((x) => (x[0]===timestamp));
};

var getFilteredData = function(data, filter) {
	var filteredData = [];
	switch (filter) {
		case "showers":
			data.forEach(function(dato, i)	{
				const count = getCount(dato);
				var index = getTimestampIndex(filteredData, dato.timestamp);
				
				//increment or append
				if (index>-1){
					filteredData[index] = [filteredData[index][0], filteredData[index][1]+count];			
				}
				else{
					filteredData.push([dato.timestamp, count]);			
				}	
			});
			break;

		default:
			data.forEach(function(dato) {
				if (!dato[filter]){
					return;
				}
				filteredData.push([new Date(dato.timestamp), dato[filter]]);
			});
	}
	//return array with dates instead of timestamps
	return filteredData.map(x => [new Date(x[0]),x[1]]);
};

var HistoryData = React.createClass({
	componentWillMount: function() {
		this.props.querySessions(this.props.activeDevice, this.props.time);
		this.props.fetchSessionsIfNeeded(this.props.activeDevice, this.props.time);
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
		filter: state.device.query.filter,
		devices: getAvailableDevices(state.user.profile.devices),
		activeDevice: state.device.query.activeDevice,
		listData: state.device.sessions.data,
		chartData: getFilteredData(state.device.query.data, state.device.query.filter),
		loading: state.device.query.status.isLoading
		};
}

function mapDispatchToProps(dispatch) {
	return {
		setQueryFilter: function(filter) {
			dispatch(DeviceActions.setQueryFilter(filter));
		},
		setTime: function(time) {
			dispatch(DeviceActions.setTime(time));
		},
		querySessions: function(deviceKey, time) {
			dispatch(DeviceActions.querySessions(deviceKey, time));
		},
		fetchSessionsIfNeeded: function(deviceKey, time) {
			dispatch(DeviceActions.fetchSessionsIfNeeded(deviceKey, time));
		},
		setActive: function(deviceKey) {
			dispatch(DeviceActions.setActiveDevice(deviceKey));
		},
		fetchSession: function(sessionId, deviceKey, time) {
			dispatch(DeviceActions.fetchSession(sessionId, deviceKey, time));
		}
	};
}


HistoryData = connect(mapStateToProps, mapDispatchToProps)(HistoryData);
module.exports = HistoryData;
