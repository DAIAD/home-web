var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var SessionsChart = require('../components/SessionsChart');

var HistoryActions = require('../actions/HistoryActions');

var { selectTimeFormatter } = require('../utils/time');
var { getFilteredData } = require('../utils/chart');


var HistoryChart = React.createClass({
  componentWillMount: function() {
    //this.props.section.history.essions(this.props.activeDevice, this.props.time);
  },
  render: function() {
    return (
      <SessionsChart {...this.props} />
    );
  }
});

function mapStateToProps(state, ownProps) {
  if(!state.user.isAuthenticated) {
    return {};
  }
  
  return {
    time: state.section.history.time,
    filter: state.section.history.filter,
    timeFilter: state.section.history.timeFilter,
    data: [{title:state.section.history.filter, data:getFilteredData(state.section.history.data, state.section.history.filter)}],
    xMin: state.section.history.time.startDate,
    xMax: state.section.history.time.endDate,
    yMargin: 0,
    fontSize: 13,
    type: (state.section.history.filter==='showers')?'bar':'line',
    formatter: selectTimeFormatter(state.section.history.timeFilter, ownProps.intl),
    };
}

HistoryChart = connect(mapStateToProps)(HistoryChart);
HistoryChart = injectIntl(HistoryChart);
module.exports = HistoryChart;
