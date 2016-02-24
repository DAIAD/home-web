var React = require('react');
var ReactDOM = require('react-dom');
var PortalMixin = require('./PortalMixin');
var echarts = require('echarts');
var theme = require('./chart/themes/blue');
var clone = require('clone');

var Chart = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	mixins: [PortalMixin],
	
	render: function() {
		var { prefix, options, data, ...other } = this.props;
		
		return (
			<div {...other} />
		);
	},

	componentDidMount: function() {
		this._chart = echarts.init(document.getElementById(this.getId()), theme); 
 
		var chartOptions = this.shapeData(this.props.options, this.props.data, this.context.intl);
		if(chartOptions) {
			this._chart.setOption(chartOptions);
		}		
	},
	
	componentWillReceiveProps : function(nextProps, nextContext) {
		var chartOptions = this.shapeData(nextProps.options, nextProps.data, nextContext.intl);
		if((this._chart) && (chartOptions)) {
			this._chart.setOption(chartOptions);
		}
	},

	componentWillUnmount : function() {

	},
	
	getChart: function() {
		return this._chart;
	},
	
	shapeData : function (options, data, intl) {
		if((!options) && (!data)) {
			return null;
		}
		if(!data) {
			return options;
		}

		// Initialize
		var chartOptions = clone(options);
		
		chartOptions.dataZoom = {
	        show: true,
	        start : intl.formatDate(new Date(2016,1,1), { day: 'numeric', month: 'long', year: 'numeric'})
	    };
		chartOptions.legend = {         
			data : []
		};
		chartOptions.xAxis = [];
		chartOptions.yAxis = [];
		chartOptions.series = [];
	
		var getLabels = function(series) {
			return series.data.map(function(record) {
				var value = record[series.xAxis];
				if(value instanceof Date) {
					return intl.formatDate(value, { day: 'numeric', month: 'long', year: 'numeric'});
				}
				return value;
			});
    	};
		
		var getValues = function(series) {
			return series.data.map(function(record) {
				return record[series.yAxis];
			});
    	};
		
        for(var i=0; i<data.series.length; i++) {
        	var series = data.series[i];

        	chartOptions.legend.data.push(series.legend);
        	
        	// Common configuration
        	if(i===0) {
                chartOptions.xAxis.push({
                	type : 'category',
                	data : getLabels(series)
            	});
                
        		chartOptions.yAxis.push({
                    type : 'value'
                });
        	}
        	
        	// Data series configuration
        	chartOptions.series.push({
                'name': series.legend,
                'type': 'line',
                'data': getValues(series)                
            });
        }

		return chartOptions;
	}

});

module.exports = Chart;
