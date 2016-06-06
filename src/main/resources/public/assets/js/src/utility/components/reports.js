
var _ = require('lodash');
var moment = require('moment');

var React = require('react');
var Bootstrap = require('react-bootstrap');
var ReactRedux = require('react-redux');
var Select = require('react-controls/select-dropdown');

var {Button, Collapse} = Bootstrap;

var PropTypes = React.PropTypes;
var _configPropType = PropTypes.shape({
  utility: PropTypes.object,
  reports: PropTypes.object,
  overview: PropTypes.object,
});

var toOptionElement = ({value, text}) => (<option value={value} key={value}>{text}</option>);

var MeasurementReport = React.createClass({
  
  propTypes: {
    config: _configPropType,
    field: PropTypes.string,
    levels: PropTypes.arrayOf(PropTypes.string),
  },
  
  childContextTypes: {
    config: _configPropType, 
  },

  getChildContext: function() {
    return {config: this.props.config};
  },

  getDefaultProps: function () {
    return {
      field: 'volume',
      levels: ['day', 'week'],
    };
  },

  getInitialState: function () {
    return {
      level: 'week',
      reportName: 'avg-daily-avg',
    };
  },

  render: function () {
    var {config, field, levels} = this.props;
    var {level, reportName} = this.state;

    if (_.isEmpty(config) || _.isEmpty(config.reports) || _.isEmpty(config.utility)) {
      return (<div>Loading configuration...</div>);
    }

    var ReportPane = require('./reports-measurements/pane');
   
    var reportsConfig = config.reports.byType.measurements; 
    
    var reportOptions = _.values(reportsConfig.levels)
      .filter(l => (levels.indexOf(l.name) >= 0))
      .map(l => ({   
        group: l.description,
        options: new Map(_.values(
          _.mapValues(l.reports, (r, k) => ([[l.name, k].join('/'), r.title]))
        ))
      }));

    var selectReport = (
      <Select className="select-report" 
        value={[level, reportName].join('/')} 
        onChange={this._setReport} 
        options={reportOptions}
       >
      </Select>
    );

    return (
      <div className="reports reports-measurements">
        <h3>{reportsConfig.fields[field].title}</h3>
        
        <div className="legend">
          <span>Choose report:</span>&nbsp;
          {selectReport}
        </div>
        
        <ReportPane.Panel
          field={field} 
          level={level} 
          reportName={reportName} 
          inlineForm={false} 
         />
        
        <ReportPane.Chart 
          field={field} 
          level={level} 
          reportName={reportName} 
         />
        
        <ReportPane.Info 
          field={field} 
          level={level} 
          reportName={reportName} 
         />
      
      </div>
    );
  },

  _setLevel: function (level) {
    this.setState({level});
  },
  
  _setReport: function (name) {
    var [level, reportName] = name.split('/');
    this.setState({level, reportName});
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

