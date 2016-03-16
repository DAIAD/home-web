var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var SessionsList = require('../components/SessionsList');

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
    if (state.section.history.data[state.section.history.activeSessionIndex+1]) {
      disabledNextSession = false;
    }
    if (state.section.history.data[state.section.history.activeSessionIndex-1]) {
      disabledPreviousSession = false;
    }
  }
  let sessions = state.section.history.data.map((session, idx, array) => Object.assign({}, session, {better:array[idx+1]?(session.volume<array[idx+1].volume?true:false):null}, {duration:getFriendlyDuration(session.duration)}, {energyClass:getEnergyClass(session.energy)}, {measurements: getFilteredData(session.measurements, state.section.history.activeSessionFilter)}));
  return {
    time: state.section.history.time,
    activeDevice: state.section.history.activeDevice,
    activeSessionFilter: state.section.history.activeSessionFilter,
    sessions: sessions,
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
    getActiveSession: function (deviceKey, time) {
      dispatch(HistoryActions.getActiveSession(deviceKey, time));
    },
    getNextSession: function(deviceKey, time) {
      dispatch(HistoryActions.increaseActiveSessionIndex());
      dispatch(HistoryActions.getActiveSession(deviceKey, time));
    },
    getPreviousSession: function(deviceKey, time) {
      dispatch(HistoryActions.decreaseActiveSessionIndex());
      dispatch(HistoryActions.getActiveSession(deviceKey, time));
    }
  };
}


HistoryList = connect(mapStateToProps, mapDispatchToProps)(HistoryList);
HistoryList = injectIntl(HistoryList);
module.exports = HistoryList;
