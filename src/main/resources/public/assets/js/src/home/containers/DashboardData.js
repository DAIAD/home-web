var React = require('react');
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { push } = require('react-router-redux');
//const { getValues } = require('redux-form');

var { STATIC_RECOMMENDATIONS, STATBOX_DISPLAYS, DEV_METRICS, METER_METRICS, DEV_PERIODS, METER_PERIODS, DEV_SORT, METER_SORT, WIDGET_TYPES  } = require('../constants/HomeConstants');

var Dashboard = require('../components/sections/Dashboard');

var DashboardActions = require('../actions/DashboardActions');
var { linkToHistory } = require('../actions/HistoryActions');
var { saveToProfile } = require('../actions/UserActions');
var { setForm } = require('../actions/FormActions');

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
    infoboxToAdd: state.forms.infoboxToAdd,
    //infoboxToAdd: state.section.dashboard.infoboxToAdd,
    //infoboxToAdd: getValues(state.form.addInfobox)
  };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(Object.assign({}, DashboardActions, {linkToHistory, saveToProfile, setForm}), dispatch);
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

  const types = WIDGET_TYPES.filter(x => deviceType ? stateProps.infoboxToAdd.deviceType === x.devType: null);

  return Object.assign({}, ownProps,
               dispatchProps,
               stateProps,
               {
                 infoboxes: stateProps.infoboxes.map(infobox => 
                   transformInfoboxData(infobox, stateProps.devices, ownProps.intl)),
                 addInfobox: () => {
                   
                   const type = types.find(x => x.id === stateProps.infoboxToAdd.type);
                   // ?  types.find(x => x.id === stateProps.infoboxToAdd.type).data : {}
                   return dispatchProps.addInfobox(Object.assign({}, {data: [], period: (deviceType === 'AMPHIRO' ? 'ten' : 'month')}, stateProps.infoboxToAdd, {title: stateProps.infoboxToAdd.title ? stateProps.infoboxToAdd.title : (type ? type.title : null)}, type ? type.data : {} ));
                 },
                 deviceCount: getDeviceCount(stateProps.devices),
                 meterCount: getMeterCount(stateProps.devices),
                 saveToProfile: () => dispatchProps.saveToProfile({configuration: JSON.stringify(saveData)}),
                 deviceTypes,
                 types,
               });
}


var DashboardData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Dashboard);
DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
