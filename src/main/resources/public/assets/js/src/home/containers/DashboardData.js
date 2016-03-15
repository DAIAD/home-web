var React = require('react');
var bs = require('react-bootstrap');
var injectIntl = require('react-intl').injectIntl;
var connect = require('react-redux').connect;
var FormattedMessage = require('react-intl').FormattedMessage;
var { push } = require('react-router-redux');

var Dashboard = require('../components/sections/Dashboard');

var DeviceActions = require('../actions/DeviceActions');
var HistoryActions = require('../actions/HistoryActions');

var timeUtil = require('../utils/time');
var { getDefaultDevice, getLastSession } = require('../utils/device');
var { getFilteredData } = require('../utils/chart');

var DashboardData = React.createClass({
  
  componentWillMount: function() {
    if (this.props.defaultDevice) {
      this.props.getLastSession(this.props.defaultDevice);
    }
  },
  componentWillReceiveProps: function(nextProps) {
    if (!this.props.defaultDevice && nextProps.defaultDevice) {
      this.props.getLastSession(nextProps.defaultDevice);
    }
  },
  render: function() {
    return (
      <Dashboard {...this.props} />
    );
  }
});


function mapStateToProps(state, ownProps) {
  const lastSession = getLastSession(state.query.data);
  const defaultDevice = getDefaultDevice(state.user.profile.devices);
  const deviceKey = defaultDevice?defaultDevice.deviceKey:null;
  return {
    time: state.query.time,
    activeDevice: state.query.activeDevice,
    defaultDevice: deviceKey,
    firstname: state.user.profile.firstname,
    chartData: [{title:'Consumption', data:(lastSession?(getFilteredData(lastSession.measurements?lastSession.measurements:[], 'volume')):[])}],
    chartFormatter: (x) => ownProps.intl.formatTime(x, { hour:'numeric', minute:'numeric', second:'numeric'}),
    lastShower: lastSession,
  };
}

function mapDispatchToProps(dispatch) {
  return {
    linkToHistory: function(options) {
      dispatch(HistoryActions.setActiveSessionIndex(options.activeSessionIndex));
      dispatch(HistoryActions.setQueryFilter(options.filter));
      dispatch(HistoryActions.setTimeFilter(options.timeFilter));

      return dispatch(push('/history'));
    },
    getLastSession: function(deviceKey) {
      const time = Object.assign({}, timeUtil.thisMonth(), {granularity: 0});
      return dispatch(DeviceActions.querySessions(deviceKey, time))
        .then((response) => dispatch(DeviceActions.fetchLastSession(deviceKey, time)));
    }
  };
}

DashboardData = connect(mapStateToProps, mapDispatchToProps)(DashboardData);
DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
