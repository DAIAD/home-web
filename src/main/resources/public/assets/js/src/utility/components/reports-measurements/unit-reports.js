
var _ = require('lodash');
var moment = require('moment');
var numeral = require('numeral');
var sprintf = require('sprintf');
var echarts = require('react-echarts');

var React = require('react');
var {connect} = require('react-redux');

var MeasurementValue = require('./measurement-value');
var {pairWithNext, padRight: padArrayRight, padLeft: padArrayLeft} = require('../../helpers/array-funcs');
var {generateTimestamps} = require('../../helpers/timestamps');
var {computeKey} = require('../../reports').measurements;

var PropTypes = React.PropTypes;
var {seriesPropType, populationPropType} = require('../../prop-types');
var reportPropType = PropTypes.shape({
  level: PropTypes.string.isRequired,
  reportName: PropTypes.string.isRequired,
  startsAt: PropTypes.string.isRequired,
  duration: PropTypes.array.isRequired,
});    

var views = require('./views');

var computeTimeRange = function(duration, startsAt, now) {
  // Get a time range suitable for a given duration and starting point
  now = moment(now);
  var t0 = moment(now).startOf(startsAt);
  var t1 = moment(t0).add(...duration);
  t0 = t0.valueOf(); 
  t1 = t1.valueOf();
  return (t0 < t1)? [t0, t1] : [t1, t0];
}

var debugData = function (data, keys) {
  for (var k of keys) {
    var d = data.get(k).points, f = 'ddd D MMM YYYY [@]HH:mm';
    console.info(sprintf(
      '%d points: From %s To %s',
      d.length, moment(d[0][0]).format(f), moment(d[d.length - 1][0]).format(f)
    ));
  }
};

var checkData = function (data, keys, k0, unit, level) {
  if (!keys.length)
    throw new Error('No data received');
  if (!keys.every(k => _.isNumber(k)))
    throw new Error('Expected number keys!');
  
  const U = moment.duration(1, unit).valueOf();
  const step = moment.duration(1, level).valueOf(); 

  if ((U < step) || (U % step !== 0))
    throw new Error(sprintf(
      'Expected that unit (%s) is a multiple of step (%s)', unit, level
    ));

  const checkStepInsideUnit = (k) => (
    data.get(k).points
      .map(v => v[0])
      .map(pairWithNext)
      .slice(0, -1)
      .every(([ta, tb]) => (
        // Note Must compute the diff at the given level (not as milliseconds!): 
        // not all days have 24 hours (DST), not all months 30 days etc.
        moment(tb).diff(ta, level, true) === 1
      ))
  );
  
  if (!(keys.every(checkStepInsideUnit)))
    throw new Error(sprintf(
      'Expected that data points have regular steps (1 %s)', level
    ));

  const checkStepToNextUnit = ([ka, kb]) => {
    var pa = _.last(data.get(ka).points); 
    var pb = _.first(data.get(kb).points);
    return (moment(pb[0]).diff(pa[0], level, true) === 1);
  };
 
  if (!(keys.map(pairWithNext).slice(0, -1).every(checkStepToNextUnit)))
    throw new Error(sprintf(
      'Expected that the step to the next unit is 1 %s', level
    ));
  
  var i0 = keys.indexOf(k0);
  if (i0 < 0) 
    throw new Error(sprintf(
      'No consumption data for current time unit (%s)', unit));
  else if (i0 === 0)
    console.error(sprintf(
      'No consumption data for previous time unit (%s)', unit));

  return;
}

