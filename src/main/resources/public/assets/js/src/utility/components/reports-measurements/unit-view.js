
var _ = require('lodash');
var numeral = require('numeral');
var sprintf = require('sprintf');
var moment = require('moment');
var React = require('react');

var echarts = require('../react-echarts');
var MeasurementValue = require('./measurement-value');

var PropTypes = React.PropTypes;
var {seriesPropType} = require('../../prop-types');

var {pairWithNext, padRight: padArrayRight, padLeft: padArrayLeft} = require('../../util/array-func');
var {generateTimestamps} = require('../../util/timestamps');

const FIELD = 'volume';

//
// Report components based on a time unit (e.g by day, week ...)
//

class _View extends React.Component {
  
  static get unit() {
    return 'ms'; // override!
  }
  
  static momentToKey(t) {
    return moment(t).valueOf();
  }

  static get defaults() {
    return {
      charts: {
        xaxis: {
          // The x-axis covers exactly the examined period
          normal: {
            formatter: (t) => (moment(t).format('LTS')),
          },
          // The x-axis spans over the examined period (e.g at forecast charts)
          wide: {
            formatter: (t) => (moment(t).format('LTS')),
          },
          // The x-axis for the comparison chart
          comparison: {
            formatter: (t) => (moment(t).format('LTS')),
          },
        },
        legend: true,
        lineWidth: 1,
        color: ['#2D6E8D', '#DB5563', '#9056B4',],
        grid: {x: '55', x2: '30', y: '30', y2: '30'},
        yAxis: {
          formatter: (y) => (numeral(y).format('0.0a')),
        },
      },
    };
  } 

  static _debugData(data, keys) {
    for (var k of keys) {
      var d = data.get(k).points;
      var f = 'ddd D MMM YYYY [@]HH:mm';
      console.info(sprintf(
        '[key=%d] %d points: From %s To %s',
        k,
        d.length,
        moment(d[0][0]).format(f),
        moment(d[d.length - 1][0]).format(f)
      ));
    }
  }

  static _densifyPoints(points, defaultValue, level, startsAt) {
    // Make a dense array of points (ensure regular step)
    
    var {unit} = this;
    var p0 = points.map(p => p[0]);
    var t0 = _.first(p0);
    var ts = moment(t0).startOf(startsAt);
    var te = moment(t0).endOf(startsAt);
    var n = _.ceil(moment.duration(te - ts).as(level), 3);
    
    console.assert(n >= points.length, 'Received too many points!');

    if (n == points.length) {
      // This array doesnt seem to have gaps: return it
      return points;
    }
    
    var timestamps = Array.from(generateTimestamps(ts, te, level));
    console.assert(n == timestamps.length, 
      'Expected exactly ' + n + ' timestamps');
    
    console.info(sprintf(
      'About to densify points for report (%s, level: %s): +%d points', 
       moment.duration(te - ts).humanize(),
       level,
       n - points.length
    ));

    return timestamps.reduce((res, t) => {
      // If found, include values from the source array
      var i0 = p0.indexOf(t);
      res.push([t, (i0 < 0)? defaultValue : points[i0][1]]);
      return res;
    }, []);
  }

