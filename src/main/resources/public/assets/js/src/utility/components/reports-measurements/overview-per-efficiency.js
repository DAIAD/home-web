
var _ = require('lodash');
var React = require('react');
var ReactRedux = require('react-redux');
var echarts = require('react-echarts');

var {computeKey} = require('../../reports').measurements;

var PropTypes = React.PropTypes;
var {seriesPropType, timespanPropType} = require('../../prop-types');

var reportPropType = PropTypes.shape({
  level: PropTypes.string,
  name: PropTypes.string,
  series: seriesPropType,
  timespan: timespanPropType,
});    

const FIELD = 'volume';

const REPORT_KEY = 'overview-per-efficiency';

//
// Presentational component
//

var GroupPerEfficiencyView = React.createClass({
  displayName: 'GroupPerEfficiencyView',
  
  propTypes: {
    now: PropTypes.number,
  },
  
  contextTypes: {config: PropTypes.object},

  componentDidMount: function () {
    console.info('GroupPerEfficiencyView mounted');
  },

  render: function () {
    
    var {config} = this.context;
    var {unit: uom} = config.reports.byType.measurements.fields[FIELD];
    
    // Todo

    return (
      <div>Hello per-efficiency view!</div>
    );
  },

});

//
// Container component
//

var actions = require('../../actions/reports-measurements');

// Todo

// Export

module.exports = GroupPerEfficiencyView;