var densifyPoints = function (points, defaultValue, unit, level, startsAt) {
  // Make a dense array of points (ensure regular step)
  
  var p0 = points.map(p => p[0]);
  var t0 = _.first(p0);
  var ts = moment(t0).utc().startOf(startsAt);
  var te = moment(t0).utc().endOf(startsAt);
  var n = _.ceil(moment(te).diff(ts, level, true), 3);
  
  console.assert(n >= points.length, 'Received too many points!');
  
  if (n == points.length) {
    // This array doesnt seem to have gaps: return as is
    return points;
  }
  
  var timestamps = Array.from(generateTimestamps(ts, te, level));
  console.assert(n == timestamps.length, 
    sprintf('Expected exactly %d timestamps (got %.1f)', timestamps.length, n));
  
  console.debug(sprintf(
    'About to densify points for report (%s, level: %s): +%d points', 
     unit, level, n - points.length
  ));

  var result = timestamps.map(pairWithNext)
    .reduce((res, [tx, tx1]) => {
      var i0;
      // If a data point concurs with tx, pick it
      i0 = p0.indexOf(tx);
      if (i0 >= 0) {
        res.push([tx, points[i0][1]]);
        return res;
      }
      
      // If a data point falls inside the bucket of (tx, tx1), pick it
      i0 = p0.findIndex(t => (t > tx && (tx1 == null || t < tx1)));
      if (i0 >= 0) {
        console.info(sprintf(
          'A data point at `%s` is picked for the bucket of 1 %s starting at `%s`',
          moment(points[i0][0]).format(), level, moment(tx).format()
        ));
        res.push([tx, points[i0][1]]);
        return res;
      } 

      // Nothing found, push default value
      res.push([tx, defaultValue]);
      return res;
    }, []);

  return result;
};

//
// Presentational components
//

class _Report extends React.Component {
  
  static get unit() {
    return 'ms'; // override!
  }
  
  static momentToKey(t) {
    return moment(t).valueOf(); // override!
  }
  
  static get defaults() {
    return {
      charts: {
        xaxis: {
          // The x-axis covers exactly the examined period
          normal: {
            formatter: (t) => (moment(t).utc().format()),
          },
          // The x-axis spans over the examined period (e.g at forecast charts)
          wide: {
            formatter: (t) => (moment(t).utc().format()),
          },
          // The x-axis for the comparison chart
          comparison: {
            formatter: (t) => (moment(t).utc().format()),
          },
        },
        legend: true,
        lineWidth: 1,
        color: ['#2D6E8D', '#DB5563', '#9056B4',],
        grid: {x: '50', x2: '22', y: '30', y2: '30'},
        yAxis: {
          formatter: (y) => (numeral(y).format('0.0a')),
        },
      },
    };
  } 
  
  static _propsToState(props) {
    // Compute state from received props.
    var cls = this;

    var {unit} = cls;
    var {now, report: {level, startsAt, duration: [K, unit1]}, series} = props;
    var data, keys, totals, forecast;
    
    if (unit != unit1)
      throw new Error(sprintf(
        'Expected that report\'s unit (%s) is same with ours (%s)', 
        unit1, unit
      ));
    
    // A moment that represents the period under examination
    var moment0 = moment(now);
    moment0 = moment0.add(moment0.utcOffset(), 'minute').utc(); // move to same wall-clock in UTC
    moment0 = moment0.add(-1, unit).startOf(startsAt);
    
    // The key of this period (e.g. day-of-year)
    var k0 = cls.momentToKey(moment0);
    
    if (!series)
      return {moment0, data: null};
    
    if (level != series.granularity.toLowerCase())
      throw new Error(sprintf(
        'Expected that report\'s level (%s) is same with series granularity (%s)',
        level, series.granularity
      ));

    // Sort data points by timestamp
    var seriesData = _.sortBy(series.data, 0); 
    
    // Group series data by our key (e.g. day-of-year)
    var densify = _.partialRight(densifyPoints, unit, level, startsAt);
    data = new Map(
      _.toPairs(
        _.groupBy(seriesData, v => cls.momentToKey(v[0]))
      )
      .map(([k, p]) => ([Number(k), {points: densify(p, 0)}]))
      // Sort pairs in-place by the first timestamp
      .sort(([k1, p1], [k2, p2]) => (p1.points[0][0] - p2.points[0][0])) 
    );
    keys = Array.from(data.keys());
    
    // Validate received data
    try {
      checkData(data, keys, k0, unit, level);
    } catch (er) {
      console.error('Check has failed: ' + er.message);
      return {moment0, data: false};
    }

    // Do we have forecast data? Todo maybe as separate series?
    var i0 = keys.indexOf(k0), k1r = keys[i0 + 1];
    forecast = false; // Fixme k1r && (data.get(k1r).points.length >= N);
    
    // Compute total consumption for each slot in our window
    totals = new Map(
      Array.from(data.entries())
        .map(([k, p]) => ([k, _.sumBy(p.points, 1)]))
    );
    
    // For each slot, add a closure point (preferrably the 1st point of next slot).
    keys.map(pairWithNext).forEach(([k1, k2]) => {
      var [t1e, v1e] = _.last(data.get(k1).points);
      var [t2s, v2s] = k2? _.first(data.get(k2).points) : [];
      var t1c = moment(t1e).add(1, level).valueOf();
      data.get(k1).closurePoint = [
        t1c,
        (t2s && (t1c == t2s))? v2s : null,
      ];
    });
    
    return {moment0, data, keys, totals, forecast};
  }

