var React = require('react');
var ReactDOM = require('react-dom');
var PortalMixin = require('./PortalMixin');
var echarts = require('echarts');
var theme = require('./chart/themes/blue');
var clone = require('clone');

var RadialChart = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	mixins: [PortalMixin],
	
	render: function() {
		var { prefix, options, ...other } = this.props;
		
		return (
			<div {...other} />
		);
	},

	componentDidMount: function() {
		this._chart = echarts.init(document.getElementById(this.getId()), theme); 
 
		var options = this.shapeData(this.props.options, this.context.intl);
		if(options) {
			this._chart.setOption(options);
		}		
	},
	
	componentWillReceiveProps : function(nextProps, nextContext) {
		var options = this.shapeData(nextProps.options, nextContext.intl);
		if((this._chart) && (options)) {
			this._chart.setOption(options);
		}
	},

	componentWillUnmount : function() {

	},
	
	getChart: function() {
		return this._chart;
	},
	
	shapeData : function (options, intl) {
		var labelTop = {
		    normal : {
		    	color: '#ecf0f1',
		    	label : {
		            show : true,
		            position : 'center',
		            formatter : '{b}',
		            textStyle: {
		                baseline : 'bottom',
		                color: '#2c3e50'
		            }
		        },
		        labelLine : {
		            show : false
		        }
		    }
		};

		var labelFromatter = {
		    normal : {
		        label : {
		            formatter : function (params){
		                return params.value + '%';
		            },
		            textStyle: {
		                baseline : 'top',
		                color: options.color
		            }
		        }
		    },
		};

		var labelBottom = {
		    normal : {
		    	color: options.color,
		    	label : {
		            show : true,
		            position : 'center'
		        },
		        labelLine : {
		            show : false
		        }
		    }
		};

		return {
		    series : [
		        {
		            type : 'pie',
		            center : ['50%', '50%'],
		            radius : [40, 55],
		            itemStyle : labelFromatter,
		            data : [
	                    {name: options.label, value: (100 - options.value), itemStyle : labelTop },
		                {name: '', value: options.value, itemStyle : labelBottom}
		            ]
		        }
		    ]
		};
	}

});

module.exports = RadialChart;
