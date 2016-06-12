
var _ = require('lodash');
var moment = require('moment');
var numeral = require('numeral');
var sprintf = require('sprintf');

var React = require('react');
var ReactRedux = require('react-redux');
var Bootstrap = require('react-bootstrap');
var {FormattedMessage} = require('react-intl');
var DatetimeInput = require('react-datetime');
var echarts = require('react-echarts');
var Select = require('react-controls/select-dropdown');

var Errors = require('../../constants/Errors');
var Granularity = require('../../model/granularity');
var TimeSpan = require('../../model/timespan');
var population = require('../../model/population');
var {computeKey, consolidateFuncs} = require('../../reports').measurements;
var {timespanPropType, populationPropType, seriesPropType, configPropType} = require('../../prop-types');
var toolbars = require('../toolbars');

var {Button, ButtonGroup, Collapse, Panel, ListGroup, ListGroupItem} = Bootstrap;
var PropTypes = React.PropTypes;

const REPORT_KEY = 'pane';

//
// Helpers
//

var checkTimespan = function (val, level, N=4) {
  // Check if a timespan (given either as name or as pair of timestamps)
  // is valid. A non-empty string represents an error, zero represents success.
  
  var [t0, t1] = computeTimespan(val);
  
  var dt = t1.valueOf() - t0.valueOf();
  if (dt <= 0)
    return Errors.reports.measurements.TIMESPAN_INVALID;
  
  var dl = Granularity.fromName(level).valueOf();
  if (dl >= dt)
    return Errors.reports.measurements.TIMESPAN_TOO_NARROW;
  
  if (dl * Math.pow(10, N) < dt)
    return Errors.reports.measurements.TIMESPAN_TOO_WIDE; // N orders of magnitude bigger than dl
  
  return 0;
};
    
var computeTimespan = function (val) {
  // Convert to a pair of moment instances
  if (_.isString(val)) {
    return TimeSpan.fromName(val).toRange();
  } else if (_.isArray(val)) {
    var [t0, t1] = val;
    return [moment(t0), moment(t1)];
  }
};

var extractPopulationGroupParams = function (target) {
  var clusterKey, groupKey;
  
  if (target instanceof population.Cluster) {
    clusterKey = target.key; 
    groupKey = null;
  } else if (target instanceof population.ClusterGroup) {
    clusterKey = target.clusterKey; 
    groupKey = target.key;
  } else if (target instanceof population.Utility) { 
    clusterKey = groupKey = null;
  } else if (target instanceof population.Group) {
    clusterKey = null; 
    groupKey = target.key;
  }
  
  return [clusterKey, groupKey];
};

var toOptionElement = function ({value, text}) {
  return (<option value={value} key={value}>{text}</option>);
};

//
// Presentational components
//

var HelpParagraph = ({errorMessage, dirty}) => {
  if (errorMessage) {
    return (<p className="help text-danger">{errorMessage}</p>);
  } else if (dirty) {
    return (<p className="help text-info">Parameters have changed. Refresh to redraw data!</p>); 
  } else {
    return (<p className="help text-muted">Refresh to redraw data.</p>);
  }
};

