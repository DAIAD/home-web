
var React = require('react');
var {connect} = require('react-redux');
var {PropTypes} = React;

var {computeKey} = require('../../reports').measurements;
var Chart = require('./chart');

var _Chart = connect(
  (state, ownProps) => {
    var {field, level, reportName, reportKey} = ownProps;
    var _state = state.reports.measurements;
    var key = computeKey(field, level, reportName, reportKey); 
    return !(key in _state)? {} : 
      _.pick(_state[key], ['finished', 'series']);
  },
  null
)(Chart);

_Chart.propTypes.reportKey = PropTypes.string.isRequired;

module.exports = _Chart;

