
var React = require('react');
var ReactRedux = require('react-redux');

var {computeKey} = require('../../reports').measurements;
var Chart = require('./chart');

module.exports = ReactRedux.connect(
  (state, ownProps) => {
    var {field, level, reportName, reportKey} = ownProps;
    var _state = state.reports.measurements;
    var key = computeKey(field, level, reportName, reportKey); 
    return !(key in _state)? {} : 
      _.pick(_state[key], ['finished', 'series']);
  },
  null
)(Chart);

