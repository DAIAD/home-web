var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var injectIntl = require('react-intl').injectIntl;

var SessionsList = require('../components/SessionsList');

var HistoryActions = require('../actions/HistoryActions');

var { getSessionByIndex } = require('../utils/device');
var { getFilteredData } = require('../utils/chart');
var { getFriendlyDuration, getEnergyClass } = require('../utils/general');


function mapStateToProps(state, ownProps) {
  return {
    time: state.section.history.time,
    activeDevice: state.section.history.activeDevice,
    activeSessionFilter: state.section.history.activeSessionFilter,
    data: state.section.history.data,
    activeSessionIndex: state.section.history.activeSessionIndex,
    };
}
function mapDispatchToProps (dispatch) {
  return bindActionCreators(HistoryActions, dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {

  let disabledNext = true;
  let disabledPrevious = true;
  if (stateProps.activeSessionIndex!==null) {
    disabledNext = stateProps.data[stateProps.activeSessionIndex+1]?false:true;
    disabledPrevious = stateProps.data[stateProps.activeSessionIndex-1]?false:true;
  }
  return Object.assign(
    {}, 
    ownProps, 
    dispatchProps,
    Object.assign({}, 
                  stateProps, 
                  { 
                    sessions: stateProps.data.map(
                      (session, idx, array) => 
                        Object.assign({}, 
                                    session, 
                                    {better:array[idx+1]?(session.volume<array[idx+1].volume?true:false):null}, 
                                    {duration:getFriendlyDuration(session.duration)}, 
                                    {energyClass:getEnergyClass(session.energy)}, 
                                    {measurements: getFilteredData(session.measurements, stateProps.activeSessionFilter)}
                                   )),
                    showModal: stateProps.activeSessionIndex===null?false:true,
                    disabledNext,
                    disabledPrevious,
                  })
  );
}

var HistoryList = connect(mapStateToProps, mapDispatchToProps, mergeProps)(SessionsList);
HistoryList = injectIntl(HistoryList);
module.exports = HistoryList;
