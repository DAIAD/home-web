var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var injectIntl = require('react-intl').injectIntl;

var { getChartDataByFilter } = require('../utils/chart');

//var SessionsList = require('../components/SessionsList');
var SessionModal = require('../components/Session');
var HistoryActions = require('../actions/HistoryActions');

function mapStateToProps(state, ownProps) {
  return {
    //time: state.section.history.time,
    activeSessionFilter: state.section.history.activeSessionFilter,
    activeSessionIndex: state.section.history.activeSessionIndex,
    activeSessionId: state.section.history.activeSessionId,
    };
}
function mapDispatchToProps (dispatch) {
  return bindActionCreators(HistoryActions, dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  const data = ownProps.sessions?(stateProps.activeSessionIndex!==null?ownProps.sessions[stateProps.activeSessionIndex]:{}):{};
  
  return Object.assign(
    {}, 
    ownProps, 
    dispatchProps,
    Object.assign({}, 
                  stateProps, 
                  {
                    data,
                    chartData: getChartDataByFilter(data?data.measurements:[], stateProps.activeSessionFilter),
                    showModal: stateProps.activeSessionIndex===null?false:true,
                  })
  );
}

var SessionData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(SessionModal);
SessionData = injectIntl(SessionData);
module.exports = SessionData;