  constructor(props) {
    super(props);
    var initialState = this.constructor._propsToState(props);
    this.state = {...initialState, shouldRefresh: true};
  }

  componentDidMount() {
    this._initialize();
    if (this.props.visible) {
      this._refreshData();
    }
  }
  
  componentWillReceiveProps(nextProps) {
    var nextState = this.constructor._propsToState(nextProps);
    
    // Decide if a refresh should be requested (when we update and become visible)  
    nextState.shouldRefresh = (
      (!this.props.visible && this.state.shouldRefresh) || // carry a shouldRefresh from past changes
      (nextProps.now != this.props.now) ||
      (nextProps.field != this.props.field) ||
      (nextProps.target != this.props.target) ||
      (nextProps.source != this.props.source) 
    );
    
    this.setState(nextState);
  }

  shouldComponentUpdate(nextProps, nextState) {
    // Skip unwanted updates caused by internal state changes
    return (
      (nextProps.now != this.props.now) ||
      (nextProps.field != this.props.field) ||
      (nextProps.target != this.props.target) ||
      (nextProps.source != this.props.source) ||
      (nextProps.visible != this.props.visible) ||
      (nextProps.series != this.props.series) 
    );
  }

  componentDidUpdate() {
    if (this.props.visible && this.state.shouldRefresh) {
      this._refreshData();
    }
  }
  
  render() {
    var {children} = this.props;
    return (
      <div className="clearfix unit-view">
        {React.Children.map(children, (c) => (this._rendererForView(c.type)(c.props)))}
      </div>
    );
  } 
  
  // Helper methods for dispatch props

  _initialize() {
    return this.props.initialize();
  }
  
  _refreshData() {
    // Schedule a refresh after this state transition
    var delay = 400 + Math.floor(Math.random() * 200); // millis
    setTimeout(this.props.refreshData, delay);
    this.setState({shouldRefresh: false});
  }

  // Provide specific views

  _rendererForView(viewType) {
    // Map view (by class) to a rendering method
    
    var method = () => (null);
    switch (viewType) {
      case views.Summary:
        method = this._renderSummary;
        break;
      case views.SimpleChart:
        method = this._renderSimpleChart;
        break;
      case views.ComparisonChart:
        method = this._renderComparisonChart;
        break;
      case views.ForecastingChart:
        method = this._renderForecastingChart;
        break;
    }
    return method.bind(this);
  }

