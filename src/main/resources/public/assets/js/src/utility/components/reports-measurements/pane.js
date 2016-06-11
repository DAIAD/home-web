
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

var {Button, ButtonGroup, ButtonToolbar, Tooltip, OverlayTrigger} = Bootstrap;
var {Collapse, Panel, ListGroup, ListGroupItem} = Bootstrap;
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

var ReportPanel = React.createClass({
  
  statics: {
  },

  propTypes: {
    field: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
  },

  contextTypes: {config: configPropType},
  
  // Lifecycle
  
  getInitialState: function () {
    return {
      level: 'week',
      reportName: 'avg-daily-avg',
    };  
  },

  render: function () {
    var {field, title} = this.props;
    
    var header = (
      <div className="header-wrapper">
        {/*<i className="fa fa-area-chart fa-fw"></i>&nbsp;*/}
        <h3>{title}</h3>
        <ButtonGroup>
          <OverlayTrigger placement="bottom" 
            overlay={(<Tooltip id="tooltip--select-source">Select source of measurements</Tooltip>)}
           > 
            <Button onClick={this._prepareFormForSource}>
              <i className="fa fa-cube fa-fw" ></i>
            </Button>
          </OverlayTrigger>
          <OverlayTrigger placement="bottom" 
            overlay={(<Tooltip id="tooltip--select-report">Choose a type of report</Tooltip>)}
           > 
            <Button onClick={this._prepareFormForReport}>
              <i className="fa fa-area-chart fa-fw" ></i>
            </Button>
          </OverlayTrigger> 
          <OverlayTrigger placement="bottom" 
            overlay={(<Tooltip id="tooltip--select-timespan">Define a time range</Tooltip>)}
           > 
            <Button onClick={this._prepareFormForTime}>
              <i className="fa fa-calendar fa-fw" ></i>
            </Button>
          </OverlayTrigger>
          <OverlayTrigger placement="left" 
            overlay={(<Tooltip id="tooltip--select-population-group">Define a population target</Tooltip>)}
           > 
            <Button onClick={this._prepareFormForPopulation}>
              <i className="fa fa-users fa-fw" ></i>
            </Button>
          </OverlayTrigger> 
        </ButtonGroup> 
      </div>
    );
    
    // Todo Provide an <Info> element
    var footer = (
      <div className="report-info">
        Info goes here!!
      </div>
    );

    return (
      <Panel header={header} footer={footer}>
        <ListGroup fill>
          <ListGroupItem className="report-form-wrapper">
            Parameters!
          </ListGroupItem>
          <ListGroupItem className="report-chart-wrapper">
            The chart goes here!
          </ListGroupItem>
        </ListGroup>
      </Panel>
    );
  },

  // Event handlers

  _prepareFormForReport: function () {
    console.info('Prepare form ...');
  },
  
  _prepareFormForSource: function () {
    console.info('Prepare form ...');
  },
  
  _prepareFormForTime: function () {
    console.info('Prepare form ...');
  },
  
  _prepareFormForPopulation: function () {
    console.info('Prepare form ...');
  },

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
          {this._markupHelp()}
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
          {this._markupHelp()}
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
  
  _markupHelp: function () {
    
    var {errorMessage, dirty} = this.state;
    var paragraph;
    
    if (errorMessage) {
      paragraph = (<p className="help text-danger">{errorMessage}</p>);
    } else if (dirty) {
      paragraph = (<p className="help text-info">Parameters have changed. Refresh to redraw data!</p>); 
    } else {
      paragraph = (<p className="help text-muted">Refresh to redraw data.</p>);
    }
    return paragraph;
  },

  _markupSummary: function (source, [t0, t1], [clusterKey, groupKey]) {
    var _config = this.context.config.reports.byType.measurements;
    
    const openingBracket = (<span>&#91;</span>); 
    const closingBracket = (<span>&#93;</span>);
    const delimiter = (<span>::</span>);

    // Todo a more friendly summary of supplied parameters
    
    t0 = moment(t0); t1 = moment(t1);
    var dateFormat = (t0.year() == t1.year())? 'D/MMM' : 'D/MMM/YYYY';
    var formattedTime = sprintf(
      'From %s To %s', t0.format(dateFormat), t1.format(dateFormat)
    ); 

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
      width: '100%',
      height: 320,
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

var actions = require('../../actions/reports-measurements');

ReportPanel = ReactRedux.connect(
  (state, ownProps) => {
    var stateProps = {};
    
    var {field, title} = ownProps;
    var {fields} = state.config.reports.byType.measurements;
    
    if (!title)
      stateProps.title = fields[field].title;
    
    return stateProps;
  }, 
  (dispatch, ownProps) => {
    return {
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
    return {
      initializeReport: (defaults) => (
        dispatch(actions.initialize(field, level, reportName, REPORT_KEY, defaults))),
      setSource: (source) => (
        dispatch(actions.setSource(field, level, reportName, REPORT_KEY, source))),
      setTimespan: (ts) => (
        dispatch(actions.setTimespan(field, level, reportName, REPORT_KEY, ts))),
      setPopulation: (p) => (
        dispatch(actions.setPopulation(field, level, reportName, REPORT_KEY, p))),
      refreshData: () => (
        dispatch(actions.refreshData(field, level, reportName, REPORT_KEY))),
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
