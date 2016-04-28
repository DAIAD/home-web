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
    data: state.section.history.data
    };
}
function mapDispatchToProps(dispatch) {
  return {};
}
function mergeProps(stateProps, dispatchProps, ownProps) {
  //TODO: devType is null
  const dataSource = stateProps.devType==="METER"?"values":"sessions";
  return Object.assign({},
                       ownProps,
                       dispatchProps,
                       Object.assign({}, stateProps, 
                                     {
                                       data:
                                         stateProps.data.map(devData =>
                                                                      {
                                                                        return {
                                                                          title: getDeviceNameByKey(stateProps.devices, devData.deviceKey), 
  data:getFilteredData(getDataSessions(stateProps.devices, devData), stateProps.filter)
                                                                          };
                                                                          }),
                                     //                               [{title:stateProps.filter, data:getFilteredData(stateProps.chartData.length?stateProps.chartData[0][value]:[], stateProps.filter, stateProps.devType)}],
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