  _renderSummary(viewProps) {
    // A summary of the examined perid

    var {title, formatDate, field, uom} = this.props;
    var {moment0, data, keys, totals} = this.state;
    
    var k0 = this.constructor.momentToKey(moment0), i0, k1;
    if (data) {
      i0 = keys.indexOf(k0);
      k1 = keys[i0 - 1];
    }
    return (
      <div className={viewProps.className} key={'summary'}>
        <MeasurementValue 
          title={formatDate(moment0)}
          subtitle={title} 
          field={field} 
          unit={uom}
          value={totals? totals.get(k0) : null}
          prevValue={(totals && k1)? totals.get(k1) : null}
        />
      </div>
    );
  }

  _renderForecastingChart(viewProps) {
    // A forecasting chart that spans over 2 periods: {k0, k1r}
    
    const {defaults, unit, momentToKey} = this.constructor;
    var {report: {level}, series, title, formatDate, field, uom} = this.props;
    var {moment0, data, keys, forecast} = this.state;
    
    if (!forecast) {
      return (<div>No forecasting data!</div>);
    }

    var k0 = momentToKey(moment0), i0, k1;
    if (data) {
      i0 = keys.indexOf(k0);
    }
    
    var chartProps = {
      legend: defaults.charts.legend,
      lineWidth: defaults.charts.lineWidth,
      grid: defaults.charts.grid,
      color: defaults.charts.color,
      loading: (data != null)? false : {text: 'Loading...'},
      yAxis: {
        ...defaults.charts.yAxis,
        name: sprintf('%s (%s)', field, uom)
      },
    };
   
    var k1r = data? keys[i0 + 1] : null;
    
    // Fixme: This computation for N is not always correct
    var N = moment.duration(1, unit).as(level); 
    
    return (
      <div className={viewProps.className} key={"forecasting-chart"}>
        <echarts.LineChart
          {...chartProps}
          xAxis={{
            ...defaults.charts.xaxis.wide,
            data: !data? [] : []
              .concat(data.get(k0).points, data.get(k1r).points)
              .map(v => v[0]),
          }}
          series={[
            // Actual consumption (1st half)
            {
              data: data? padArrayRight(data.get(k0).points.map(v => v[1]), 2 * N) : null,
              name: 'Consumption',
            },
            // Forecast (2nd half)
            {
              data: data? padArray(data.get(k1r).points.map(v => v[1]), 2 * N) : null,
              name: 'Forecast'
            },
          ]}
         /> 
      </div>
    ); 
  }
  
  _renderSimpleChart(viewProps) {
    // A simple chart for the exact period
    
    const {defaults, unit, momentToKey} = this.constructor;
    var {report: {level}, series, title, formatDate, field, uom} = this.props;
    var {moment0, data, keys} = this.state;
    
    var k0 = momentToKey(moment0), i0, k1, data0, data0p1;
    if (data) {
      i0 = keys.indexOf(k0);
      k1 = keys[i0 - 1];
      data0 =  data.get(k0);
      data0p1 = data0.points.concat([data0.closurePoint]);
    }
    
    var chartProps = {
      legend: defaults.charts.legend,
      lineWidth: defaults.charts.lineWidth,
      grid: defaults.charts.grid,
      color: defaults.charts.color,
      loading: (data != null)? false : {text: 'Loading...'},
      yAxis: {
        ...defaults.charts.yAxis,
        name: sprintf('%s (%s)', field, uom)
      },
    }; 
    return (
      <div className={viewProps.className} key={"simple-chart"}>
        <echarts.LineChart 
          {...chartProps}
          xAxis={{
            ...defaults.charts.xaxis.normal,
            data: data? data0p1.map(v => v[0]) : [], 
          }}
          series={[
            {
              data: data? data0p1.map(v => v[1]) : [],
              name: formatDate(moment0),
            }
          ]}
         />
      </div>
    ); 
  }

