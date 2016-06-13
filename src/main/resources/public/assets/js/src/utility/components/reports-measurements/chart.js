var _ = require('lodash');
var moment = require('moment');
var numeral = require('numeral');
var sprintf = require('sprintf');

var React = require('react');
var echarts = require('react-echarts');

var Granularity = require('../../model/granularity');
var TimeSpan = require('../../model/timespan');
var population = require('../../model/population');
var {consolidateFuncs} = require('../../reports').measurements;
var {timespanPropType, populationPropType, seriesPropType, configPropType} = require('../../prop-types');

var PropTypes = React.PropTypes;

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
    reportKey: PropTypes.string.isRequired,
    series: PropTypes.arrayOf(seriesPropType),
    finished: PropTypes.oneOfType([PropTypes.bool, PropTypes.number]),
    // Appearence
    draw: PropTypes.bool,
    width: PropTypes.number,
    height: PropTypes.number,
    scaleTimeAxis: PropTypes.bool,
  }, 
  
  contextTypes: {config: configPropType},

  getDefaultProps: function () {
    return {
      draw: true,
      width: this.defaults.width,
      height: this.defaults.height,
      series: [],
      finished: true,
      scaleTimeAxis: false,
    };
  },
  
  shouldComponentUpdate: function (nextProps) {
    var update = (nextProps.draw !== false);
    
    if (!update) console.info('Skipping update of Chart!...');
    
    return update;
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
    d = moment.duration(d, durationUnit);

    // Use a sorted (by timestamp t) copy of series data [t,y]
    
    series = series.map(s => (_.extend({}, s, {
      data: s.data.slice(0).sort((p1, p2) => (p1[0] - p2[0])),
    })));

    // Find time range
    
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

module.exports = Chart;
