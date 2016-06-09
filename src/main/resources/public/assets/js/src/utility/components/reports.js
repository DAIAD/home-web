
var _ = require('lodash');
var moment = require('moment');

var React = require('react');
var Bootstrap = require('react-bootstrap');
var ReactRedux = require('react-redux');
var Select = require('react-controls/select-dropdown');
var DatetimeInput = require('react-datetime');
var {Button, Collapse} = Bootstrap;

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
  
  statics: {
    defaults: {
      datetimeProps: {
        dateFormat: 'D MMM[,] YYYY', 
        timeFormat: null,
        inputProps: {size: 10},
      },
    },
  },

  propTypes: {
    config: _configPropType,
    now: PropTypes.number,
    field: PropTypes.string,
  },
 
  childContextTypes: {
    config: _configPropType, 
  },

  getDefaultProps: function () {
    return {
      field: 'volume',  
    };
  },

  getChildContext: function() {
    return {config: this.props.config};
  },
 
  getInitialState: function () {
    return {
      showInput: false,
      inputNow: null,
    }; 
  },

  render: function () { 
    var {OverviewAsAccordion} = require('./reports-measurements/overview');
    var {config, field, now} = this.props; 
    var {inputNow, showInput} = this.state; 

    if (!config || _.isEmpty(config.reports) || _.isEmpty(config.utility) || _.isEmpty(config.overview)) {
      return (<div>Loading configuration...</div>);
    }
    
    var reportProps = {
      field,
      now,
      uom: config.reports.byType.measurements.fields[field].unit,
      reports: config.overview.reports,
    };
    
    var {datetimeProps} = this.constructor.defaults;

    return (
      <div className="overview reports">
        <h3>{'Overview'}</h3>
        <form className="form-inline">
          <span className="help">
            The reports are generated around a reference time of <strong>{moment(now).format('D MMM, YYYY')}</strong>.&nbsp;
          </span>
          <a style={{cursor: 'pointer'}} 
            onClick={() => (this.setState({showInput: !this.state.showInput}))}
           >Choose a different time
          </a>
          {showInput? ':' : ' '}&nbsp;
          <div className="form-group" style={{display: showInput? 'inline-block' : 'none'}}>
            <DatetimeInput {...datetimeProps} value={inputNow || now}
              onChange={(t) => (this.setState({inputNow: t.valueOf()}))}
             />
            &nbsp;
            <Button onClick={this._refresh} title="Re-generate the reports">
              <i className='fa fa-refresh'/>
            </Button>
          </div>
        </form>
        <OverviewAsAccordion {...reportProps} />
      </div>
    );
  },

  _refresh: function () {
    // Get the timestamp of the chosen YYYY-MM-DD date at UTC
    var {inputNow} = this.state;
    
    if (!inputNow)
      return;
    
    var t = moment(moment(inputNow).format('YYYY-MM-DD') + 'T00:00:00Z');
    this.props.setReferenceTime(t.valueOf());
    
    setTimeout(() => {this.setState({showInput: false});}, 500);
  },  
});

Overview = ReactRedux.connect(
  (state, ownProps) => ({now: state.overview.referenceTime}),
  (dispatch, ownProps) => {
    var actions = require('../actions/overview.js');
    return {
      setReferenceTime: (t) => (dispatch(actions.setReferenceTime(t))),
    };
  }
)(Overview);

module.exports = {MeasurementReport, SystemReport, Overview};

