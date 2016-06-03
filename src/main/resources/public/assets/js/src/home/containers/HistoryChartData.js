var React = require('react');
var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');
var { injectIntl } = require('react-intl');

var Chart = require('../components/helpers/Chart');

var HistoryActions = require('../actions/HistoryActions');

var { selectTimeFormatter } = require('../utils/time');
var { getChartTimeDataByFilter, getChartDataByFilter, getChartMeterCategories, getChartAmphiroCategories } = require('../utils/chart');
var { getDeviceTypeByKey, getDeviceKeyByName, getDeviceNameByKey, getDataSessions, getMetricMu, getSessionsIdOffset } = require('../utils/device');


function mapStateToProps(state, ownProps) {
  if(!state.user.isAuthenticated) {
    return {};
  }
  
  return {
    time: state.section.history.time,
    filter: state.section.history.filter,
    devices: state.user.profile.devices,
    activeDeviceType: state.section.history.activeDeviceType,
    timeFilter: state.section.history.timeFilter,
    data: state.section.history.data,
    comparisonData: state.section.history.comparisonData
    };
}
function mapDispatchToProps(dispatch) {
  return bindActionCreators(HistoryActions, dispatch);
}
function mergeProps(stateProps, dispatchProps, ownProps) {
  
  const xAxisData = stateProps.activeDeviceType === 'METER' ? 
    (stateProps.timeFilter === 'custom' ? null : getChartMeterCategories(stateProps.timeFilter, ownProps.intl)) : 
      getChartAmphiroCategories(stateProps.timeFilter, getSessionsIdOffset(stateProps.data[0] ? stateProps.data[0].sessions : []));

    
   const comparison = stateProps.comparisonData.map(devData =>
                                                   ({
                                                     title: `${getDeviceNameByKey(stateProps.devices, devData.deviceKey)} (previous ${stateProps.timeFilter})`, 
                                                     data: getChartDataByFilter(getDataSessions(stateProps.devices, devData), stateProps.filter, xAxisData)
                                                   })
                                                 );
  return Object.assign({},
                       ownProps,
                       dispatchProps,
                       stateProps, 
                       {
                         data: stateProps.data.map(devData =>
                             ({
                               title: getDeviceNameByKey(stateProps.devices, devData.deviceKey), 
                               data: stateProps.activeDeviceType === 'METER' ? getChartDataByFilter(getDataSessions(stateProps.devices, devData), stateProps.filter, xAxisData) : getChartDataByFilter(getDataSessions(stateProps.devices, devData), stateProps.filter, xAxisData)
                             })).concat(comparison),
                             xMin: stateProps.timeFilter === 'custom' ? stateProps.time.startDate : 0,
                             xMax: stateProps.timeFilter === 'custom' ? stateProps.time.endDate : xAxisData.length-1,
                         xAxis: stateProps.timeFilter === 'custom' ? 'time' : 'category',
                         xAxisData,
                         type: 'line',
                         //xTicks: xAxisData.length,
                         mu: getMetricMu(stateProps.filter),
                         clickable: false,
                         onClick: (series, x) => {
                            const dev = getDeviceKeyByName(stateProps.devices, series.seriesName);
                            const id = parseInt(x.substring(1));
                            dispatchProps.setActiveSession(dev, id);
                           },
                         fontSize: 13,
                       }
                      );
}

var HistoryChart = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Chart);
HistoryChart = injectIntl(HistoryChart);

module.exports = HistoryChart;