var ReportPanel = React.createClass({
  
  statics: {
    defaults: {
      templates: {
      },    
    },
    
    configurationForReport: function (props, context) {
      var {config} = context;
      return config.reports.byType.measurements
        .levels[props.level]
        .reports[props.reportName];
    },
    
    toolbarSpec: [ 
      {
        key: 'parameters',
        //component: 'div', // Note default is Bootstrap.ButtonGroup
        buttons: [
          {
            key: 'source', 
            tooltip: {message: 'Select source of measurements', placement: 'bottom'}, 
            iconName: 'tachometer', //'cube',
            //text: 'Source',
            buttonProps: {bsStyle: 'default', /*className: 'btn-circle'*/ },
          },
          {
            key: 'report', 
            tooltip: {message: 'Choose type of report', placement: 'bottom'}, 
            iconName: 'area-chart',
            //text: 'Metric',
            buttonProps: {bsStyle: 'default', /*className: 'btn-circle'*/ },
          },
          {
            key: 'timespan', 
            tooltip: {message: 'Define a time range', placement: 'bottom'}, 
            iconName: 'calendar',
            //text: 'Time',
            buttonProps: {bsStyle: 'default', /*className: 'btn-circle'*/},
          },
          {
            key: 'population-group', 
            tooltip: {message: 'Define a population target', placement: 'bottom'},
            iconName: 'users',
            //text: 'Groups',
            buttonProps: {bsStyle: 'default', /*className: 'btn-circle'*/},
          },
        ],
      },
      {
        key: 'actions',
        //component: 'div', // Note default is Bootstrap.ButtonGroup
        buttons: [
          {
            key: 'export',
            tooltip: {message: 'Export to a CSV table', placement: 'bottom'},
            text: 'Export',
            iconName: 'table',
            buttonProps: {disabled: true, bsStyle: 'default', /*className: 'btn-circle'*/ },
          },
          {
            key: 'refresh',
            tooltip: {message: 'Re-generate report and redraw the chart', placement: 'bottom'},
            text: 'Refresh',
            iconName: 'refresh',
            buttonProps: {bsStyle: 'primary', /*className: 'btn-circle'*/ },
          },
        ],  
      }, 
    ],
  },

  propTypes: {
    field: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    level: PropTypes.string.isRequired,
    reportName: PropTypes.string.isRequired,
    source: PropTypes.string,
    timespan: timespanPropType,
    population: populationPropType,
    initializeReport: PropTypes.func.isRequired,
    refreshData: PropTypes.func.isRequired,
    setReport: PropTypes.func.isRequired,
    setField: PropTypes.func.isRequired,
    setTimespan: PropTypes.func.isRequired,
    setPopulation: PropTypes.func.isRequired,
    setSource: PropTypes.func.isRequired,
  },

  contextTypes: {config: configPropType},
  
  // Lifecycle
  
  getInitialState: function () {
    return {
      dirty: false,
      timespan: this.props.timespan,
      error: null,
      errorMessage: null,
      formFragment: 'report',
      disabledButtons: '', // a '|' delimited string, e.g 'export|refresh'
      fadeIn: false, // animation (seconds)  
    };  
  },
  
  getDefaultProps: function () {
    return {
      field: 'volume',
      level: 'week',
      source: 'meter',
      timespan: 'quarter',
    };
  },

  componentDidMount: function () {
    var cls = this.constructor;
    var {field, level, reportName} = this.props;

    if (_.isEmpty(field) || _.isEmpty(level) || _.isEmpty(reportName)) {
      return; // cannot yet initialize the target report
    }
    
    var {timespan} = cls.configurationForReport(this.props, this.context);
    this.props.initializeReport(field, level, reportName, {timespan});
    this.props.refreshData(field, level, reportName);
  },
  
  componentWillReceiveProps: function (nextProps, nextContext) {
    var cls = this.constructor;
    
    // In any case, reset temporary copy of timespan, clear error/dirty flags
    this.setState({
      dirty: false,
      error: null,
      errorMessage: null,
      disabledButtons: '',
      timespan: nextProps.timespan,
    });

    // If moving to another report, take care to initialize it first
    if (
      (nextProps.field != this.props.field) || 
      (nextProps.level != this.props.level) ||
      (nextProps.reportName != this.props.reportName)
    ) {
      console.info(sprintf(
        'The panel will switch to report (%s, %s, %s)',
        nextProps.field, nextProps.level, nextProps.reportName
      ));
      console.assert(nextContext.config == this.context.config, 
        'Unexpected change for configuration in context!');
      var {timespan} = cls.configurationForReport(nextProps, nextContext);
      nextProps.initializeReport(
        nextProps.field, nextProps.level, nextProps.reportName, {timespan}
      );
      setTimeout(
        () => (nextProps.refreshData(
          nextProps.field, nextProps.level, nextProps.reportName)
        ), 
        100
      );
    } 
  },

  componentDidUpdate: function () {
    // Schedule updates after a successfull component update

    // If under a CSS transition, be sure to clear the flag afterwards
    // This is needed, because only a change "" -> ".fade-in" can trigger a new animation.
    var d = Number(this.state.fadeIn);
    if (d > 0) {
      setTimeout(
        () => (this.state.fadeIn && this.setState({fadeIn: false})), 
        (d + 1) * 1e+3
      );
    }
  },

  render: function () {
    var {field, title, level, reportName} = this.props;
    var {dirty, error, errorMessage} = this.state;

    var toolbarSpec = this._specForToolbar();
    var header = (
      <div className="header-wrapper">
        <h3>{title}</h3>
        <toolbars.ButtonToolbar className="header-toolbar" 
          groups={toolbarSpec} 
          onSelect={this._handleToolbarEvent} 
         />
      </div>
    );
    
    var footer = (
      <Info field={field} level={level} reportName={reportName} />
    );
    
    var formFragment = this._renderFormFragment();
    var {fadeIn} = this.state;
    return (
      <Panel header={header} footer={footer}>
        <ListGroup fill>
          <ListGroupItem className="report-form-wrapper">
            <form className="report-form form-horizontal">
              <fieldset className={!fadeIn? '' : sprintf('fade-in x%d', fadeIn)}>
                {formFragment}
              </fieldset>
            </form>
          </ListGroupItem>
          <ListGroupItem className="report-form-help">
            <HelpParagraph dirty={dirty} errorMessage={errorMessage}/>
          </ListGroupItem>
          <ListGroupItem className="report-chart-wrapper">
            <Chart field={field} level={level} reportName={reportName} />
          </ListGroupItem>
        </ListGroup>
      </Panel>
    );
  },

  // Event handlers

  _handleToolbarEvent: function (groupKey, key) {
    switch (groupKey) {
      case 'parameters':
        return this._switchToFormFragment(key);
        break;
      case 'actions':
        return this._performAction(key);
        break;
    }
  },

  _switchToFormFragment: function (key) {
    if (this.state.formFragment != key)
      this.setState({formFragment: key, fadeIn: 1.0});
    return false;
  },

  _performAction: function (key) {
    var {field, level, reportName} = this.props;
    switch (key) {
      case 'refresh':
        {
          console.info(sprintf(
            'About to refresh data for report (%s, %s, %s)...',
            field, level, reportName
          ));
          this.props.refreshData(field, level, reportName);
          // Note is this needed? as it will always be cleared at next props
          //this.setState({dirty: false});
        }
        break;
      case 'export':
        // Todo
        console.info('Todo: Export to CSV table');
        break;
    }
  },

  _setSource: function (source) {
    var {field, level, reportName} = this.props;
    this.props.setSource(field, level, reportName, source);
    this.setState({dirty: true});
    return false;
  },
  
  _setReport: function (level, reportName) {
    this.props.setReport(level, reportName);
    return false;
  },
  
  _setField: function (field) {
    this.props.setField(field);
    return false;
  },

  _setTimespan: function (ts) {
    // Todo
    return false;
  },
  
  _setPopulation: function (target) {
    // Todo
    return false;
  },

  // Helpers
  
  _renderFormFragment: function () {
    var {config} = this.context;
    var {fields, sources, levels} = config.reports.byType.measurements;

    var fragment1; // single element or array of keyed elements
    switch (this.state.formFragment) {
      case 'source':
        {
          var {source} = this.props;
          var sourceOptions = new Map(
            _.intersection(_.keys(sources), fields[this.props.field].sources)
              .map(k => ([k, sources[k].title]))
          );
          fragment1 = ( 
            <div className="form-group">
              <label className="col-sm-2 control-label">Source:</label>
              <div className="col-sm-9">
                <Select className="select-source" 
                  value={source} options={sourceOptions} onChange={this._setSource} 
                 />
                <p className="help text-muted">
                  {'Specify the source device for measurements.'}
                </p>
              </div>
            </div>
          );
        } 
        break;
      case 'report':
        {
          var {level, reportName} = this.props;
          var levelOptions = new Map(
            _.values(
              _.mapValues(levels, (u, k) => ([k, u.name]))
            )
          );
          var reportOptions = new Map(
            _.values(
              _.mapValues(levels[level].reports, (r, k) => ([k, r.title]))
            )  
          );
          fragment1 = [
            (
              <div key="level" className="form-group" >
                <label className="col-sm-2 control-label">Level:</label>
                <div className="col-sm-9">
                  <Select className="select-level" 
                    value={level} 
                    options={levelOptions} 
                    onChange={(val) => this._setReport(val, reportName)} 
                   />
                  <p className="help text-muted">
                    {'Specify the level of detail (unit of time for charts).'}
                  </p>
                </div>
              </div>
            ),
            (
              <div key="report-name" className="form-group" >
                <label className="col-sm-2 control-label">Metric:</label>
                <div className="col-sm-9">
                  <Select className="select-report"
                    value={reportName} 
                    options={reportOptions} 
                    onChange={(val) => this._setReport(level, val)} 
                   />
                  <p className="help text-muted">
                    {'Select the metric to be applied to measurements.'}
                  </p>
                </div>
              </div>
            ),
          ];
        }
        break;
      case 'timespan':
        {
          // Todo
        }
        break;
      case 'population-group':
        {
          // Todo
        }
        break;
      default:
        console.error(sprintf(
          'Got unexpected key (%s) representing a form fragment',
          this.state.formFragment
        ));
        break;
    }

    return fragment1;
  },

  _enableButton: function(key, flag=true) {
    var {disabledButtons: value} = this.state, nextValue = null;
    var disabledKeys = value? value.split('|') : [];
    var i = disabledKeys.indexOf(key);

    if (flag && (i >= 0)) {
      // The button is currently disabled and must be enabled
      disabledKeys.splice(i, 1);
      nextValue = disabledKeys.join('|'); 
    } else if (!flag && (i < 0)) {
      // The button is currently enabled and must be disabled
      disabledKeys.push(key);
      nextValue = disabledKeys.sort().join('|');
    }
    
    if (nextValue != null)
      this.setState({disabledButtons: nextValue});
  },
  
  _specForToolbar: function () {
    // Make a spec object suitable to feed toolbars.ButtonToolbar "groups" prop.
    // Note we must take into account our current state (disabled flags for buttons)
    
    var cls = this.constructor;
    var {disabledButtons} = this.state;
    
    if (_.isEmpty(disabledButtons))
      return cls.toolbarSpec; // return the original spec

    var disabledKeys = disabledButtons.split('|');
    return cls.toolbarSpec.map(spec => ({
      ...spec, 
      buttons: spec.buttons.map(b => _.merge({}, b, {
        buttonProps: {
          // A key disabled in the original spec cannot ever be enabled!
          disabled: (b.buttonProps.disabled || disabledKeys.indexOf(b.key) >= 0)
        },
      }))
    }));
  },

  // Wrap dispatch actions

});

