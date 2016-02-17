var connect = require('react-redux').connect;

var DeviceActions = require('../actions/DeviceActions');
var History = require('../components/sections/History');

var getAvailableDevices = require('../utils/device').getAvailableDevices;
var getAvailableDeviceKeys = require('../utils/device').getAvailableDeviceKeys;

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

function mapStateToProps(state, ownProps) {
	return {
		time: state.device.time,
		filter: state.device.sessions.filter,
		devices: getAvailableDevices(state.user.profile.devices),
		activeDevice: state.device.sessions.activeDevice,
		listData: state.device.sessions.showers,
		chartData: getFilteredData(state.device.sessions.data, state.device.sessions.filter),
		//sessionData: state.device.activeSession?getSessionById(state.device.data, state.device.activeSession):{},
		loading: state.device.sessions.status.isLoading
		};
}

function mapDispatchToProps(dispatch) {
	return {
		setFilter: function(filter) {
			dispatch(DeviceActions.setFilter(filter));
		},
		setTime: function(time) {
			dispatch(DeviceActions.setTime(time));
		},
		searchSessions: function() {
			dispatch(DeviceActions.searchSessions());
		},
		setActive: function(deviceKey) {
			dispatch(DeviceActions.setActiveDevice(deviceKey));
		},
		fetchSession: function(sessionId) {
			dispatch(DeviceActions.fetchSession(sessionId));
		}
	};
}


var HistoryData = connect(mapStateToProps, mapDispatchToProps)(History);
module.exports = HistoryData;
