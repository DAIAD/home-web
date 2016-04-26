var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var SessionsChart = require('../components/SessionsChart');

var HistoryActions = require('../actions/HistoryActions');

var { selectTimeFormatter } = require('../utils/time');
var { getFilteredData } = require('../utils/chart');
var { getDeviceTypeByKey, getDeviceNameByKey, getDataSessions } = require('../utils/device');


function mapStateToProps(state, ownProps) {
  if(!state.user.isAuthenticated) {
    return {};
  }
  
  return {
    time: state.section.history.time,
    filter: state.section.history.filter,
    devices: state.user.profile.devices,
    devType: getDeviceTypeByKey(state.user.profile.devices, state.section.history.activeDevice), 
    timeFilter: state.section.history.timeFilter,
    data: state.section.history.data,
    comparisonData: state.section.history.comparisonData
    };
}
function mapDispatchToProps(dispatch) {
  return {};
}
function mergeProps(stateProps, dispatchProps, ownProps) {
  console.log('in history chart data merge props');

  const comparison = stateProps.comparisonData.map(devData =>
                                                   ({
                                                     title: `${getDeviceNameByKey(stateProps.devices, devData.deviceKey)} (previous ${stateProps.timeFilter})`, 
                                                     data:getFilteredData(getDataSessions(stateProps.devices, devData), stateProps.filter, 'METER', stateProps.timeFilter, true)
                                                   })
                                                  );
                                                  
  return Object.assign({},
                       ownProps,
                       dispatchProps,
                       Object.assign({}, stateProps, 
                                     {
                                       data:
                                         stateProps.data.map(devData =>
                                                             ({
                                                             title: getDeviceNameByKey(stateProps.devices, devData.deviceKey), 
  data: getFilteredData(getDataSessions(stateProps.devices, devData), stateProps.filter)
                                                             })).concat(comparison),
                         xMin: stateProps.time.startDate,
                         xMax: stateProps.time.endDate,
                         type: stateProps.filter==='showers'?'bar':'line',
                         formatter: selectTimeFormatter(stateProps.timeFilter, ownProps.intl),
                         fontSize: 13
                                     }
                                    ));
}

var HistoryChart = connect(mapStateToProps, mapDispatchToProps, mergeProps)(SessionsChart);
HistoryChart = injectIntl(HistoryChart);
module.exports = HistoryChart;