var Form = React.createClass({
  
  statics: {
    defaults: {
      datetimeProps: {
        closeOnSelect: true,
        dateFormat: 'ddd D MMM[,] YYYY',
        timeFormat: null, 
        inputProps: {size: 8}, 
      },  
    },
    
    timespanOptions: [].concat(
      Array.from(TimeSpan.common.entries()).map(([name, u]) => ({value: name, text: u.title})),
      [{value: '', text: 'Custom...'}]
    ),
  },
  
  propTypes: {
    field: PropTypes.string.isRequired,
    level: PropTypes.string.isRequired,
    reportName: PropTypes.string.isRequired,
    source: PropTypes.oneOf(['meter', 'device']),
    timespan: timespanPropType,
    population: populationPropType,
  },

  contextTypes: {config: configPropType},

  // Lifecycle

  getInitialState: function () {
    return {
      dirty: false,
      error: null,
      errorMessage: null,
      timespan: this.props.timespan,
      collapsed: false,
    };
  },

  getDefaultProps: function () {
    return {
      inlineForm: true,
      source: 'meter',
      timespan: 'month',
      population: null,
    };
  },
  
  componentDidMount: function () {
    var {level, reportName} = this.props;
    
    var _config = this.context.config.reports.byType.measurements; 
    var report = _config.levels[level].reports[reportName];

    this.props.initializeReport({timespan: report.timespan});
    this.props.refreshData();
  },
 
  componentWillReceiveProps: function (nextProps, nextContext) {
    // Check if moving to another report
    if (
      (nextProps.field != this.props.field) || 
      (nextProps.level != this.props.level) ||
      (nextProps.reportName != this.props.reportName)
    ) {
      console.assert(nextContext.config == this.context.config, 
        'Unexpected change for configuration in context!');
      var _config = nextContext.config.reports.byType.measurements;
      var report = _config.levels[nextProps.level].reports[nextProps.reportName];

      this.setState({dirty: false, error: null, errorMessage: null});
      nextProps.initializeReport({timespan: report.timespan});
      setTimeout(nextProps.refreshData, 100);
    }
    
    // Reset timespan
    if (nextProps.timespan != this.props.timespan) {
      this.setState({timespan: nextProps.timespan});
    }
  },

  render: function () {
    var cls = this.constructor;
    var {config} = this.context;
    var {field, level, reportName, source, population: target, inlineForm} = this.props;
    var {timespan, error, collapsed} = this.state;
    
    var _config = config.reports.byType.measurements;
    var [t0, t1] = computeTimespan(timespan);

    var datetimeProps = _.merge({}, cls.defaults.datetimeProps, {
      inputProps: {disabled: _.isString(timespan)? 'disabled' : null}
    });
    
    var sourceOptions = _config.fields[field].sources.map(k => ({
      value: k, text: _config.sources[k].title
    }));

    var timespanOptions = cls.timespanOptions.filter(
      o => (!o.value || checkTimespan(o.value, level) === 0)
    );

    var clusterOptions = config.utility.clusters.map(
      c => ({value: c.key, text: c.name })
    );
    
    var [clusterKey, groupKey] = extractPopulationGroupParams(target);
    var groupOptions = !clusterKey? [] :
      config.utility.clusters
        .find(c => (c.key == clusterKey))
          .groups.map(g => ({value: g.key, text: g.name}));
    
    var selectSource = (
      <Select className="select-source" value={source} onChange={this._setSource}>
        {sourceOptions.map(toOptionElement)}
      </Select>
    );
    
    var selectTimespan = (
      <Select className="select-timespan" 
        value={_.isString(timespan)? timespan : ''} 
        onChange={(val) => (this._setTimespan(val? (val) : ([t0, t1])))}
       >
        {timespanOptions.map(toOptionElement)}
      </Select>
    ); 
    
    var inputStarts = (
      <DatetimeInput {...datetimeProps} 
        value={t0.toDate()} 
        onChange={(val) => (this._setTimespan([val, t1]))} 
       />
    );
    
    var inputEnds = (
      <DatetimeInput {...datetimeProps} 
        value={t1.toDate()}
        onChange={(val) => (this._setTimespan([t0, val]))} 
       />
    );
    
    var selectCluster = (
      <Select className='select-cluster'
        value={clusterKey || ''}
        onChange={(val) => this._setPopulation(val, null)}
       >
        <option value="" key="" >None</option>
        <optgroup label="Cluster by:">
          {clusterOptions.map(toOptionElement)}
        </optgroup>
      </Select>
    );

    var selectClusterGroup = (
      <Select className='select-cluster-group'
        value={groupKey || ''}
        onChange={(val) => this._setPopulation(clusterKey, val)}
       >
        <optgroup label={clusterKey? 'All groups' : 'No groups'}>
          <option value="" key="">{clusterKey? 'All' : 'Everyone'}</option>
        </optgroup>
        <optgroup label="Pick a specific group:">
          {groupOptions.map(toOptionElement)}
        </optgroup>
      </Select>
    );

    var buttonRefresh = (
      <Button onClick={this._refresh} bsStyle="primary" disabled={!!error} title="Refresh">
        <i className="fa fa-refresh"></i>&nbsp; Refresh
      </Button>
    );

    var buttonSave = (
      <Button onClick={this._saveAsImage} bsStyle="default" disabled={true} title="Save">
        <i className="fa fa-save" ></i>&nbsp; Save as image
      </Button>
    );
  
    var buttonExport = (
      <Button onClick={this._exportToTable} bsStyle="default" disabled={true} title="Export">
        <i className="fa fa-table" ></i>&nbsp; Export as table
      </Button>
    );
    
    var form, formId = ['panel', field, level, reportName].join('--');
    var helpParagraph = (
      <HelpParagraph dirty={this.state.dirty} errorMessage={this.state.errorMessage}/>
    );
    if (inlineForm) {
      form = (
        <form className="form-inline report-form" id={formId}>
          <div className="form-group">
            <label>Source:</label>&nbsp;{selectSource}
          </div>
          <div className="form-group">
            <label>Time:</label>&nbsp;
            {selectTimespan}&nbsp;{inputStarts}&nbsp;{inputEnds}
          </div>
          <div className="form-group">
            <label>Group:</label>&nbsp;
            {selectCluster}&nbsp;{selectClusterGroup}
          </div>
          <br />
          <div className="form-group">
            {buttonRefresh}&nbsp;&nbsp;{buttonSave}&nbsp;&nbsp;{buttonExport}
          </div>
          {helpParagraph}
        </form> 
      );
    } else {
      form = (
        <form className="form-horizontal report-form" id={formId}>
          <fieldset>
          <legend>
            <span className="title" style={{cursor: 'pointer'}} onClick={this._toggleCollapsed}>Parameters</span>
            <i className={collapsed? "fa fa-fw fa-caret-down" : "fa fa-fw fa-caret-up"}></i>
            {this._markupSummary(source, [t0, t1], [clusterKey, groupKey])}
          </legend>
          <Collapse in={!collapsed}><div>
            <div className="form-group">
              <label className="col-sm-2 control-label">Source:</label>
              <div className="col-sm-9">{selectSource}</div>
            </div>
            <div className="form-group">
              <label className="col-sm-2 control-label">Time:</label>
              <div className="col-sm-9">
                {selectTimespan}&nbsp;&nbsp;{inputStarts}&nbsp;-&nbsp;{inputEnds}
              </div>
            </div>
            <div className="form-group">
              <label className="col-sm-2 control-label">Group:</label> 
              <div className="col-sm-9">
                {selectCluster}&nbsp;&nbsp;{selectClusterGroup}
               </div>
            </div>
          </div></Collapse> 
          </fieldset> 
          <div className="form-group">
            <div className="col-sm-12">
              {buttonRefresh}&nbsp;&nbsp;{buttonSave}&nbsp;&nbsp;{buttonExport}
            </div>
          </div>
          {helpParagraph}
        </form>
      );
    }
    
    return form;
  },

  // Event handlers

  _toggleCollapsed: function () {
    this.setState({collapsed: !this.state.collapsed});
  },

  _setTimespan: function (val) {
    var errors = Errors.reports.measurements;
    var error = null, errorMessage = null, ts = null;
    
    // Validate
    if (_.isString(val)) {
      // Assume a symbolic name is always valid
      ts = val;
    } else if (_.isArray(val)) {
      // Check if given timespan is a valid range 
      console.assert(val.length == 2 && val.every(t => moment.isMoment(t)), 
        'Expected a pair of moment instances');
      error = checkTimespan(val, this.props.level);
      // Todo Provide an i18n message keyed on error
      switch (error) {
        case errors.TIMESPAN_INVALID:
          errorMessage = 'The given timespan is invalid.'
          break;
        case errors.TIMESPAN_TOO_NARROW:
          errorMessage = 'The given timespan is too narrow.'
          break;
        case errors.TIMESPAN_TOO_WIDE: 
          errorMessage = 'The given timespan is too wide.'
          break;
        case 0:
        default:
          ts = [val[0].valueOf(), val[1].valueOf()];
          break;
      }
    }
    
    // Set state and decide if must setTimespan()
    if (ts != null) {
      // The input is valid
      this.props.setTimespan(ts);
    }
    this.setState({dirty: true, timespan: val, error, errorMessage});
    return false;
  },
  
  _setPopulation: function (clusterKey, groupKey) {
    
    var {config} = this.context;
    var p;

    if (!clusterKey && !groupKey) {
      p = new population.Utility(config.utility.key, config.utility.name);
    } else if (clusterKey && !groupKey) {
      p = new population.Cluster(clusterKey);
    } else if (!clusterKey && groupKey) {
      p = new population.Group(groupKey);
    } else {
      p = new population.ClusterGroup(clusterKey, groupKey);
    }

    this.props.setPopulation(p);
    this.setState({dirty: true});
    return false;
  },

  _setSource: function (val) {
    this.props.setSource(val);
    this.setState({dirty: true});
    return false;
  },

  _refresh: function () {
    this.props.refreshData();
    this.setState({dirty: false});
    return false;
  },
  
  _saveAsImage: function () {
    console.info('Todo: Saving to image...');
    return false;
  },

  _exportToTable: function () {
    console.info('Todo: Exporting to CSV...');
    return false;
  },

  // Helpers

  _markupSummary: function (source, [t0, t1], [clusterKey, groupKey]) {
    var _config = this.context.config.reports.byType.measurements;
    const openingBracket = (<span>&#91;</span>); 
    const closingBracket = (<span>&#93;</span>);
    const delimiter = (<span>::</span>);
    
    t0 = moment(t0); t1 = moment(t1);
    var datefmt = (t0.year() == t1.year())? 'D/MMM' : 'D/MMM/YYYY';
    var formattedTime = sprintf('From %s To %s', t0.format(datefmt), t1.format(datefmt)); 
    return (
      <span className="summary-wrapper">
        {openingBracket}&nbsp;
        <span className="summary">
          <span>{_config.sources[source].name}</span>
          &nbsp;{delimiter}&nbsp;
          <span>{formattedTime}</span>
        </span>
        &nbsp;{closingBracket}
      </span>  
    );
  },
}); 

var Chart = React.createClass({
  
  statics: {
    
    nameTemplates: {
      basic: _.template('<%= metric %> of <%= label %>'),
      ranking: _.template('<%= ranking.type %>-<%= ranking.index + 1 %> of <%= label %>'),
    },
   
    defaults: {
      lineWidth: 1,
      smooth: false,
      tooltip: true,
      fill: 0.35,
      colors: [
        '#2D6E8D', '#DB5563', '#9056B4', '#DD4BCF', '#30EC9F',
        '#C23531', '#2F4554', '#61A0A8', '#ECA63F', '#41B024',
      ],
      symbolSize: 4,
      grid: {
        x: '80', x2: '35', y: '30', y2: '30',
      },
      xAxis: {
        dateformat: {
          'minute': 'HH:mm',
          'hour': 'HH:00',
          'day': 'DD/MMM',
          'week': 'DD/MMM',
          'month': 'MM/YYYY',
          'quarter': 'Qo YYYY',
          'year': 'YYYY',
        },
      }
    },
  }, 

  propTypes: {
    field: PropTypes.string.isRequired,
    level: PropTypes.string.isRequired,
    reportName: PropTypes.string.isRequired,
    series: PropTypes.arrayOf(seriesPropType),
    finished: PropTypes.oneOfType([PropTypes.bool, PropTypes.number]),
    // Appearence
    width: PropTypes.number,
    height: PropTypes.number,
    scaleTimeAxis: PropTypes.bool,
  }, 
  
  contextTypes: {config: configPropType},

  getDefaultProps: function () {
    return {
      width: this.defaults.width,
      height: this.defaults.height,
      series: [],
      finished: true,
      scaleTimeAxis: false,
    };
  },

  render: function () {
    var {defaults} = this.constructor;
    var {field, level, reportName} = this.props;
    var {config} = this.context;
    var {title, unit, name: fieldName} = config.reports.byType.measurements.fields[field];
    
    var {xaxisData, series} = this._consolidateData();
    xaxisData || (xaxisData = []);
    
    series = (series || []).map(s => ({
      name: this._getNameForSeries(s),
      symbolSize: defaults.symbolSize,
      fill: defaults.fill,
      smooth: defaults.smooth,
      data: s.data,
    }));

    var xf = defaults.xAxis.dateformat[level];

    return (
      <div className="report-chart" id={['chart', field, level, reportName].join('--')}>
        <echarts.LineChart
            width={this.props.width}
            height={this.props.height}
            loading={this.props.finished? null : {text: 'Loading data...'}}
            tooltip={defaults.tooltip}
            lineWidth={defaults.lineWidth}
            color={defaults.colors}
            grid={defaults.grid}
            xAxis={{
              data: xaxisData,
              boundaryGap: false, 
              formatter: (t) => (moment(t).format(xf)),
            }}
            yAxis={{
              name: fieldName + (unit? (' (' + unit + ')') : ''),
              numTicks: 4,
              formatter: (y) => (numeral(y).format('0.0a')),
            }}
            series={series}
         />
      </div>
    );
  },

  // Helpers

  _consolidateData: function () {
    var result = {xaxisData: null, series: null};
    var {field, level, reportName, series, scaleTimeAxis} = this.props;
    
    var {config} = this.context;
    var _config = config.reports.byType.measurements;

    if (!series || !series.length || series.every(s => !s.data.length))
      return result; // no data available
    
    var report = _config.levels[level].reports[reportName];
    var {bucket, duration} = config.reports.levels[level];
    var [d, durationUnit] = duration;
    var d = moment.duration(d, durationUnit);

    // Use a sorted (by timestamp t) copy of series data [t,y]
    
    series = series.map(s => (_.extend({}, s, {
      data: s.data.slice(0).sort((p1, p2) => (p1[0] - p2[0])),
    })));

    // Find time span
    
    var start, end;
    if (scaleTimeAxis) {
      start = _.min(series.map(s => s.data[0][0]));
      end = _.max(series.map(s => s.data[s.data.length -1][0]));
    } else {
      start = _.min(series.map(s => s.timespan[0]));
      end = _.max(series.map(s => s.timespan[1]));
    }
    
    var startx = moment(start).startOf(bucket);
    var endx = moment(end).endOf(bucket);
    
    // Generate x-axis data,
    
    result.xaxisData = [];
    for (let m = startx; m < endx; m.add(d)) {
      result.xaxisData.push(m.valueOf());
    }

    // Collect points in level-wide buckets, then consolidate
    
    var groupInBuckets = (data, boundaries) => {
      // Group y values into buckets defined yb x-axis boundaries:
      var N = boundaries.length;
      // For i=0..N-2 all y with (b[i] <= y < b[i+1]) fall into bucket #i ((i+1)-th)
      var yb = []; // hold buckets of y values
      for (var i = 1, j = 0; i < N; i++) {
        yb.push([]);
        while (j < data.length && data[j][0] < boundaries[i]) {
          var y = data[j][1];
          (y != null) && yb[i - 1].push(y);
          j++;
        }
      }
      // The last (N-th) bucket will always be empty
      yb.push([]);
      return yb;
    };

    var cf = consolidateFuncs[report.consolidate]; 
    result.series = series.map(s => (
      _.extend({}, s, {
        data: groupInBuckets(s.data, result.xaxisData).map(cf)
      })
    ));
    
    return result;
  },

  _getNameForSeries: function ({ranking, population: target, metric}) {
    var {nameTemplates} = this.constructor;
    var {config} = this.context;
    
    var label;
    if (target instanceof population.Utility) {
      // Use utility's friendly name
      label = config.utility.name;
    } else if (target instanceof population.ClusterGroup) {
      // Use group's friendly name
      label = config.utility.clusters
        .find(c => (c.key == target.clusterKey))
          .groups.find(g => (g.key == target.key)).name;
    }

    var tpl = (ranking)? nameTemplates.ranking : nameTemplates.basic;
    return tpl({metric, label, ranking});
  },
  
});

var Info = React.createClass({
  
  statics: {},

  propTypes: {
    field: PropTypes.string.isRequired,
    level: PropTypes.string.isRequired,
    reportName: PropTypes.string.isRequired,
    requested: PropTypes.number,
    finished: PropTypes.oneOfType([PropTypes.bool, PropTypes.number]),
    errors: PropTypes.arrayOf(PropTypes.string),
    series: PropTypes.arrayOf(PropTypes.shape({
      data: PropTypes.array,
    })),
    requests: PropTypes.number,
  },
  
  getDefaultProps: function () {
    return {
      requested: null,
      finished: null,
    };
  },

  shouldComponentUpdate: function (nextProps) {
    if (
      (nextProps.field != this.props.field) || 
      (nextProps.level != this.props.level) ||
      (nextProps.reportName != this.props.reportName)
    )
      return true;
    return _.isNumber(nextProps.finished);
  },

  render: function () {
    var {field, level, reportName} = this.props;
    var {errors, series, requests, requested, finished} = this.props;
    var paragraph, message;
    
    var n = !series? 0 : series.filter(s => (s.data.length > 0)).length; 
    
    if (errors) {
      message = _.first(errors);
      paragraph = (<p className="help text-danger">{message}</p>);
    } else if (!n) {
      message = _.isNumber(finished)? 
        ('No data received! Last attempt was at ' + moment(finished).format('HH:mm:ss')):
        ('No data!');
      paragraph = (<p className="help text-warning">{message}</p>);
    } else {
      message = 'Everything is fine. Updated at ' + moment(finished).format('HH:mm:ss');
      paragraph = ( <p className="help text-muted">{message}</p>);
    }

    return (
      <div className="report-info" id={['info', field, level, reportName].join('--')}>
        {paragraph}
      </div>
    );
  },
});

//
// Container components
//

var reportingActions = require('../../actions/reports-measurements');
var chartingActions = require('../../actions/charting');

ReportPanel = ReactRedux.connect(
  (state, ownProps) => {
    var stateProps;
    var {fields} = state.config.reports.byType.measurements;
    var {field, level, reportName} = state.charting;
    
    stateProps = {field, level, reportName};
    
    if (!ownProps.title)
      stateProps.title = fields[field].title;
     
    var key = computeKey(field, level, reportName, REPORT_KEY);
    var r1 = state.reports.measurements[key];
    if (r1) {
      // There is an initialized intance of the report for (field, level, reportName)
      var {source, timespan, population} = r1;
      _.extend(stateProps, {source, timespan, population});
    }

    return stateProps;
  }, 
  (dispatch, ownProps) => {
    var {setField, setLevel, setReport} = chartingActions;
    var {initialize, setSource, setTimespan, setPopulation, refreshData} = reportingActions;
    return {
      setField: (field) => (dispatch(setField(field))),
      setReport: (level, reportName) => (dispatch(setReport(level, reportName))), 
      initializeReport: (field, level, reportName, defaults) => (
        dispatch(initialize(field, level, reportName, REPORT_KEY, defaults))  
      ),
      refreshData: (field, level, reportName) => (
        dispatch(refreshData(field, level, reportName, REPORT_KEY))
      ),
      setSource: (field, level, reportName, source) => (
        dispatch(setSource(field, level, reportName, REPORT_KEY, source))
      ),
      setTimespan: (field, level, reportName, ts) => (
        dispatch(setTimespan(field, level, reportName, REPORT_KEY, ts))
      ),
      setPopulation: (field, level, reportName, p) => (
        dispatch(setPopulation(field, level, reportName, REPORT_KEY, p))
      ),
    };
  }, 
)(ReportPanel);

Form = ReactRedux.connect(
  (state, ownProps) => {
    var {field, level, reportName} = ownProps;
    var _state = state.reports.measurements;
    var key = computeKey(field, level, reportName, REPORT_KEY); 
    return !(key in _state)? {} : 
      _.pick(_state[key], ['source', 'timespan', 'population']);
  }, 
  (dispatch, ownProps) => {
    var {field, level, reportName} = ownProps;
    var {initialize, setSource, setTimespan, setPopulation, refreshData} = reportingActions;
    return {
      initializeReport: (defaults) => (
        dispatch(initialize(field, level, reportName, REPORT_KEY, defaults))
      ),
      setSource: (source) => (
        dispatch(setSource(field, level, reportName, REPORT_KEY, source))
      ),
      setTimespan: (ts) => (
        dispatch(setTimespan(field, level, reportName, REPORT_KEY, ts))
      ),
      setPopulation: (p) => (
        dispatch(setPopulation(field, level, reportName, REPORT_KEY, p))
      ),
      refreshData: () => (
        dispatch(refreshData(field, level, reportName, REPORT_KEY))
      ),
    };
  }
)(Form);

Chart = ReactRedux.connect(
  (state, ownProps) => {
    var {field, level, reportName} = ownProps;
    var _state = state.reports.measurements;
    var key = computeKey(field, level, reportName, REPORT_KEY); 
    return !(key in _state)? {} : 
      _.pick(_state[key], ['finished', 'series']);
  },
  null
)(Chart);

Info = ReactRedux.connect(
  (state, ownProps) => {
    var {field, level, reportName} = ownProps;
    var _state = state.reports.measurements;
    var key = computeKey(field, level, reportName, REPORT_KEY);
    return !(key in _state)? {} : 
      _.pick(_state[key], ['requested', 'finished', 'requests', 'errors', 'series']
    );
  },
  null
)(Info);

// Export

module.exports = {Form, Chart, Info, ReportPanel};
