
var _ = require('lodash');
var moment = require('moment');
var React = require('react');

var {computeKey} = require('../../reports').measurements;

var {DayView, WeekView, MonthView} = require('./unit-view');

var PropTypes = React.PropTypes;
var {seriesPropType, timespanPropType} = require('../../prop-types');

var reportPropType = PropTypes.shape({
  level: PropTypes.string.isRequired,
  reportName: PropTypes.string.isRequired,
  startsAt: PropTypes.string.isRequired,
  duration: PropTypes.array.isRequired,
});    

class BaseReport extends React.Component {
  
  static computeTimespan(reportConfig, now) {
    // Get a timespan (range) suitable for a given report configuration 
    var {startsAt, duration} = reportConfig;
  
    now = moment(now);
    var t0 = moment(now).startOf(startsAt);
    var t1 = moment(t0).add(...duration);
  
    t0 = t0.valueOf(); 
    t1 = t1.valueOf();
    return (t0 < t1)? [t0, t1] : [t1, t0];
  }
  
  static get reportKey() {
    // Distinguish reports generated from this class
    console.assert(false, 'Must be implemented in subclass!');
  }
  
  // Helpers for Redux mappers
  
  static mapStateToProps(state, ownProps) {
    // A default implementation for a Redux state selector.
    
    var cls = this;
    var {field, reports} = ownProps;
    
    var _state = state.reports.measurements;
    
    var series = null;
    if (_.isEmpty(_state))
      return {series};

    series = _.mapValues(reports, (r, k) => {
      k = computeKey(field, r.level, r.reportName, [cls.reportKey, k]);
      return (k in _state)? _.first(_state[k].series) : null;
    });
    return {series};
  }
  
  static mapDispatchToProps(dispatch, ownProps) {
    // A default implementation for a Redux dispatch mapper.
    // You probably want to override!

    var actions = require('../../actions/reports-measurements');
    
    var cls = this;
    var {field} = ownProps;
    
    return {
      initializeReport: (level, reportName, key) => {
        var kp = [cls.reportKey, key];
        return dispatch(actions.initialize(field, level, reportName, kp));
      },
      refreshReportData: (level, reportName, key, timespan) => {
        var kp = [cls.reportKey, key];
        dispatch(actions.setTimespan(field, level, reportName, kp, timespan));
        return dispatch(actions.refreshData(field, level, reportName, kp));
      },
    };
  }

  // Lifecycle instance methods

  constructor(props) {
    super(props);
    this.state = {refreshAt: null};
  }

  componentDidMount() {
    this._initialize();
    if (this.props.visible) {
      this._refreshData();
    }
  }

  componentDidUpdate() {
    if (this.props.visible) {
      if (this.state.refreshAt == null || (this.props.now != this.state.refreshAt)) {
        this._refreshData();
      }
    }
  }
  
  render() {
    var {field, uom, now, reports, series} = this.props;
    if (!series) series = {};

    var viewProps = {now, uom, field};
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
  }

  // Helper instance methods
  
  _initialize() {
    return _.mapValues(this.props.reports, (r, k) => (
      this.props.initializeReport(r.level, r.reportName, k)
    ));
  }
  
  _refreshData() {
    var cls = this.constructor;
    var {now, reports} = this.props;
    
    var p = _.mapValues(reports, (r, k) => {
      var ts = cls.computeTimespan(r, now);
      return this.props.refreshReportData(r.level, r.reportName, k, ts);
    });
    
    this.setState({refreshAt: now});
    return Promise.all(_.values(p));
  }
}

BaseReport.displayName = 'Overview.BaseReport';

BaseReport.defaultProps = {
  field: 'volume',
  uom: null,
  visible: true,
  reports: null,
  series: null,
};
  
BaseReport.propTypes = {
  field: PropTypes.string.isRequired,
  uom: PropTypes.string.isRequired,
  now: PropTypes.number.isRequired,
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
};

module.exports = BaseReport;
