var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var injectIntl = require('react-intl').injectIntl;

var History = require('../components/sections/History');

var HistoryActions = require('../actions/HistoryActions');

var { getDeviceByKey, getDeviceNameByKey, getAvailableDevices, getDeviceTypeByKey, getDefaultDevice, reduceSessions, reduceMetric, getMetricMu, sortSessions } = require('../utils/device');
var timeUtil = require('../utils/time');
var { getFriendlyDuration, getEnergyClass } = require('../utils/general');

function mapStateToProps(state, ownProps) {
  const defaultDevice = getDefaultDevice(state.user.profile.devices);
  const deviceKey = defaultDevice?defaultDevice.deviceKey:null;

  return {
    firstname: state.user.profile.firstname,
    time: state.section.history.time,
    activeDevice: state.section.history.activeDevice,
    activeDeviceType: state.section.history.activeDeviceType,
    errors: state.query.errors,
    //activeSessionIndex: state.section.history.activeSessionIndex,
    //activeSessionFilter: state.section.history.activeSessionFilter,
    metricFilter: state.section.history.filter,
    timeFilter: state.section.history.timeFilter,
    sortFilter: state.section.history.sortFilter,
    sortOrder: state.section.history.sortOrder,
    devices: state.user.profile.devices?state.user.profile.devices:[],
    synced: state.section.history.synced,
    data: state.section.history.data,
    comparison: state.section.history.comparison
  };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(HistoryActions, dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  const devType = stateProps.activeDeviceType;
  const devMetrics = [
    {id:'showers', title:'Showers'},
    {id:'volume', title:'Volume'},
    {id:'energy', title:'Energy'},
    {id:'duration', title:'Duration'},
    {id:'temperature', title:'Temperature'}
  ];
  
  const meterMetrics = [
    {id:'difference', title:'Volume'},
  ];

  const metrics = devType === 'AMPHIRO' ? devMetrics : (devType === 'METER' ? meterMetrics : [] );

  const deviceSortOptions = [{id: 'timestamp', title: 'Time'}, {id:'volume', title: 'Volume'}, {id:'devName', title: 'Device'}, {id: 'energy', title: 'Energy'}, {id:'temperature', title:'Temperature'}, {id:'duration', title: 'Duration'}];

  const meterSortOptions = [{id: 'timestamp', title: 'Time'}, {id:'difference', title: 'Volume'}];
  const sortOptions = devType === 'AMPHIRO' ? deviceSortOptions : meterSortOptions;

  const comparisons = stateProps.timeFilter !== 'custom' ? [{id:'last', title: timeUtil.getLastPeriod(stateProps.timeFilter, stateProps.time.startDate)}] : [];

  return Object.assign(
    {}, 
    ownProps, 
    dispatchProps,
    Object.assign({}, 
                  stateProps, 
                  { 
                    nextPeriod: stateProps.time?timeUtil.getNextPeriod(stateProps.timeFilter, stateProps.time.startDate):{}, 
                    previousPeriod: stateProps.time?timeUtil.getPreviousPeriod(stateProps.timeFilter, stateProps.time.endDate):{},
                    metrics,
                    comparisons,
                    sortOptions,
                    reducedMetric: `${reduceMetric(stateProps.devices, stateProps.data, stateProps.metricFilter)} ${getMetricMu(stateProps.metricFilter)}`,
                  sessions: sortSessions(reduceSessions(stateProps.devices, stateProps.data), stateProps.sortFilter, stateProps.sortOrder),
                    amphiros: getAvailableDevices(stateProps.devices),
                  }));
}

var HistoryData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(History);
HistoryData = injectIntl(HistoryData);
module.exports = HistoryData;