  static _propsToState(props) {
    // Compute state from received props.
    
    var {unit} = this;
    var {now, level, startsAt, duration: [K, unit1], series} = props;
    
    if (unit != unit1)
      throw new Error(sprintf(
        'Expected that report\'s unit (%s) is same with ours (%s)', 
        unit1, unit
      ));
    
    var data, keys, totals, forecast;
    
    // A moment that represents the period under examination
    var moment0 = moment(now).add(-1, unit).startOf(startsAt);
    // The key of this period (e.g. day-of-year)
    var k0 = this.momentToKey(moment0);
    
    if (!series)
      return {moment0};
    
    if (level != series.granularity.toLowerCase())
      throw new Error(sprintf(
        'Expected that report\'s level (%s) is same with series granularity (%s)',
        level, series.granularity
      ));

    // Sort data points by timestamp
    var seriesData = _.sortBy(series.data, 0); 
    
    // Group series data by our key (e.g. day-of-year)
    var densify = _.partialRight(this._densifyPoints, level, startsAt).bind(this);
    data = new Map(
      _.toPairs(
        _.groupBy(seriesData, v => this.momentToKey(v[0]))
      )
      .map(([k, p]) => ([Number(k), {points: densify(p, 0)}]))
      // Sort pairs in-place by the first timestamp
      .sort(([k1, p1], [k2, p2]) => (p1.points[0][0] - p2.points[0][0])) 
    );
    keys = Array.from(data.keys());
    this._checkData(data, keys, k0, level);
    //this._debugData(data, keys);

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

  static _checkData(data, keys, k0, level) {
    
    var {diffNumber} = require('../../util/array-func');
    var {unit} = this;
    
    if (!keys.every(k => _.isNumber(k)))
      throw new Error('Expected number keys!');
    
    const U = moment.duration(1, unit).valueOf();
    const step = moment.duration(1, level).valueOf(); 

    if ((U < step) || (U % step !== 0))
      throw new Error(sprintf(
        'Expected that unit (%s) is a multiple of step (%s)', unit, level
      ));

    const checkStepInUnit = (d) => (
      data.get(d).points
        .map(v => v[0])
        .map(diffNumber)
        .slice(1)
        .every(s => (s == step))
    );

    if (!keys.every(checkStepInUnit))
      throw new Error(sprintf(
        'Expected that data points have regular steps (%d ms)', step
      ));
 
    const checkStepToNextUnit = (k1, k2) => {
      var a1, a2, p1, p2;
      if (k2 == null)
        return true;
      a1 = data.get(k1).points; 
      a2 = data.get(k2).points;
      p1 = _.last(a1); 
      p2 = _.first(a2);
      return (moment.duration(p2[0] - p1[0]).as(level) == 1);
    };
    
    if (!(keys.map(pairWithNext).every(_.spread(checkStepToNextUnit))))
      throw new Error(sprintf(
        'Expected that the step to the next unit is 1 %s', level
      ));
   
    if (keys.indexOf(k0) <= 0) 
      console.error(sprintf(
        'No consumption data for previous time unit (%s)', unit
      ));
    
    return;
  }

  constructor(props) {
    super(props);
    this.state = this.constructor._propsToState(props);
  }
  
  componentWillReceiveProps(nextProps) {
    this.setState(this.constructor._propsToState(nextProps));
  }
 
  render() {
    const {defaults, unit, momentToKey} = this.constructor;
    var {duration: [K, unit1], level, series, title, formatDate, uom} = this.props;
    var {moment0, data, keys, totals, forecast} = this.state;
    
    var k0 = momentToKey(moment0), i0, k1, data0, data0p1;
    if (data) {
      i0 = keys.indexOf(k0);
      k1 = keys[i0 - 1];
      data0 =  data.get(k0);
      data0p1 = data0.points.concat([data0.closurePoint]);
    }
  
    var summary = this._markupSummary(
      formatDate(moment0), 
      title,
      totals? totals.get(k0) : null,
      (totals && k1)? totals.get(k1) : null
    );
    
    var chartProps = {
      legend: defaults.charts.legend,
      lineWidth: defaults.charts.lineWidth,
      grid: defaults.charts.grid,
      color: defaults.charts.color,
      loading: data? false : {text: 'Loading...'},
      yAxis: {
        ...defaults.charts.yAxis,
        name: sprintf('%s (%s)', FIELD, uom)
      },
    };

    // Chart for the examined period
    // Todo move to a separate method
    
    var chart;
    
    if (!forecast) {
      // A simple chart for the exact period
      chart = (
        <echarts.LineChart 
          {...chartProps}
          xAxis={{
            ...defaults.charts.xaxis.normal,
            data: data? data0p1.map(v => v[0]) : null, 
          }}
          series={[
            {
              data: data? data0p1.map(v => v[1]) : [],
              name: 'Consumption',
            }
          ]}
         />
      ); 
    } else {
      // A chart that spans over 2 periods: {k0, k1r}
      var k1r = data? keys[i0 + 1] : null;
      // Fixme: This computation for N is not always correct
      var N = moment.duration(1, unit).as(level); 
      chart = (
        <echarts.LineChart
          {...chartProps}
          xAxis={{
            ...defaults.charts.xaxis.wide,
            data: !data? null : []
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
      );  
    }

    // Comparison chart (compare last K periods)
    // Todo move to a separate method

    var comparisonKeys = (keys || []).filter((k, i) => (i <= i0)).slice(K);
    var kX = _.maxBy(comparisonKeys, k => data.get(k).points.length);
    var dataX = data? data.get(kX) : null;
    var comparisonChart = (
      <echarts.LineChart 
        {...chartProps}
        xAxis={{
          ...defaults.charts.xaxis.comparison,
          data: data? dataX.points.map(v => v[0]).concat(dataX.closurePoint[0]) : null
          //data: data? data0p1.map(v => v[0]) : null,
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
    );

    return (
      <div className="clearfix level"> 
        {summary}
        {chart}
        {comparisonChart}
      </div>
    );
  }
  
  _markupSummary(title, subtitle, total, prevTotal) {
    var {uom} = this.props;
    return (
      <MeasurementValue 
        title={title}
        subtitle={subtitle} 
        field={FIELD} 
        unit={uom}
        value={total}
        prevValue={prevTotal}
       />  
    );
  }

};

_View.propTypes = {
  now: PropTypes.number, // reference point for time 
  uom: PropTypes.string, // unit of measurement
  level: PropTypes.string, // detail level (granularity) of data points 
  startsAt: PropTypes.string, // boundary of unit-based intervals
  duration: PropTypes.array,
  series: seriesPropType,
  formatDate: PropTypes.func,
  title: PropTypes.string,
};

_View.defaultProps = {
  formatDate: (m, brief=false) => (moment(m).format('LTS')),
};

// Day View

class DayView extends _View {
  
  static get unit() {return 'day';}
  
  static momentToKey(t) {
    return moment(t).dayOfYear();
  }
  
  static get defaults() {
    return _.merge({}, _View.defaults, {
      charts: {
        xaxis: {
          normal: {
            formatter: (t) => (moment(t).format('hA')),
          },
          wide: {
            formatter: (t) => {
              var m = moment(t);
              return (m.hour() != 0)? m.format('hA') : m.format('ddd hA');
            },
          },
          comparison: {
            formatter: (t) => (moment(t).format('hA')),
          },
        },
      },
    });
  }
};

DayView.displayName = 'UnitView.Day';

DayView.defaultProps = {
  formatDate: (m, brief=false) => (
    moment(m).format(brief? 'D/MMM' : 'ddd D MMM')
  ),
  title: 'Daily Consumption',
};

// Week View

class WeekView extends _View {
  
  static get unit() {return 'week';}
  
  static momentToKey(t) {
    return moment(t).isoWeek();
  }
  
  static get defaults() {
    return _.merge({}, _View.defaults, {
      charts: {
        xaxis: {
          normal: {
            formatter: (t) => (moment(t).format('dd')),
          },
          wide: {
            formatter: (t) => (moment(t).format('D/M')),
          },
          comparison: {
            formatter: (t) => (moment(t).format('dd')),
          },
        },
      },
    });
  }
};

WeekView.displayName = 'UnitView.Week';

WeekView.defaultProps = {
  formatDate: (m, brief=false) => {
    m = moment(m);
    if (brief) 
      return m.format('[Week #]W');
    var m0 = m.clone().startOf('isoweek');
    var m1 = m.clone().endOf('isoweek');
    return m0.format('D MMM') + ' - ' + m1.format('D MMM');
  },
  title: 'Weekly Consumption',
};

// Month View

class MonthView extends _View {
  
  static get unit() {return 'month';}
  
  static momentToKey(t) {
    return moment(t).month();
  }
  
  static get defaults() {
    return _.merge({}, _View.defaults, {
      charts: {
        xaxis: {
          normal: {
            formatter: (t) => (moment(t).format('dd D')),
          },
          wide: {
            formatter: (t) => (moment(t).format('D MMM')),
          },
          comparison: {
            labelFilter: (i, t) => (i % 5 == 0), // place every 5 items
            formatter: (t, i) => (moment(t).format('D')),
          },
        },
      },
    });
  }
};

MonthView.displayName = 'UnitView.Month';

MonthView.defaultProps = {
  formatDate: (m, brief=false) => (
    moment(m).format(brief? 'MMM' : 'MMMM YYYY')
  ),
  title: 'Monthly Consumption',
};

module.exports = {DayView, WeekView, MonthView};
