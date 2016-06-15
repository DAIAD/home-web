
var _ = require('lodash');
var moment = require('moment');
var numeral = require('numeral');
var sprintf = require('sprintf');

var React = require('react');
var ReactRedux = require('react-redux');
var Bootstrap = require('react-bootstrap');
var {FormattedMessage} = require('react-intl');
var DatetimeInput = require('react-datetime');
var Select = require('react-controls/select-dropdown');

var toolbars = require('../toolbars');
var Errors = require('../../constants/Errors');
var Granularity = require('../../model/granularity');
var TimeSpan = require('../../model/timespan');
var population = require('../../model/population');
var {computeKey} = require('../../reports').measurements;
var {timespanPropType, populationPropType, seriesPropType, configPropType} = require('../../prop-types');
var {equalsPair} = require('../../helpers/comparators');

var Chart = require('./chart');

var {Button, ButtonGroup, Collapse, Panel, ListGroup, ListGroupItem} = Bootstrap;
var {PropTypes} = React;

const REPORT_KEY = 'pane';

// Todo Move under react-intl
const ErrorMessages = {
  [Errors.reports.measurements.TIMESPAN_INVALID]: 
    'The given time range is invalid.',
  [Errors.reports.measurements.TIMESPAN_TOO_NARROW]:
    'The given time range is too narrow.',
  [Errors.reports.measurements.TIMESPAN_TOO_WIDE]:
    'The given time range is too wide.',
};

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

var Option = ({value, text}) => (<option value={value} key={value}>{text}</option>);

//
// Presentational components
//

