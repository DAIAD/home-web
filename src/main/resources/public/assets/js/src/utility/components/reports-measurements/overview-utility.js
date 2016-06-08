
var _ = require('lodash');
var moment = require('moment');
var React = require('react');
var ReactRedux = require('react-redux');
var Bootstrap = require('react-bootstrap');

var {computeKey} = require('../../reports').measurements;

var PropTypes = React.PropTypes;
var {seriesPropType, timespanPropType} = require('../../prop-types');

var reportPropType = PropTypes.shape({
  level: PropTypes.string.isRequired,
  reportName: PropTypes.string.isRequired,
  startsAt: PropTypes.string.isRequired,
  duration: PropTypes.array.isRequired,
});    

const FIELD = 'volume';

const REPORT_KEY = 'overview-utility';

var getTimespanForReport = function (reportConfig, now) {
  // Get a timespan (range) suitable for this report configuration 
  var {startsAt, duration} = reportConfig;
  
  now = moment(now);
  var t0 = moment(now).startOf(startsAt);
  var t1 = moment(t0).add(...duration);
  
  t0 = t0.valueOf(); 
  t1 = t1.valueOf();
  return (t0 < t1)? [t0, t1] : [t1, t0];
};

//
// Presentational components
//

var UtilityView = React.createClass({
  displayName: 'UtilityView',

  propTypes: {
    now: PropTypes.number,
    visible: PropTypes.bool,
    reports: PropTypes.shape({
      day: reportPropType,
      week: reportPropType,
      month: reportPropType,
      year: reportPropType,
    }),
    series: PropTypes.shape({
      day: seriesPropType,
      week: seriesPropType,
      month: seriesPropType,
      year: seriesPropType,
    }),
    initializeReport: PropTypes.func, // specific report
    initialize: PropTypes.func, // all reports
    refreshReportData: PropTypes.func, // specific report
    refreshData: PropTypes.func, // all reports
  },

  contextTypes: {config: PropTypes.object},
  
  getDefaultProps: function () {
    return {
      visible: true,
      reports: null,
      series: null,
    };
  },
  
  getInitialState: function () {
    return {
      refreshAt: null,
    }; 
  },

  componentDidMount: function () {
    this.props.initialize();
    if (this.props.visible) {
      this.setState({refreshAt: this.props.now});
      this.props.refreshData(this.props.now);
    }
  },

  componentWillReceiveProps: function (nextProps) {
    if (nextProps.visible) {
      if (this.state.refreshAt == null || (nextProps.now != this.state.refreshAt)) {
        this.setState({refreshAt: nextProps.now});
        nextProps.refreshData(nextProps.now);
      }
    }
  },
  
  render: function () {
    var {DayView, WeekView, MonthView} = require('./unit-view');
    var {now, reports, series} = this.props;
    var {config} = this.context;
    var {unit: uom} = config.reports.byType.measurements.fields[FIELD];
    
    var viewProps = {now, uom};
    return (
      <div>
        <h4>Last Day</h4>
        <DayView {...viewProps} 
          level={reports.day.level}
          startsAt={reports.day.startsAt}
          duration={reports.day.duration}
          series={series.day} 
         />
        
        <h4>Last Week</h4>
        <WeekView {...viewProps} 
          level={reports.week.level}
          startsAt={reports.week.startsAt}
          duration={reports.week.duration}
          series={series.week} 
         />

        <h4>Last Month</h4>
        <MonthView {...viewProps}
          level={reports.month.level}
          startsAt={reports.month.startsAt}
          duration={reports.month.duration}
          series={series.month} 
         />
      </div>
    );
  },
}); 

//
// Container components
//

var {connect} = ReactRedux;
var actions = require('../../actions/reports-measurements');

var mapStateToProps = function (state, ownProps) {
  var _state = state.reports.measurements;
  var {reports} = state.config.overview;
  var series = _.mapValues(reports, (r, k) => {
    k = computeKey(FIELD, r.level, r.reportName, [REPORT_KEY, k]);
    return (k in _state)? _.first(_state[k].series) : null;
  });
  return {reports, series};
};

var mapDispatchToProps = function (dispatch, ownProps) {
  return {
    initializeReport: (level, reportName, key) => {
      var kp = [REPORT_KEY, key];
      return dispatch(actions.initialize(FIELD, level, reportName, kp));
    },
    refreshReportData: (level, reportName, key, timespan) => {
      var kp = [REPORT_KEY, key];
      dispatch(actions.setTimespan(FIELD, level, reportName, kp, timespan));
      return dispatch(actions.refreshData(FIELD, level, reportName, kp));
    },
  };
};

var mergeProps = function (stateProps, dispatchProps, ownProps) {
  // Provide convenience functions to initialize/refresh all reports

  var initialize = () => {
    return _.mapValues(stateProps.reports, (r, k) => (
      dispatchProps.initializeReport(r.level, r.reportName, k)
    ));
  };
  
  var refreshData = (now) => {
    var p = _.mapValues(stateProps.reports, (r, k) => {
      var ts = getTimespanForReport(r, now);
      return dispatchProps.refreshReportData(r.level, r.reportName, k, ts);
    });
    return Promise.all(_.values(p));
  };
  
  return _.extend({}, ownProps, stateProps, dispatchProps, {initialize, refreshData});
};

UtilityView = connect(mapStateToProps, mapDispatchToProps, mergeProps)(UtilityView);

// Export

module.exports = UtilityView;
