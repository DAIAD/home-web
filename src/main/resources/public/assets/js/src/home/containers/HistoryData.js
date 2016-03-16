var React = require('react');
var connect = require('react-redux').connect;
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

function mapDispatchToProps(dispatch, ownProps) {
  return {
    setQueryFilter: function(filter) {
      return dispatch(HistoryActions.setQueryFilter(filter));
    },
    setTimeFilter: function(filter) {
       return dispatch(HistoryActions.setTimeFilter(filter));
    },
    setActiveDevice: function (deviceKey) {
      dispatch(HistoryActions.setActiveDevice(deviceKey));
    },
    setActiveAndQuery: function(deviceKey) {
      dispatch(HistoryActions.setActiveDevice(deviceKey));
      return this.queryDeviceOrMeter(deviceKey, this.time);
    },
    setTimeAndQuery: function(time) {
      dispatch(HistoryActions.setTime(Object.assign({}, this.time, time)));
      return this.queryDeviceOrMeter(this.activeDevice, time);
    },  
    queryDevice: function(deviceKey, time) {
      return dispatch(HistoryActions.getDeviceSessions(deviceKey, time));
    },
    queryDeviceAndFetchAllSessions: function(deviceKey, time) {
      return this.queryDevice(deviceKey, time)
        .then((response) => dispatch(HistoryActions.getAllDeviceSessions(deviceKey, time)));
    },
    queryMeter: function(deviceKey, time) {
      return dispatch(HistoryActions.getMeterHistory(deviceKey, time));
    },
    queryDeviceOrMeter: function(deviceKey, time) {
      const devType = getDeviceTypeByKey(this.devices, deviceKey);
      if (devType === 'AMPHIRO') {
        this.queryDevice(deviceKey, time); 
      }
      else if (devType === 'METER') {
        return this.queryMeter(deviceKey, time)
          .then(() => this.setQueryFilter('volume'));
      }
    }
  };
}


HistoryData = connect(mapStateToProps, mapDispatchToProps)(HistoryData);
HistoryData = injectIntl(HistoryData);
module.exports = HistoryData;
