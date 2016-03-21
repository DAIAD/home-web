var React = require('react');
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { injectIntl } = require('react-intl');
var { bindActionCreators } = require('redux');
var connect = require('react-redux').connect;
var FormattedMessage = require('react-intl').FormattedMessage;
var { push } = require('react-router-redux');

var Dashboard = require('../components/sections/Dashboard');

var HistoryActions = require('../actions/HistoryActions');
var DashboardActions = require('../actions/DashboardActions');

var timeUtil = require('../utils/time');
var { getDefaultDevice, getLastSession } = require('../utils/device');
var { getFilteredData } = require('../utils/chart');


function mapStateToProps(state, ownProps) {
  const lastSession = state.section.dashboard.lastSession;
  const defaultDevice = getDefaultDevice(state.user.profile.devices);
  const deviceKey = defaultDevice?defaultDevice.deviceKey:null;
  return {
    defaultDevice: deviceKey,
    firstname: state.user.profile.firstname,
    lastShower: lastSession,
  };
}

function mapDispatchToProps(dispatch) {
  return merged(bindActionCreators(DashboardActions, dispatch),
               {
                 linkToHistoryTest: function(options) {
                   dispatch(HistoryActions.resetActiveSessionIndex());
                   dispatch(HistoryActions.setActiveDevice(options.device));
                   dispatch(HistoryActions.setQueryFilter(options.filter));
                   dispatch(HistoryActions.setTimeFilter(options.timeFilter));
                   dispatch(HistoryActions.setTime(Object.assign({}, timeUtil.thisYear(), {granularity:4})));
                   dispatch(push('/history'));
                 }
               });
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  return merged(ownProps,
               dispatchProps,
               merged(stateProps,
                     {
                       lastSessionTime: merged(timeUtil.thisMonth(), {granularity: 0}),
                       chartFormatter: (intl => (x) => { console.log(intl); return intl.formatTime(x, { hour:'numeric', minute:'numeric', second:'numeric'}); }),
                       chartData: [{title:'Consumption', data:(stateProps.lastShower?(getFilteredData(stateProps.lastShower.measurements?stateProps.lastShower.measurements:[], 'volume')):[])}]
                     }));
}

function merged (...objects) {
  return Object.assign({}, ...objects);
}

var DashboardData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Dashboard);
DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
