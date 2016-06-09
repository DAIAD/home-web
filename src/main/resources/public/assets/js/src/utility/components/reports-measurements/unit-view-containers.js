
var _ = require('lodash');
var moment = require('moment');
var React = require('react');
var {connect} = require('react-redux');

var {computeKey} = require('../../reports').measurements;

var {ViewByDay, ViewByWeek, ViewByMonth} = require('./unit-views');

var PropTypes = React.PropTypes;
var {seriesPropType, populationPropType} = require('../../prop-types');

var reportPropType = PropTypes.shape({
  level: PropTypes.string.isRequired,
  reportName: PropTypes.string.isRequired,
  startsAt: PropTypes.string.isRequired,
  duration: PropTypes.array.isRequired,
});    

var actions = require('../../actions/reports-measurements');

var computeTimeRange = function(duration, startsAt, now) {
  // Get a time range suitable for a given report configuration
  now = moment(now);
  var t0 = moment(now).startOf(startsAt);
  var t1 = moment(t0).add(...duration);
  t0 = t0.valueOf(); 
  t1 = t1.valueOf();
  return (t0 < t1)? [t0, t1] : [t1, t0];
}

class _Report extends React.Component {

  static get presentationalComponent() {
    return null; // override!
  }
  
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
    var {views, field, uom, now, report, source, target, series} = this.props;
    
    var View = this.constructor.presentationalComponent;
    
    var viewProps = {
      views,
      now,
      uom, 
      field, 
      series,
      target,
      source,
      level: report.level,
      duration: report.duration,
      startsAt: report.startsAt,
    };
    
    return (
      <View {...viewProps}/>
    );
  }

  // Helper instance methods
  
  _initialize() {
    return this.props.initialize();
  }
  
  _refreshData() {
    var {now} = this.props;
    this.setState({refreshAt: now});
    return this.props.refreshData();
  }
}

_Report.defaultProps = {
  views: ['summary', 'simple-chart'],
  field: 'volume',
  uom: null,
  reportKey: 'overview',
  visible: true,
  report: null,
  source: 'meter',
  target: null,
  series: null,
};
  
_Report.propTypes = {
  field: PropTypes.string.isRequired,
  uom: PropTypes.string.isRequired,
  now: PropTypes.number.isRequired,
  reportKey: PropTypes.string.isRequired,
  visible: PropTypes.bool,
  report: reportPropType,
  target: populationPropType,
  source: PropTypes.string,
  // Injected from container 
  series: seriesPropType,
  initialize: PropTypes.func,
  refreshData: PropTypes.func,
};

class ReportByDay extends _Report {
  static get presentationalComponent() {
    return ViewByDay;
  }
}

class ReportByWeek extends _Report {
  static get presentationalComponent() {
    return ViewByWeek;
  }
}

class ReportByMonth extends _Report {
  static get presentationalComponent() {
    return ViewByMonth;
  }
}

var mapStateToProps = function (state, ownProps) {
  // A default implementation for a Redux state selector.
  var {field, reportKey, report: {duration, level, reportName}} = ownProps;
  var [n, unit] = duration;

  var _state = state.reports.measurements;
  var series = null;
  if (!_.isEmpty(_state)) {
    var k = computeKey(field, level, reportName, [reportKey, unit]);
    series = (k in _state)? _.first(_state[k].series) : null;
  }
  return {series};
}

var mapDispatchToProps = function(dispatch, ownProps) {
  // A default implementation for a Redux dispatch mapper.

  var {field, reportKey, report: {startsAt, duration, level, reportName}} = ownProps;
  var {now, source, target} = ownProps;
  
  var ts = computeTimeRange(duration, startsAt, now);
  var [n, unit] = duration;
  var reportArgs = [field, level, reportName, [reportKey, unit]];
  
  var initialize = () => (dispatch(actions.initialize(...reportArgs)));

  var refreshData = () => {
    dispatch(actions.setTimespan(...reportArgs, ts));
    if (target)
      dispatch(actions.setPopulation(...reportArgs, target));
    if (source)
      dispatch(actions.setSource(...reportArgs, source));
    return dispatch(actions.refreshData(...reportArgs));   
  };

  return {initialize, refreshData};
}

module.exports = {
  ReportByDay: connect(mapStateToProps, mapDispatchToProps)(ReportByDay),
  ReportByWeek: connect(mapStateToProps, mapDispatchToProps)(ReportByWeek),
  ReportByMonth: connect(mapStateToProps, mapDispatchToProps)(ReportByMonth),
} 

