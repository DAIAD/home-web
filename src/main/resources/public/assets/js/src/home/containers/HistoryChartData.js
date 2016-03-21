var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var SessionsChart = require('../components/SessionsChart');

var HistoryActions = require('../actions/HistoryActions');

var { selectTimeFormatter } = require('../utils/time');
var { getFilteredData } = require('../utils/chart');


function mapStateToProps(state, ownProps) {
  if(!state.user.isAuthenticated) {
    return {};
  }
  
  return {
    time: state.section.history.time,
    filter: state.section.history.filter,
    timeFilter: state.section.history.timeFilter,
    chartData: state.section.history.data
    };
}
function mapDispatchToProps(dispatch) {
  return {};
}
function mergeProps(stateProps, dispatchProps, ownProps) {
  return Object.assign({},
                       ownProps,
                       dispatchProps,
                       Object.assign({}, stateProps, {
                                     data: [{title:stateProps.filter, data:getFilteredData(stateProps.chartData, stateProps.filter)}],
                                     xMin: stateProps.time.startDate,
                                     xMax: stateProps.time.endDate,
                                     type: stateProps.filter==='showers'?'bar':'line',
                                     formatter: selectTimeFormatter(stateProps.timeFilter, ownProps.intl),
                                     yMargin: 0,
                                     fontSize: 13
                       }));
}

var HistoryChart = connect(mapStateToProps, mapDispatchToProps, mergeProps)(SessionsChart);
HistoryChart = injectIntl(HistoryChart);
module.exports = HistoryChart;
