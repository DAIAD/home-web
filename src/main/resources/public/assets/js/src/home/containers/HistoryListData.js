var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var injectIntl = require('react-intl').injectIntl;

var SessionsList = require('../components/SessionsList');

var HistoryActions = require('../actions/HistoryActions');

var { getSessionByIndex,  getReducedDeviceType, getDeviceNameByKey, getDeviceTypeByKey, reduceSessions, reduceMetric } = require('../utils/device');
var { getFilteredData } = require('../utils/chart');
var { getFriendlyDuration, getEnergyClass } = require('../utils/general');


function mapStateToProps(state, ownProps) {
  return {
    time: state.section.history.time,
    activeDevice: state.section.history.activeDevice,
    activeSessionFilter: state.section.history.activeSessionFilter,
    data: state.section.history.data,
    metricFilter: state.section.history.filter,
    //activeSessionIndex: state.section.history.activeSessionIndex,
    devices: state.user.profile.devices,
    //devType: getReducedDeviceType(state.user.profile.devices, state.section.history.activeDevice), 
    };
}
function mapDispatchToProps (dispatch) {
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
                    reducedMetric: reduceMetric(stateProps.devices, stateProps.data, stateProps.metricFilter),
                    sessions: reduceSessions(stateProps.devices, stateProps.data)
                    .map((session, idx, array) => Object.assign({}, session, 
                                 {
                                   next:array[idx+1]?[array[idx+1].id, array[idx+1].device]:null,
                                   prev:array[idx-1]?[array[idx-1].id, array[idx-1].device]:null
                                 },
                                 {
                                   devName:getDeviceNameByKey(stateProps.devices, session.device),
                                   devType: getDeviceTypeByKey(stateProps.devices, session.device),
                                   better:array[idx-1]?(array[idx].volume<=array[idx-1].volume?true:false):null,
                                   duration:getFriendlyDuration(session.duration), 
                                   energyClass:getEnergyClass(session.energy), 
                                   chartData: getFilteredData(session.measurements, stateProps.activeSessionFilter),

                                   hasChartData: session.measurements?true:false 
                                 }
                            ))
                  }));
}

var HistoryList = connect(mapStateToProps, mapDispatchToProps, mergeProps)(SessionsList);
HistoryList = injectIntl(HistoryList);
module.exports = HistoryList;