  _renderComparisonChart(viewProps) {
    // A comparison chart (compare last K periods)
    
    const {defaults, unit, momentToKey} = this.constructor;
    var {report: {duration: [K, unit1], level}, series, title, formatDate, field, uom} = this.props;
    var {moment0, data, keys} = this.state;
    
    var k0 = momentToKey(moment0), i0, k1;
    if (data) {
      i0 = keys.indexOf(k0);
      k1 = keys[i0 - 1];
    }
    
    var chartProps = {
      legend: defaults.charts.legend,
      lineWidth: defaults.charts.lineWidth,
      grid: defaults.charts.grid,
      color: defaults.charts.color,
      loading: (data != null)? false : {text: 'Loading...'},
      yAxis: {
        ...defaults.charts.yAxis,
        name: sprintf('%s (%s)', field, uom)
      },
    };
    
    var comparisonKeys = (keys || []).filter((k, i) => (i <= i0)).slice(K);
    var kX = _.maxBy(comparisonKeys, k => data.get(k).points.length);
    var dataX = data? data.get(kX) : null;
    
    return (
      <div className={viewProps.className} key={"comparison-chart"}>
        <echarts.LineChart 
          {...chartProps}
          xAxis={{
            ...defaults.charts.xaxis.comparison,
            data: data? dataX.points.map(v => v[0]).concat(dataX.closurePoint[0]) : []
          }}
          series={!data? [] : comparisonKeys.reverse()
            .map(k => {
              var dataY = data.get(k);
              return {
                data: padArrayRight(
                  dataY.points.map(v => v[1]).concat([dataY.closurePoint[1]]),
                  dataX.points.length + 1
                ),
                name: sprintf('%s', formatDate(dataY.points[0][0], true)),
              };
            })
          }
       />
      </div>
    );
  }
}

_Report.defaultProps = {
  source: 'meter',
  field: 'volume',
  uom: null,
  reportKey: null,
  visible: true,
  report: null,
  formatDate: (m, brief=false) => (moment(m).format('LTS')),
  target: null,
  series: null,
};
  
_Report.propTypes = {
  source: PropTypes.string,
  field: PropTypes.string.isRequired,
  uom: PropTypes.string.isRequired,
  now: PropTypes.number.isRequired,
  reportKey: PropTypes.string.isRequired,
  visible: PropTypes.bool,
  report: reportPropType,
  target: populationPropType,
  title: PropTypes.string,
  formatDate: PropTypes.func,
  // Injected from ReactRedux
  series: seriesPropType,
  initialize: PropTypes.func,
  refreshData: PropTypes.func,
};

// Daily

class ReportByDay extends _Report {
  
  static get unit() {
    return 'day';
  }
  
  static momentToKey(t) {
    return moment(t).utc().dayOfYear();
  }
  
  static get defaults() {
    return _.merge({}, _Report.defaults, {
      charts: {
        xaxis: {
          normal: {
            formatter: (t) => (moment(t).utc().format('hA')),
          },
          wide: {
            formatter: (t) => {
              var m = moment(t).utc();
              return (m.hour() != 0)? m.format('hA') : m.format('ddd hA');
            },
          },
          comparison: {
            formatter: (t) => (moment(t).utc().format('hA')),
          },
        },
      },
    });
  }
};

ReportByDay.defaultProps = _.extend({}, _Report.defaultProps, {
  formatDate: (m, brief=false) => (
    moment(m).utc().format(brief? 'D/MMM' : 'ddd D MMM')
  ),
  title: 'Daily Consumption',
});

// Weekly

class ReportByWeek extends _Report {
  
  static get unit() {return 'week';}
  
  static momentToKey(t) {
    return moment(t).utc().isoWeek();
  }
  
  static get defaults() {
    return _.merge({}, _Report.defaults, {
      charts: {
        xaxis: {
          normal: {
            formatter: (t) => (moment(t).utc().format('dd')),
          },
          wide: {
            formatter: (t) => (moment(t).utc().format('D/M')),
          },
          comparison: {
            formatter: (t) => (moment(t).utc().format('dd')),
          },
        },
      },
    });
  }
};

