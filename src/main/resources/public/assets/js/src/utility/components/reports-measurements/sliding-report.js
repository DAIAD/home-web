var _ = require('lodash');
var moment = require('moment');
var sprintf = require('sprintf');

var React = require('react');
var Bootstrap = require('react-bootstrap');
var {connect} = require('react-redux');
var {PropTypes} = React;

var population = require('../../model/population');
var {computeKey} = require('../../reports').measurements;
var {seriesPropType, populationPropType, reportPropType, configPropType} = require('../../prop-types');
var Chart = require('./chart-container');

// A simple sliding report (on "now")
// If "now" is updated, it dispatches the appropriate actions to 
// refresh data of the examined report.

var Report = React.createClass({
  statics: {
    defaults: {
    },

    _assertProps: function (props, nextProps) {
      // A sanity check: we only expext "now" to change
      var b = (
        (props.source == nextProps.source) &&
        (props.field == nextProps.field) && 
        (props.reportKey == nextProps.reportKey) &&
        (props.report == nextProps.report) &&
        (
          (props.target == null && nextProps.target == null) || 
          (props.target.toString() == nextProps.target.toString()) 
        )
      );
      console.assert(b, 'Unexpected change for props of <Report>! ');
    },
  },
  
  propTypes: {
    now: PropTypes.number.isRequired, // indicates the period under examination
    field:  PropTypes.string.isRequired,
    source: PropTypes.string.isRequired,
    report: reportPropType,
    target: populationPropType,
    reportKey: PropTypes.string.isRequired,
  },

  contextTypes: {config: configPropType},

  getDefaultProps: function () {
    return {};
  },
 
  componentDidMount: function () {
    this.props.initialize();
    setTimeout(this.props.refreshData, _.random(100, 900));
  },
  
  componentWillReceiveProps: function (nextProps) {
    this.constructor._assertProps(this.props, nextProps);
    nextProps.initialize(); // this is a noop, since only "now" is updated
    setTimeout(nextProps.refreshData, _.random(100, 900));
  },

  render: function () {
    var {now, field, report: {level, reportName}, reportKey} = this.props;
    var chartProps = {field, level, reportName, reportKey};    
    return (
      <Chart {...chartProps} /> 
    );
  },
});

Report.displayName = 'SlidingReport';

var actions = require('../../actions/reports-measurements');

Report = connect(
  (state, ownProps) => {
    return {};
  },
  (dispatch, ownProps) => {
    var {now, field, source, reportKey, target} = ownProps;
    var {report: {level, startsAt, reportName, duration}} = ownProps;
    
    var t0 = moment(now).utc().startOf(startsAt);
    var t1 = moment(t0).utc().add(...duration); 
    t1.add(1, level); // to use as closure point
    t0 = t0.valueOf();
    t1 = t1.valueOf();
    var timespan = (t0 < t1)? [t0, t1] : [t1, t0];
    
    var initialize = function () {
      dispatch(actions.initialize(
        field, level, reportName, reportKey, {
          timespan, 
          source, 
          population: target,
        }
      ));
    };
    
    var refreshData = function () {
      dispatch(actions.setTimespan(
        field, level, reportName, reportKey, timespan
      ));
      dispatch(actions.refreshData(
        field, level, reportName, reportKey
      ));
    };

    return {initialize, refreshData};
  }
)(Report);


module.exports = Report;
