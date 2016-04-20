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

	getDefaultProps: function() {
		return {
			type: 'line'
		};
	},
	
	onResize: function() {
	  this._chart.resize();
	},
	
	render: function() {
		var { prefix, options, data, type, ...other } = this.props;
		
		return (
		  <div className={prefix + '-container'} {...other} />
		);
	},

	componentDidMount: function() {
		this._chart = echarts.init(document.getElementById(this.getId()), theme); 
 
		var chartOptions = this.shapeData(this.props.type, this.props.options, this.props.data, this.context.intl);
		if(chartOptions) {
			this._chart.setOption(chartOptions, true);
		}		
	},
	
	componentWillReceiveProps : function(nextProps, nextContext) {
		var chartOptions = this.shapeData(nextProps.type, nextProps.options, nextProps.data, nextContext.intl);

		if((this._chart) && (chartOptions)) {
			this._chart.setOption(chartOptions, true);
		}
	},

	componentWillUnmount : function() {

	},
	
	getChart: function() {
		return this._chart;
	},
	
	shapeData : function (type, options, data, intl) {
		if((!options) && (!data)) {
			return null;
		}
		if(!data) {
			return options;
		}

		// Initialize
		var chartOptions = clone(options);

		if(!chartOptions.dataZoom) {
		  chartOptions.dataZoom = {
	      show: true,
	      start : 0
		  };
		}

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
				  switch(chartOptions.dataZoom.format) {
				    case 'day-hour':
				      return (intl.formatDate(value, { day: 'numeric', month: 'long', year: 'numeric'})  + 
					            ' ' + 
					            intl.formatTime(value, { hour: 'numeric', minute: 'numeric'}));
				    case 'day':
	            return intl.formatDate(value, { day: 'numeric', month: 'long', year: 'numeric'});
				  }
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
            	data : getLabels(series),
            	name: series.xAxisName || '',
        	});
            
    		chartOptions.yAxis.push({
              type : 'value',
            	name: series.yAxisName || 'Volume',
            	nameLocation: 'end',
            	nameTextStyle: {
            		color: '#000'
            	}
            });
    	}
        	
    	// Data series configuration
    	var itemStyle = options.itemStyle || null;
    	if(type === 'area') {
    	  itemStyle = itemStyle || {
    	    normal: {
    	      areaStyle: {
    	        type: 'default'
    	      }
    	    }
    	  };
    	}

    	chartOptions.series.push({
            name: series.legend,
            type: (type === 'area' ? 'line' : type),
            itemStyle: itemStyle,
            data: getValues(series)                
        });
    }

		return chartOptions;
	}

});

module.exports = Chart;