ReportByWeek.defaultProps = _.extend({}, _Report.defaultProps, {
  formatDate: (m, brief=false) => {
    m = moment(m).utc();
    if (brief) 
      return m.format('[Week #]W');
    var m0 = m.clone().startOf('isoweek');
    var m1 = m.clone().endOf('isoweek');
    return m0.format('D MMM') + ' - ' + m1.format('D MMM');
  },
  title: 'Weekly Consumption',
});

// Monthly 

class ReportByMonth extends _Report {
  
  static get unit() {return 'month';}
  
  static momentToKey(t) {
    return moment(t).utc().month();
  }
  
  static get defaults() {
    return _.merge({}, _Report.defaults, {
      charts: {
        xaxis: {
          normal: {
            formatter: (t) => (moment(t).utc().format('dd D')),
          },
          wide: {
            formatter: (t) => (moment(t).utc().format('D MMM')),
          },
          comparison: {
            labelFilter: (i, t) => (i % 5 == 0), // place 1 every 5 items
            formatter: (t, i) => (moment(t).utc().format('D')),
          },
        },
      },
    });
  }
};

ReportByMonth.defaultProps = _.extend({}, _Report.defaultProps, {
  formatDate: (m, brief=false) => (
    moment(m).utc().format(brief? 'MMM' : 'MMMM YYYY')
  ),
  title: 'Monthly Consumption',
});

// Yearly

class ReportByYear extends _Report {
  
  static get unit() {return 'year';}
  
  static momentToKey(t) {
    return moment(t).utc().year();
  }
  
  static get defaults() {
    return _.merge({}, _Report.defaults, {
      charts: {
        xaxis: {
          normal: {
            formatter: (t) => (moment(t).utc().format('MMM')),
          },
          wide: {
            formatter: (t) => (moment(t).utc().format('MMM')),
          },
          comparison: {
            formatter: (t, i) => (moment(t).utc().format('MMM')),
          },
        },
      },
    });
  }
};

ReportByYear.defaultProps = _.extend({}, _Report.defaultProps, {
  formatDate: (m, brief=false) => (
    moment(m).utc().format('YYYY')
  ),
  title: 'Yearly Consumption',
});

//
// Container components
//

var actions = require('../../actions/reports-measurements');

var mapStateToProps = function (state, ownProps) {
  // A default implementation for a Redux state selector.
  var {field, reportKey, report: {duration, level, reportName}} = ownProps;
  var [n, unit] = duration;

  var _state = state.reports.measurements;
  var series = null;
  if (!_.isEmpty(_state)) {
    var k = computeKey(field, level, reportName, [reportKey, unit]);
    series = (k in _state)? _.first(_state[k].series) : null;
  }
  return {series};
}

var mapDispatchToProps = function(dispatch, ownProps) {
  // A default implementation for a Redux dispatch mapper.

  var {now, field, source, target} = ownProps;
  var {reportKey, report: {startsAt, duration, level, reportName}} = ownProps;
  
  var ts = computeTimeRange(duration, startsAt, now);
  var [n, unit] = duration;

  var reportArgs = [field, level, reportName, [reportKey, unit]];

  var initialize = () => (dispatch(actions.initialize(...reportArgs)));

  var refreshData = () => {
    dispatch(actions.setTimespan(...reportArgs, ts));
    if (target)
      dispatch(actions.setPopulation(...reportArgs, target));
    if (source)
      dispatch(actions.setSource(...reportArgs, source));
    return dispatch(actions.refreshData(...reportArgs));   
  };

  return {initialize, refreshData};
}

module.exports = {
  ReportByDay: connect(mapStateToProps, mapDispatchToProps)(ReportByDay),
  ReportByWeek: connect(mapStateToProps, mapDispatchToProps)(ReportByWeek),
  ReportByMonth: connect(mapStateToProps, mapDispatchToProps)(ReportByMonth),
  ReportByYear: connect(mapStateToProps, mapDispatchToProps)(ReportByYear),
}; 

