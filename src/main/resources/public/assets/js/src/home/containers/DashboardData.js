var React = require('react');
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { push } = require('react-router-redux');
const { getValues } = require('redux-form');

var { STATIC_RECOMMENDATIONS, STATBOX_DISPLAYS, DEV_METRICS, METER_METRICS, DEV_PERIODS, METER_PERIODS, DEV_SORT, METER_SORT } = require('../constants/HomeConstants');

var Dashboard = require('../components/sections/Dashboard');

var DashboardActions = require('../actions/DashboardActions');
var { linkToHistory } = require('../actions/HistoryActions');
var { saveToProfile } = require('../actions/UserActions');

var timeUtil = require('../utils/time');

var { getDeviceByKey, getDeviceNameByKey, getDeviceKeysByType, getDeviceTypeByKey, getAvailableDevices, getAvailableDeviceKeys, getAvailableMeters, getDefaultDevice } = require('../utils/device');
var { getLastSession, reduceMetric, reduceSessions, getDataSessions, getDataMeasurements, getShowersCount, transformInfoboxData } = require('../utils/transformations');

var { getEnergyClass, getMetricMu } = require('../utils/general');
var { getChartTimeDataByFilter, getChartDataByFilter, getChartMeterCategories, getChartAmphiroCategories } = require('../utils/chart');


function mapStateToProps(state, ownProps) {
  return {
    firstname: state.user.profile.firstname,
    devices: state.user.profile.devices,
    layout: state.section.dashboard.layout,
    mode: state.section.dashboard.mode,
    infoboxes: state.section.dashboard.infobox,
    infoboxToAdd: getValues(state.form.addInfobox)
  };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(Object.assign({}, DashboardActions, {linkToHistory, saveToProfile}), dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  
  const deviceType = (stateProps.infoboxToAdd && stateProps.infoboxToAdd.deviceType) ? stateProps.infoboxToAdd.deviceType : null;

  const deviceTypes = [{id: 'AMPHIRO', title: 'Shower'}, {id: 'METER', title: 'Smart Water Meter'}];

  const saveData = {infoboxes: stateProps.infoboxes.map(x => Object.assign({}, {id:x.id, deviceType:x.deviceType, display:x.display, metric:x.metric, period:x.period, title:x.title, type:x.type})), layout: stateProps.layout};

  const types = [
    {id: 'totalVolume', title: 'Total Volume', devType: 'AMPHIRO', data: {type: 'total', metric: 'volume', display: 'chart'}},
    {id: 'totalEnergy', title: 'Total Energy', devType: 'AMPHIRO', data: {type: 'total', metric: 'energy', display: 'chart'}},
    {id: 'totalTemperature', title: 'Total Temperature', devType: 'AMPHIRO', data: {type: 'total', metric: 'temperature', display: 'chart'}},
    {id: 'totalDifference', title: 'Total Volume', devType:'METER', data: {type: 'total', metric: 'difference', display: 'chart'}}, 
    {id: 'last', title: 'Last shower', devType: 'AMPHIRO', data: {type: 'last', metric: 'volume', display: 'chart'}},
    {id:'efficiencyEnergy', title: 'Energy efficiency', devType: 'AMPHIRO', data: {type: 'efficiency', metric: 'energy', display: 'stat'}},
    {id: 'breakdown', title: 'Water breakdown', devType: 'METER', data: {type: 'breakdown', metric: 'difference', display: 'chart'}},
    {id: 'forecast', title: 'Forecast', devType: 'METER', data: {type: 'forecast', metric: 'difference', display: 'chart'}}
  ].filter(x => deviceType ? stateProps.infoboxToAdd.deviceType === x.devType: null);


  return Object.assign({}, ownProps,
               dispatchProps,
               stateProps,
               {
                 infoboxes: stateProps.infoboxes.map(infobox => Object.assign({}, transformInfoboxData(infobox, stateProps.devices), {linkToHistory: () => dispatchProps.linkToHistory(infobox)})),
                 addInfobox: () => dispatchProps.addInfobox(Object.assign({}, {data: [], period: (deviceType === 'AMPHIRO' ? 'ten' : 'month')}, stateProps.infoboxToAdd, types.find(x => x.id === stateProps.infoboxToAdd.type) ?  types.find(x => x.id === stateProps.infoboxToAdd.type).data : {} )),
                 saveToProfile: () => dispatchProps.saveToProfile(saveData),
                 deviceTypes,
                 types,
               });
}


var DashboardData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Dashboard);
DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
