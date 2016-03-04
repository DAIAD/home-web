var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var SessionsChart = require('../components/SessionsChart');

var HistoryActions = require('../actions/HistoryActions');

var timeUtil = require('../utils/time');

var getCount = function(metrics) {
	return metrics.count?metrics.count:1;
};

var getTimestampIndex = function(points, timestamp) {
	  return points.findIndex((x) => (x[0]===timestamp));
};

var selectTimeFormatter = function(key, intl) {
	switch (key) {
		case "always":
			return (x) => intl.formatDate(x);
		case "year":
			return (x) => intl.formatDate(x, { day: 'numeric', month: 'long', year: 'numeric' });
		case "month":
			return (x) => intl.formatDate(x, { day: 'numeric', month: 'short' });
		case "week":
			return (x) => intl.formatMessage({id: "weekdays." + (new Date(x).getDay()+1).toString()});
			//intl.formatDate((x), { day: 'numeric' });
		case "day":
			return (x) => intl.formatTime(x, { hour: 'numeric', minute: 'numeric'});
		default:
			return (x) => intl.formatDate(x);
	}
};

var getTimeRange = function(key) {
	switch (key) {
		case "year":
			return timeUtil.thisYear();
		case "month":
			return timeUtil.thisMonth();
		case "week":
			return timeUtil.thisWeek();
		case "day":
			return timeUtil.today();
		default:
			return timeUtil.thisYear();
	}

};

var getFilteredData = function(data, filter) {
	var filteredData = [];
	if (!data) return [];
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
			return filteredData.map(x => [new Date(x[0]),x[1]]);

		case "volume12":
			filteredData = [[]];
			data.forEach(function(dato, i)	{
					var index = getTimestampIndex(filteredData[0], dato.timestamp);
					
					if (index>-1){
						if (!filteredData[1]){
							filteredData[1] = [];
						}
						filteredData[1].push([dato.timestamp, dato.volume]);
					}
					else{
						filteredData[0].push([dato.timestamp, dato.volume]);
						//filteredData.push([dato.timestamp, count]);			
											
					}
					
			});
			return filteredData.map(dI => dI.map(x => [new Date(x[0]),x[1]]));
		default:
			data.forEach(function(dato) {
				if (!dato[filter]){
					return;
				}
				filteredData.push([dato.timestamp, dato[filter]]);
			});
			return filteredData.map(x => [new Date(x[0]),x[1]]);
	}
	return;
	//return array with dates instead of timestamps
	//return filteredData;
	//return filteredData.map(x => [new Date(x[0]),x[1]]);
};

var HistoryChart = React.createClass({
	componentWillMount: function() {
		//this.props.querySessions(this.props.activeDevice, this.props.time);
	},
	render: function() {
		return (
			<SessionsChart {...this.props} />
		);
	}
});

function mapStateToProps(state, ownProps) {
	if(!state.user.isAuthenticated) {
		return {};
  }
  const dummySeries = {title:'Comparison', data:[[new Date("2016-02-25"),35], [new Date("2016-02-26"), 52], [new Date("2016-02-28"), 100], [new Date("2016-03-01"), 102]]};
	return {
		time: state.query.time,
		filter: state.section.history.filter,
    timeFilter: state.section.history.timeFilter,
    data: [{title:state.section.history.filter, data:getFilteredData(state.query.data, state.section.history.filter)}],
		xMin: state.query.time.startDate,
    xMax: state.query.time.endDate,
    fontSize: 13,
    type: (state.section.history.filter==='showers')?'bar':'line',
		formatter: selectTimeFormatter(state.section.history.timeFilter, ownProps.intl),
		loading: state.query.status.isLoading 
		};
}

function mapDispatchToProps(dispatch, ownProps) {
	return {
		setQueryFilter: function(filter) {
			return dispatch(HistoryActions.setQueryFilter(filter));
    },
	};
}
 
HistoryChart = connect(mapStateToProps, mapDispatchToProps)(HistoryChart);
HistoryChart = injectIntl(HistoryChart);
module.exports = HistoryChart;
