var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var injectIntl = require('react-intl').injectIntl;

var History = require('../components/sections/History');

var HistoryActions = require('../actions/HistoryActions');

var { getDeviceTypeByKey, getDefaultDevice } = require('../utils/device');
var timeUtil = require('../utils/time');

var HistoryData = React.createClass({
  componentWillMount: function() {
    //if (this.props.activeDevice) {
      //this.props.queryAndFetchAllSessions(this.props.activeDevice, this.props.time);
    const device = this.props.activeDevice || this.props.defaultDevice;
    if (!this.props.activeDevice) {
      this.props.setActiveDevice(this.props.defaultDevice);
    }
    this.props.queryDeviceOrMeter(device, this.props.time);
      //}
  },
  componentWillReceiveProps: function(nextProps) {
    //if (!this.props.activeDevice && nextProps.activeDevice) {
    ////this.props.queryAndFetchAllSessions(nextProps.activeDevice, this.props.time);
    // this.props.queryDeviceSessions(nextProps.activeDevice, this.props.time);
    //}
  },

  render: function() {
    return (
      <History {...this.props} />
    );
  }
});

function mapStateToProps(state, ownProps) {
  const defaultDevice = getDefaultDevice(state.user.profile.devices);
  const deviceKey = defaultDevice?defaultDevice.deviceKey:null;

  return {
    time: state.section.history.time,
    devType: getDeviceTypeByKey(state.user.profile.devices, state.section.history.activeDevice), 
    metricFilter: state.section.history.filter,
    timeFilter: state.section.history.timeFilter,
    devices: state.user.profile.devices,
    activeDevice: state.section.history.activeDevice,
    defaultDevice: deviceKey,
    nextPeriod: timeUtil.getNextPeriod(state.section.history.timeFilter, state.section.history.time.endDate),
    previousPeriod: timeUtil.getPreviousPeriod(state.section.history.timeFilter, state.section.history.time.endDate),
    };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(HistoryActions, dispatch);
}

HistoryData = connect(mapStateToProps, mapDispatchToProps)(HistoryData);
HistoryData = injectIntl(HistoryData);
module.exports = HistoryData;
