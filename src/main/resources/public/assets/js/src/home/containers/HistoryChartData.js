var React = require('react');
var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');
var { injectIntl } = require('react-intl');

var Chart = require('../components/helpers/Chart');

var HistoryActions = require('../actions/HistoryActions');

var { selectTimeFormatter } = require('../utils/time');
var { getChartTimeDataByFilter, getChartDataByFilter, getChartMeterCategories, getChartAmphiroCategories, getChartMetadata } = require('../utils/chart');
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


      const chartData = stateProps.data.map(devData => {
         const sessions = getDataSessions(stateProps.devices, devData);
         return ({
           title: getDeviceNameByKey(stateProps.devices, devData.deviceKey), 
           data: getChartDataByFilter(sessions, stateProps.filter, xAxisData),
           metadata: {
             device: devData.deviceKey,
             ids: getChartMetadata(sessions , xAxisData)
           }
         });
   });
   
   const comparison = stateProps.comparisonData.map(devData => {
     const sessions = getDataSessions(stateProps.devices, devData);
     return ({
       title: `${getDeviceNameByKey(stateProps.devices, devData.deviceKey)} (previous ${stateProps.timeFilter})`, 
       data: getChartDataByFilter(sessions, stateProps.filter, xAxisData)
     });
   }
                                                 );
  return Object.assign({},
                       ownProps,
                       dispatchProps,
                       stateProps, 
                       {
                         data: chartData.concat(comparison),
                             xMin: stateProps.timeFilter === 'custom' ? stateProps.time.startDate : 0,
                             xMax: stateProps.timeFilter === 'custom' ? stateProps.time.endDate : xAxisData.length-1,
                         xAxis: stateProps.timeFilter === 'custom' ? 'time' : 'category',
                         xAxisData,
                         type: 'line',
                         //xTicks: xAxisData.length,
                         mu: getMetricMu(stateProps.filter),
                         clickable: true,
                         onPointClick: (series, index) => {
                           const device = chartData[series] ? chartData[series].metadata.device : null;
                           const [id, timestamp] = chartData[series] ? (chartData[series].metadata.ids[index] ? chartData[series].metadata.ids[index] : [null, null]) : [null, null];
                           //console.log('clicked x', series, index, chartData[series].metadata.device, chartData[series].metadata.ids[index]);
                            
                           dispatchProps.setActiveSession(device, id, timestamp);
                           },
                         fontSize: 13,
                       }
                      );
}

var HistoryChart = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Chart);
HistoryChart = injectIntl(HistoryChart);

module.exports = HistoryChart;
