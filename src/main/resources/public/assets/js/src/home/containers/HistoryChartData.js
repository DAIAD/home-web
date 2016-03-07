var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;

var SessionsChart = require('../components/SessionsChart');

var HistoryActions = require('../actions/HistoryActions');

var timeUtil = require('../utils/time');
var { getFilteredData } = require('../utils/chart');


var selectTimeFormatter = function(key, intl) {
  switch (key) {
    case "always":
      return (x) => intl.formatDate(x);
    case "year":
      return (x) => intl.formatDate(x, { day: 'numeric', month: 'long', year: 'numeric' });
    case "month":
      return (x) => intl.formatDate(x, { day: 'numeric', month: 'short' });
    case "week":
      return (x) => intl.formatMessage({id: "weekdays." + (new Date(x).getDay()+1).toString()});
      //intl.formatDate((x), { day: 'numeric' });
    case "day":
      return (x) => intl.formatTime(x, { hour: 'numeric', minute: 'numeric'});
    default:
      return (x) => intl.formatDate(x);
  }
};


var HistoryChart = React.createClass({
  componentWillMount: function() {
    //this.props.querySessions(this.props.activeDevice, this.props.time);
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
    time: state.query.time,
    filter: state.section.history.filter,
    timeFilter: state.section.history.timeFilter,
    data: [{title:state.section.history.filter, data:getFilteredData(state.query.data, state.section.history.filter)}],
    xMin: state.query.time.startDate,
    xMax: state.query.time.endDate,
    fontSize: 13,
    type: (state.section.history.filter==='showers')?'bar':'line',
    formatter: selectTimeFormatter(state.section.history.timeFilter, ownProps.intl),
    };
}

function mapDispatchToProps(dispatch, ownProps) {
  return {
    setQueryFilter: function(filter) {
      return dispatch(HistoryActions.setQueryFilter(filter));
    },
  };
}
 
HistoryChart = connect(mapStateToProps, mapDispatchToProps)(HistoryChart);
HistoryChart = injectIntl(HistoryChart);
module.exports = HistoryChart;
