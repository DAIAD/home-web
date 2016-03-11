var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var History = require('../components/sections/History');

var DeviceActions = require('../actions/DeviceActions');
var MeterActions = require('../actions/MeterActions');
var HistoryActions = require('../actions/HistoryActions');

var { getAvailableDevices, getDeviceTypeByKey } = require('../utils/device');
var timeUtil = require('../utils/time');

var HistoryData = React.createClass({
  componentWillMount: function() {
    if (this.props.activeDevice) {
      this.props.queryAndFetchAllSessions(this.props.activeDevice, this.props.time);
    }
  },
  componentWillReceiveProps: function(nextProps) {
    if (!this.props.activeDevice && nextProps.activeDevice) {
      this.props.queryAndFetchAllSessions(nextProps.activeDevice, this.props.time);
    }
  },
  render: function() {
    return (
      <History {...this.props} />
    );
  }
});

function mapStateToProps(state, ownProps) {
  return {
    time: state.query.time,
    devType: getDeviceTypeByKey(state.user.profile.devices, state.query.activeDevice), 
    metricFilter: state.section.history.filter,
    timeFilter: state.section.history.timeFilter,
    devices: state.user.profile.devices,
    activeDevice: state.query.activeDevice,
    nextPeriod: timeUtil.getNextPeriod(state.section.history.timeFilter, state.query.time.endDate),
    previousPeriod: timeUtil.getPreviousPeriod(state.section.history.timeFilter, state.query.time.endDate),
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
    setActiveAndQuery: function(deviceKey) {
      dispatch(DeviceActions.setActiveDevice(deviceKey));
      return this.queryDeviceOrMeter(deviceKey, this.time);
    },
    setTimeAndQuery: function(time) {
      dispatch(DeviceActions.setTime(Object.assign({}, this.time, time)));
      return this.queryDeviceOrMeter(this.activeDevice, time);
    },  
    querySessions: function(deviceKey, time) {
      return dispatch(DeviceActions.querySessions(deviceKey, time));
    },
    fetchAllSessions: function(deviceKey, time) {
      return dispatch(DeviceActions.fetchAllSessions(deviceKey, time));
    },
    queryAndFetchAllSessions: function(deviceKey, time) {
      return this.querySessions(deviceKey, time)
        .then((response) => this.fetchAllSessions(deviceKey, time));
    },
    queryMeter: function(deviceKey, time) {
      return dispatch(MeterActions.getHistory(deviceKey, time));
    },
    queryDeviceOrMeter: function(deviceKey, time) {
      const devType = getDeviceTypeByKey(this.devices, deviceKey);
      if (devType === 'AMPHIRO') {
        this.queryAndFetchAllSessions(deviceKey, time); 
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
