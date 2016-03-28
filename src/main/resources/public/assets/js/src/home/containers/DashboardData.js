var React = require('react');
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { bindActionCreators } = require('redux');
var connect = require('react-redux').connect;
var FormattedMessage = require('react-intl').FormattedMessage;
var { push } = require('react-router-redux');

var Dashboard = require('../components/sections/Dashboard');

var HistoryActions = require('../actions/HistoryActions');
var DashboardActions = require('../actions/DashboardActions');

var timeUtil = require('../utils/time');
var { getDeviceByKey, getDefaultDevice, getLastSession } = require('../utils/device');
var { getFilteredData } = require('../utils/chart');


function mapStateToProps(state, ownProps) {
  const lastSession = state.section.dashboard.lastSession;
  const defaultDevice = getDefaultDevice(state.user.profile.devices);
  const deviceKey = defaultDevice?defaultDevice.deviceKey:null;
  
  return {
    defaultDevice: deviceKey,
    devices: state.user.profile.devices,
    firstname: state.user.profile.firstname,
    lastShower: lastSession,
    layouts: state.section.dashboard.layout,
    edit: state.section.dashboard.mode==="edit"?true:false,
    infoboxData: state.section.dashboard.infobox,
  };
}

function mapDispatchToProps(dispatch) {
  return Object.assign({},
                        merged(bindActionCreators(DashboardActions, dispatch)),
                        {linkToHistory: options => dispatch(HistoryActions.linkToHistory(options))}); 
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  return merged(ownProps,
               dispatchProps,
               merged(stateProps,
                     {
                       lastSessionTime: merged(timeUtil.thisMonth(), {granularity: 0}),
                       chartFormatter: (intl => (x) => { console.log(intl); return intl.formatTime(x, { hour:'numeric', minute:'numeric', second:'numeric'}); }),
                         chartData: [{title:'Consumption', data:(stateProps.lastShower?(getFilteredData(stateProps.lastShower.measurements?stateProps.lastShower.measurements:[], 'volume')):[])}],
                         infoboxData: stateProps.infoboxData.map(infobox => Object.assign({}, infobox, {deviceDetails: getDeviceByKey(stateProps.devices, infobox.device)})).map(infobox => { return infobox.type !== 'last'?infobox:Object.assign({}, infobox, {data:Object.assign({}, infobox.data, { chartData: getFilteredData(infobox.data.measurements, infobox.metric)})});})
                     }));
}

function merged (...objects) {
  return Object.assign({}, ...objects);
}

var DashboardData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Dashboard);
//DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
