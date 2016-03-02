var React = require('react');
var ReactDOM = require('react-dom');
var PortalMixin = require('./PortalMixin');
var echarts = require('echarts');
var theme = require('./chart/themes/blue');
var clone = require('clone');

var randomDataArray = function(count, max1, max2, size) {
    var d = [];
    var len = count;
    while (len--) {
        d.push([
            Math.round(Math.random() * max1),
            Math.round(Math.random() * max2),
            Math.round(Math.random() * size),
        ]);
    }
    return d;
};

var chartOptions = {
    tooltip : {
        trigger: 'item',
        showDelay : 0,
        formatter: function (params) {
            return 'Average Income ' + params.value[0] + '.00 €</br>Average Consumption ' + params.value[1] + '</br>Size ' + params.value[2];
        }
    },
    dataRange: {
        min: 1,
        max: 100,
        y: 'center',
        text:['Max Size',''],
        color:['#2c3e50','#ecf0f1'],
        calculable : true
    },    
    xAxis : [
        {
            type : 'value',
            splitNumber: 4,
            scale: false,
            name: 'Average Income'
        }
    ],
    yAxis : [
        {
            type : 'value',
            splitNumber: 4,
            scale: false,
            name: 'Average daily consumption'
        }
    ],
    series : [
        {
            name:'Clusters of households based on income and consumption',
            type:'scatter',
            symbolSize: function (value){
                return value[2];
            },
            data: randomDataArray(15, 50000, 2000, 100)
        }
    ]
};

var ClusterChart = React.createClass({
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
 
		if(chartOptions) {
			this._chart.setOption(chartOptions);
		}		
	},
	
	componentWillReceiveProps : function(nextProps, nextContext) {
		if((this._chart) && (chartOptions)) {
			this._chart.setOption(chartOptions);
		}
	},

	componentWillUnmount : function() {

	},
	
	getChart: function() {
		return this._chart;
	}

});

module.exports = ClusterChart;