var FormStatusParagraph = ({errorMessage, dirty}) => {
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
      datetimeInputProps: {
        closeOnSelect: true,
        dateFormat: 'ddd D MMM[,] YYYY',
        timeFormat: null, 
        inputProps: {size: 10}, 
      },
      
      helpMessages: {
        'source': 'Specify the source device for measurements.',
        'population-group': 'Target a group (or cluster of groups) of consumers.',
        'timespan': 'Specify the time range you are interested into.',
        'report-name': 'Select the metric to be applied to measurements.',
        'level': 'Specify the level of detail (unit of time for charts).'
      },
    },
    
    templates: {
      reportTitle: _.template('<%= report.title %> - <%= populationName %>'),
    },    
    
    configForReport: function (props, {config}) {
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
            iconName: 'cube',
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
    // Model
    field: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    level: PropTypes.string.isRequired,
    reportName: PropTypes.string.isRequired,
    source: PropTypes.string,
    timespan: timespanPropType,
    population: populationPropType,
    finished: PropTypes.oneOfType([PropTypes.bool, PropTypes.number]),
    series: PropTypes.arrayOf(seriesPropType),
    
    // Funcs (dispatchers)
    initializeReport: PropTypes.func.isRequired,
    refreshData: PropTypes.func.isRequired,
    setReport: PropTypes.func.isRequired,
    setField: PropTypes.func.isRequired,
    setTimespan: PropTypes.func.isRequired,
    setPopulation: PropTypes.func.isRequired,
    setSource: PropTypes.func.isRequired,

    // Appearence
    fadeIn: PropTypes.oneOfType([
      PropTypes.bool,
      PropTypes.shape({
        className: PropTypes.string, // class to apply to fade-in elements
        duration: PropTypes.number, // the duration of fade-in animation (seconds)
     })
    ]),
  },

  contextTypes: {config: configPropType},
  
  // Lifecycle
  
  getInitialState: function () {
    return {
      draw: true, // should draw chart?
      fadeIn: false, // animation effect in progress
      dirty: false,
      timespan: this.props.timespan,
      error: null,
      formFragment: 'report',
      disabledButtons: '', // a '|' delimited string, e.g 'export|refresh'
    };  
  },
  
  getDefaultProps: function () {
    return {
      field: 'volume',
      level: 'week',
      reportName: null,
      source: null,
      timespan: null,
      population: null,
      finished :null,
      fadeIn: false, // {className: 'fade-in', duration: 0.5},
    };
  },

  componentDidMount: function () {
    var cls = this.constructor;
    var {field, level, reportName} = this.props;

    if (_.isEmpty(field) || _.isEmpty(level) || _.isEmpty(reportName)) {
      return; // cannot yet initialize the target report
    }
     
    var {timespan} = cls.configForReport(this.props, this.context);
    this.props.initializeReport(field, level, reportName, {timespan});
    this.props.refreshData(field, level, reportName)
      .then(() => (this.setState({draw: false})));
  },
  
  componentWillReceiveProps: function (nextProps, nextContext) {
    var cls = this.constructor;
    
    // In any case, reset temporary copy of timespan, clear error/dirty flags
    this.setState({
      error: null,
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
      var {timespan} = cls.configForReport(nextProps, nextContext);
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

  shouldComponentUpdate: function (nextProps, nextState) {
    // Suppress some (rather expensive) updates 

    var changedProps, changedState;
    var ignoredNextState = {
      draw: false, // i.e changed true -> false (a drawing request was fullfilled)
    };

    changedProps = _.differenceWith(
      _.toPairs(nextProps), _.toPairs(this.props), equalsPair
    );
    if (changedProps.length > 0)
      return true; // always update on incoming props

    changedState = _.differenceWith(
      _.toPairs(nextState), _.toPairs(this.state), equalsPair
    );
    changedState = _.differenceWith(
      changedState, _.toPairs(ignoredNextState), equalsPair
    );

    if (changedState.length == 0) console.info('Skipping update of <ReportPanel>'); 
    return (changedState.length > 0);
  },
  
  componentDidUpdate: function () {
    // The component has successfully updated

    // Check if redrawing or just redrawn
    if (this.state.draw) {
      var nextState = {dirty: false};
      if (_.isNumber(this.props.finished)) 
        nextState.draw = false; // next drawing will happen only on-demand
      this.setState(nextState);
    }

    // If under a CSS animation, remember to clear the flag after the animation has ended.
    // This is needed, because only a change "" -> ".fade-in" can trigger a new animation.
    if (this.state.fadeIn) {
      setTimeout(
        () => (this.state.fadeIn && this.setState({fadeIn: false})), 
        (this.props.fadeIn.duration + 0.25) * 1e+3
      );
    }
  },

  render: function () {
    var {field, title, level, reportName, finished, series} = this.props;
    var {dirty, draw, error} = this.state;

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
      <ReportInfo field={field} level={level} reportName={reportName} />
    );
    
    var formFragment = this._renderFormFragment();

    var reportTitle = this._titleForReport();

    return (
      <Panel header={header} footer={footer}>
        <ListGroup fill>
          <ListGroupItem className="report-form-wrapper">
            <form className="report-form form-horizontal">
              <fieldset className={!this.state.fadeIn? '' : this.props.fadeIn.className}>
                {formFragment}
              </fieldset>
            </form>
          </ListGroupItem>
          <ListGroupItem className="report-form-help report-title-wrapper">
            <h4>{reportTitle}</h4>
            <FormStatusParagraph 
              dirty={dirty} errorMessage={!error? null : ErrorMessages[error]}
             />
          </ListGroupItem>
          <ListGroupItem className="report-chart-wrapper">
            <Chart 
              draw={draw}
              field={field} 
              level={level} 
              reportName={reportName} 
              finished={finished}
              series={series}
             />
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
    if (this.state.formFragment != key) {
      var nextState = {formFragment: key};
      if (this.props.fadeIn)
        nextState.fadeIn = true;
      this.setState(nextState);
    }
    return false;
  },

  _performAction: function (key) {
    var {field, level, reportName} = this.props;
    switch (key) {
      case 'refresh':
        {
          console.debug(sprintf(
            'About to refresh data for report (%s, %s, %s)...',
            field, level, reportName
          ));
          this.props.refreshData(field, level, reportName);
          this.setState({draw: true});
        }
        break;
      case 'export':
        // Todo
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
    this.setState({dirty: true});
    return false;
  },
  
  _setField: function (field) {
    this.props.setField(field);
    this.setState({dirty: true});
    return false;
  },

  _setTimespan: function (value) {
    var error = null, timespan = null;
    
    // Validate
    if (_.isString(value)) {
      // Assume a symbolic name is always valid
      timespan = value;
    } else if (_.isArray(value)) {
      // Check if given timespan is a valid range 
      var [t0, t1] = value;
      console.assert(moment.isMoment(t0) && moment.isMoment(t1), 
        'Expected a pair of moment instances');
      error = checkTimespan(value, this.props.level);
      if (!error) {
        // Truncate the time part, we only care about integral days!
        t0.millisecond(0).second(0).minute(0).hour(0);
        t1.millisecond(0).second(0).minute(0).hour(0);
        // Make a pair of timestamps to dispatch upstairs
        timespan = [t0.valueOf(), t1.valueOf()];
      }
    }
    
    // If valid, invoke setTimespan()
    if (!error) {
      var {field, level, reportName} = this.props;
      this.props.setTimespan(field, level, reportName, timespan);
    }
    
    // Update state with (probably invalid) timespan (to keep track of user input)
    this.setState({dirty: true, timespan: value, error});
    return false;
  },
  
  _setPopulation: function (clusterKey, groupKey) {
    var {field, level, reportName} = this.props;
    var {config} = this.context;
    
    var target;
    if (!clusterKey && !groupKey) {
      target = new population.Utility(config.utility.key, config.utility.name);
    } else if (clusterKey && !groupKey) {
      target = new population.Cluster(clusterKey);
    } else if (!clusterKey && groupKey) {
      target = new population.Group(groupKey);
    } else {
      target = new population.ClusterGroup(clusterKey, groupKey);
    }

    this.props.setPopulation(field, level, reportName, target);
    this.setState({dirty: true});
    return false;
  },

  // Helpers
  
  _renderFormFragment: function () {
    var {defaults} = this.constructor;
    var {helpMessages} = defaults;
    var {config} = this.context;
    var {fields, sources, levels} = config.reports.byType.measurements;
    var {level} = this.props;

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
                <p className="help text-muted">{helpMessages['source']}</p>
              </div>
            </div>
          );
        } 
        break;
      case 'report':
        {
          var {reportName} = this.props;
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
                  <p className="help text-muted">{helpMessages['level']}</p>
                </div>
              </div>
            ), (
              <div key="report-name" className="form-group" >
                <label className="col-sm-2 control-label">Metric:</label>
                <div className="col-sm-9">
                  <Select className="select-report"
                    value={reportName} 
                    options={reportOptions} 
                    onChange={(val) => this._setReport(level, val)} 
                   />
                  <p className="help text-muted">{helpMessages['report-name']}</p>
                </div>
              </div>
            ),
          ];
        }
        break;
      case 'timespan':
        {
          var {timespan} = this.state;
          var [t0, t1] = computeTimespan(timespan);

          var datetimeProps = _.merge({}, defaults.datetimeInputProps, {
            inputProps: {
              disabled: _.isString(timespan)? 'disabled' : null
            },
          });
          
          var timespanOptions = new Map(
            Array.from(TimeSpan.common.entries())
              .map(([k, u]) => ([k, u.title]))
              .filter(([k, u]) => checkTimespan(k, level) === 0)
          );
          timespanOptions.set('', 'Custom...');

          fragment1 = (
            <div className="form-group">
              <label className="col-sm-2 control-label">Time:</label>
              <div className="col-sm-9">
                <Select className="select-timespan" 
                  value={_.isString(timespan)? timespan : ''}
                  options={timespanOptions}
                  onChange={(val) => (this._setTimespan(val? (val) : ([t0, t1])))}
                 />
                &nbsp;&nbsp;
                <DatetimeInput {...datetimeProps} 
                  value={t0.toDate()} 
                  onChange={(val) => (this._setTimespan([val, t1]))} 
                 />
                &nbsp;-&nbsp;
                <DatetimeInput {...datetimeProps} 
                  value={t1.toDate()}
                  onChange={(val) => (this._setTimespan([t0, val]))} 
                 />
                <p className="help text-muted">{helpMessages['timespan']}</p>
              </div>
            </div>
          );
        }
        break;
      case 'population-group':
        {
          var target = this.props.population;
          var {clusters} = config.utility;
          
          var clusterOptions = [
            {
              group: null, 
              options: new Map([['', 'None']])
            }, {
              group: 'Cluster By:', 
              options: new Map(clusters.map(c => ([c.key, c.name ])))
            },
          ];

          var [clusterKey, groupKey] = population.extractGroupParams(target);
          var selectedCluster = !clusterKey? null : clusters.find(c => (c.key == clusterKey));

          var groupOptions = [
            {
              group: clusterKey? 'All groups' : 'No groups',
              options: new Map([['', clusterKey? 'All' : 'Everyone']]),
            }, {
              group: 'Pick a specific group:',
              options: !clusterKey? [] : new Map(
                selectedCluster.groups.map(g => ([g.key, selectedCluster.name + ': ' + g.name]))
              ),
            },
          ];

          fragment1 = (
            <div className="form-group">
              <label className="col-sm-2 control-label">Group:</label>
              <div className="col-sm-9">
                <Select className='select-cluster'
                  value={clusterKey || ''}
                  onChange={(val) => this._setPopulation(val, null)}
                  options={clusterOptions}
                 />
                &nbsp;&nbsp;
                <Select className='select-cluster-group'
                  value={groupKey || ''}
                  onChange={(val) => this._setPopulation(clusterKey, val)}
                  options={groupOptions}
                 />
                <p className="help text-muted">{helpMessages['population-group']}</p>
              </div> 
            </div>
          );
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
    
    // Todo This fuctionality fits better to the Toolbar component, 
    // e.g.: <Toolbar spec={spec} enabledButtons={flags1} activeButtons={flags2} ... />

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

  _titleForReport: function () {    
    var {config} = this.context;
    var {configForReport, templates} = this.constructor;
    var {population: target} = this.props;

    var report = configForReport(this.props, {config});
    
    // Find a friendly name for population target
    var populationName = target? target.name : 'Utility';
    if (target instanceof population.Utility || target == null) {
      populationName = 'Utility'; //config.utility.name;  
    } else if (target instanceof population.Cluster) {
      var cluster = config.utility.clusters.find(c => c.key == target.key);
      populationName = 'Cluster by: ' + cluster.name;
    } else if (target instanceof population.ClusterGroup) {
      var cluster = config.utility.clusters.find(c => c.key == target.clusterKey);
      var group = cluster.groups.find(g => g.key == target.key);
      populationName = cluster.name + ': ' + group.name;
    }

    return templates.reportTitle({report, populationName});
  },

  // Wrap dispatch actions
});

