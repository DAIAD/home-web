var React = require('react');
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { push } = require('react-router-redux');
//const { getValues } = require('redux-form');

var { STATIC_RECOMMENDATIONS, STATBOX_DISPLAYS, DEV_METRICS, METER_METRICS, DEV_PERIODS, METER_PERIODS, DEV_SORT, METER_SORT } = require('../constants/HomeConstants');

var Dashboard = require('../components/sections/Dashboard');

var DashboardActions = require('../actions/DashboardActions');
var { linkToHistory } = require('../actions/HistoryActions');
var { saveToProfile } = require('../actions/UserActions');

var timeUtil = require('../utils/time');

var { getDeviceByKey, getDeviceNameByKey, getDeviceKeysByType, getDeviceTypeByKey, getAvailableDevices, getAvailableDeviceKeys, getAvailableMeters, getDefaultDevice, getDeviceCount, getMeterCount } = require('../utils/device');
var { getLastSession, reduceMetric, reduceSessions, getDataSessions, getDataMeasurements, getShowersCount, transformInfoboxData } = require('../utils/transformations');

var { getEnergyClass, getMetricMu } = require('../utils/general');
var { getChartTimeDataByFilter, getChartDataByFilter, getChartMeterCategories, getChartAmphiroCategories } = require('../utils/chart');


function mapStateToProps(state, ownProps) {
  return {
    firstname: state.user.profile.firstname,
    devices: state.user.profile.devices,
    layout: state.section.dashboard.layout,
    mode: state.section.dashboard.mode,
    dirty: state.section.dashboard.dirty,
    infoboxes: state.section.dashboard.infobox,
    infoboxToAdd: state.section.dashboard.infoboxToAdd,
    //infoboxToAdd: getValues(state.form.addInfobox)
  };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(Object.assign({}, DashboardActions, {linkToHistory, saveToProfile}), dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  
  let deviceType = (stateProps.infoboxToAdd && stateProps.infoboxToAdd.deviceType) ? stateProps.infoboxToAdd.deviceType : null;

  let deviceTypes = [{id: 'AMPHIRO', title: 'Shower'}, {id: 'METER', title: 'Smart Water Meter'}];
  
  const meterCount = getMeterCount(stateProps.devices);
  const deviceCount = getDeviceCount(stateProps.devices);

  if (meterCount === 0) {
    deviceTypes = deviceTypes.filter(x => x.id !== 'METER');
  }
  
  if (deviceCount === 0) {
    deviceTypes = deviceTypes.filter(x => x.id !== 'AMPHIRO');
  }

  const saveData = {infoboxes: stateProps.infoboxes.map(x => Object.assign({}, {id:x.id, deviceType:x.deviceType, display:x.display, metric:x.metric, period:x.period, title:x.title, type:x.type})), layout: stateProps.layout};

  const types = [
    {id: 'totalVolumeStat', title: 'Shower Volume Stat', description: 'A stat widget displaying the total consumption for your last 10 showers. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'volume', display: 'stat'}},
    {id: 'totalVolumeChart', title: 'Shower Volume Chart', description: 'A chart widget presenting the consumption for your last 10 showers for all installed devices. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'volume', display: 'chart'}},
    {id: 'totalEnergyStat', title: 'Shower Energy Stat', description: 'A stat widget displaying the total energy consumption for your last 10 showers. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'energy', display: 'stat'}},
    {id: 'totalEnergyChart', title: 'Shower Energy Chart', description: 'A chart widget displaying the total energy progress for your last 10 showers. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'energy', display: 'chart'}},
    {id: 'totalTemperatureStat', title: 'Shower Temperature Stat', description: 'A widget displaying the average temperature for your last 10 showers. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'temperature', display: 'stat'}},
    {id: 'totalTemperatureChart', title: 'Shower Temperature Chart', description: 'A widget displaying the average temperature variation for your last 10 showers. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'temperature', display: 'chart'}},
    
    {id: 'totalDifferenceStat', description: 'A widget displaying your household\'s total water consumption for the last month. You can later change it to daily, weekly or yearly consumption.', title: 'Total Volume Stat', devType:'METER', data: {type: 'total', metric: 'difference', display: 'stat'}}, 
    {id: 'totalDifferenceChart', title: 'Total Volume Chart', description: 'A chart widget displaying your household\'s total water consumption progress for the last month. You can later change it to daily, weekly or yearly consumption.', devType:'METER', data: {type: 'total', metric: 'difference', display: 'chart'}}, 
    {id: 'last', title: 'Last shower', description: 'A widget displaying the last shower recorded for all your devices.', devType: 'AMPHIRO', data: {type: 'last', metric: 'volume', display: 'chart'}},
    {id:'efficiencyEnergy', title: 'Energy efficiency', description: 'A widget displaying your shower energy score for the last 10 showers. You can later change this to see the energy efficiency for the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'efficiency', metric: 'energy', display: 'stat'}},
    {id: 'breakdown', title: 'Water breakdown', description: 'A chart widget displaying your computed water use per household appliance.', devType: 'METER', data: {type: 'breakdown', metric: 'difference', display: 'chart'}},
    {id: 'forecast', title: 'Forecast', description: 'A chart widget depicting our estimations for your water use for the next month based on your use so far! You can later change this to see estimations for the next day, week, or year.', devType: 'METER', data: {type: 'forecast', metric: 'difference', display: 'chart'}},
    {id: 'comparison', title: 'Comparison', description: 'A widget showing your consumption in comparison to others, like your neighbors or your city average for the last month. You can later change this to see comparison data for the current day, week, or year.', devType: 'METER', data: {type: 'comparison', metric: 'difference', display: 'chart'}}
  ].filter(x => deviceType ? stateProps.infoboxToAdd.deviceType === x.devType: null);


  return Object.assign({}, ownProps,
               dispatchProps,
               stateProps,
               {
                 infoboxes: stateProps.infoboxes.map(infobox => Object.assign({}, transformInfoboxData(infobox, stateProps.devices, ownProps.intl), {linkToHistory: () => dispatchProps.linkToHistory(infobox)})),
                   addInfobox: () => {
                     
                     const type = types.find(x => x.id === stateProps.infoboxToAdd.type);
                     // ?  types.find(x => x.id === stateProps.infoboxToAdd.type).data : {}
                     return dispatchProps.addInfobox(Object.assign({}, {data: [], period: (deviceType === 'AMPHIRO' ? 'ten' : 'month')}, stateProps.infoboxToAdd, {title: stateProps.infoboxToAdd.title ? stateProps.infoboxToAdd.title : (type ? type.title : null)}, type ? type.data : {} ));
                   },
                 deviceCount: getDeviceCount(stateProps.devices),
                 meterCount: getMeterCount(stateProps.devices),
                 saveToProfile: () => dispatchProps.saveToProfile(saveData),
                 deviceTypes,
                 types,
               });
}


var DashboardData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Dashboard);
DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
