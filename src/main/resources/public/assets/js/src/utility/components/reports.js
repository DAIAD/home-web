
var _ = require('lodash');
var moment = require('moment');

var React = require('react');
var ReactBootstrap = require('react-bootstrap');
var ReactRedux = require('react-redux');

var PropTypes = React.PropTypes;
var {Nav, Navbar, NavItem, NavDropdown, MenuItem} = ReactBootstrap;
var {Router, Route, IndexRoute, Link, hashHistory} = ReactRouter;

var _configPropType = PropTypes.shape({
  utility: PropTypes.object,
  reports: PropTypes.object,
});

//
// Presentational components
//

var MeasurementReportsPage = React.createClass({
  
  propTypes: {
    config: _configPropType,
    params: PropTypes.shape({
      field: PropTypes.string,
      level: PropTypes.string,
      reportName: PropTypes.string,
    }) 
  },
  
  childContextTypes: {
    config: _configPropType, 
  },

  getChildContext: function() {
    return {config: this.props.config};
  },

  render: function () {
    var {Panel, Chart, Info} = require('./reports-measurements/pane');
 
    var {config, params: {field, level, reportName}} = this.props; 
    var _config = config.reports.byType.measurements; 
    
    var heading = (
      <h2>
        {_config.fields[field].title}
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {_config.levels[level].title}
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {_config.levels[level].reports[reportName].title}
      </h2>
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

var SystemReportsPage = React.createClass({
  
  propTypes: {
    config: _configPropType,
    params: PropTypes.shape({
      level: PropTypes.string,
      reportName: PropTypes.string,
    }),
  },
  
  childContextTypes: {
    config: _configPropType, 
  },

  getChildContext: function() {
    return {config: this.props.config};
  },

  render: function () {
    var {config, params: {level, reportName}} = this.props; 
    var _config = config.reports.byType.system; 
   
    var heading = (
      <h2>
        {_config.title} 
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {_config.levels[level].title}
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {_config.levels[level].reports[reportName].title}
      </h2>
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

var OverviewPage = React.createClass({
  
  propTypes: {
    config: _configPropType,
  },
 
  childContextTypes: {
    config: _configPropType, 
  },

  getChildContext: function() {
    return {config: this.props.config};
  },
 
  render: function () { 
    var overview = require('./reports-measurements/overview');
    var {config, params: {section}, location: {query: q}} = this.props; 
    
    var now = Number(q.now); 
    now = _.isNaN(now)? moment().valueOf() : now;

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
       <h2>
        {'Overview'}
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {'Water Consumption'}
        <span className="delimiter">&nbsp;/&nbsp;</span>
        {config.overview.sections[section].title}
      </h2>
    );

    return (
      <div className="overview">
        {heading}
        {body}
      </div>
    );
  },
});

//
// Container components:
//

// Inject global configuration to basic page components
var injectConfigToProps = (state, ownProps) => ({config: state.config});

var {connect} = ReactRedux;

MeasurementReportsPage = connect(injectConfigToProps, null)(MeasurementReportsPage); 

SystemReportsPage = connect(injectConfigToProps, null)(SystemReportsPage); 

OverviewPage = connect(injectConfigToProps, null)(OverviewPage); 

// Export

module.exports = {MeasurementReportsPage, SystemReportsPage, OverviewPage};
