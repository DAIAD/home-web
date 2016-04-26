var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var injectIntl = require('react-intl').injectIntl;

var History = require('../components/sections/History');

var HistoryActions = require('../actions/HistoryActions');

var { getReducedDeviceType, getDeviceByKey, getDeviceNameByKey, getDeviceTypeByKey, getDefaultDevice, reduceSessions, reduceMetric } = require('../utils/device');
var timeUtil = require('../utils/time');
var { getFriendlyDuration, getEnergyClass } = require('../utils/general');
var { getFilteredData } = require('../utils/chart');

function mapStateToProps(state, ownProps) {
  const defaultDevice = getDefaultDevice(state.user.profile.devices);
  const deviceKey = defaultDevice?defaultDevice.deviceKey:null;

  return {
    time: state.section.history.time,
    activeDevice: state.section.history.activeDevice,
    //devType: getReducedDeviceType(state.user.profile.devices, state.section.history.activeDevice),
    //activeSessionIndex: state.section.history.activeSessionIndex,
    //activeSessionFilter: state.section.history.activeSessionFilter,
    metricFilter: state.section.history.filter,
    timeFilter: state.section.history.timeFilter,
    devices: state.user.profile.devices?state.user.profile.devices:[],
    data: state.section.history.data,
    comparison: state.section.history.comparison
  };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(HistoryActions, dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  
  const devType = getReducedDeviceType(stateProps.devices, stateProps.activeDevice);
  const allMetrics = [
    {id:'showers', title:'Showers'},
    {id:'volume', title:'Volume'},
    {id:'energy', title:'Energy'},
    {id:'duration', title:'Duration'},
    {id:'temperature', title:'Temperature'}
  ];

  return Object.assign(
    {}, 
    ownProps, 
    dispatchProps,
    Object.assign({}, 
                  stateProps, 
                  { 
                    nextPeriod: timeUtil.getNextPeriod(stateProps.timeFilter, stateProps.time.startDate), 
                    previousPeriod: timeUtil.getPreviousPeriod(stateProps.timeFilter, stateProps.time.endDate),
                    metrics: devType==='AMPHIRO'?allMetrics:allMetrics.filter(m=>m.id==='volume'),
                    reducedMetric: reduceMetric(stateProps.devices, stateProps.data, stateProps.metricFilter),
                    sessions: reduceSessions(stateProps.devices, stateProps.data)
                  }));
}

var HistoryData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(History);
HistoryData = injectIntl(HistoryData);
module.exports = HistoryData;
