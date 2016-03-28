var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var SessionsChart = require('../components/SessionsChart');

var HistoryActions = require('../actions/HistoryActions');

var { selectTimeFormatter } = require('../utils/time');
var { getFilteredData } = require('../utils/chart');
var { getDeviceTypeByKey } = require('../utils/device');


function mapStateToProps(state, ownProps) {
  if(!state.user.isAuthenticated) {
    return {};
  }
  
  return {
    time: state.section.history.time,
    filter: state.section.history.filter,
    devType: getDeviceTypeByKey(state.user.profile.devices, state.section.history.activeDevice), 
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
                                     data: [{title:stateProps.filter, data:getFilteredData(stateProps.chartData, stateProps.filter, stateProps.devType)}],
                                     xMin: stateProps.time.startDate,
                                     xMax: stateProps.time.endDate,
                                     type: stateProps.filter==='showers'?'bar':'line',
                                     formatter: selectTimeFormatter(stateProps.timeFilter, ownProps.intl),
                                     fontSize: 13
                       }));
}

var HistoryChart = connect(mapStateToProps, mapDispatchToProps, mergeProps)(SessionsChart);
HistoryChart = injectIntl(HistoryChart);
module.exports = HistoryChart;
