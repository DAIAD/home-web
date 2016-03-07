var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var SessionsList = require('../components/SessionsList');

var DeviceActions = require('../actions/DeviceActions');
var HistoryActions = require('../actions/HistoryActions');

var { getSessionByIndex } = require('../utils/device');

var getFilteredData = function(data, filter) {
	if (!data) return [];
	var filteredData = [];
	
	data.forEach(function(dato) {
		if (!dato[filter]){
			return;
		}
		filteredData.push([dato.timestamp, dato[filter]]);
	});
	return filteredData.map(x => [new Date(x[0]),x[1]]);
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
	var activeSessionData = getSessionByIndex(state.query.data, state.section.history.activeSessionIndex);
	var activeSessionDataMeasurements = [];
 	if (activeSessionData) {
    activeSessionDataMeasurements = activeSessionData.measurements?activeSessionData.measurements:[];
	}
	var disabledNextSession = true;
	var disabledPreviousSession = true;
	if (state.section.history.activeSessionIndex!==null) {
		if (state.query.data[state.section.history.activeSessionIndex+1]) {
			disabledNextSession = false;
		}
		if (state.query.data[state.section.history.activeSessionIndex-1]) {
			disabledPreviousSession = false;
		}
  }
  var data = getFilteredData(activeSessionDataMeasurements, state.section.history.sessionFilter);

  if (state.section.history.sessionFilter === 'volume' || state.section.history.sessionFilter === 'energy'){
    
    //TODO: refactor this monster
    data = data.map((val, idx, arr) => [val[0], arr.map((array) => array[1]?array[1]:0).reduce((prev, curr, idx2, array, initial) => idx2<=idx?prev+curr:prev)]);
  }

  //console.log('data is ');
  //console.log(data);
   var arr = [{title: state.section.history.sesionFilter, data:data}];
  //arr.splice(0, 1);
	return {
		time: state.query.time,
		activeDevice: state.query.activeDevice,
    sessionFilter: state.section.history.sessionFilter,
    sessions: state.query.data,
		activeSession: state.query.activeSession,
		disabledNext: disabledNextSession,
		disabledPrevious: disabledPreviousSession,
		activeSessionIndex: state.section.history.activeSessionIndex,
    getSessionByIndex: getSessionByIndex,
    getFilteredData: getFilteredData,
    activeSessionData: activeSessionData,
    activeSessionChartData: arr,
    queryData: state.query.data,
		showModal: state.section.history.activeSessionIndex===null?false:true,
		loading: state.query.status.isLoading 
		};
}

function mapDispatchToProps(dispatch, ownProps) {
	return {
		setActiveSessionIndex: function(sessionIndex) {
			dispatch(HistoryActions.setActiveSessionIndex(sessionIndex));
		},
		resetActiveSessionIndex: function() {
			dispatch(HistoryActions.resetActiveSessionIndex());
		},
		setSessionFilter: function(filter) {
			dispatch(HistoryActions.setSessionFilter(filter));
		},
		fetchSession: function(sessionId, deviceKey, time) {
			dispatch(DeviceActions.fetchSession(sessionId, deviceKey, time));
		},
		getNextSession: function(deviceKey, time) {
			dispatch(HistoryActions.increaseActiveSessionIndex());
			dispatch(DeviceActions.fetchActiveSession(deviceKey, time));
		},
		getPreviousSession: function(deviceKey, time) {
			dispatch(HistoryActions.decreaseActiveSessionIndex());
			dispatch(DeviceActions.fetchActiveSession(deviceKey, time));
		}
	};
}


HistoryList = connect(mapStateToProps, mapDispatchToProps)(HistoryList);
HistoryList = injectIntl(HistoryList);
module.exports = HistoryList;