var ReportForm = React.createClass({
  statics: {
    defaults: {
      datetimeInputProps: {
        closeOnSelect: true,
        dateFormat: 'ddd D MMM[,] YYYY',
        timeFormat: null, 
        inputProps: {size: 9}, 
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

      this.setState({dirty: false, error: null});
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

    var datetimeProps = _.merge({}, cls.defaults.datetimeInputProps, {
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
    
    var [clusterKey, groupKey] = population.extractGroupParams(target);
    var groupOptions = !clusterKey? [] :
      config.utility.clusters
        .find(c => (c.key == clusterKey))
          .groups.map(g => ({value: g.key, text: g.name}));
    
    var selectSource = (
      <Select className="select-source" value={source} onChange={this._setSource}>
        {sourceOptions.map(Option)}
      </Select>
    );
    
    var selectTimespan = (
      <Select className="select-timespan" 
        value={_.isString(timespan)? timespan : ''} 
        onChange={(val) => (this._setTimespan(val? (val) : ([t0, t1])))}
       >
        {timespanOptions.map(Option)}
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
          {clusterOptions.map(Option)}
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
          {groupOptions.map(Option)}
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
    var statusParagraph = (
      <FormStatusParagraph 
        dirty={this.state.dirty} errorMessage={!error? null : ErrorMessages[error]} 
       />
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
          {statusParagraph}
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
          {statusParagraph}
        </form>
      );
    }
    
    return form;
  },

  // Event handlers

  _toggleCollapsed: function () {
    this.setState({collapsed: !this.state.collapsed});
  },

  _setTimespan: function (value) {
    var error = null, timespan = null;
    
    // Validate
    if (_.isString(value)) {
      // Assume a symbolic name is always valid
      timespan = value;
    } else if (_.isArray(value)) {
      // Check if given timespan is a valid range 
      console.assert(value.length == 2 && value.every(t => moment.isMoment(t)), 
        'Expected a pair of moment instances');
      error = checkTimespan(value, this.props.level);
      if (!error)
        timespan = [value[0].valueOf(), value[1].valueOf()];
    }
    
    // If valid, invoke setTimespan()
    if (timespan != null)
      this.props.setTimespan(timespan);
    
    // Update state with a (probably invalid) timespan (to keep track of user input)
    this.setState({dirty: true, timespan: value, error});
    
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

var ReportInfo = React.createClass({
  statics: {},

  propTypes: {
    field: PropTypes.string.isRequired,
    level: PropTypes.string.isRequired,
    reportName: PropTypes.string.isRequired,
    requested: PropTypes.number,
    finished: PropTypes.oneOfType([PropTypes.bool, PropTypes.number]),
    errors: PropTypes.arrayOf(PropTypes.string),
    series: PropTypes.arrayOf(seriesPropType),
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
      // Found an initialized intance of the report for (field, level, reportName)
      var {source, timespan, population, series, finished} = r1;
      _.extend(stateProps, {source, timespan, population, series, finished});
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

ReportForm = ReactRedux.connect(
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
)(ReportForm);

ReportInfo = ReactRedux.connect(
  (state, ownProps) => {
    var {field, level, reportName} = ownProps;
    var _state = state.reports.measurements;
    var key = computeKey(field, level, reportName, REPORT_KEY);
    return !(key in _state)? {} : 
      _.pick(_state[key], ['requested', 'finished', 'requests', 'errors', 'series']
    );
  },
  null
)(ReportInfo);

//
// Export
//

var ChartContainer = require('./chart-container');

module.exports = {
  Panel: ReportPanel,
  Form: ReportForm, 
  Info: ReportInfo, 
  Chart: (props) => (<ChartContainer {...props} reportKey={REPORT_KEY} />),
};
