
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
    visible: PropTypes.bool,
  },
  
  contextTypes: {config: PropTypes.object},

  getInitialState: function () {
    return {foo: null};
  },

  componentDidMount: function () {
    console.info(' ** GroupPerEfficiencyView mounted (visible='+this.props.visible+')');
    if (this.props.visible) {this._computeFoo();}
  },
  
  componentWillReceiveProps: function (nextProps) {
    console.info(' ** GroupPerEfficiencyView received props (visible='+nextProps.visible+')');
    if (nextProps.visible) {
      if (this.state.foo == null || (nextProps.now != this.props.now)) 
        this._computeFoo();
    }
  },

  render: function () {
    
    var {config} = this.context;
    var {unit: uom} = config.reports.byType.measurements.fields[FIELD];
    
    // Todo

    return (
      <div>Hello per-efficiency view! <strong>{this.state.foo || 'n/a'}</strong></div>
    );
  },

  _computeFoo: function () {
    console.info(' ** Computing foo ...');
    setTimeout(() => {this.setState({foo: 42});}, 6000);
  },

});

//
// Container component
//

var actions = require('../../actions/reports-measurements');

// Todo

// Export

module.exports = GroupPerEfficiencyView;
