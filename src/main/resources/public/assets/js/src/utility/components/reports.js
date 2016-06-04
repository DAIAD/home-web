
var _ = require('lodash');
var moment = require('moment');

var React = require('react');
var ReactBootstrap = require('react-bootstrap');
var ReactRedux = require('react-redux');

var PropTypes = React.PropTypes;
var _configPropType = PropTypes.shape({
  utility: PropTypes.object,
  reports: PropTypes.object,
  overview: PropTypes.object,
});

var MeasurementReport = React.createClass({
  
  propTypes: {
    config: _configPropType,
    field: PropTypes.string,
    level: PropTypes.string,
    reportName: PropTypes.string,
  },
  
  childContextTypes: {
    config: _configPropType, 
  },

  getChildContext: function() {
    return {config: this.props.config};
  },

  render: function () {
    var {config, field, level, reportName} = this.props;

    if (_.isEmpty(config) || _.isEmpty(config.reports) || _.isEmpty(config.utility)) {
      return (<div>Loading configuration...</div>);
    }

    var {Panel, Chart, Info} = require('./reports-measurements/pane');
    var _config = config.reports.byType.measurements; 
    
    var heading = (
      <h3>
        {_config.fields[field].title}
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {_config.levels[level].title}
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {_config.levels[level].reports[reportName].title}
      </h3>
    );
    
    return (
      <div className="reports reports-measurements">
        {heading}
        <Panel field={field} level={level} reportName={reportName} />
        <Chart field={field} level={level} reportName={reportName} />
        <Info field={field} level={level} reportName={reportName} />
      </div>
    );
  },

});

var SystemReport = React.createClass({
  
  propTypes: {
    config: _configPropType,
    level: PropTypes.string,
    reportName: PropTypes.string,
  },
  
  childContextTypes: {
    config: _configPropType, 
  },

  getChildContext: function() {
    return {config: this.props.config};
  },

  render: function () {
    var {config, level, reportName} = this.props; 
    
    if (_.isEmpty(config)) {
      return (<div>Loading configuration...</div>);
    }

    var _config = config.reports.byType.system; 
   
    var heading = (
      <h3>
        {_config.title} 
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {_config.levels[level].title}
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {_config.levels[level].reports[reportName].title}
      </h3>
    );
    
    var Report;
    switch (reportName) {
      default:
        Report = require('./reports-system/data-transmission').Report;
        break;
      case 'data-transmission':
        Report = require('./reports-system/data-transmission').Report;
        break;
    }

    return (
      <div className="reports reports-system">
        {heading}
        <Report level={level} reportName={reportName} />
      </div>
    );
  },
});

var Overview = React.createClass({
  
  propTypes: {
    config: _configPropType,
    section: PropTypes.string,
    now: PropTypes.number,
  },
 
  childContextTypes: {
    config: _configPropType, 
  },

  getChildContext: function() {
    return {config: this.props.config};
  },
 
  render: function () { 
    var overview = require('./reports-measurements/overview');
    var {config, section, now} = this.props; 
    
    now = (now == null)? moment().valueOf() : now;

    var body;
    switch (section) {
      case 'utility':
        body = (<overview.UtilityView now={now} />);
        break;
      case 'per-efficiency':
        body = (<overview.GroupPerEfficiencyView now={now} />);
        break;
      case 'per-household-size':
        body = (<overview.GroupPerSizeView now={now} />);
        break;
      case 'per-household-members':
        body = (<overview.GroupPerMembersView now={now} />);
        break;
      case 'per-income':
        body = (<overview.GroupPerIncomeView now={now} />);
        break;
      default:
        body = (<overview.UtilityView now={now} />);
        break;
    }
  
    var heading = (
       <h3>
        {'Overview'}
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {'Water Consumption'}
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {config.overview.sections[section].title}
      </h3>
    );

    return (
      <div className="overview">
        {heading}
        {body}
      </div>
    );
  },
});

module.exports = {MeasurementReport, SystemReport, Overview};

