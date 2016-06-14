var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var injectIntl = require('react-intl').injectIntl;

var History = require('../components/sections/History');

var HistoryActions = require('../actions/HistoryActions');

var { getDeviceByKey, getDeviceNameByKey, getAvailableDevices, getDeviceTypeByKey, getDefaultDevice, getDeviceCount, getMeterCount } = require('../utils/device');
var { reduceSessions, reduceMetric, sortSessions, meterSessionsToCSV, deviceSessionsToCSV } = require('../utils/transformations');

var timeUtil = require('../utils/time');
var { getFriendlyDuration, getEnergyClass, getMetricMu } = require('../utils/general');
var { getTimeLabelByGranularity } = require('../utils/chart');

var { DEV_METRICS, METER_METRICS, DEV_PERIODS, METER_PERIODS, DEV_SORT, METER_SORT } = require('../constants/HomeConstants');

function mapStateToProps(state, ownProps) {

  return {
    firstname: state.user.profile.firstname,
    time: state.section.history.time,
    activeDevice: state.section.history.activeDevice,
    activeDeviceType: state.section.history.activeDeviceType,
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
  const sessions = sortSessions(reduceSessions(stateProps.devices, stateProps.data), stateProps.sortFilter, stateProps.sortOrder)
  .map(s => Object.assign({}, s, {date: getTimeLabelByGranularity(s.timestamp, stateProps.time.granularity, ownProps.intl)}));

  

  const csvData = stateProps.activeDeviceType === 'METER' ? meterSessionsToCSV(sessions) : deviceSessionsToCSV(sessions);

  let deviceTypes = [{id:'METER', title: 'Water meter', image: 'water-meter.svg'}, {id:'AMPHIRO', title:'Shower devices', image: 'amphiro_small.svg'}];


  const amphiros = getAvailableDevices(stateProps.devices); 
  const meterCount = getMeterCount(stateProps.devices);
  const deviceCount = getDeviceCount(stateProps.devices);

  if (meterCount === 0) {
    deviceTypes = deviceTypes.filter(x => x.id !== 'METER');
  }
  
  if (deviceCount === 0) {
    deviceTypes = deviceTypes.filter(x => x.id !== 'AMPHIRO');
  }

  const metrics = devType === 'AMPHIRO' ? DEV_METRICS : METER_METRICS;

  const periods = devType === 'AMPHIRO' ? DEV_PERIODS : METER_PERIODS;
  
  const sortOptions = devType === 'AMPHIRO' ? DEV_SORT : METER_SORT;

  const comparisons = stateProps.timeFilter !== 'custom' ?
    (devType === 'AMPHIRO' ? [] : 
     [{id: 'last', title: timeUtil.getComparisonPeriod(stateProps.time.startDate, stateProps.time.granularity, ownProps.intl)}]
    ) 
      : [];


  return Object.assign(
    {}, 
    ownProps, 
    dispatchProps,
    Object.assign({}, 
                  stateProps, 
                  { 
                    nextPeriod: stateProps.time?timeUtil.getNextPeriod(stateProps.timeFilter, stateProps.time.startDate):{}, 
                    previousPeriod: stateProps.time?timeUtil.getPreviousPeriod(stateProps.timeFilter, stateProps.time.endDate):{},
                    amphiros,
                    periods,
                    metrics,
                    comparisons,
                    sortOptions,
                    sessions,
                    deviceTypes,
                    csvData,
                    reducedMetric: `${reduceMetric(stateProps.devices, stateProps.data, stateProps.metricFilter)} ${getMetricMu(stateProps.metricFilter)}`,
                  }));
}

var HistoryData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(History);
HistoryData = injectIntl(HistoryData);
module.exports = HistoryData;
