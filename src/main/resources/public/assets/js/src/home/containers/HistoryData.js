var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var injectIntl = require('react-intl').injectIntl;

var History = require('../components/sections/History');

var HistoryActions = require('../actions/HistoryActions');

var { getReducedDeviceType, getDeviceByKey, getDeviceTypeByKey, getDefaultDevice } = require('../utils/device');
var timeUtil = require('../utils/time');


function mapStateToProps(state, ownProps) {
  const defaultDevice = getDefaultDevice(state.user.profile.devices);
  const deviceKey = defaultDevice?defaultDevice.deviceKey:null;

  return {
    time: state.section.history.time,
    devType: getReducedDeviceType(state.user.profile.devices, state.section.history.activeDevice),
    metricFilter: state.section.history.filter,
    timeFilter: state.section.history.timeFilter,
    devices: state.user.profile.devices?state.user.profile.devices:[],
    activeDevice: state.section.history.activeDevice,
    defaultDevice: deviceKey,
  };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(HistoryActions, dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {
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
                    metrics: stateProps.devType===-1?allMetrics.filter(m=>m.id!=='volume'):allMetrics,
                    //activeDevice: getDeviceByKey(stateProps.devices, stateProps.activeDeviceId),
                  }
                 )
  );
}

var HistoryData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(History);
HistoryData = injectIntl(HistoryData);
module.exports = HistoryData;
