var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var injectIntl = require('react-intl').injectIntl;

var History = require('../components/sections/History');

var HistoryActions = require('../actions/HistoryActions');

var { getDeviceByKey, getDeviceNameByKey, getAvailableDevices, getDeviceTypeByKey, getDefaultDevice, reduceSessions, reduceMetric, getMetricMu, sortSessions } = require('../utils/device');
var timeUtil = require('../utils/time');
var { getFriendlyDuration, getEnergyClass } = require('../utils/general');

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
  const sessions = sortSessions(reduceSessions(stateProps.devices, stateProps.data), stateProps.sortFilter, stateProps.sortOrder);

  const metrics = devType === 'AMPHIRO' ? DEV_METRICS : METER_METRICS;

  const periods = devType === 'AMPHIRO' ? DEV_PERIODS : METER_PERIODS;
  
  const sortOptions = devType === 'AMPHIRO' ? DEV_SORT : METER_SORT;

  const comparisons = stateProps.timeFilter !== 'custom' ?
    (devType === 'AMPHIRO' ? [] : 
     [{id: 'last', title: timeUtil.getLastPeriod(stateProps.timeFilter, stateProps.time.startDate)}]
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
                    amphiros: getAvailableDevices(stateProps.devices),
                    periods,
                    metrics,
                    comparisons,
                    sortOptions,
                    sessions,
                    reducedMetric: `${reduceMetric(stateProps.devices, stateProps.data, stateProps.metricFilter)} ${getMetricMu(stateProps.metricFilter)}`,
                  }));
}

var HistoryData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(History);
HistoryData = injectIntl(HistoryData);
module.exports = HistoryData;
