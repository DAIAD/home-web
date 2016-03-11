var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var SessionsList = require('../components/SessionsList');

var DeviceActions = require('../actions/DeviceActions');
var HistoryActions = require('../actions/HistoryActions');

var { getSessionByIndex } = require('../utils/device');
var { getFilteredData } = require('../utils/chart');
var { getFriendlyDuration, getEnergyClass } = require('../utils/general');

var HistoryList = React.createClass({
  componentWillMount: function() {
    //this.props.fetchSessionsIfNeeded(this.props.activeDevice, this.props.time);
  },
  render: function() {
    return (
      <SessionsList {...this.props} />
    );
  }
});

function mapStateToProps(state, ownProps) {
  let disabledNextSession = true;
  let disabledPreviousSession = true;
  if (state.section.history.activeSessionIndex!==null) {
    if (state.query.data[state.section.history.activeSessionIndex+1]) {
      disabledNextSession = false;
    }
    if (state.query.data[state.section.history.activeSessionIndex-1]) {
      disabledPreviousSession = false;
    }
  }

  return {
    time: state.query.time,
    activeDevice: state.query.activeDevice,
    sessionFilter: state.section.history.sessionFilter,
    sessions: state.query.data.map((session) => Object.assign({}, session, {duration:getFriendlyDuration(session.duration)}, {energyClass:getEnergyClass(session.energy)}, {measurements: getFilteredData(session.measurements, state.section.history.sessionFilter)})),
    activeSessionIndex: state.section.history.activeSessionIndex,
    disabledNext: disabledNextSession,
    disabledPrevious: disabledPreviousSession,
    showModal: state.section.history.activeSessionIndex===null?false:true,
    };
}

function mapDispatchToProps(dispatch, ownProps) {
  return {
    setActiveSessionIndex: function(sessionIndex) {
      dispatch(HistoryActions.setActiveSessionIndex(sessionIndex));
    },
    resetActiveSessionIndex: function() {
      dispatch(HistoryActions.resetActiveSessionIndex());
    },
    setSessionFilter: function(filter) {
      dispatch(HistoryActions.setSessionFilter(filter));
    },
    fetchSession: function(sessionId, deviceKey, time) {
      dispatch(DeviceActions.fetchSession(sessionId, deviceKey, time));
    },
    getNextSession: function(deviceKey, time) {
      dispatch(HistoryActions.increaseActiveSessionIndex());
      dispatch(DeviceActions.fetchActiveSession(deviceKey, time));
    },
    getPreviousSession: function(deviceKey, time) {
      dispatch(HistoryActions.decreaseActiveSessionIndex());
      dispatch(DeviceActions.fetchActiveSession(deviceKey, time));
    }
  };
}


HistoryList = connect(mapStateToProps, mapDispatchToProps)(HistoryList);
HistoryList = injectIntl(HistoryList);
module.exports = HistoryList;
