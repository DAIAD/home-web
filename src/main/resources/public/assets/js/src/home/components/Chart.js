var React = require('react');
var ReactDOM = require('react-dom');

var ChartOptions = require('./ChartOptions');

var echarts  = require('echarts');
//var scatter = require('echarts').chart.scatter;


var EChart = React.createClass({
	displayName: 'Echart',

	getDefaultProps: function() {
		//var echartSeriesArray = this._makeDataSeries(seriesArray);
		return {
			width: 700,
			height: 400,
		};
	},
	componentDidMount: function() {
		this._initChart();
	},
	componentWillUnmount: function() {
		this._destroyChart();
	},
	shouldComponentUpdate: function(nextProps, nextState) {
		return false;
	},
	componentWillReceiveProps: function(nextProps) {
		this._updateChart(nextProps);
	},
	render: function() {
		return <div 
			className="chartContainer"
			style= {{
				'height': this.props.height,
				'width': this.props.width
			}} 
			/>;
	},
	_initChart: function() {
		this.chart = echarts.init(ReactDOM.findDOMNode(this));
		
		this.chart.setOption(ChartOptions);
		this._updateChart(this.props);
	},
	_updateChart: function(nextProps) {
		if (!nextProps) {
			return null;
		}
		this.chart.setOption(nextProps);
	},
	_destroyChart: function() {
		this.chart.dispose();
	},

});

module.exports = EChart;
