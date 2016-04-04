var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var injectIntl = require('react-intl').injectIntl;

var History = require('../components/sections/History');

var HistoryActions = require('../actions/HistoryActions');

var { getDeviceByKey, getDeviceTypeByKey, getDefaultDevice } = require('../utils/device');
var timeUtil = require('../utils/time');


function mapStateToProps(state, ownProps) {
  const defaultDevice = getDefaultDevice(state.user.profile.devices);
  const deviceKey = defaultDevice?defaultDevice.deviceKey:null;

  return {
    time: state.section.history.time,
    devType: getDeviceTypeByKey(state.user.profile.devices, state.section.history.activeDevice), 
    metricFilter: state.section.history.filter,
    timeFilter: state.section.history.timeFilter,
    devices: state.user.profile.devices?state.user.profile.devices:[],
    activeDeviceId: state.section.history.activeDevice,
    defaultDevice: deviceKey,
    };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(HistoryActions, dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  return Object.assign(
    {}, 
    ownProps, 
    dispatchProps,
    Object.assign({}, 
                  stateProps, 
                  { 
                    nextPeriod: timeUtil.getNextPeriod(stateProps.timeFilter, stateProps.time.startDate), 
                    previousPeriod: timeUtil.getPreviousPeriod(stateProps.timeFilter, stateProps.time.endDate),
                    activeDevice: getDeviceByKey(stateProps.devices, stateProps.activeDeviceId),
                  }
                 )
  );
}

var HistoryData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(History);
HistoryData = injectIntl(HistoryData);
module.exports = HistoryData;
