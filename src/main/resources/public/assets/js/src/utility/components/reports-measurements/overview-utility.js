
var _ = require('lodash');
var moment = require('moment');
var React = require('react');
var ReactRedux = require('react-redux');

var {computeKey} = require('../../reports').measurements;

var PropTypes = React.PropTypes;
var {seriesPropType, timespanPropType} = require('../../prop-types');

var reportPropType = PropTypes.shape({
  level: PropTypes.string,
  reportName: PropTypes.string,
  startsAt: PropTypes.string,
  duration: PropTypes.array,
});    

const FIELD = 'volume';

const REPORT_KEY = 'overview-utility';

//
// Presentational components
//

var UtilityView = React.createClass({
  displayName: 'UtilityView',

  propTypes: {
    now: PropTypes.number,
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
      reports: null,
      series: null,
    };
  },
  
  getInitialState: function () {
    return {}; 
  },

  componentDidMount: function () {
    this.props.initialize();
    this.props.refreshData();
  },

  render: function () {
    var {DayView, WeekView, MonthView} = require('./unit-view');
    var {now, reports, series} = this.props;
    var {config} = this.context;
    var {unit: uom} = config.reports.byType.measurements.fields[FIELD];
    
    var viewProps = {now, uom};

    return (
      <div className="overview-utility">
        
        <h3>Last Day</h3>
        <DayView {...viewProps} 
          level={reports.day.level}
          startsAt={reports.day.startsAt}
          duration={reports.day.duration}
          series={series.day} 
         />

        <h3>Last Week</h3>
        <WeekView {...viewProps} 
          level={reports.week.level}
          startsAt={reports.week.startsAt}
          duration={reports.week.duration}
          series={series.week} 
         />

        <h3>Last Month</h3>
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

var actions = require('../../actions/reports-measurements');

UtilityView = ReactRedux.connect(
  (state, ownProps) => {
    var _state = state.reports.measurements;
    var {reports} = state.config.overview.sections.utility;
    var series = _.mapValues(reports, 
      (u, k) => (
        k = computeKey(FIELD, u.level, u.reportName, [REPORT_KEY, k]),
        (k in _state)? _.first(_state[k].series) : null
      )
    );
    return {reports, series};
  }, 
  (dispatch, ownProps) => ({
    initializeReport: (level, reportName, key, defaults) => (
      dispatch(actions.initialize(FIELD, level, reportName, [REPORT_KEY, key], defaults))
    ),
    refreshReportData: (level, reportName, key) => (
      dispatch(actions.refreshData(FIELD, level, reportName, [REPORT_KEY, key]))
    ),
  }),
  (stateProps, dispatchProps, ownProps) => {
    // Provide convenience functions to initialize/refresh all reports

    var initialize = () => {
      var now = moment(ownProps.now);
      return _.mapValues(stateProps.reports, (u, k) => { 
        var t0 = now.clone().startOf(u.startsAt);
        var t1 = t0.clone().add(...u.duration);
        t0 = t0.valueOf(); 
        t1 = t1.valueOf();
        return dispatchProps.initializeReport(u.level, u.reportName, k, {
          timespan: (t0 < t1)? [t0, t1] : [t1, t0]
        });
      });
    };
    
    var refreshData = () => {
      var p = _.mapValues(stateProps.reports, (u, k) => (
        dispatchProps.refreshReportData(u.level, u.reportName, k)
      ));
      return Promise.all(_.values(p));
    };
    
    return _.extend({}, ownProps, stateProps, dispatchProps, {initialize, refreshData});
  }
)(UtilityView);

// Export

module.exports = UtilityView;
